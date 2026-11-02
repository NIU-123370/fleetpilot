package com.wangzan101.mika.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 头像颜色是"一台设备一个固定色":同 id 永远同色、不同 id 要散得开、大小写不影响。 */
class AvatarColorTest {
    @Test
    fun sameIdAlwaysSameColor() {
        for (id in listOf("local", "abc", "R5CT1234", "mcp-f055db0b")) {
            assertEquals(avatarColorIndex(id), avatarColorIndex(id))
        }
    }

    @Test
    fun indexStaysInsidePalette() {
        for (i in 0 until 500) {
            val idx = avatarColorIndex("device-$i")
            assertTrue("越界: $idx", idx in 0..7)
        }
    }

    @Test
    fun differentDevicesSpreadOverThePalette() {
        val used = (0 until 64).map { avatarColorIndex("device-$it") }.toSet()
        assertTrue("64 个设备只用到 $used（调色板散不开）", used.size >= 5)
    }

    @Test
    fun caseInsensitive() {
        assertEquals(avatarColorIndex("ABC"), avatarColorIndex("abc"))
    }

    /** 形状也要散得开:六种形状至少用到 4 种。 */
    @Test
    fun shapesSpread() {
        val used = (0 until 64).map { avatarShapeIndex("device-$it") }.toSet()
        assertTrue("64 个设备只用到 $used（形状散不开）", used.size >= 4)
    }

    /** 相邻编号不该都撞在一起:连号是最常见的 id 形态。 */
    @Test
    fun consecutiveIdsDoNotCollide() {
        val pairs = (0 until 20).map { avatarColorIndex("d$it") to avatarColorIndex("d${it + 1}") }
        assertTrue("20 组连号全同色", pairs.any { (a, b) -> a != b })
    }
}
