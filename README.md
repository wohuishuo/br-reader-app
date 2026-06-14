# 📱 阅读 App · br-reader-app

**[书域 BookRealm](https://github.com/wohuishuo/book-realm) 电子书平台的 Android 客户端(MVP-2)**

书域是拆成 5 个独立模块的电子书平台;本仓是用户真正"拿在手里"的那一块:
登录(走用户中心)、书城与书架(内容来自书库服务)、沉浸阅读器、AI 划词问答。

> ✅ MVP-2 第一条闭环已完成:登录走用户中心,书城/详情/章节走书库服务,书架用 Room 缓存,
> token/字号/阅读进度用 DataStore 保存。当前版本适合课程展示和继续扩展统计、AI 能力。

> ⚠️ **Windows 必须克隆到纯 ASCII 路径**(如 `C:\dev\`)——AGP/aapt2 不支持中文路径,本机仓库就在 `C:\dev\br-reader-app`。

## 技术栈

Kotlin · Jetpack Compose + Material 3 · MVVM(StateFlow + UiState)· Navigation Compose · Hilt · Retrofit + kotlinx-serialization · Room · DataStore · Coil

## 快速开始

```powershell
# 1. 起用户中心
cd C:\Users\艾莉\团队项目\user-center
docker compose up -d --build

# 2. 起书库 MySQL 与书库服务
docker run -d --name bookrealm-library-mysql `
  -e MYSQL_ALLOW_EMPTY_PASSWORD=yes `
  -e MYSQL_DATABASE=book_realm_library `
  -p 3306:3306 mysql:8

java -jar C:\Users\艾莉\知识数据库\起点-安卓项目\br-library-service\target\br-library-service-0.1.0-SNAPSHOT.jar

# 3. 真机 USB 调试:把手机 localhost 转发到电脑后端
adb reverse tcp:8080 tcp:80
adb reverse tcp:8082 tcp:8082

# 4. 构建并安装
cd C:\dev\br-reader-app
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

默认演示账号:`root / 12345678`。当前 `ApiConfig.kt` 默认面向真机 USB 调试:
用户中心 `http://127.0.0.1:8080/api/`,书库 `http://127.0.0.1:8082/api/`。
模拟器改成 `10.0.2.2`;无线真机改成电脑局域网 IP。

## 已实现功能

- 登录:调用 MVP-0 用户中心 `/api/user/login`,并写入 App 登录事件类型 `App`。
- 书城:调用 MVP-1 书库 `/api/books`,支持关键词搜索。
- 详情:展示简介、标签、章节目录,可加入书架。
- 书架:Room 本地缓存,重启后仍可看到收藏书。
- 阅读器:调用 `/api/chapters/{id}`,支持字号调节和阅读进度记忆。

讲解见平台书:[MVP-2 实战章](https://wohuishuo.github.io/book-realm/project/reader)

