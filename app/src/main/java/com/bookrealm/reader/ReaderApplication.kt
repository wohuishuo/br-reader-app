package com.bookrealm.reader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** 书域阅读 App。@HiltAndroidApp 启动依赖注入容器。 */
@HiltAndroidApp
class ReaderApplication : Application()
