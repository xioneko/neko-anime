<div align="center">

<img width="150px" src="./docs/images/app_icon.png" />

<h1>Neko Anime</h1>
<p>一个可以在线看番的 Android App</p>
<p>

[![Android](https://img.shields.io/badge/android-10+-green)](https://developer.android.com/about/versions/10)
[![Release](https://img.shields.io/github/v/release/xioneko/neko-anime)](https://github.com/xioneko/neko-anime/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/xioneko/neko-anime/total)](https://github.com/xioneko/neko-anime/releases/)
[![Powered-by](https://img.shields.io/badge/powered%20by-%E6%A8%B1%E8%8A%B1%E5%8A%A8%E6%BC%AB-ea5c7b)](https://yhdm6.top/)
[![License](https://img.shields.io/badge/license-GPLv3-yellow)](https://www.gnu.org/licenses/gpl-3.0.html)

</p>
</div>

## 介绍
<p>

Neko Anime 使用樱花动漫 [🌸](https://yhdm6.top/)
作为数据源，灵感来源于项目 [Imomoe](https://github.com/androiddevnotesforks/Imomoe)。技术栈方面采用了
Kotlin 搭配 Jetpack
Compose，借鉴了官方指南中的“[现代 Android 应用架构](https://developer.android.com/topic/architecture)
”最佳实践。同时，为尽可能地提高用户体验，Neko Anime 在 UI/UX 设计上广泛参考了相关优秀 app。

</p>

<p>

App 的开发仍在进行中 🚧，但是版本迭代可能会比较慢，可在 GitHub
上 <b>[⬇️下载最新版本](https://github.com/xioneko/neko-anime/releases)</b>
，最新的代码可在 [dev](https://github.com/xioneko/neko-anime/tree/dev) 分支上查看。

</p>

<p>

这个项目最初是为了学习 Android 开发而诞生的，后续的更新和维护就靠爱发电了😋，如果有什么功能或者技术上的好建议，欢迎在 [Issues](https://github.com/xioneko/neko-anime/issues) 中讨论☺️。

</p>

### 功能特色

- 海量番剧在线观看，无任何内部广告
- 丰富的搜索和分类检索功能
- 新番动态展示，以及每日更新表
- 一键追番，收藏你喜爱的番剧
- 本地观看历史，以及播放进度记忆
- 离线缓存番剧，摆脱龟速加载
- ...

### 应用截图

| 首页                              | 搜索                                  | 分类                                      | 播放                                  | 时间表                                     |
|---------------------------------|-------------------------------------|-----------------------------------------|-------------------------------------|-----------------------------------------|
| ![Home](./docs/images/home.png) | ![Search](./docs/images/search.png) | ![Category](./docs/images/category.png) | ![Player](./docs/images/player.png) | ![Schedule](./docs/images/schedule.png) |

## 技术特性
 - 参考了[官方指南](https://developer.android.com/topic/architecture)，并借鉴了 [nowinandroid](https://github.com/android/nowinandroid/) 的模块设计，UI Layer、Domain Layer 和 Data Layer 三层分离。
 - Data Layer 层使用了 Room (database)、Retrofit (http client)、jsoup (HTML 解析器)、jetpack datastore (数据存储)、kotlinx-serialization (数据结构化/序列化) 以及 store5 (“离线优先”解决方案)
 - UI Layer 层基于 Jetpack Compose，使用了 material3 (基础 UI 组件)、media3 (视频播放器)、coil (图片异步加载) 和 lottie-compose (矢量图形动画)
 - 使用 Hilt 实现依赖注入，使用 kotlin flow 实现异步数据流传输
 - ...

## 构建

- Android Studio Koala | 2024.1.1 Patch 2
- compileSdk 35
- Gradle JDK 17

## 更新日志

### v0.5.2 (2025-1-23)

**缺陷修复**

- 进入播放页时软件崩溃 ([#39](https://github.com/xioneko/neko-anime/issues/39))

### v0.5.1 (2024-11-19)

**改进功能**

- 主页面导航不丢失页面状态
- 追番列表排序优化

**缺陷修复**

- 番剧信息解析相关问题
- 检索页番剧加载不全
- 离线缓存偶尔失效

### v0.5.0 (2024-9-30)
**新增功能**
- 番剧离线缓存
- 播放器可通过滑动调节亮度和音量

**改进功能**
- 番剧视频加载性能优化
- 剧集列表滚动位置跟踪当前集数

**缺陷修复**
- 修复番剧数据缓存问题
- 搜索页和检索页相关问题修复

...(更多内容见[发布页](https://github.com/xioneko/neko-anime/releases))

## 许可证

[GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html)

## 免责声明

1. 此软件所展示的所有内容均来自互联网。
2. 此软件只提供服务，不存储、不制作任何数据内容，不承担任何由于内容的合法性及健康性所引起的争议和法律责任。
3. 若此软件收录的内容侵犯了您的权益，请联系邮箱 [xioneko@outlook.com](mailto:xioneko@outlook.com)。
4. 此软件仅可用作学习交流，不得用于商业用途。
