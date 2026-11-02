package com.wangzan101.mika.remote

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction

/**
 * 修 mDNS 实例名里的乱码。
 *
 * 安卓的 `NsdManager` 把 DNS 标签按单字节字符集解成 String，而 DNS-SD 的实例名实际是 UTF-8 字节。
 * 对端如果直接把设备名当实例名（中文名很常见），传过来就成了 `äº‘å®` 这种——
 * 2026-11-02 在模拟器上真截到过这一屏：左栏一台远端设备显示成 `äº‘å®.`，
 * 反解回去正好是「云宝」的 UTF-8 字节被按 Windows-1252 读了一遍。
 *
 * 只在**取不到 TXT 里的 name、退回 serviceName** 时才用它：[repair] 先判断这段字符串像不像
 * "UTF-8 字节被当单字节读"的结果，像就还原，不像就原样返回——绝不能把正常的中文或西文改坏。
 */
internal object DeviceName {
    /** Windows-1252 在 0x80–0x9F 那段与 ISO-8859-1 不同，这里只要把常见的那几个字符映回字节。 */
    private val CP1252_EXTRA = mapOf(
        '\u20AC' to 0x80, '\u201A' to 0x82, '\u0192' to 0x83, '\u201E' to 0x84, '\u2026' to 0x85,
        '\u2020' to 0x86, '\u2021' to 0x87, '\u02C6' to 0x88, '\u2030' to 0x89, '\u0160' to 0x8A,
        '\u2039' to 0x8B, '\u0152' to 0x8C, '\u017D' to 0x8E, '\u2018' to 0x91, '\u2019' to 0x92,
        '\u201C' to 0x93, '\u201D' to 0x94, '\u2022' to 0x95, '\u2013' to 0x96, '\u2014' to 0x97,
        '\u02DC' to 0x98, '\u2122' to 0x99, '\u0161' to 0x9A, '\u203A' to 0x9B, '\u0153' to 0x9C,
        '\u017E' to 0x9E, '\u0178' to 0x9F,
    )

    /**
     * windows-1252 有 5 个字节没定义（0x81 0x8D 0x8F 0x90 0x9D），Java 解不了就塞 U+FFFD，
     * 位置上的信息丢了。所以这些位置当未知量，逐个试——UTF-8 校验很严，通常只有一个能过。
     * 「云宝」的末字节 0x9D 正好落在这里。
     */
    private val UNDEFINED_BYTES = intArrayOf(0x9D, 0x81, 0x8D, 0x8F, 0x90)
    private const val MAX_WILDCARDS = 4

    fun repair(raw: String): String {
        if (raw.all { it.code < 0x80 }) return raw
        val slots = ArrayList<Int>(raw.length)
        val wildcards = ArrayList<Int>()
        for ((i, c) in raw.withIndex()) {
            when {
                c.code <= 0xFF -> slots.add(c.code)
                CP1252_EXTRA.containsKey(c) -> slots.add(CP1252_EXTRA.getValue(c))
                c == '\uFFFD' -> { if (wildcards.size >= MAX_WILDCARDS) return raw; wildcards.add(i); slots.add(-1) }
                else -> return raw          // 真·多字节字符：本来就是正常文本，别动
            }
        }
        for (bytes in solve(slots, wildcards, 0)) {
            val decoded = strictUtf8(bytes) ?: continue
            if (decoded != raw && decoded.any { it.code >= 0x80 }) return decoded
        }
        return raw
    }

    /** 把替换符位置的候选字节一个个试过去；先试最可能的 0x9D。 */
    private fun solve(slots: MutableList<Int>, wildcards: List<Int>, at: Int): Sequence<ByteArray> = sequence {
        if (at == wildcards.size) {
            yield(ByteArray(slots.size) { slots[it].toByte() })
            return@sequence
        }
        for (candidate in UNDEFINED_BYTES) {
            slots[wildcards[at]] = candidate
            yieldAll(solve(slots, wildcards, at + 1))
        }
        slots[wildcards[at]] = -1
    }

    private fun strictUtf8(bytes: ByteArray): String? = try {
        Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    } catch (_: CharacterCodingException) {
        null
    }
}
