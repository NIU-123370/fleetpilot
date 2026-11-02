package com.wangzan101.mika.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 界面文案里不许留着**上一个名字**。
 *
 * 这个仓库的名字改过两次：上游 phongent 把 Mika 改成了自己的名字，2026-11-02 本仓库又统一成 FleetPilot。
 * 第一次改名漏了一大片——对话页顶栏、关于页顶上那行、卡片抬头、下载通知标题、诊断信息表头、
 * 繁简两版的示例文案都还写着旧名，界面上同时念两个名字。这条守卫就是为了下次改名不再只改一处。
 *
 * 管的范围是**给用户看的文案**（`app/src/main/res` 下各 `values` 语言目录里的 `strings` 文件，`<string>` 的值）。
 * 不管这些，都有各自的理由，见 `UPSTREAM.md` 的「还没改名的部分」：
 *  - 代码里的标识符（`MikaTheme`、`Theme.Mika` 这些样式名、资源名）；
 *  - 落在设备上的目录名与系统授权状态（`Download/Mika/`、`Pictures/Mika/`、通知监听服务的 label）；
 *  - 有功能作用的字符串（剪贴板归属标记、注入到网页里的 JS 函数名）；
 *  - 发给模型的提示词里那句 "on Mika's screen"。
 * 仓库地址也不管：紧贴在一个 `/` 后面的那种是 URL 的一部分，不是产品名。
 */
class NoStaleProductNameInUserCopyTest {
    private val root = File("..").canonicalFile

    /**
     * 两代旧名都要挡：Mika 是上游上游的名字，phongent 是本仓库统一成 FleetPilot 之前的名字。
     * 留着这一条，下次改名再也不会只改一处。
     */
    private val staleNames = listOf("Mika", "phongent")

    /** 带 strings*.xml 的语言目录才是要管的（values-night 这类只有样式，没有文案）。 */
    private val localeDirs: List<File> = File(root, "app/src/main/res").listFiles()
        ?.filter { dir ->
            dir.isDirectory && dir.name.startsWith("values") &&
                dir.listFiles()?.any { it.name.startsWith("strings") && it.extension == "xml" } == true
        }
        ?.sortedBy { it.name }
        .orEmpty()

    /** `<string ...>值</string>` 的值部分；注释、属性名、样式名都不算。 */
    private data class Site(val file: String, val key: String, val value: String)

    private val strings: List<Site> by lazy {
        val pattern = Regex("""<string name="([^"]+)"[^>]*>([^<]*)</string>""")
        localeDirs.flatMap { dir ->
            dir.listFiles()?.filter { it.name.startsWith("strings") && it.extension == "xml" }
                ?.sortedBy { it.name }
                ?.flatMap { f ->
                    pattern.findAll(f.readText()).map { Site(f.relativeTo(root).invariantSeparatorsPath, it.groupValues[1], it.groupValues[2]) }.toList()
                }.orEmpty()
        }
    }

    /**
     * 旧名紧贴在一个 `/` 后面时才是仓库地址的一部分（`github.com/mylea1/phongent` 这类）。
     * 只看"这句里有没有 http"会放走"访问 https://… 后打开 phongent 的设置"这种一句话里两样都有的文案，
     * 所以判位置不判上下文。上面那两次改名有一批就是靠这个位置规则才没被误伤。
     */
    private fun isRepoUrl(value: String, index: Int): Boolean = index > 0 && value[index - 1] == '/'

    /** 一句文案里每个旧名的每一处都要判，有一处不是仓库地址就算漏改。 */
    private fun saysAnOldName(value: String): Boolean = staleNames.any { name ->
        var i = value.indexOf(name)
        var bad = false
        while (i >= 0) {
            if (!isRepoUrl(value, i)) bad = true
            i = value.indexOf(name, i + name.length)
        }
        bad
    }

    @Test
    fun scansTheLocaleFolders() {
        // 三个语言目录都在，少一个就是扫描范围写错了，不许当成"那个语言没问题"
        assertEquals(
            listOf("values", "values-b+zh+Hant", "values-zh").sorted(),
            localeDirs.map { it.name }.sorted(),
        )
        assertTrue("一条 <string> 都没扫到，这条守卫就是空跑", strings.isNotEmpty())
    }

    @Test
    fun appNameExistsOnceAndIsNotTranslated() {
        val byLocale = strings.filter { it.key == "app_name" }
        // 产品名只该有一份：语言目录里再"翻译"一个，界面就同时念两个名字，正是这一串改动在修的事
        assertEquals("app_name 只该定义一次，实际在 ${byLocale.map { it.file }}", 1, byLocale.size)
        assertEquals("而且该在默认 values 目录", "values", File(byLocale.first().file).parentFile.name)
        assertTrue("app_name 不该是空", byLocale.first().value.isNotBlank())
        assertEquals("app_name 该是当前这个名字", "FleetPilot", byLocale.first().value)
    }

    @Test
    fun userCopyDoesNotSayTheOldName() {
        val hits = strings.filter { it.key != "app_name" && saysAnOldName(it.value) }
            .map { "${it.file} › ${it.key}: ${it.value.take(60)}" }
        assertTrue(
            "界面文案里还留着上一个名字（${staleNames.joinToString()}），共 ${hits.size} 条：\n${hits.joinToString("\n")}",
            hits.isEmpty(),
        )
    }

    /** 正对照：位置规则本身要两个方向都验过，不然上面那条红/绿都说明不了什么。 */
    @Test
    fun theRuleItselfWorksBothWays() {
        for (name in staleNames) {
            val url = "https://github.com/mylea1/$name/releases"
            assertTrue("仓库地址不该被算成漏改: $url", isRepoUrl(url, url.indexOf(name)))
            val copy = "打开 $name 的设置页"
            assertTrue("普通文案里的旧名必须被抓到: $copy", saysAnOldName(copy))
        }
        // 同一句话里既有链接又有旧名：按位置判，不许因为句子里有 http 就整句放过
        val mixed = "访问 https://a.test/x 后打开 ${staleNames[1]} 的设置页"
        assertTrue("句子里有 URL 不该顺带放过另一个旧名", saysAnOldName(mixed))
        val onlyUrl = "仓库地址 https://github.com/mylea1/${staleNames[1]}"
        assertTrue("整句只有仓库地址时不该算漏改", !saysAnOldName(onlyUrl))
    }
}
