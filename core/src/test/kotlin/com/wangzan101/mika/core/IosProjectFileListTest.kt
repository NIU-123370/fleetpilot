package com.wangzan101.mika.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * iOS 的 Xcode 工程按名字逐个列源码文件,往 `ios/` 里新增一个 .swift 不会自动进工程。
 * 这条测试存在的原因是:这个仓库在普通开发机和 CI 上都编译不了 iOS(要 Xcode 与签名),
 * 而语法级检查(`swiftc -parse`)只看单个文件的语法,看不见"这个文件根本没参与编译"。
 * 真出事过两次,都是这一条能测而上面那条测不出来的:配色改动新加的 `Shared/FleetPalette.swift`
 * 被七十多个文件引用,却一处都没写进工程;补上主 App 之后又漏了 Widget 扩展——
 * `Widgets/MikaWidgets.swift` 也在用同一批颜色 token。
 *
 * 范围与 `ios/project.yml` 一致:`App`、`Shared`、`Widgets` 三处的源码,`Resources` 除外。
 * `Shared/` 是给两个 target 共用的,按上游现在的做法两边的 Sources 阶段都要编它。
 */
class IosProjectFileListTest {
    private val root = File("..").canonicalFile
    private val pbxFile = File(root, "ios/MikaRemote.xcodeproj/project.pbxproj")
    private val pbx: String by lazy { pbxFile.readText() }

    /** 磁盘上该进工程的源码。 */
    private val onDisk: List<File> = File(root, "ios").walkTopDown()
        .filter { it.isFile && it.extension == "swift" }
        .filter { "Resources" !in it.relativeTo(File(root, "ios")).invariantSeparatorsPath }
        .sortedBy { it.name }
        .toList()

    private val id = "[0-9A-F]{24}"

    /**
     * 把 pbxproj 拆成"块"：以两个 tab 缩进、以 `= {` 结尾的行开一块，到 `};` 收。
     * 单行的条目（`... = {isa = PBXFileReference; ...};`）不算块，所以不会被误当成容器。
     */
    private val blocks: Map<String, List<String>> by lazy {
        val out = LinkedHashMap<String, MutableList<String>>()
        var key: String? = null
        for (line in pbx.split("\n")) {
            val trimmed = line.trimEnd()
            when {
                trimmed.startsWith("\t\t") && trimmed.endsWith("= {") && trimmed.contains("*/ = {") -> {
                    key = trimmed.trim().removeSuffix("= {").trim()
                    out.getOrPut(key!!) { mutableListOf() }
                }
                trimmed == "\t\t};" -> key = null
                key != null -> out.getValue(key) += line
            }
        }
        out
    }

    /** 每个 target 的 Sources 阶段实际编进去的文件名(key = target 名)。 */
    private val compiledByTarget: Map<String, Set<String>> by lazy {
        blocks.filter { (_, body) -> body.any { it.contains("isa = PBXNativeTarget;") } }
            .mapNotNull { (header, body) ->
                val target = Regex("""/\* (.+) \*/""").find(header)?.groupValues?.get(1) ?: return@mapNotNull null
                val sourcesPhase = body.dropWhile { !it.contains("buildPhases = (") }
                    .takeWhile { !it.trim().endsWith(");") }
                    .firstOrNull { it.contains("/* Sources */,") }
                    ?: error("target $target 没有 Sources 阶段")
                val phaseId = Regex("""($id) /\* Sources \*/""").find(sourcesPhase)?.groupValues?.get(1)
                    ?: error("target $target 的 Sources 阶段 id 读不到")
                val phaseBody = blocks.getValue("$phaseId /* Sources */")
                target to phaseBody.mapNotNull { line ->
                    Regex("""/\* ([^*]+\.swift) in Sources \*/,""").find(line)?.groupValues?.get(1)
                }.toSet()
            }.toMap()
    }

    /** 工程里声明的源码文件名(PBXFileReference)。 */
    private val declared: Set<String> by lazy {
        Regex("""isa = PBXFileReference;[^}]*?path = ([^;]+\.swift);""").findAll(pbx)
            .map { it.groupValues[1].trim() }.toSet()
    }

    private fun sharedOnDisk() = onDisk.filter { it.relativeTo(File(root, "ios")).invariantSeparatorsPath.startsWith("Shared/") }

    @Test
    fun projectFileExistsAndBothTargetsParse() {
        assertTrue("找不到 ${pbxFile.relativeTo(root)}", pbxFile.isFile)
        assertTrue("ios/ 下一个 .swift 都没扫到,这条测试就成了空跑", onDisk.isNotEmpty())
        // 两个 target 都解析到了才算这条测得动;少一个就是解析器写错了,不许当成"没有文件要编"
        assertEquals(setOf("MikaRemote", "MikaWidgets"), compiledByTarget.keys)
        compiledByTarget.forEach { (target, files) ->
            assertTrue("$target 的 Sources 阶段解析为空", files.isNotEmpty())
        }
    }

    /** 按名索引的前提:不许有两个同名 .swift——重名会让"在工程里"这件事对错的那个文件也成立。 */
    @Test
    fun swiftFileNamesAreUnique() {
        val twice = onDisk.groupBy { it.name }.filterValues { it.size > 1 }.keys
        assertTrue("有重名的 .swift,按文件名核对会失真: $twice", twice.isEmpty())
    }

    @Test
    fun everySwiftFileIsDeclaredInTheProject() {
        val missing = onDisk.map { it.name }.filterNot { it in declared }
        assertTrue("这些源码在磁盘上、没写进 Xcode 工程: $missing", missing.isEmpty())
    }

    /** 光有文件引用还不够:没挂进 Sources 阶段的文件等于没参与编译。 */
    @Test
    fun everySwiftFileIsCompiledBySomeTarget() {
        val compiled = compiledByTarget.values.flatten().toSet()
        val notCompiled = onDisk.map { it.name }.filterNot { it in compiled }
        assertTrue("这些 .swift 没有出现在任何 Sources 阶段里: $notCompiled", notCompiled.isEmpty())
    }

    /** `Shared/` 是两个 target 共用的,漏任何一边都会在真编译时报"找不到符号"。 */
    @Test
    fun sharedSourcesCompileIntoEveryTarget() {
        for ((target, files) in compiledByTarget) {
            val missing = sharedOnDisk().map { it.name }.filterNot { it in files }
            assertTrue("$target 的 Sources 阶段少了共用的源码: $missing", missing.isEmpty())
        }
    }

    /** 反方向:工程里不许留着已经删掉的源文件——那会让别人以为文件在别处。 */
    @Test
    fun projectDoesNotReferenceMissingFiles() {
        val names = onDisk.map { it.name }.toSet()
        val stale = declared.filterNot { it in names }
        assertTrue("Xcode 工程引用了磁盘上不存在的 .swift: $stale", stale.isEmpty())
    }

    /** project.yml 声明的源码目录就是这三处;目录变了要同步改这条,不然上面几条会空跑。 */
    @Test
    fun scannedDirsMatchTheDeclaredSourceFolders() {
        val dirs = onDisk.map { it.relativeTo(File(root, "ios")).invariantSeparatorsPath.substringBefore('/') }.toSet()
        assertEquals(setOf("App", "Shared", "Widgets"), dirs)
    }
}
