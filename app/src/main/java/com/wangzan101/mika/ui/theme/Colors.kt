package com.wangzan101.mika.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 界面用到的全部颜色。页面一律从 [MikaTheme.colors] 取,不写死色值。
 * 实色只用在强调(主按钮、选中、开关、用户消息)和状态(成功、需要你、出错);图标块一律中性。
 *
 * **单色相规矩(HUD-1):** 整套调色板只用一个色相(紫,258°)。状态不再靠绿/橙/红区分,
 * 靠点位、字重和图标区分——加第二种色相之前先数这一屏已经有几处彩色。
 * 深浅两套是同一条色相的明度翻转,不是换色相。
 */
@Immutable
class MikaColors(
    /** 页面背景。 */
    val bg: Color,
    /** 卡片、分组。 */
    val surface: Color,
    /** 浅填充:次按钮、分段控件底、代码块底。 */
    val fill: Color,
    /** 文字三级:正文、次要、辅助。 */
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    /** 分隔线。 */
    val hair: Color,
    /** 卡片描边。 */
    val edge: Color,
    val accent: Color,
    val accentSoft: Color,
    /** 强调色上的文字和图标。 */
    val onAccent: Color,
    val ok: Color,
    val okSoft: Color,
    /** 「需要你」:[warn] 用于文字,[warnSolid] 用于圆点、描边。 */
    val warn: Color,
    val warnSolid: Color,
    val warnSoft: Color,
    val bad: Color,
    val badSoft: Color,
    /** 图标块的底。 */
    val neutralSoft: Color,
    /** Mika 回答的底:比页面背景深一点点的灰,和用户的蓝色气泡区分开。 */
    val answer: Color,
    /** 灰色胶囊、关着的开关。 */
    val greySoft: Color,
    /** 悬浮玻璃(输入栏、浮在内容上的按钮)的底和描边。 */
    val glass: Color,
    val glassEdge: Color,
    /** 卡片阴影;深色下卡片只靠描边,为透明。 */
    val cardShadow: Color,
    /** 悬浮元素的阴影。 */
    val floatShadow: Color,
    /** 开关的圆钮。 */
    val thumb: Color,
    /**
     * 图表里分类的颜色(例如每家服务商一种),按固定的先后取;超出的归到 [seriesOther]。
     * 只给图形上色,文字一律用 ink 三级。
     */
    val series: List<Color>,
    val seriesOther: Color,
    /** 图表的网格线。 */
    val grid: Color,
    /**
     * 壁纸渐变的上下两端。玻璃面板不是"随便一块半透明白":它把这条渐变按自己的形状
     * 再画一遍并模糊,所以透出来的颜色和页面背景一致。见 [FleetGlassSurface]。
     */
    val wallTop: Color,
    val wallBottom: Color,
    /** 玻璃面板的填充与上缘高光;描边复用 [glassEdge]。 */
    val glassFill: Color,
    val glassHi: Color,
    val isDark: Boolean,
)

val LightColors = MikaColors(
    bg = Color(0xFFF3F1F9),
    surface = Color(0xFFFFFFFF),
    fill = Color(0x145959A6),
    ink = Color(0xFF272036),
    ink2 = Color(0xFF645A7C),
    ink3 = Color(0xFF948DA6),
    hair = Color(0x145959A6),
    edge = Color(0x0F5959A6),
    accent = Color(0xFF6D41D2),
    accentSoft = Color(0x216D41D2),
    onAccent = Color(0xFFFFFFFF),
    // 单色相下 ok / warn / bad 只差明度,状态靠点位与字重读,不靠色相读
    ok = Color(0xFF524474),
    okSoft = Color(0x1F524474),
    warn = Color(0xFF645A7C),
    warnSolid = Color(0xFF9C8FD6),
    warnSoft = Color(0x1F9C8FD6),
    bad = Color(0xFF7147D1),
    badSoft = Color(0x1F7147D1),
    neutralSoft = Color(0x115959A6),
    answer = Color(0x66FFFFFF),
    greySoft = Color(0x3D6F6889),
    glass = Color(0x75FFFFFF),
    glassEdge = Color(0x245959A6),
    cardShadow = Color(0xFF000000),
    floatShadow = Color(0xFF000000),
    thumb = Color(0xFFFFFFFF),
    series = listOf(Color(0xFF6D41D2), Color(0xFF9C8FD6), Color(0xFFC9C0EA), Color(0xFF524474), Color(0xFF35305C)),
    seriesOther = Color(0xFFB4AECB),
    grid = Color(0x145959A6),
    wallTop = Color(0xFFFAF8FE),
    wallBottom = Color(0xFFDCD7EE),
    glassFill = Color(0x75FFFFFF),
    glassHi = Color(0xCCFFFFFF),
    isDark = false,
)

val DarkColors = MikaColors(
    bg = Color(0xFF14121C),
    surface = Color(0xFF1E1B29),
    fill = Color(0x1A8C80B8),
    ink = Color(0xFFF2EFFA),
    ink2 = Color(0xFFA9A2C0),
    ink3 = Color(0xFF6F6889),
    hair = Color(0x1F8C80B8),
    edge = Color(0x148C80B8),
    accent = Color(0xFF9C8FD6),
    accentSoft = Color(0x299C8FD6),
    onAccent = Color(0xFFFFFFFF),
    ok = Color(0xFFA9A2C0),
    okSoft = Color(0x24A9A2C0),
    warn = Color(0xFFC0B6DE),
    warnSolid = Color(0xFF9C8FD6),
    warnSoft = Color(0x299C8FD6),
    bad = Color(0xFFC4A9F0),
    badSoft = Color(0x24C4A9F0),
    neutralSoft = Color(0x148C80B8),
    answer = Color(0x14C9C0EA),
    greySoft = Color(0x386F6889),
    glass = Color(0xB81E1B29),
    glassEdge = Color(0x14FFFFFF),
    cardShadow = Color.Transparent,
    floatShadow = Color(0xFF000000),
    thumb = Color(0xFFFFFFFF),
    series = listOf(Color(0xFF9C8FD6), Color(0xFFC9C0EA), Color(0xFF6F6889), Color(0xFFE4DEF6), Color(0xFF4A4477)),
    seriesOther = Color(0xFF5A5470),
    grid = Color(0x148C80B8),
    wallTop = Color(0xFF241F35),
    wallBottom = Color(0xFF0C0A14),
    glassFill = Color(0x592E2A3D),
    glassHi = Color(0x1FFFFFFF),
    isDark = true,
)

/** 没有包在 [MikaTheme] 里时(比如界面测试)按浅色。 */
val LocalMikaColors = staticCompositionLocalOf { LightColors }
