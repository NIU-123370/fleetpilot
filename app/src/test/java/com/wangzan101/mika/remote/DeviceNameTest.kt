package com.wangzan101.mika.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.nio.charset.Charset

/**
 * 这组用例的输入是"真实的坏字节"，不是手写的近似：
 * 把「云宝」的 UTF-8 字节分别按 windows-1252 与 ISO-8859-1 解一遍，
 * 得到的就是模拟器上截到的那种 `äº‘å®`。
 */
class DeviceNameTest {
    private val cp1252: Charset = Charset.forName("windows-1252")
    private fun utf8As(charset: Charset, s: String) = s.toByteArray(Charsets.UTF_8).toString(charset)

    @Test
    fun repairsChineseNameMangledAsWindows1252() {
        val mangled = utf8As(cp1252, "云宝")
        assertFalse("这组用例的前提是它真的坏了", mangled == "云宝")
        assertEquals("云宝", DeviceName.repair(mangled))
    }

    @Test
    fun repairsChineseNameMangledAsIso88591() {
        assertEquals("云宝", DeviceName.repair(utf8As(Charsets.ISO_8859_1, "云宝")))
    }

    @Test
    fun repairsLongerMixedName() {
        assertEquals("云宝的 Pixel 8", DeviceName.repair(utf8As(cp1252, "云宝的 Pixel 8")))
        assertEquals("李四的手機", DeviceName.repair(utf8As(Charsets.ISO_8859_1, "李四的手機")))
    }

    @Test
    fun leavesGoodTextAlone() {
        for (s in listOf("云宝", "Pixel 8", "sdk_gphone64_arm64", "Mika-1a2b3c4d", "Café", "Ñandú")) {
            assertEquals(s, DeviceName.repair(s))
        }
    }

    @Test
    fun neverProducesReplacementCharacters() {
        for (s in listOf(utf8As(cp1252, "云宝"), utf8As(Charsets.ISO_8859_1, "云宝"), utf8As(cp1252, "李四的手機"))) {
            assertFalse("输入 $s 修出了替换符", DeviceName.repair(s).contains('\uFFFD'))
        }
        // 本来就含替换符的输入原样返回：修不了就别改
        assertEquals("\uFFFD", DeviceName.repair("\uFFFD"))
    }
}
