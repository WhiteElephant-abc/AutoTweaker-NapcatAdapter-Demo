package io.github.autotweaker.demo.adapter.napcat.model.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * OneBot 11 事件基类
 *
 * 所有 OneBot 事件都继承此类，包含事件的通用属性。
 * 事件通过 WebSocket 推送，客户端通过订阅方法接收。
 */
@Serializable
sealed class OneBotEvent {
    /** 事件发生时间戳（秒） */
    abstract val time: Long

    /** 收到事件的机器人 QQ 号 */
    @SerialName("self_id") abstract val selfId: Long
}

/**
 * 通知事件类型
 */
@Serializable
enum class NoticeType {
    /** 群成员增加 */
    @SerialName("group_increase") GROUP_INCREASE,

    /** 群成员减少 */
    @SerialName("group_decrease") GROUP_DECREASE,

    /** 群禁言 */
    @SerialName("group_ban") GROUP_BAN,

    /** 群消息撤回 */
    @SerialName("group_recall") GROUP_RECALL,

    /** 群管理员变动 */
    @SerialName("group_admin") GROUP_ADMIN,

    /** 群文件上传 */
    @SerialName("group_upload") GROUP_UPLOAD,

    /** 群名片修改 */
    @SerialName("group_card") GROUP_CARD,

    /** 好友添加 */
    @SerialName("friend_add") FRIEND_ADD,

    /** 好友消息撤回 */
    @SerialName("friend_recall") FRIEND_RECALL,

    /** 通知（戳一戳等） */
    @SerialName("notify") NOTIFY
}

/**
 * 请求事件类型
 */
@Serializable
enum class RequestType {
    /** 好友请求 */
    @SerialName("friend") FRIEND,

    /** 入群请求 */
    @SerialName("group") GROUP
}
