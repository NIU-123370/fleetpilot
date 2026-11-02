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
| `README.md` | 重写（新名字、新首屏、徽标、章节结构、二次开发计划）；Android 最低版本随 minSdk 抬到 12；补了 CI 跑哪四件事、以及 iOS 为什么要编两路 |
| `README.en.md` | 同上，英文版 |
| `docs/media/brand/fleetpilot-logo.png` | 新增，本仓库自己的标识 |
| `app/src/main/res/drawable-nodpi/mika_launcher_art.png`、`ic_launcher_foreground.png`、`mipmap-anydpi-v26/ic_launcher.xml`、`values/colors.xml` | 桌面图标换成浅紫那版（手机 + 播放 + 对勾 + 星芒 + 环绕）。前景按 108dp 画布、画面占 74% 居中预排，安卓 12+ 的圆形遮罩只裁留白、不切画框；底色取素材四角的 #FBF8FD。文件名沿用上游的，因为工程文件和关于页都按名字引用它 |
| `ios/App/Assets.xcassets/AppIcon.appiconset/phongent.png` | 同一张图缩到 1024×1024 顶掉上游图标；文件名没改，`Contents.json` 不用动 |
| `UPSTREAM.md` | 本文件 |
| `docs/ui-redesign.md` | 新增，界面重设计的决策记录（配色口径、材质矩阵、逐屏清单） |
| `docs/roadmap.md` | 顶上补一句：这份是**上游**的路线图，本衍生项目自己的计划在 `README.md` 的「二次开发计划」 |
| `app/src/main/res/values*/strings*.xml`（三个语言的整份文案，526 处）与 `app/src/test`、`app/src/androidTest` 里对应的期望 | 把界面上的产品名统一成 **FleetPilot**（原来念 phongent，是上游改的名）。`app_name` 只有一份、URL 与上游名一律没动：紧贴 `/` 的那些是仓库地址（`mylea1/phongent`），这类误改我抓到过一次并补回来了 |
| `ios/App/Info.plist`、`ios/Widgets/Info.plist`、`ios/project.yml`、`ios/App/ModelAccount.swift` | iOS 的显示名跟上（App 与小组件都改；小组件那条在系统"桌面小组件"列表里显示） |
| `core/.../chatgpt/ChatGptAuth.kt` 与它的测试 | 发给 ChatGPT 登录的 `agent_name_hint` 也跟着走，不再冒上游的名字 |
| `docs/setup-guide.md`、`.en.md` | 安卓 11 那一档随 minSdk 抬到 31 后不再存在，逐条改写 |
| `docs/install-with-agent.md`、`.en.md` | 同上；另外第 2 步从"到上游 release 页下载 APK"改成"在本机从源码构建"，并写明不要拿上游的包顶替（理由见下节） |
| `app/src/main/java/com/wangzan101/mika/ui/theme/Colors.kt` | 调色板改成单色相（紫），新增 wallTop / wallBottom / glassFill / glassHi |
| `app/src/main/java/com/wangzan101/mika/ui/theme/Glass.kt` | 新增：壁纸与玻璃面板，零第三方依赖 |
| `app/src/main/java/com/wangzan101/mika/ui/FleetShell.kt` | 根节点铺壁纸；底部导航、输入框、设备侧栏改玻璃/半透明 |
| `app/build.gradle.kts` | minSdk 30 → 31（背景模糊要 API 31）；versionName 的后缀从 `-phongent` 改成 `-FleetPilot`（带 `-` 的那段在 `AppVersion` 里算"预发布"，只改名不动比较语义） |
| `ios/Shared/FleetPalette.swift` | 新增：与安卓同一套单色相 token、壁纸、`fleetGlass`（iOS 26 系统材质，17–19 退回超薄材质）；后来再加一个 `fleetGlassBackground`，把"只在背后垫一层、还能带色调"的编译器条件也收进同一个出口 |
| `ios/App/DeviceChat.swift` | 输入框那层原来直接调 `.glassEffect(...)`，外面只包 `#available(iOS 26.0, *)`——运行时的判断救不了编译期，Xcode 16.4 那一路就是这么红的；改成走 `fleetGlassBackground` |
| `core/src/test/kotlin/com/wangzan101/mika/core/IosCompilerGateTest.kt` | 新增守卫：26-only 的调用只许待在 `FleetPalette` 那个带 `#if compiler(>=6.2)` 的出口里，别处出现就红（投毒验过） |
| `core/src/test/kotlin/com/wangzan101/mika/core/NoStaleProductNameInUserCopyTest.kt` | 新增守卫：界面文案里不许留着上一个名字（URL 里的仓库地址按位置放行），并钉住产品名只定义一次 |
| `ios/MikaRemote.xcodeproj/project.pbxproj` | 上面那个新文件挂进工程：主 App 与 Widget 扩展两边的 Sources 阶段都要（`Widgets/MikaWidgets.swift` 也在用同一批 token）。Xcode 工程按名字逐个列源码，新增文件不会自己进去 |
| `core/src/test/kotlin/com/wangzan101/mika/core/IosProjectFileListTest.kt` | 新增守卫：磁盘上的 .swift 与工程里的文件引用、两边 Sources 阶段双向核对（见下节） |
| `ios/App`、`ios/Shared`、`ios/Widgets` 下若干 Swift 文件 | 散落的系统彩色（mint/teal/orange/red/green/blue/purple/pink）换成同一套 token；头像逐机色相旋转收敛到紫带 |
| `app/src/main/java/com/wangzan101/mika/capsule/CapsuleContent.kt` | 胶囊里两处写死的 `"Mika"` 改成 `tr(R.string.app_name)`（上游改名漏了它们，胶囊会一半念 phongent 一半念 Mika） |
| `app/src/main/java/com/wangzan101/mika/ui/LicensesScreen.kt` | 许可页补上 `Aora Bot / Mood Mates` 一条——`THIRD_PARTY_NOTICES.md` 有它，许可页漏列是合规问题 |
| `app/src/test/**`、`app/src/androidTest/**`（10 个文件） | 把 17 处还写着 Mika 的文案期望对齐到实际文案；URL 里的 `wangzan101/Mika` 一律没动 |
| `ui/ChatCopy.kt`、`ui/AboutScreen.kt`、`ui/McpScreen.kt`、`capsule/CapsuleCardModel.kt`、`capsule/CapsuleCardContent.kt`、`about/ApkUpdater.kt`、`about/Diagnostics.kt`、`about/UpdateChecker.kt`、`MikaApp.kt`、`settings/ProviderStore.kt`、`values-zh`/`values-b+zh+Hant/strings_chat.xml` | 界面上还写死自己名字的地方全部接到 `R.string.app_name` 这一个来源：对话页顶栏、关于页图标下面那行、卡片抬头（两处）、复制给剪贴板的标签、下载通知标题、诊断信息表头、发给模型服务与 GitHub 的 User-Agent、示例文案的繁简两版 |
| `ui/FleetAvatar.kt`（整份重写）、`ui/FleetShell.kt`、新测试 `AvatarColorTest.kt` | 设备头像从"每台设备一个 WebView 画紫色卡通"改成**画出来的玻璃小图标**：颜色按 id 取 8 色调色板，形状按列表顺序取边数（第三台三角形、第四台正方形、第五台五边形……），眼睛的大小/间距/高低也按 id 变化；填充半透明 + 顶部高光 + 白边 + 底部内阴影；左栏新增「**电脑端**」小球，点开是电脑经 MCP 派来的任务清单（会话 id 带 `mcp-` 前缀，点一行用通知同款深链打开那次对话） |
| `ui/FleetManagement.kt`、`ui/MainActivity.kt`、`ui/SettingsScreen.kt`、`ui/theme/Tokens.kt` | 补入口、修遮挡、修侧栏消失：agent 页的左边栏以前只在"选中了设备"时才画，"没选设备"直接把对话铺满整屏，用户看到的就是"侧栏没了"——现在左栏常驻，右侧按剩余宽度排；外壳设置页加「手机操作」一行（真机上配对内置助手就靠它）；外壳的设置 tab 底部加一张只读的版本信息卡（名称/版本/构建/上游出处）；旧的设置页给底部悬浮导航补了 88dp 留白，否则最后那张「关于」卡片正好压在导航底下，滚到底也点不到。关于页本身仍走对话页左侧抽屉（模拟器上复核：外壳有选中设备时不渲染 MikaRoot，从设置 tab 跳不过去，所以没做跳转） |
| `ui/LicensesScreen.kt` | 许可页的源码说明改成指本仓库真的有的文件（`tools/sandbox/sources.lock`、`patches/`、`build-native.sh`、`runtime-sources.py`），并写明这个仓库还没有发布页；顺手删掉那条列出来只为按名字滤掉的"App 自己" |
| `LicensesTest.kt`、`ChatCopyTest.kt`、`UpdateCheckerTest.kt`、`androidTest/AboutScreenTest.kt` | 钉死旧名字的三条期望改成跟同一个来源；新增"许可页指的脚本必须真存在"和"许可页与声明文件双向一一对应"两条用例 |
| `app/src/main/java/com/wangzan101/mika/remote/DeviceName.kt` 与 `remote/FleetController.kt` | 修中文设备名乱码：安卓 `NsdManager` 把 DNS 实例名按单字节解，对端用中文设备名做实例名时就成 `äº‘å®`（模拟器上真截到过）。TXT 的 name 和退回的 serviceName 两条路都过一遍（对正常中文原样返回，不会改坏）；windows-1252 里未定义的那几个字节被 Java 解成 U+FFFD，按位置当未知量反解。5 条单测 + 投毒验过（投毒后 4 条红） |
| `app/src/main/java/com/wangzan101/mika/about/MikaLinks.kt` | 对外地址分成两组：反馈/发布页/检查更新指向本仓库，上游两级（phongent、mika）作为 `UPSTREAM_*` 保留并显示在「关于」页 |
| `app/src/main/java/com/wangzan101/mika/about/UpdateChecker.kt` | 新增 `CheckStatus.NoReleases`：查发布吃 404 是"这个仓库还没发布过"，不是"连不上"（本 fork 现在正是前者） |
| `app/src/main/java/com/wangzan101/mika/ui/AboutCopy.kt`、`AboutScreen.kt` | 「关于」页加一行上游出处，并把"没有 release"单独提示出来 |
| `app/src/main/res/values*/strings_about.xml`（简中、繁中、英文） | 同上三条文案；"上游作者"这个改法把 wangzan101 的微博/微信标成**上游的个人渠道**，不是本项目的客服入口 |
| `app/src/test/java/com/wangzan101/mika/about/`（`UpdateCheckerTest.kt`、`MikaLinksTest.kt`、`ApkIntegrityTest.kt`） | 404 与"发布后清标记"两条新用例、上游出处在改名后仍在用例里、`ApkIntegrity` 的可信下载白名单跟着 `RELEASES` 改（**上游的 APK 现在会被拒**，见下节） |
| `core/.../core/mcp/*.kt`、`docs/mcp.md`、`docs/mcp.en.md`、`docs/architecture.md` 与 MCP 测试 | 电脑连接（MCP）那层的品牌对齐：握手信息 `name`/`title` 改成 fleetpilot/FleetPilot，六个工具名 `mika_*` → `fleetpilot_*`，给模型和用户的英文提示也跟着改（资源名 `mika_launcher_art` 这类没动）。真机实测 `tools/list` 六项全是 `fleetpilot_*` |
| `tools/provenance_check.py`、`tools/provenance-changed.txt` | 新增：把"diff 出来只有表里那些文件"变成机器判定——逐文件清单 + 双向核对（没申报 / 已陈旧都红） |
| `.github/workflows/ci.yml` | 新增：安卓全套（测试＋出包＋编译 androidTest）、与上游逐文件比对、iOS 语法解析、iOS 真编译（Xcode 26.3 与 16.4 两支） |
| `.git` | 未继承上游历史，从零开始提交；出处以本文件与两份 README 顶部标注为准 |

