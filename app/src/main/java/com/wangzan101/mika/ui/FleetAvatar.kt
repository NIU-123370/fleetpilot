package com.wangzan101.mika.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 设备/对话的小图标:半透明的玻璃色块 + 两只眼睛。
 *
 * 三件事一起做,让它一眼能认出是哪台设备,又不抢界面的紫:
 *  - **颜色**按 id 从固定调色板取(随机色相会撞色,也给不出黑、青这类);
 *  - **形状**也按 id 取(方圆/圆/六边形/宝石/有机圆),形状和颜色都不随改名变;
 *  - 填充是半透明的,配顶部高光、白色描边和底部内阴影 —— 浅紫底上看起来像一块磨砂玻璃,
 *    而不是一块实心色块。界面本身仍然只有一套紫色,彩色只出现在这个"头像"上。
 */
private val AvatarPalette = listOf(
    Color(0xFFF2761F), // 橙
    Color(0xFF14A79B), // 青
    Color(0xFF2F6FDE), // 蓝
    Color(0xFF15794B), // 绿
    Color(0xFF12151A), // 近黑
    Color(0xFFE0A31F), // 琥珀
    Color(0xFFD8443C), // 红
    Color(0xFF7A5AE0), // 紫
)

private const val SHAPES = 6

/** 同一个 id 永远是同一个颜色:FNV-1a 取模,只跟 id 有关,改名字不影响。 */
internal fun avatarColorIndex(id: String): Int = (avatarHash(id) % AvatarPalette.size).toInt()

/**
 * 形状单独取一次模,并用乘法混合打散:最初写成 `(hash / 8) % 形状数`,实测本机和"电脑端"两个 id
 * 会同时落到五边形 —— 除以 8 丢掉低位后高位太接近。乘一个奇数常量再取模,两个 id 就分开了。
 */
internal fun avatarShapeIndex(id: String): Int =
    (((avatarHash(id) * 2654435761L) and 0xffffffffL) % SHAPES).toInt()

private fun avatarHash(id: String): Long {
    var hash = 2166136261L
    for (b in id.lowercase(java.util.Locale.ROOT).toByteArray(Charsets.UTF_8)) {
        hash = ((hash xor (b.toInt() and 255).toLong()) * 16777619L) and 0xffffffffL
    }
    return hash
}

internal fun avatarColor(id: String): Color = AvatarPalette[avatarColorIndex(id)]

/** 在 [side]×[side] 的方框里画出第 [index] 种形状;左上角是原点。 */
private fun shapePath(index: Int, side: Float, sides: Int? = null): Path {
    val inset = side * 0.03f
    val box = side - inset * 2
    val c = side / 2f
    val r = box / 2f
    fun polygon(n: Int, rotation: Double): Path {
        val pts = (0 until n).map { i ->
            val a = (2 * PI / n * i) + rotation
            Offset(c + (r * cos(a)).toFloat(), c + (r * sin(a)).toFloat())
        }
        return roundedPolygon(pts, side * 0.09f)
    }
    // 有 ordinal 时按顺序给边数:第 3 台三角形、第 4 台四边形、第 5 台五边形……
    if (sides != null) {
        val n = sides.coerceIn(3, 8)
        // 四边形要从 45° 起角,否则顶点朝上会变成菱形;其它奇数边保持尖端朝上。
        return polygon(n, if (n == 4) PI / 4 else -PI / 2)
    }
    val path = Path()
    when (index % SHAPES) {
        0 -> return polygon(3, -PI / 2)                    // 三角形
        1 -> {                                             // 圆形
            path.addOval(androidx.compose.ui.geometry.Rect(inset, inset, inset + box, inset + box))
            return path
        }
        2 -> {                                             // 四边形(方圆)
            path.addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    inset, inset, inset + box, inset + box,
                    CornerRadius(box * 0.26f, box * 0.26f),
                ),
            )
            return path
        }
        3 -> return polygon(5, -PI / 2)                    // 五边形
        4 -> return polygon(6, 0.0)                        // 六边形
        else -> {                                          // 有机圆:手捏的一样
            path.moveTo(c, c - r)
            path.cubicTo(c + r * 0.9f, c - r * 0.95f, c + r * 1.02f, c - r * 0.2f, c + r * 0.86f, c + r * 0.42f)
            path.cubicTo(c + r * 0.72f, c + r * 1.02f, c - r * 0.5f, c + r * 0.98f, c - r * 0.82f, c + r * 0.4f)
            path.cubicTo(c - r * 1.04f, c - r * 0.3f, c - r * 0.9f, c - r * 0.98f, c, c - r)
            path.close()
            return path
        }
    }
}

