# Zifeng Lite（子枫技能树 Lite，1.7.10 Forge）

> 开源仓库：https://github.com/aszqb1125763/zifeng_lite （MIT）

Minecraft **1.7.10 / Forge 10.13.4.1614** 的简化版技能树：保留技能树界面与快捷键，只保留五项常驻技能。**没有技能点、没有 HUD、没有转换机、没有光环/木棍/机械/垂钓等模块。**

## 五项技能

| 技能 | 效果 | 默认 |
|---|---|---|
| 万载不磨（不毁） | 手持/穿戴物品永不消耗耐久 | 开启 |
| 子枫的苍穹之翼（飞行） | 创造式飞行 | 开启 |
| 饱食无忧（不饿） | 饱食度/饱和度永远满值 | 开启 |
| 御空翱翔（飞行速度） | 等级 0~100 免费调 | 8 级（约 2×） |
| 星瞳夜视（夜视） | 永久夜视，不闪烁 | 开启 |

## 使用

- 按 `N` 打开技能树（单页，五行）。
- 点击每行右侧的「开启/关闭」开关切换；只有点中开关框才会切换，点行内其它位置不触发。
- 飞行速度行点击 `-` / `+` 调整等级。
- 默认快捷键（可在 `控制` 中修改）：
  - `U` 不毁
  - `I` 飞行
  - `O` 不饿
  - `J` 飞行速度开关
  - `P` 夜视
  - `K` 飞行速度 +1（按住 Shift +10）
- 五项首次进入自动满级/开启，不需要学习或技能点。
- 无前置，不需要 FE/发电模组。

## 安装

1. Minecraft 1.7.10 + Forge 10.13.4.1614。
2. 普通 1.7.10 Forge 用 `ZifengLite-1.7.10-1.6.0-lite.jar`，放进 `mods/`。
3. 无其他前置。

## GTNH 2.9（与普通 Forge 共用同一个 jar）

GTNH 2.9 是**混合命名**运行时：原版方法用 SRG，Forge 补的 `PlayerCapabilities.getFlySpeed/setFlySpeed` 用 MCP 名。

本模组已经把这几个飞行速度调用改成**运行时反射兼容**，所以普通 1.7.10 Forge 和 GTNH 2.9 都用同一个：

`ZifengLite-1.7.10-1.6.0-lite.jar`

另外已兼容**龙之研究（Draconic Evolution）**的龙之胸甲：目标速度 = `0.05 + 龙研 ArmorFlightSpeedMult 加成 + 本模组等级加成`。服务端和客户端都在 DE 的 `PlayerTick` 之后重新写入，避免被 DE 每 tick 重置。

- 水平速度：`flySpeed`，随等级/龙甲叠加。
- 垂直速度：1.7.10 的 `flySpeed` 只管水平；本模组额外在客户端写 `motionY`，上升/下降也随等级和龙研 `flightVModifier` 缩放。
- 调等级：`-`/`+` 普通 +1，按住 `Shift` +10，按住 `Ctrl` +100。

旧的 `-gtnh.jar` 已废弃，不要使用。客户端和服务端只放一个，不要重复。

## 构建（开发者）

- JDK 8（运行 `runClient` 必须；构建建议 JDK 8）
- Gradle 7.4.2（项目自带 wrapper）
- ForgeGradle 1.2 fork `com.anatawa12.forge:ForgeGradle:1.2-1.1.+`
- 受限网络可选：设置 `ZIFENG_MAVEN_LOCAL` 指向本地 Maven 镜像；不设置时使用公开镜像
- 默认输出到 `build/exports`；需要固定输出目录时设置 `ZIFENG_OUTPUT_DIR`

```bat
gradlew.bat build
gradlew.bat exportLiteJar
```

## 开源 / 致谢

MIT，源码：https://github.com/aszqb1125763/zifeng_lite

源自子枫的原版《子枫技能树》（MIT），五项技能与技能树界面由原项目简化/重写为 1.7.10 原生实现；未打包第三方游戏素材。AI 辅助开发。
