# CobblemonSkin Mod

基于 Cobblemon 的 `VaryingRenderableResolver` + `forcedAspects` 机制，为精灵添加自定义皮肤（贴图替换）。

## 环境要求

- Minecraft 1.21.1
- Fabric Loader ≥ 0.15.11
- Fabric API 0.102.0+1.21.1
- Fabric Language Kotlin 1.12.1+kotlin.2.0.20
- Cobblemon（任意兼容 1.21.1 的版本）

---

## 编译步骤

### 前置准备

**① 获取 gradle-wrapper.jar**（只需做一次）

方法 A — 如果已安装全局 Gradle（推荐）：
```powershell
# 在项目根目录运行
gradle wrapper --gradle-version 8.8
```

方法 B — IntelliJ IDEA：
直接用 IDEA 打开项目文件夹，它会自动下载 Gradle Wrapper。

方法 C — PowerShell 直接下载：
```powershell
$url = "https://github.com/gradle/gradle/raw/v8.8.0/gradle/wrapper/gradle-wrapper.jar"
Invoke-WebRequest -Uri $url -OutFile "gradle\wrapper\gradle-wrapper.jar"
```

**② 放入 Cobblemon JAR**

从 [Modrinth](https://modrinth.com/mod/cobblemon/versions) 下载 Cobblemon Fabric 1.21.1 版本，
将 JAR 文件放到 `libs/` 目录（文件名需包含 `cobblemon-fabric`）。

### 构建

```powershell
.\gradlew.bat build
```

输出：`build/libs/cobblemon-skin-mod-1.0.0.jar`

---

## 命令

| 命令 | 说明 |
|------|------|
| `/pokemonskin set <skinId> [slot]` | 给槽位精灵应用皮肤（slot 1-6，默认 1）|
| `/pokemonskin clear [slot]` | 清除槽位精灵的皮肤 |
| `/pokemonskin list` | 列出所有已注册皮肤 ID |
| `/pokemonskin info [slot]` | 查看槽位精灵当前皮肤 |

权限等级：2（OP）

---

## 如何添加新皮肤

### 步骤 1 — 制作贴图

将皮肤贴图（PNG）放到：
```
src/main/resources/assets/cobblemon_skin/textures/pokemon/skins/<skinId>.png
```
贴图尺寸与目标精灵的原版贴图一致即可。

### 步骤 2 — 创建变体 JSON

在以下目录创建 JSON 文件：
```
src/main/resources/assets/cobblemon/bedrock/pokemon/variations/<skinId>.json
```

文件内容模板：
```json
{
  "species": "cobblemon:<物种ID>",
  "order": 100,
  "variations": [
    {
      "aspects": ["skin_<skinId>"],
      "texture": "cobblemon_skin:textures/pokemon/skins/<skinId>.png"
    }
  ]
}
```

**示例（皮卡丘黑色皮肤）：**
```json
{
  "species": "cobblemon:pikachu",
  "order": 100,
  "variations": [
    {
      "aspects": ["skin_pikachu_black"],
      "texture": "cobblemon_skin:textures/pokemon/skins/pikachu_black.png"
    }
  ]
}
```

### 步骤 3 — 注册皮肤 ID

在 `CobblemonSkinMod.kt` 的 `registerBuiltinSkins()` 方法里加一行：
```kotlin
registerSkin("pikachu_black")
```

这样 `/pokemonskin list` 才能显示、`/pokemonskin set` 才能使用 Tab 补全。

---

## 技术原理

```
/pokemonskin set pikachu_black 1
  ↓
pokemon.forcedAspects += "skin_pikachu_black"
  ↓
pokemon.updateAspects() (自动触发)
  ↓
AspectsUpdatePacket → 同步到所有客户端
  ↓
PokemonClientDelegate.currentAspects 更新
  ↓
VaryingRenderableResolver.getResolvedTexture(state)
  → 找到 aspects=["skin_pikachu_black"] 的变体
  → 返回 cobblemon_skin:textures/pokemon/skins/pikachu_black.png
  ↓
渲染使用新贴图
```

皮肤数据通过 `forcedAspects` 持久化在 Pokémon NBT 里，
重载服务器后依然保留。

---

## 支持自定义模型（进阶）

如果需要替换 3D 模型而不只是贴图，在变体 JSON 里额外指定 `model` 和 `poser`：

```json
{
  "species": "cobblemon:pikachu",
  "order": 100,
  "variations": [
    {
      "aspects": ["skin_pikachu_armored"],
      "model": "cobblemon_skin:bedrock/pokemon/models/pikachu_armored.geo.json",
      "poser": "cobblemon_skin:bedrock/pokemon/posers/pikachu_armored.json",
      "texture": "cobblemon_skin:textures/pokemon/skins/pikachu_armored.png"
    }
  ]
}
```

模型格式为 Bedrock Edition `.geo.json`，可以用 Blockbench 制作。

---

## 其他模组集成

其他模组可以在 `onInitialize()` 里调用：
```kotlin
CobblemonSkinMod.registerSkin("my_skin_id")
```
然后自行提供对应的变体 JSON 和贴图资源即可。