/** 多边形圆角:每个顶点用一段二次曲线过渡,免得尖锐。 */
private fun roundedPolygon(pts: List<Offset>, radius: Float): Path {
    val p = Path()
    val n = pts.size
    for (i in 0 until n) {
        val cur = pts[i]
        val prev = pts[(i - 1 + n) % n]
        val next = pts[(i + 1) % n]
        val tp = Offset(prev.x - cur.x, prev.y - cur.y)
        val tn = Offset(next.x - cur.x, next.y - cur.y)
        val lp = kotlin.math.hypot(tp.x, tp.y).takeIf { it > 0f } ?: 1f
        val ln = kotlin.math.hypot(tn.x, tn.y).takeIf { it > 0f } ?: 1f
        val rp = kotlin.math.min(radius, lp * 0.45f)
        val rn = kotlin.math.min(radius, ln * 0.45f)
        val a = Offset(cur.x + tp.x / lp * rp, cur.y + tp.y / lp * rp)
        val b = Offset(cur.x + tn.x / ln * rn, cur.y + tn.y / ln * rn)
        if (i == 0) p.moveTo(a.x, a.y) else p.lineTo(a.x, a.y)
        p.quadraticBezierTo(cur.x, cur.y, b.x, b.y)
    }
    p.close()
    return p
}

/** 玻璃:半透明填充 + 顶部高光 + 白色细边 + 底部内阴影,再画两只眼睛。 */
private fun DrawScope.glassAvatar(side: Float, base: Color, shape: Int, active: Boolean, sides: Int?, seed: Long) {
    val path = shapePath(shape, side, sides)
    val tint = if (active) base else base.copy(alpha = 0.32f)
    // 半透明填充:上亮下暗一点,像玻璃有厚度
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            listOf(tint.copy(alpha = 0.72f), tint.copy(alpha = 0.46f)),
        ),
    )
    clipPath(path) {
        // 顶部高光:上面三分之一处一层白,往下化掉
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.White.copy(alpha = if (active) 0.66f else 0.24f),
                0.38f to Color.White.copy(alpha = if (active) 0.28f else 0.10f),
                0.72f to Color.Transparent,
            ),
            size = Size(side, side),
        )
        // 底部内阴影:压一道深色,玻璃有边缘感
        drawRect(
            brush = Brush.verticalGradient(
                0.62f to Color.Transparent,
                1f to base.copy(alpha = if (active) 0.30f else 0.12f),
            ),
            size = Size(side, side),
        )
    }
    // 白色细边
    drawPath(path, Color.White.copy(alpha = if (active) 0.85f else 0.35f), style = Stroke(width = side * 0.024f))
    // 眼睛:大小、间距、高低都按 id 变一点,不然一排看过去像同一个模板
    val eyeW = side * (0.100f + (seed / 45 % 4) * 0.008f)
    val eyeH = side * (0.155f + (seed / 180 % 4) * 0.012f)
    val eyeY = side * (0.355f + (seed % 9) * 0.013f)
    val halfSpread = side * (0.135f + (seed / 9 % 5) * 0.009f)
    val ink = if (active) Color(0xFF16181D).copy(alpha = 0.88f) else Color(0xFF3A3D45).copy(alpha = 0.45f)
    for (cx in listOf(side * 0.5f - halfSpread, side * 0.5f + halfSpread)) {
        drawRoundRect(
            color = ink,
            topLeft = Offset(cx - eyeW / 2f, eyeY),
            size = Size(eyeW, eyeH),
            cornerRadius = CornerRadius(eyeW / 2f, eyeW / 2f),
        )
    }
}

/** 设备头像;[active] 为 false 时整体变淡(还没连上/不在干活),形状和颜色不变。 */
@Composable
fun FleetAvatar(id: String, modifier: Modifier = Modifier, active: Boolean = true, ordinal: Int? = null) {
    val context = androidx.compose.ui.platform.LocalContext.current
    // 本机用设备自己的稳定 id:改设备名不该换颜色和形状。
    val stableId = if (id == "local") {
        context.getSharedPreferences("lan-identity", 0).getString("id", id) ?: id
    } else id
    val base = avatarColor(stableId)
    val shape = avatarShapeIndex(stableId)
    // ordinal 给了就按它在列表里的位置定边数(3、4、5……),没给(雷达页那种)才按 id 取形状
    val sides = ordinal?.let { 3 + it }
    Canvas(modifier) { glassAvatar(size.minDimension, base, shape, active, sides, avatarHash(stableId)) }
}

@Composable
fun FleetAvatarButton(id: String, name: String, selected: Boolean, onClick: () -> Unit, onMention: () -> Unit, ordinal: Int? = null) {
    var pressing by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val progress by androidx.compose.animation.core.animateFloatAsState(
        if (pressing) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(if (pressing) 450 else 100),
        label = "mention-fill",
    )
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Box(
        Modifier.width(72.dp).height(94.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .background(if (selected) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
            FleetAvatar(id, Modifier.size(60.dp), ordinal = ordinal)
            androidx.compose.material3.Text(name, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, maxLines = 2)
        }
        Box(Modifier.fillMaxWidth().fillMaxHeight(progress).background(androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)))
        Box(
            Modifier.matchParentSize().pointerInput(id) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onMention()
                    },
                    onPress = { pressing = true; tryAwaitRelease(); pressing = false },
                )
            },
        )
    }
}