上游的 `docs/media/brand/phongent-logo.png`、`phongent-icon.png` 以及 `ios/App/Assets.xcassets/AppIcon.appiconset/phongent.png`
都留在原位没删——它们仍被工程文件引用，删了会让构建找不到资源。

## 改名之外，必须跟着转过去的三处

拉下来时界面上的对外地址仍指向上游，这不是"署名"问题，是会走错的功能：

1. **提反馈**：`MikaLinks.REPO` / `NEW_ISSUE` 原指 `wangzan101/Mika`。用这个 fork 的人点「反馈问题」，
   bug 会落到别人仓库的 issue 里，而本仓库的 issue 列表永远是空的——修的人看不见问题。
2. **检查更新**：`LATEST_RELEASE_API` 原来读上游的发布页。上游一发新版，本仓库的客户端就提示"有新版本"，
   并按上游的版本号判断新旧，而本仓库的改动根本不在那个包里。现在查本仓库，而本仓库还没有 release，
   GitHub 对这种情况回 **404**——所以 `CheckStatus` 多了一档 `NoReleases`，提示"这个仓库还没发布过安装包"，
   而不是和"网络连不上"共用一句失败提示。这一档在 `UpdateCheckerTest` 里有用例，
   包括"吃过一次 404、后来真出了 release 时标记要清掉"（否则第一次的 404 会永久盖住后面的成功）。
