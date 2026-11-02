package com.wangzan101.mika.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LicensesTest {
    /** 单元测试的工作目录是 app/;仓库根目录在上一级。 */
    private val notices = File("../THIRD_PARTY_NOTICES.md").takeIf { it.isFile } ?: File("THIRD_PARTY_NOTICES.md")

    @Test
    fun everyNoticeEntryShown() {
        val headings = notices.readLines().filter { it.startsWith("## ") }.map { it.removePrefix("## ").trim() }
        assertTrue("notices has headings", headings.size >= 16)
        val shown = Licenses.ENTRIES.map { it.name }
        headings.forEach { assertTrue("missing on licenses screen: $it", it in shown) }
        // 反方向也要成立:许可页不许出现声明文件没有的条目(从前那条"App 自己"就是——它既不显示又对不上任何声明)。
        shown.forEach { assertTrue("not in THIRD_PARTY_NOTICES.md: $it", it in headings) }
    }

    @Test
    fun gplSourceOfferPresent() {
        val byName = Licenses.ENTRIES.associateBy { it.name }
        assertEquals("GPL-2.0-or-later", byName.getValue("PRoot").license)
        assertEquals("LGPL-3.0-or-later", byName.getValue("talloc").license)
        assertEquals("Apache-2.0", byName.getValue("Apache Commons Compress").license)
        listOf("OkHttp", "Okio", "kotlinx.coroutines", "kotlinx.serialization", "Kotlin 标准库", "AndroidX(含 Jetpack Compose)")
            .forEach { assertEquals(it, "Apache-2.0", byName.getValue(it).license) }
        // 源码承诺必须指到仓库里真的有的文件,否则用户照着这段话找不到源码。
        listOf("sources.lock", "patches", "build-native.sh", "gpl-sources").forEach {
            assertTrue("源码指引少了 $it", Licenses.GPL_SOURCE_OFFER.contains(it))
        }
        assertTrue(Licenses.GPL_SOURCE_OFFER.contains("PRoot") && Licenses.GPL_SOURCE_OFFER.contains("talloc"))
        assertTrue(byName.getValue("Alpine Linux").note.contains("runtime-sources.py"))
        assertEquals("0BSD", byName.getValue("XZ for Java").license)
    }

    /** 许可页写的路径得真的存在——这段话是给用户提供源码的入口,写错了就是空头承诺。 */
    @Test
    fun sourceOfferPathsExist() {
        val root = File("..").canonicalFile
        listOf(
            "tools/sandbox/sources.lock", "tools/sandbox/patches", "tools/sandbox/build-native.sh",
            "tools/sandbox/runtime-sources.py", "tools/sandbox/README.md",
        ).forEach { assertTrue("许可页指向的 $it 不存在", File(root, it).exists()) }
    }

    /** 「开源许可」页逐个列出自带运行环境里的软件包,每个都有许可证。 */
    @Test
    fun runtimePackagesListed() {
        val manifest = File("src/main/assets/runtime/packages.json")
        val entries = Licenses.runtimePackages(manifest.readText())
        assertTrue(entries.size > 40)
        listOf("busybox", "musl", "python3", "nodejs", "py3-pillow", "py3-pypdf").forEach { n -> assertTrue(n, entries.any { it.name == n }) }
        entries.forEach { assertTrue(it.name, it.license.isNotBlank() && it.note.startsWith("版本 ")) }
    }
}
