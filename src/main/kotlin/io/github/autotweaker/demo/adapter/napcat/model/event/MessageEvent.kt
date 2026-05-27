package io.github.autotweaker.demo.adapter.napcat.model.event

import io.github.autotweaker.demo.adapter.napcat.model.data.Sender
import io.github.autotweaker.demo.adapter.napcat.model.message.MessageChain
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 消息事件基类
 *
 * 收到消息时触发，包括私聊消息和群消息。
 */
@Serializable
sealed class MessageEvent : OneBotEvent() {
    /** 消息 ID */
    abstract val messageId: Int

    /** 消息内容（结构化） */
    abstract val message: MessageChain

    /** 原始消息文本 */
    @SerialName("raw_message") abstract val rawMessage: String

    /** 发送者信息 */
    abstract val sender: Sender
}

/**
 * 私聊消息事件
 *
 * @property time 事件时间戳
 * @property selfId 机器人 QQ 号
 * @property messageId 消息 ID
 * @property message 消息内容
 * @property rawMessage 原始消息文本
 * @property sender 发送者信息
 * @property userId 发送者 QQ 号
 */
@Serializable
@SerialName("private")
data class PrivateMessageEvent(
    override val time: Long,
    @SerialName("self_id") override val selfId: Long,
    @SerialName("message_id") override val messageId: Int,
    override val message: MessageChain,
    @SerialName("raw_message") override val rawMessage: String,
    override val sender: Sender,
    @SerialName("user_id") val userId: Long
) : MessageEvent()

/**
 * 群消息事件
 *
 * @property time 事件时间戳
 * @property selfId 机器人 QQ 号
 * @property messageId 消息 ID
 * @property message 消息内容
 * @property rawMessage 原始消息文本
 * @property sender 发送者信息
 * @property groupId 群号
 * @property userId 发送者 QQ 号
 */
@Serializable
@SerialName("group")
data class GroupMessageEvent(
    override val time: Long,
    @SerialName("self_id") override val selfId: Long,
    @SerialName("message_id") override val messageId: Int,
    override val message: MessageChain,
    @SerialName("raw_message") override val rawMessage: String,
    override val sender: Sender,
    @SerialName("group_id") val groupId: Long,
    @SerialName("user_id") val userId: Long
) : MessageEvent()