3. **APK 下载白名单**：`ApkIntegrity.requireOfficialUrl` 是按 `MikaLinks.RELEASES` 拼前缀的，
   所以第 1、2 条一改，**上游 `wangzan101/Mika` 和 `mylea1/phongent` 的 release 地址就不再是可信来源**。
   这是对的——本仓库不能把一个自己没核对过的产物当成"官方包"装进手机——
   但它是改地址的连带后果，不该被当成"顺手放宽一点"处理，所以 `ApkIntegrityTest` 把这两条明确写成"应被拒绝"。

上游的出处没有因为这三条消失：`UPSTREAM_REPO`（phongent）与 `UPSTREAM_OF_REPO`（mika）都在，
「关于」页新增一行显示它们，`MikaLinksTest::upstreamCreditSurvivesTheRename` 盯着这一条。
而 `X`、微信号那三个仍是**上游作者个人**的渠道，界面文案改成"上游作者"，避免有人把它们当成本项目的客服。

## 怎么核对改了哪些

上面那张表是人写的，机器读不动它，所以另有一份逐文件清单 `tools/provenance-changed.txt`，
由 `tools/provenance_check.py` 核对。本机跑（要先把上游拉到 /tmp/upstream）：

```sh
git init /tmp/upstream
git -C /tmp/upstream remote add origin https://github.com/mylea1/phongent.git
git -C /tmp/upstream fetch --depth 1 origin 0b57e35d4e4c8460fdd87fcd33f751f764e7423d
git -C /tmp/upstream checkout FETCH_HEAD
python3 tools/provenance_check.py /tmp/upstream .
```

