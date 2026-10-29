# Changelog — Zifeng Lite 1.7.10

## 1.6.0-lite（2026-10-29）

- 首个 1.7.10 Forge 版本，独立工程/独立 modid `zifeng_lite`。
- 五项免费常驻技能：不毁、飞行、不饿、飞行速度、夜视。
- 1.7.10 原生 `GuiScreen` 技能树：单页五行、右侧独立开关框、飞行速度 `-`/`+` 精确按钮。
- 1.7.10 原生 `KeyBinding`：N 打开，U/I/O/J/P 切换，K 加速（Shift +10）。
- 补 `pack.mcmeta`，消除 FML 的“missing pack.mcmeta”警告（GTNH 环境更干净）。
- 飞行速度改为运行时反射兼容：普通 1.7.10 Forge（SRG）与 GTNH 2.9（混合命名）共用同一个 jar，修复 `PlayerCapabilities.func_75092_a` / `NBTTagCompound.hasKey` 的 `Internal server error`。
- 兼容龙之研究（Draconic Evolution）龙之胸甲：读取 `ArmorSummary.flightSpeedModifier`，速度按 `0.05 + 龙研加成 + 等级加成` 叠加；服务端/客户端都在 DE tick 之后写入，避免被 DE 重置。
- 修复垂直飞行速度：1.7.10 的 `flySpeed` 只影响水平，新增客户端 `motionY` 缩放（上升/下降），并叠加 DE `flightVModifier`。
- 飞行速度调节支持 Shift +10 / Ctrl +100，避免 100 级点 100 次。
- FoodStats / 药水 / 物品耐久 / 能力同步 / 服务端 player 字段全部改为按候选名+签名反射；修复 GTNH 服务端 `FoodStats.func_75114_a` 缺失导致的 `Internal server error`。
- 服务端 NBT 持久化（`player.getEntityData()`），登录/重生同步。
- 无技能点、无 HUD、无转换机、无前置、无需发电。
- 仅保留五项技能逻辑；不包含原版的光环/木棍/机械/垂钓/枪械等模块。
