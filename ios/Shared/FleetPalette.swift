import SwiftUI

/// 单色相调色板(紫)。与安卓侧 `app/src/main/java/com/wangzan101/mika/ui/theme/Colors.kt` 同一套值。
///
/// 规矩 HUD-1:整套界面只用一条色相。状态(已核实 / 未核实 / 不通过)不再靠绿橙红区分,
/// 靠点位、字重和图标区分——加第二种色相之前先数这一屏已经有几处彩色。
/// 深浅两套是同一条色相的明度翻转,不是换色相。
///
/// 每个 token 都是 dynamic color(跟随系统深浅),所以调用处不需要拿 ColorScheme——
/// 少一层 @Environment 传递,就少一处会写错的地方。
enum FleetPalette {
    private static func rgb(_ hex: UInt32, _ alpha: Double = 1) -> UIColor {
        UIColor(red: CGFloat((hex >> 16) & 255) / 255,
                green: CGFloat((hex >> 8) & 255) / 255,
                blue: CGFloat(hex & 255) / 255,
                alpha: CGFloat(alpha))
    }

    private static func dyn(_ light: UInt32, _ lightAlpha: Double = 1,
                            _ dark: UInt32, _ darkAlpha: Double = 1) -> Color {
        Color(uiColor: UIColor { traits in
            traits.userInterfaceStyle == .dark
                ? rgb(dark, darkAlpha)
                : rgb(light, lightAlpha)
        })
    }

    /// 主文字 / 次文字 / 辅助文字
    static let ink = dyn(0x272036, 1, 0xF2EFFA)
    static let ink2 = dyn(0x645A7C, 1, 0xA9A2C0)
    static let ink3 = dyn(0x948DA6, 1, 0x6F6889)

    /// 唯一的强调色:主按钮、选中态、用户消息、开关
    static let accent = dyn(0x6D41D2, 1, 0x9C8FD6)
    static let accentSoft = dyn(0x6D41D2, 0.13, 0x9C8FD6, 0.16)

    /// 三档状态色:同一条色相,只差明度。读状态靠点位与字重,不靠色相。
    static let ok = dyn(0x524474, 1, 0xA9A2C0)
    static let warn = dyn(0x645A7C, 1, 0xC0B6DE)
    static let bad = dyn(0x7147D1, 1, 0xC4A9F0)

    /// 玻璃与面板:常驻 chrome 用 `fleetGlass`,滚动区卡片用 `panel`(半透明、不采样背景)。
    static let glass = dyn(0xFFFFFF, 0.46, 0x2E2A3D, 0.35)
    static let panel = dyn(0xFFFFFF, 0.62, 0x1E1B29, 0.72)
    static let hairline = dyn(0x5959A6, 0.10, 0x8C80B8, 0.14)

    /// 壁纸两端:单色相纵向渐变。玻璃面板透出来的就是它。
    static let wallTop = dyn(0xFAF8FE, 1, 0x241F35)
    static let wallBottom = dyn(0xDCD7EE, 1, 0x0C0A14)
}

/// 页面壁纸。放在每屏根节点的 background 上,玻璃层才有东西可透。
struct FleetWallpaper: View {
    var body: some View {
        LinearGradient(colors: [FleetPalette.wallTop, FleetPalette.wallBottom],
                       startPoint: .top, endPoint: .bottom)
            .ignoresSafeArea()
    }
}

extension View {
    /// 玻璃材质:iOS 26 用系统液态玻璃,iOS 17–19 退回超薄材质(视觉接近,但不会随壁纸自动取色)。
    ///
    /// 这是 HUD-2 那条决定:工程 deploymentTarget 仍是 iOS 17.0,所以每处玻璃都必须带降级分支,
    /// 不许假设自己跑在 26 上。
    @ViewBuilder
    func fleetGlass(cornerRadius: CGFloat = 24) -> some View {
        if #available(iOS 26.0, *) {
            self.glassEffect(.regular, in: RoundedRectangle(cornerRadius: cornerRadius))
        } else {
            self.background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: cornerRadius))
        }
    }

    /// 滚动区里的卡片:半透明不采样背景。长列表逐卡实时模糊会掉帧,所以只有常驻三条边用 `fleetGlass`。
    @ViewBuilder
    func fleetPanel(cornerRadius: CGFloat = 18) -> some View {
        self.background(FleetPalette.panel, in: RoundedRectangle(cornerRadius: cornerRadius))
            .overlay(RoundedRectangle(cornerRadius: cornerRadius).stroke(FleetPalette.hairline, lineWidth: 1))
    }
}