不一致只有两种，都会一条条列出来：**改了但没申报**（动了代码没更新清单与上表），
和**申报了却已不存在差异**（某处改动被撤回去了，清单却没跟着删）。两种都判红。
改完代码重新生成：`python3 tools/provenance_check.py /tmp/upstream . --write`。

CI 的 `provenance` 那条 job 每天跑同一个脚本，并且**钉在拉下来那一笔提交上**——
跟着上游 main 走的话，上游自己继续演进就会把别人的改动算成"本仓库动了代码"，
红得和本仓库无关。这个判定是后加的：在那之前，本文件那句话只是印在纸上，
job 只打印不判定。

两条投毒都验过：往 `LICENSE` 后面加一行 → 报"改了但没申报: LICENSE"；
把 `README.md` 换成上游那份 → 报"申报了却已不存在差异: README.md"。撤掉后回到"一致"。

## 这批界面改动验证到什么程度（别当成已验证）

| 检查 | 状态 |
| --- | --- |
| `./gradlew :core:test`（纯 JVM 模块，含文档一致性测试与上面两节的 iOS 守卫） | ✅ 2439 条，0 失败，2 跳过 |
| `./gradlew :app:testDebugUnitTest` | ✅ 986 条，**0 失败**（原来 18 条红，见下一节，已修；之后新增 4 条用例） |
| `./gradlew :app:compileDebugKotlin` | ✅ 通过（先失败两次：`matchParentSize` 不该 import、`@Composable` 不能在 DrawScope 里调用；都已修） |
| `./gradlew :app:assembleDebug` | ✅ 出包 `app-debug.apk` 99,475,887 字节（约 95 MB；同一次改名前后差 4 MB，是 dex/压缩的差异，不是少了东西——包里 `assets/runtime/rootfs.tar.xz` 31.5 MB、`lib/arm64-v8a/libproot.so / libmikarun.so / libproot_loader.so` 都在，908 条目 / 解压后 123.4 MB）。`classes26.dex` 里能查到 FleetGlassSurface / FleetWallpaper 的符号，证明新代码真进了产物；`aapt dump badging` 读到的 `application-label` 是 FleetPilot、`versionName` 是 1.0.12-FleetPilot；`classes26.dex` 里能查到 FleetGlassSurface / FleetWallpaper 的符号，证明新代码真进了产物 |
| `./gradlew :app:assembleDebugAndroidTest` | ✅ 编译并打包 `app-debug-androidTest.apk` 4.1 MB。CI 里也跑这一步——**只编译，不运行**（没有设备）。加它是因为 androidTest 里"关于页顶上那行写着 Mika"那条断言早就和界面不一致，而平时没人跑得到 |
| GitHub Actions（`.github/workflows/ci.yml`） | ✅ `4f0a81f` 起三条 job 全绿（2026-11-02 06:0x 那一次）：`android` 的"跑测试并出包"跑了 442 秒并附上两个 APK，`provenance`、`ios-syntax` 绿。此前 `android` 连红四次，原因见下面那节 |
| Swift 语法解析（`swiftc -parse`，30 个文件） | ✅ 全过；正对照：故意截断一个文件能报 4 个 error。**这条只到语法**——看不见"某文件没参与编译"和类型错误，上面那节讲的就是它漏掉的东西 |
| Swift 类型检查与链接、iOS 构建（本机） | ❌ 没做——本机只有 Command Line Tools，没有 Xcode（装它要 Apple ID 且 12 GB 起） |
| iOS 真编译（CI 的 `ios-build`，两个 Xcode 各一路） | ✅ 两路都绿：26.3 在 `2b4a348`（App 与 Widget 扩展都编，101 秒，模拟器、不签名、`DEVELOPMENT_TEAM` 清空），16.4 在 `3e74c90`。中间 16.4 那一路在 `2235dab` 红过一次，报 `has no member 'glassEffect'`——是真 bug，见上节第 3 条。这仓库第一次真的做上 Swift 类型检查 |
| 跑起来好不好看、滚起来掉不掉帧 | ❌ 没跑过任何一台真机或模拟器 |

