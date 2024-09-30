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

### v0.4.0 (2024-6-29)
**缺陷修复**
- 更换樱花动漫数据源

**新增功能**
- 播放线路自动切换
- 支持播放缓存

### v0.3.1 (2024-5-20)
**缺陷修复**
- 进度条被拖拽时自动消失的问题

### v0.3.0 (2024-5-5)
**新增功能**
- 支持长按倍速播放和滑动改变播放进度 [#15](https://github.com/xioneko/neko-anime/issues/15)
- 添加无结果页（搜索、历史记录、追番列表）

**改进功能**

- 改进视频源获取方式，提高视频加载速度 [#12](https://github.com/xioneko/neko-anime/issues/12)
- 压缩资源体积，提升 App 整体性能

**缺陷修复**

- 进入时间表页面发生崩溃 [#13](https://github.com/xioneko/neko-anime/issues/13)

**界面美化**

- 改进播放器控件 UI 

**开发重构**

- 升级部分依赖项
- 重构播放页模块

**版本备注**

- 此版本未在平板或折叠屏设备进行充分测试
- 折叠屏用户若进入播放页异常，可尝试打开 “允许在竖屏状态下全屏播放” 选项

### v0.2.1 (2023-9-17)

**改进功能**

- 优化自动旋转，修复若干问题
- 增加“我的”->“禁用横屏模式”配置选项
- 增加“我的”->“允许在竖屏状态下全屏播放”配置选项

**界面优化**

- 优化平板/宽屏 UI 显示
- 更新轮播图内容（为了更好的视觉体验，暂且在宽屏状态下隐藏）

**缺陷修复**

- 更新视频源域名
- 崩溃提示对话框，可通过“我的”->"问题反馈"访问 GitHub Issues

**开发重构**

- 适配 Android 10
- 升级部分依赖

### v0.2.0 (2023-7-20)

**新增功能**

- 自动检查更新，直达新版下载地址
- 时间表筛选番剧
- 清空观看历史
- 清除番剧数据缓存
- 播放器全屏状态下进行选集
- 直达 GitHub 仓库

**界面优化**
- 优化播放器控件交互

**缺陷修复**
- 更新视频源域名
- 修复番剧集数加载错乱的问题

**开发重构**
- 缩减安装包体积 (50% ↓)

### v0.1.3 (2023-6-13)
**缺陷修复**
- 播放页动漫信息不更新的问题
### v0.1.2 (2023-6-6)
**改进功能**
- 支持 Android 11
- 在搜索结果页使用系统返回键只会返回到历史搜索页

**缺陷修复**
- 视频源 www.yinghuacd.com 无法使用的问题
- 视频源 m.yhdmz2.com 偶尔不可用的问题
- App 直接退出导致播放记录无法保存的问题
- 获取番剧信息时的集数解析问题

### v0.1.1 (2023-5-27)
**改进功能**
- 图片加载失败显示占位图
- 时间表页若数据获取异常给予反馈

**界面优化**
- 播放界面番剧信息展示布局
- “时间表”和“我的”界面背景色

**缺陷修复**
- 启动屏滞留时间过短
- 网络不可用时 Snackbar 不弹出
- 番剧播放界面的系统状态栏外观问题
- 获取番剧信息时，图片地址识别问题
- 播放器播放状态改变问题

### v0.1.0 (2023-5-24)
- 第一个发布版本，大多数功能基本实现
- 待实现功能包括但不限于，番剧下载、明亮/暗黑模式切换、个性化番剧推荐、触摸滑动改变播放时间条。


## 许可证

[GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html)

## 免责声明

1. 此软件所展示的所有内容均来自互联网。
2. 此软件只提供服务，不存储、不制作任何数据内容，不承担任何由于内容的合法性及健康性所引起的争议和法律责任。
3. 若此软件收录的内容侵犯了您的权益，请联系邮箱 [xioneko@outlook.com](mailto:xioneko@outlook.com)。
4. 此软件仅可用作学习交流，不得用于商业用途。
