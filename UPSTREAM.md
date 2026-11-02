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

只改文档和门面，**一行代码没动**：

| 文件 | 改动 |
| --- | --- |
| `README.md` | 重写（新名字、新首屏、徽标、章节结构、二次开发计划） |
| `README.en.md` | 同上，英文版 |
| `docs/media/brand/fleetpilot-logo.png` | 新增，本仓库自己的标识 |
| `UPSTREAM.md` | 本文件 |
| `.git` | 未继承上游历史，从零开始提交；出处以本文件与两份 README 顶部标注为准 |

上游的 `docs/media/brand/phongent-logo.png`、`phongent-icon.png` 以及 `ios/App/Assets.xcassets/AppIcon.appiconset/phongent.png`
都留在原位没删——它们仍被工程文件引用，删了会让构建找不到资源。

## 怎么核对"只改了这些"

```sh
git clone --depth 1 https://github.com/mylea1/phongent.git /tmp/upstream
git clone --depth 1 https://github.com/NIU-123370/fleetpilot.git /tmp/fleetpilot
diff -rq -x .git /tmp/upstream /tmp/fleetpilot
```

期望输出只有上表那几项（新增两份文件、改了两份 README）。多出来任何一条，
说明有人在没更新本文件的情况下动了代码。

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

本仓库重写 README 时把四对标记补上了，标记之间先留空。跑一次就能填上并让测试转绿：

```sh
./gradlew updateReadme
```

（这条命令我没在本机执行过：它要 JDK 17 加 Android SDK 的完整构建环境，本机没配齐。
所以标记是照 `ReadmeSections` 的字符串格式手写的，执行结果以你跑出来的为准。）