**Why：** 这个仓库的立身之处是"没验证的话不写成已验证"。上面这些格子空着，比补一个好看的结论有用。

## 那 18 条红测试：不是本仓库改坏的，但确实是个真 bug

`:app:testDebugUnitTest` 一开始 18 条红。光看数字会被读成"这次改动引入 18 个失败"，
所以把改动前那笔提交 `7291fbf` 单独 checkout 成 worktree 跑同一批用例：
两边的 `classname::name` 清单**逐条相同** ⇒ 净回归 0 条。

复现命令：

```sh
git worktree add -f /tmp/fp-baseline 7291fbf
cd /tmp/fp-baseline && JAVA_HOME=/opt/homebrew/opt/openjdk@17 ANDROID_HOME=~/Library/Android/sdk \
  ./gradlew :app:testDebugUnitTest
# 再把两边 app/build/test-results/testDebugUnitTest/*.xml 里的 classname::name 集合 diff
```

但那 18 条本身暴露的是上游改名没改完，所以该修的不是"让数字好看"，是把不一致修掉：

1. **17 条**：文案资源已被上游从 Mika 改成 phongent，测试期望还写着 Mika。
   把这些期望改成 phongent（URL 里的 `wangzan101/Mika` 是仓库地址，不是产品名，一处都没动）。
2. **`CapsuleModelTest::ballDescribesItsStatus`** 反过来暴露了产品里的真 bug：
   `CapsuleContent.kt` 里 IDLE 和"办好了"那一句还写着 `"Mika"` 字面量，
   而同一个胶囊的另一句来自资源、已经念 phongent——**用户会看到"phongent 在干活"却报"Mika 办好了"**。
   修法不是把测试改回 Mika，而是把那两处字面量改成 tr(R.string.app_name)：
   品牌名从此只有一个来源，下次改名不会再漏。
3. **`LicensesTest::everyNoticeEntryShown`**：`THIRD_PARTY_NOTICES.md` 里有
   "Aora Bot / Mood Mates" 这一条，许可页却没列——**许可页少列一项第三方组件是合规问题，不是测试太严**。
   已把这一条补进 `Licenses.ENTRIES`。

修完：`:app:testDebugUnitTest` 982 条 0 失败。

## iOS：语法检查看不见的三个洞，以及挡住它们的守卫

`swiftc -parse` 是**逐文件、只到语法**的检查。它看不见两类问题：某个符号根本没参与编译，以及类型不对。
配色那批改动就撞上第一类，而且是两层：

