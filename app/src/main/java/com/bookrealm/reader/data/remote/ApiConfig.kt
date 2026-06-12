package com.bookrealm.reader.data.remote

/**
 * 后端地址集中配置(架构裁决:两套后端,地址只在这里改)。
 * - 真机(同一 WiFi):用电脑局域网 IP——跑 book-realm/start-platform.ps1 会打印
 * - 模拟器:用 10.0.2.2
 */
object ApiConfig {
    // TODO(联调时改成 start-platform.ps1 打印的局域网 IP)
    private const val HOST = "10.0.2.2"

    /** 用户中心(MVP-0):登录/注册。经其 Nginx :80 */
    const val USER_CENTER_BASE_URL = "http://$HOST/api/"

    /** 书库服务(MVP-1):书与章节内容 */
    const val LIBRARY_BASE_URL = "http://$HOST:8082/api/"

    /** 统计服务(MVP-3):阅读进度上报(HTTP,不直连 MQ) */
    const val STATS_BASE_URL = "http://$HOST:8083/api/"

    /** AI 服务(MVP-4):摘要/问答 */
    const val AI_BASE_URL = "http://$HOST:8084/api/"
}
