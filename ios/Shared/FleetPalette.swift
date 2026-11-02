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
    /// 玻璃材质:iOS 26 用系统液态玻璃,其余退回超薄材质(视觉接近,但不会随壁纸自动取色)。
    ///
    /// 这是 HUD-2 那条决定:工程 deploymentTarget 仍是 iOS 17.0,所以每处玻璃都必须带降级分支,
    /// 不许假设自己跑在 26 上。
    ///
    /// `#if compiler(>=6.2)` 不是多余的:液态玻璃的 API 只随 Xcode 26 的 SDK 提供,
    /// 用旧编译器(哪怕运行时分支写得对)连这个名字都看不见,整包会直接编译失败。
    /// 把它圈在编译器条件里,旧 Xcode 走 fallback,新 Xcode 才看到系统材质。
    @ViewBuilder
    func fleetGlass(cornerRadius: CGFloat = 24) -> some View {
        #if compiler(>=6.2)
        if #available(iOS 26.0, *) {
            self.glassEffect(.regular, in: RoundedRectangle(cornerRadius: cornerRadius))
        } else {
            self.background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: cornerRadius))
        }
        #else
        self.background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: cornerRadius))
        #endif
    }

    /// 只在**背后**加一层玻璃,不动 content 自己的绘制,还能带一点色调。
    ///
    /// 需要单独一个方法的原因很具体:`App/DeviceChat.swift` 的输入框原来是直接写
    /// `.glassEffect(...)`,外面只包了 `#available(iOS 26.0, *)`——那是**运行时**判断,
    /// 旧 Xcode 的 SDK 里根本没有这个名字,编译期就红了(CI 的 16.4 那一路真报的正是这条)。
    /// 编译器条件的圈法不能散在各处靠人记住,收进这一个出口。
    @ViewBuilder
    func fleetGlassBackground(cornerRadius: CGFloat = 28, tint: Color = .clear) -> some View {
        #if compiler(>=6.2)
        if #available(iOS 26.0, *) {
            self.background {
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(.clear)
                    .glassEffect(.regular.tint(tint), in: RoundedRectangle(cornerRadius: cornerRadius))
                    .allowsHitTesting(false)
            }
        } else {
            self.background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: cornerRadius))
        }
        #else
        self.background(.ultraThinMaterial, in: RoundedRectangle(cornerRadius: cornerRadius))
        #endif
    }

    /// 滚动区里的卡片:半透明不采样背景。长列表逐卡实时模糊会掉帧,所以只有常驻三条边用 `fleetGlass`。
    @ViewBuilder
    func fleetPanel(cornerRadius: CGFloat = 18) -> some View {
        self.background(FleetPalette.panel, in: RoundedRectangle(cornerRadius: cornerRadius))
            .overlay(RoundedRectangle(cornerRadius: cornerRadius).stroke(FleetPalette.hairline, lineWidth: 1))
    }
}