1. 新加的 `ios/Shared/FleetPalette.swift` 写进了磁盘，**15 个文件、66 处**引用它的 token
   （现算：`grep -rn "FleetPalette\|fleetPanel\|fleetGlass\|FleetWallpaper" ios --include=*.swift` 去掉它自己那 9 处），
   而 `ios/MikaRemote.xcodeproj/project.pbxproj` 里**一处都没有**——Xcode 工程按名字逐个列源码，
   新增文件不会自己进去。这条 parse 检查全绿也照样发现不了。
   （更正一下：这一节最早写的是"七十多个文件"，那是**引用行数**，不是文件数。）
   （上游用 `ios/project.yml` + XcodeGen 生成工程，按目录收源码；提交进仓库的那份 `.xcodeproj` 是生成结果，
   只有重新生成才带上新文件。）
2. 补上主 App 之后才发现还有第二边：`Widgets/MikaWidgets.swift` 也在用同一批 token，
   所以 `Shared/` 的文件必须**同时**出现在两个 target 的 Sources 阶段里，只补一边照样编译不过。

为此加了 `core/src/test/kotlin/com/wangzan101/mika/core/IosProjectFileListTest.kt`：
磁盘上的 .swift 与工程里的 `PBXFileReference`、两个 target 各自的 Sources 阶段做双向核对，
`Shared/` 单独要求两边都有。它自己不依赖 Xcode，所以本机、CI 都跑得到。

守卫本身验过两次投毒（不验不算数）：

| 投毒 | 红的那条 |
| --- | --- |
| 只摘掉 Widget 一边的阶段条目 | `sharedSourcesCompileIntoEveryTarget`（另外六条全绿，说明这两条不是同一件事） |
| 两边的阶段条目都摘掉 | 上面那条 + `everySwiftFileIsCompiledBySomeTarget` |

还有一处防"守卫自己空跑"的断言：解析不出 `MikaRemote` 与 `MikaWidgets` 两个 target 就直接失败——
第一版正是栽在这里（按两个 tab 切块把 target 块切碎了，`compiledByTarget` 是空表，
于是"所有文件都没编译"被读成"没有文件要编译"，全绿）。

3. 还有一层只在**旧 Xcode** 上才暴露：`App/DeviceChat.swift` 的输入框直接写了 `.glassEffect(...)`，
   外面只包 `#available(iOS 26.0, *)`。那是运行时判断——旧 SDK 编译期连这个名字都不认识，整包直接编译失败。
   `2b4a348` 加了 Xcode 16.4 那一路之后当场就红了（26.3 那一路永远看不见这个问题，因为它不需要回退支）。
   修法是给 `FleetPalette` 再加一个 `fleetGlassBackground`（编译器条件＋运行时判断＋降级都在里面），
   界面只调它；`IosCompilerGateTest` 再把"别处不许裸调 26-only 的 API"钉住——
   往 Widget 文件里插一处裸调用，它报的是 `ios/Widgets/MikaWidgets.swift:2`，撤掉就绿。

仍然没验到的：真机运行、签名与上架。`ios/project.yml` 里 `DEVELOPMENT_TEAM: Y726TUG6G3`
仍是上游作者的签名团队 id，本 fork 换自己的 Apple 账号时要一起改。

## CI 连红四次的原因：两个环境变量指了两个 SDK

`android` 这条 job 从没绿过。前四次能读到的公开读数只有这些（都是这一轮从 GitHub 公共 API 现查的）：

| 提交 | 装 SDK 那步 | 跑测试那步 | artifact 数 |
| --- | --- | --- | --- |
| `4ff2fdf` | ❌ 6 秒 | 跳过 | 0 |
| `e001a1e` | ✅ 19 秒 | ❌ 72 秒 | 0 |
| `25c59e7` | ✅ 6 秒 | ❌ 43 秒 | 0 |
| `e679b98` | ✅ 8 秒（并按目录断言过）| ❌ 56 秒 | 0 |

`4ff2fdf` 那条的原因当场查实并修掉了：`yes \| sdkmanager --licenses` 里 `yes` 被 SIGPIPE 打成 141，
`set -o pipefail` 把它算成整步失败（本机复现过）。改成预置许可文件后这一步绿了。

后三条一直红在同一个地方，而日志下载要登录（未认证回 403），注释里只有一句 `Process completed with exit code 1`。
所以第四次没再猜，改成让失败自己讲话：单拆一步 `./gradlew --version`、装完 SDK 断言目录真的存在、
把日志里 `What went wrong` 那段压成一行写成 `::error::` 注释（注释不要登录就能读），全文另存 artifact。

第五次推送跑出来，答案就在注释里：

