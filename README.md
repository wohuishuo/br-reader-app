# BookRealm Reader App

**Android 阅读客户端:登录、书城、书架、阅读器、阅读进度、AI 摘要和原文问答**

这是 BookRealm 的移动端入口,也可以作为 Jetpack Compose 阅读类 App 的学习样例。它把认证、书库、统计和 AI 服务连接到一个真实手机体验里。

[BookRealm 平台书](https://wohuishuo.github.io/book-realm/) · [本 App 实战章](https://wohuishuo.github.io/book-realm/project/reader)

## 一分钟理解

**br-reader-app 是读者真正拿在手里的部分。**

用户登录后,可以搜索公版书、加入书架、打开章节阅读。App 会在本地保存 token、字号、书架和阅读进度;阅读时还能上报统计,并请求 AI 对当前章节做摘要或围绕原文回答问题。

## 已实现功能

| 功能 | 说明 |
| --- | --- |
| 登录 | 调用用户中心 `/api/user/login`,保存 JWT 和 userId |
| 书城 | 调用书库服务 `/api/books`,支持关键词搜索 |
| 详情 | 展示简介、标签、章节目录,可加入书架 |
| 书架 | Room 本地缓存,重启后仍可见 |
| 阅读器 | 拉取章节段落,支持字号调节、本地进度记忆、段落级划线/笔记、章节离线缓存 |
| 统计 | 阅读时调用统计服务 `/api/stats/progress` |
| AI | 支持章节摘要和基于原文的问答 |
| 阅读体验 | 阅读页隐藏状态栏,AI 圆形入口按需展开,返回键按页面层级退出 |

## 代码结构

**结论:AppRoot 只负责导航,页面和组件各回各家。**

```text
app/src/main/java/com/bookrealm/reader/
├── navigation/AppRoot.kt       # 根导航、Snackbar、沉浸阅读切换
├── ui/design/                  # BrTheme、BrTokens、BrTopBar、BrNavBar、ReaderChrome
├── ui/screen/                  # 书架、书城、我的、详情、阅读器页面
├── ui/component/               # 封面、列表行、Loading/Error/Empty 状态
├── ui/reader/ReadStyle.kt      # 阅读主题、字号、行距模型
├── ui/theme/Tokens.kt          # 颜色、圆角、间距 token
├── viewmodel/ReaderViewModel.kt
├── data/remote/                # Retrofit API 与 DTO
└── data/local/                 # Room + DataStore
```

这次拆分是 v2.1 第二轮的基础。后面加划线、笔记、TTS、词典、竖排阅读时,优先改对应页面或组件,不要再把新功能塞回 `AppRoot.kt`。

## 设计系统

**结论:颜色、间距、形状和导航 chrome 只有一个真相来源。**

- `BrTheme`:统一 Material 3 color scheme,预留 Android 12+ dynamic color。
- `BrColors` / `BrDimens` / `BrShapes` / `BrMotion`:统一颜色、间距、圆角和动效参数。
- `BrTopBar` / `BrNavBar` / `BrNavItem`:普通页面统一顶栏和底部导航。
- `BrReaderTopSurface` / `BrReaderBottomSurface`:阅读器和沉浸页统一系统栏避让与工具层容器。

旧 `ReaderTokens` 已降级为兼容层,真实值来自 `ui/design`。

## 交互规则

**结论:读书时先服务正文,退出时先回到上一层。**

- 阅读页会隐藏手机顶部通知栏,底部工具栏避开系统导航栏;
- 阅读页顶部工具条会避开刘海/摄像头区域;
- AI 默认是右下角圆形入口,点击后展开提问面板,点关闭按钮或手机返回键会收回圆点;
- 长按段落会进入蓝色选中态,出现黑色浮动工具条;点另一个段落可扩展为段落范围选择;复制会提示成功,也可以划线、写想法、送去问 AI;
- AI 问书使用临时全屏窗口,和阅读正文分离;摘要也在这个窗口里显示,不再塞进正文;
- 在线打开过的章节会缓存到 Room;加入书架时会预缓存前 8 章,断网后可继续打开已缓存章节;
- 在阅读页按返回键:回到书籍详情;
- 在详情页按返回键:回到书架/书城;
- 在书城或我的页按返回键:回到书架;
- 在书架按返回键:第一次提示,第二次才退出 App。

## 技术栈

Kotlin · Jetpack Compose · Material 3 · MVVM · StateFlow · Navigation Compose · Hilt · Retrofit · kotlinx-serialization · Room · DataStore · Coil

## 快速开始

> Windows 建议克隆到纯 ASCII 路径,例如 `C:\dev\br-reader-app`。Android Gradle Plugin/aapt2 对中文路径不稳定。

```powershell
# 1. 启动后端服务
# 用户中心: http://localhost/api
# 书库服务: http://localhost:8082/api
# 统计服务: http://localhost:8083/api
# AI 服务:   http://localhost:8084/api

# 2. 真机 USB 调试:把手机 localhost 转发到电脑
adb reverse tcp:8080 tcp:80
adb reverse tcp:8082 tcp:8082
adb reverse tcp:8083 tcp:8083
adb reverse tcp:8084 tcp:8084

# 3. 构建并安装
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

默认演示账号:`root / 12345678`。

当前 `ApiConfig.kt` 默认面向 USB 真机调试:

| 服务 | 手机访问地址 |
| --- | --- |
| 用户中心 | `http://127.0.0.1:8080/api/` |
| 书库服务 | `http://127.0.0.1:8082/api/` |
| 统计服务 | `http://127.0.0.1:8083/api/` |
| AI 服务 | `http://127.0.0.1:8084/api/` |

模拟器改成 `10.0.2.2`;无线真机改成电脑局域网 IP。

## 在 BookRealm 中的位置

| 依赖 | 用途 |
| --- | --- |
| [user-center-team-project](https://github.com/wohuishuo/user-center-team-project) | 登录、JWT、用户身份 |
| [br-library-service](https://github.com/wohuishuo/br-library-service) | 搜书、目录、章节 |
| [br-event-stats](https://github.com/wohuishuo/br-event-stats) | 阅读进度上报 |
| [br-ai-service](https://github.com/wohuishuo/br-ai-service) | 摘要、原文问答 |
| [book-realm](https://github.com/wohuishuo/book-realm) | 平台总书和完整教学 |

## 验证

```powershell
./gradlew test
./gradlew assembleDebug
```
