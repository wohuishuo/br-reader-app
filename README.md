# 📱 阅读 App · br-reader-app

**[书域 BookRealm](https://github.com/wohuishuo/book-realm) 电子书平台的 Android 客户端(MVP-2)**

书域是拆成 5 个独立模块的电子书平台;本仓是用户真正"拿在手里"的那一块:
登录(走用户中心)、书城与书架(内容来自书库服务)、沉浸阅读器、AI 划词问答。

> 🚧 骨架阶段:工程全配(Compose + Hilt + Room + Retrofit + Navigation)、底部三 Tab、
> 双后端 Retrofit 实例、Room 缓存与 UiState 模式均已就绪并可编译;
> 业务功能按 [工单](https://github.com/wohuishuo/book-realm/blob/main/工单-MVP2阅读App.md) 从 R1 起开发。

> ⚠️ **Windows 必须克隆到纯 ASCII 路径**(如 `C:\dev\`)——AGP/aapt2 不支持中文路径,本机仓库就在 `C:\dev\br-reader-app`。

## 技术栈

Kotlin · Jetpack Compose + Material 3 · MVVM(StateFlow + UiState)· Navigation Compose · Hilt · Retrofit + kotlinx-serialization · Room · DataStore · Coil

## 快速开始

```powershell
# 1. 起后端(book-realm 仓):./start-platform.ps1   ← 会打印手机用的局域网 IP
# 2. 把 IP 填进 app/src/main/java/com/bookrealm/reader/data/remote/ApiConfig.kt
# 3. 构建并装到真机(USB 调试)
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

> 真机连后端用**电脑局域网 IP**;模拟器才用 `10.0.2.2`。地址只在 `ApiConfig.kt` 一处改。

讲解见平台书:[MVP-2 实战章](https://wohuishuo.github.io/book-realm/project/reader)

