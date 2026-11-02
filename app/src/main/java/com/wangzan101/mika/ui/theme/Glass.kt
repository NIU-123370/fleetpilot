package com.wangzan101.mika.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.blur
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 页面壁纸:单色相(紫)的纵向渐变。
 *
 * 玻璃面板采样的就是这条渐变,所以面板透出来的颜色永远和页面背景对得上——
 * 不需要真的去读身后内容。
 */
@Composable
fun FleetWallpaper(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxSize().background(wallpaperBrush())) { content() }
}

/** 壁纸刷子和 [FleetWallpaper] 共用同一个来源,别让两处各写一遍色值。 */
@Composable
fun wallpaperBrush(): Brush {
    val c = MikaTheme.colors
    return Brush.verticalGradient(listOf(c.wallTop, c.wallBottom))
}

/**
 * 玻璃面板:按自己的形状把壁纸再画一遍并模糊,再盖半透明填充、描边和上缘高光。
 *
 * 为什么不引第三方模糊库:背景是静态渐变,"重画一遍再模糊"和"采样身后内容"在观感上等价,
 * 而前者零依赖、零版本分支。代价是**身后有内容滚动时它不会跟着变**——所以只用在
 * 导航栏、输入条、设备侧栏这三条常驻边上;滚动区里的卡片用半透明不模糊,
 * 长列表逐卡实时模糊会掉帧。
 *
 * [Modifier.blur] 需要 Android 12(API 31),这就是 minSdk 从 30 抬到 31 的理由。
 */
@Composable
fun FleetGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    blurRadius: Dp = 18.dp,
    content: @Composable () -> Unit,
) {
    val c = MikaTheme.colors
    Box(modifier.clip(shape).border(1.dp, c.glassEdge, shape)) {
        Canvas(Modifier.matchParentSize().blur(blurRadius)) { drawRect(brush = wallpaperBrush()) }
        Box(Modifier.matchParentSize().background(c.glassFill))
        // 上缘高光:一条从白到透明的窄带,是"玻璃"而不是"磨砂塑料"的那一下
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(listOf(c.glassHi, androidx.compose.ui.graphics.Color.Transparent), endY = 40f)
            )
        )
        content()
    }
}

/** 常驻底栏用的大圆角;输入条用中等圆角。集中在这里,别让各屏自己写数字。 */
object GlassShape {
    val Bar = RoundedCornerShape(28.dp)
    val Composer = RoundedCornerShape(22.dp)
    val Rail = RoundedCornerShape(0.dp)
}
