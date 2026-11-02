package com.wangzan101.mika.core

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * iOS 26 才有的 API 只许出现在一个带编译器条件的出口里。
 *
 * 起因是 CI 的 Xcode 16.4 那一路编译失败，报的是
 * `value of type '_ShapeView<...>' has no member 'glassEffect'`——
 * `App/DeviceChat.swift` 直接写了 `.glassEffect(...)`，外面只包 `#available(iOS 26.0, *)`。
 * `#available` 是**运行时**判断，救不了编译期：旧 SDK 里连这个名字都没有（`project.yml` 的
 * deploymentTarget 仍是 iOS 17，所以每一处都得带降级分支，不许假设自己跑在 26 上）。
 *
 * 这条检查的意义是把"记得圈 #if compiler"从人的记忆里拿走：新写一处裸调用就会红，
 * 唯一的合法去处是 `Shared/FleetPalette.swift` 里那两个 `fleetGlass` / `fleetGlassBackground`。
 */
class IosCompilerGateTest {
    private val root = File("..").canonicalFile
    private val ios = File(root, "ios")

    /** 26 才有、旧 SDK 不认识的调用。加新的时这里也要加，否则等于没守。 */
    private val newApi = listOf("glassEffect", "regular.tint")

    private fun calls(): List<String> = ios.walkTopDown()
        .filter { it.isFile && it.extension == "swift" }
        .filter { "Resources" !in it.relativeTo(ios).invariantSeparatorsPath }
        .flatMap { f ->
            f.readLines().mapIndexedNotNull { i, line ->
                // 注释里提这个API名不算调用
                if (line.trimStart().startsWith("//") || line.trimStart().startsWith("///")) return@mapIndexedNotNull null
                if (newApi.any { line.contains("$it(") }) "${f.relativeTo(root).invariantSeparatorsPath}:${i + 1}" else null
            }
        }.toList()

    @Test
    fun newSdkApisOnlyLiveBehindTheCompilerGate() {
        val allowed = "ios/Shared/FleetPalette.swift"
        val offenders = calls().filterNot { it.startsWith("$allowed:") }
        assertTrue(
            "这些调用没圈在 #if compiler(>=6.2) 里，Xcode 16.4 那一路会红：$offenders\n" +
                "正确写法是走 FleetPalette 的 fleetGlass / fleetGlassBackground，而不是在界面里裸调。",
            offenders.isEmpty(),
        )
    }

    /** 正对照：出口文件本身必须真的被算进"允许"，否则上面那条会因为它而永远红。 */
    @Test
    fun theGateFileItselfIsCounted() {
        val inGate = calls().filter { it.startsWith("ios/Shared/FleetPalette.swift:") }
        assertTrue("FleetPalette.swift 里一处 26-only 调用都没扫到，说明扫描或名单写错了：${calls()}", inGate.isNotEmpty())
    }
}
