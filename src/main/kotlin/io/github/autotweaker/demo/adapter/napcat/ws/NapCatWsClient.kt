package io.github.autotweaker.demo.adapter.napcat.ws

import io.github.autotweaker.demo.adapter.napcat.api.NapCatApi
import io.github.autotweaker.demo.adapter.napcat.model.event.*
import io.ktor.websocket.*
import java.io.Closeable

/**
 * NapCat WebSocket 客户端接口
 *
 * 继承 [NapCatApi] 提供所有 OneBot 11 API 方法，
 * 同时提供 WebSocket 连接管理和事件订阅功能。
 *
 * 使用示例：
 * ```kotlin
 * val client = NapCatWsClientImpl()
 * client.connect("localhost", 3001, "token")
 * client.onGroupMessageEvent { event ->
 *     println("收到群消息: ${event.rawMessage}")
 * }
 * ```
 */
interface NapCatWsClient : NapCatApi, Closeable {
    /**
     * 当前是否已连接
     */
    val isConnected: Boolean

    /**
     * 连接到 NapCat WebSocket 服务器
     *
     * @param host 服务器地址
     * @param port 服务器端口，默认 3001
     * @param token 认证令牌，可选
     */
    suspend fun connect(host: String, port: Int, token: String? = null)

    /**
     * 断开 WebSocket 连接
     *
     * 会清理所有待处理的请求和资源。
     */
    suspend fun disconnect()

    // ==================== 事件订阅 ====================

    /**
     * 订阅所有事件
     *
     * @param handler 事件处理器
     */
    fun onEvent(handler: suspend (OneBotEvent) -> Unit)

    /**
     * 订阅消息事件（包括私聊和群聊）
     *
     * @param handler 消息事件处理器
     */
    fun onMessageEvent(handler: suspend (MessageEvent) -> Unit)

    /**
     * 订阅群消息事件
     *
     * @param handler 群消息事件处理器
     */
    fun onGroupMessageEvent(handler: suspend (GroupMessageEvent) -> Unit)

    /**
     * 订阅私聊消息事件
     *
     * @param handler 私聊消息事件处理器
     */
    fun onPrivateMessageEvent(handler: suspend (PrivateMessageEvent) -> Unit)

    /**
     * 订阅通知事件
     *
     * @param handler 通知事件处理器
     */
    fun onNoticeEvent(handler: suspend (NoticeEvent) -> Unit)

    /**
     * 订阅请求事件
     *
     * @param handler 请求事件处理器
     */
    fun onRequestEvent(handler: suspend (RequestEvent) -> Unit)

    /**
     * 订阅元事件（心跳、生命周期）
     *
     * @param handler 元事件处理器
     */
    fun onMetaEvent(handler: suspend (MetaEvent) -> Unit)

    // ==================== 生命周期 ====================

    /**
     * 设置连接成功回调
     *
     * @param handler 连接成功时调用
     */
    fun onConnect(handler: () -> Unit)

    /**
     * 设置断开连接回调
     *
     * @param handler 断开连接时调用，参数为断开原因
     */
    fun onDisconnect(handler: (CloseReason?) -> Unit)

    /**
     * 设置错误回调
     *
     * @param handler 发生错误时调用
     */
    fun onError(handler: (Throwable) -> Unit)
}