```
What went wrong: Could not determine the dependencies of task ':app:testDebugUnitTest'.
> Several environment variables and/or system properties contain different paths to the SDK.
  ANDROID_HOME: /home/runner/android-sdk
  ANDROID_SDK_ROOT: /usr/local/lib/android/sdk
```

ubuntu-latest 镜像自带一个 SDK，并用 `ANDROID_SDK_ROOT` 指过去；我只设了 `ANDROID_HOME`，
两个变量指了两个目录，AGP 直接拒绝算依赖。**这条错误发生在依赖解析阶段**，
所以一条测试结果都不会留下——这正好是前三次"红得看不出原因"的样子。
修法是把两个变量写成同一个目录，并在 `./gradlew` 之前自己检查一遍、不对就自己报一行。

顺带把三条只有本机才量得到的事实记下来，免得下次又当谜猜：
`:core:test` 在 `ANDROID_HOME` 指向一个空目录时**照样过**（rc=0，120 秒）——纯 JVM 模块不需要安卓 SDK；
本机 `licenses/android-sdk-license` 只有一个摘要（41 字节，`\n24333f8a…1fee`），CI 预置的是它的超集；
`platforms;android-36` 与 `build-tools;36.0.0` 在 Google 的 `repository2-3.xml` 里确实列着。

改完的下一次推送（`4f0a81f`）三条 job 全绿：`android` 的"跑测试并出包"这一步跑了 442 秒——
这是这个仓库的 CI 第一次真的把测试跑起来。

## 还没改名的部分（二次开发要一起处理的）

代码里的客体标识仍是上游的，这是"代码不动"的直接后果，装到手机上会看得出来：

- 安卓包名与 namespace：`app/build.gradle.kts` 里的 `com.wangzan101.mika`
- iOS 工程与 scheme：`ios/MikaRemote.xcodeproj`、target 名 MikaRemote
- 源码目录树按包名组织：`app/src/main/java/com/wangzan101/mika/`、`core/src/*/kotlin/com/wangzan101/mika/`
- 通知渠道名与无障碍服务名里出现的旧名（下面第 2 类）——**给用户看的文案已经统一到 FleetPilot**，这三类没跟着动是有原因的
- 构建产物名沿用上游（本仓库不发布 release，README 里也不挂上游的 APK 下载链）

这一轮把"界面上说自己是谁"接到了一个来源之后，剩下的 `Mika` 都归在下面这三类，各自都有不动的理由：

1. **标识符，不是文案**：`MikaTheme`、`MikaButton`、`MikaType`、`Theme.Mika` 这两个样式名、
   `res/drawable-nodpi/mika_launcher_art` 这类资源名。改它们等于全仓重命名，和上面包名那条是同一件事。
2. **落在设备上的名字**：`AndroidManifest.xml` 里通知监听服务的 `android:label="Mika 消息同步"`（在系统"通知使用权"列表里显示），
   以及导出文件用的目录 `Download/Mika/`、相册里的 `Pictures/Mika/`。目录名不能随意改——
   `ShareExporter` 是按那个路径找回之前导出的文件的，改了旧文件就"消失"了；服务标签要改得连着系统设置里的授权状态看，本机没有设备可验。
3. **有功能作用的字符串**：`ShellService` 的 `CLIP_LABEL`（它同时是"这段剪贴板内容是不是我们自己写的"的判据，
   两处必须同值），以及喂给模型的屏幕状态描述里那句 "on Mika's screen"（改了要重跑评测才知道有没有影响）。

`docs/architecture.md` 里"只接受官方仓库 `releases/download/` 下的 HTTPS 地址"这类描述仍然是对的——
"官方"现在指本仓库，代码里的白名单跟着 `MikaLinks.RELEASES` 走。

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
随后 `./gradlew :core:test` 全跑一遍：**0 失败，2 跳过**（条数随用例增加会变，现算：`grep -ho 'tests="[0-9]*"' core/build/test-results/test/*.xml`），
其中包含 `ReadmeSectionsTest`（README 与源头一致）和 `DocsConsistencyTest`
（文档里反引号包着的路径与类名必须真实存在、不写死会变的个数）。
这条能跑是因为 `core` 是纯 JVM 模块、本机有 JDK 17；**`app` 模块仍然没编译过**，它要 Android SDK。

也就是说：上游带过来的这个死结在这里已经解开了，而且解开的方式是仓库自己的工具，不是我手填的。
