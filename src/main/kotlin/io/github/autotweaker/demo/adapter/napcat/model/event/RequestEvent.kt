package io.github.autotweaker.demo.adapter.napcat.model.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 请求事件基类
 *
 * 收到好友请求或入群请求时触发。
 * 需要通过 API 调用来处理请求（同意/拒绝）。
 */
@Serializable
sealed class RequestEvent : OneBotEvent() {
    /** 请求类型（friend/group） */
    @SerialName("request_type") abstract val requestType: RequestType

    /** 请求标识，用于处理请求 */
    abstract val flag: String
}

/**
 * 好友请求事件
 *
 * @property time 事件时间戳
 * @property selfId 机器人 QQ 号
 * @property requestType 请求类型
 * @property flag 请求标识
 * @property userId 请求者 QQ 号
 * @property comment 验证信息
 * @property nick 请求者昵称
 */
@Serializable
data class FriendRequestEvent(
    override val time: Long,
    @SerialName("self_id") override val selfId: Long,
    @SerialName("request_type") override val requestType: RequestType,
    override val flag: String,
    @SerialName("user_id") val userId: Long,
    val comment: String,
    val nick: String
) : RequestEvent()

/**
 * 入群请求事件
 *
 * @property time 事件时间戳
 * @property selfId 机器人 QQ 号
 * @property requestType 请求类型
 * @property flag 请求标识
 * @property groupId 群号
 * @property userId 请求者 QQ 号
 * @property comment 验证信息
 * @property subType 子类型（add/invite）
 */
@Serializable
data class GroupRequestEvent(
    override val time: Long,
    @SerialName("self_id") override val selfId: Long,
    @SerialName("request_type") override val requestType: RequestType,
    override val flag: String,
    @SerialName("group_id") val groupId: Long,
    @SerialName("user_id") val userId: Long,
    val comment: String,
    @SerialName("sub_type") val subType: String
) : RequestEvent()
