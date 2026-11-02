# 出处与差异（UPSTREAM）

本仓库是 **[mylea1/phongent](https://github.com/mylea1/phongent)** 的衍生项目（fork 后改名），
在 Apache License 2.0 下分发。原始代码的著作权属于其作者，本仓库不主张对既有代码的原创。

| 项 | 值 |
| --- | --- |
| 上游仓库 | https://github.com/mylea1/phongent |
| 拉取时的上游提交 | `0b57e35d4e4c8460fdd87fcd33f751f764e7423d` |
| 拉取日期 | 2026-11-02 |
| 许可证 | Apache License 2.0（见 [LICENSE](LICENSE)） |
| 上游的第三方声明 | 见 [NOTICE](NOTICE) 与 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)，原样保留 |

授权链是两层的：phongent 自身派生自 [wangzan101/mika](https://github.com/wangzan101/mika)，
`NOTICE` 里那行 `Mika / Copyright 2026 wangzan101` 就是它留下的，本仓库继续带着它。

## 本仓库相对上游改了什么

第一阶段（提交 `7291fbf`）只改文档和门面，一行代码没动。第二阶段开始改界面，下表是当前累计状态：

| 文件 | 改动 |
| --- | --- |
| `README.md` | 重写（新名字、新首屏、徽标、章节结构、二次开发计划）；Android 最低版本随 minSdk 抬到 12 |
| `README.en.md` | 同上，英文版 |
| `docs/media/brand/fleetpilot-logo.png` | 新增，本仓库自己的标识 |
| `UPSTREAM.md` | 本文件 |
| `docs/ui-redesign.md` | 新增，界面重设计的决策记录（配色口径、材质矩阵、逐屏清单） |
| `docs/install-with-agent.md`、`.en.md`、`docs/setup-guide.md`、`.en.md` | 安卓 11 那一档随 minSdk 抬到 31 后不再存在，逐条改写 |
| `app/src/main/java/com/wangzan101/mika/ui/theme/Colors.kt` | 调色板改成单色相（紫），新增 wallTop / wallBottom / glassFill / glassHi |
| `app/src/main/java/com/wangzan101/mika/ui/theme/Glass.kt` | 新增：壁纸与玻璃面板，零第三方依赖 |
| `app/src/main/java/com/wangzan101/mika/ui/FleetShell.kt` | 根节点铺壁纸；底部导航、输入框、设备侧栏改玻璃/半透明 |
| `app/build.gradle.kts` | minSdk 30 → 31（背景模糊要 API 31） |
| `ios/Shared/FleetPalette.swift` | 新增：与安卓同一套单色相 token、壁纸、`fleetGlass`（iOS 26 系统材质，17–19 退回超薄材质） |
| `ios/App`、`ios/Shared`、`ios/Widgets` 下若干 `.swift` | 散落的系统彩色（mint/teal/orange/red/green/blue/purple/pink）换成同一套 token；头像逐机色相旋转收敛到紫带 |
| `.git` | 未继承上游历史，从零开始提交；出处以本文件与两份 README 顶部标注为准 |

上游的 `docs/media/brand/phongent-logo.png`、`phongent-icon.png` 以及 `ios/App/Assets.xcassets/AppIcon.appiconset/phongent.png`
都留在原位没删——它们仍被工程文件引用，删了会让构建找不到资源。

## 怎么核对改了哪些

```sh
git clone --depth 1 https://github.com/mylea1/phongent.git /tmp/upstream
git clone --depth 1 https://github.com/NIU-123370/fleetpilot.git /tmp/fleetpilot
diff -rq -x .git /tmp/upstream /tmp/fleetpilot
```

输出应当只有上表列出的那些文件。**多出来任何一条，说明有人在没更新本表的情况下动了代码。**

## 这批界面改动验证到什么程度（别当成已验证）

| 检查 | 状态 |
| --- | --- |
| `./gradlew :core:test`（纯 JVM 模块，含文档一致性测试） | ✅ 2426 条，0 失败，2 跳过 |
| `./gradlew :app:compileDebugKotlin` | ✅ 通过（先失败两次：`matchParentSize` 不该 import、`@Composable` 不能在 DrawScope 里调用；都已修） |
| `./gradlew :app:assembleDebug` | ✅ 出包 `app-debug.apk` 95 MB；`classes26.dex` 里能查到 FleetGlassSurface / FleetWallpaper 的符号，证明新代码真进了产物 |
| `./gradlew :app:testDebugUnitTest` | ⚠️ 982 条，**18 条红**；与改动前基线（提交 `7291fbf` 的 worktree）跑出的 18 条**逐条相同** ⇒ 本批改动净回归 0 条 |
| 那 18 条红是什么 | 上游遗留：App 文案已被上游从 Mika 改成 phongent，但 17 处测试期望还写着 Mika；另 1 条 `LicensesTest` 说许可页少了 Aora Bot / Mood Mates 两项。**都不是本仓库改出来的** |
| Swift 语法解析（`swiftc -parse`，30 个文件） | ✅ 全过；正对照：故意截断一个文件能报 4 个 error |
| Swift 类型检查与链接、iOS 构建 | ❌ 没做——本机只有 Command Line Tools，没有 Xcode（装它要 Apple ID 且 12 GB 起） |
| iOS 26 的 `glassEffect(_:in:)` 调用形状 | ⚠️ 照公开写法写，未经编译验证；Xcode 装上后第一件事就是验这一行 |
| 跑起来好不好看、滚起来掉不掉帧 | ❌ 没跑过任何一台真机或模拟器 |

**Why：** 这个仓库的立身之处是"没验证的话不写成已验证"。上面这些格子空着，比补一个好看的结论有用。
净回归那条为什么重要：18 条红如果只看数字，很容易被写成"改动引入 18 个失败"；
把改动前的提交单独 checkout 成 worktree 跑同一批用例、再逐条比失败清单，
才知道这 18 条本来就在——**复现命令**：

```sh
git worktree add -f /tmp/fp-baseline 7291fbf
cd /tmp/fp-baseline && JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=~/Library/Android/sdk \
  ./gradlew :app:testDebugUnitTest
# 再把两边 testDebugUnitTest/*.xml 里的 classname::name 集合 diff 一下
```

## 还没改名的部分（二次开发要一起处理的）

代码里的客体标识仍是上游的，这是"代码不动"的直接后果，装到手机上会看得出来：

- 安卓包名与 namespace：`app/build.gradle.kts` 里的 `com.wangzan101.mika`
- iOS 工程与 scheme：`ios/MikaRemote.xcodeproj`、target 名 `MikaRemote`
- 源码目录树按包名组织：`app/src/main/java/com/wangzan101/mika/`、`core/src/*/kotlin/com/wangzan101/mika/`
- 界面文案、通知渠道、无障碍服务名里出现的 `Mika` 字样
- 构建产物名沿用上游（本仓库不发布 release，README 里也不挂上游的 APK 下载链）

改这些不是重命名一个字符串：包名一变，已安装设备的升级路径、无障碍服务的已授权状态、
以及 `docs/` 里大量按包路径写的说明都要跟着动。真要改名，单开一笔提交做，别和二次开发混在一起。

## 复制过来时就存在的一处文档-测试死结

`core` 的 `ReadmeSectionsTest` 要求两份 README 里带有

```
<!-- generated:knowledge:start --> … <!-- generated:knowledge:end -->
<!-- generated:providers:start --> … <!-- generated:providers:end -->
```

四对标记，标记之间的清单由 `ReadmeSections.sections()` 从内置知识索引和
`ProviderPresets.kt` 生成（校验逻辑在 `core/src/testFixtures/kotlin/com/wangzan101/mika/core/docs/ReadmeSections.kt:102`，
断言在 `core/src/test/kotlin/com/wangzan101/mika/core/docs/ReadmeSectionsTest.kt:12`）。
上游拉下来的两份 README 里这四对标记**一个都没有**（`grep -c generated README.md` → 0），
所以 `./gradlew :core:test` 和 `./gradlew updateReadme` 都会在那条 `check` 上失败。

本仓库重写 README 时把四对标记补上了，并**在本机跑过那条命令**：

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew updateReadme
```

结果：`README.md 已更新`、`README.en.md 已更新`，标记之间现在是生成内容。
随后 `./gradlew :core:test` 全跑一遍：**2426 条测试，0 失败，2 跳过**，
其中包含 `ReadmeSectionsTest`（README 与源头一致）和 `DocsConsistencyTest`
（文档里反引号包着的路径与类名必须真实存在、不写死会变的个数）。
这条能跑是因为 `core` 是纯 JVM 模块、本机有 JDK 17；**`app` 模块仍然没编译过**，它要 Android SDK。

也就是说：上游带过来的这个死结在这里已经解开了，而且解开的方式是仓库自己的工具，不是我手填的。
