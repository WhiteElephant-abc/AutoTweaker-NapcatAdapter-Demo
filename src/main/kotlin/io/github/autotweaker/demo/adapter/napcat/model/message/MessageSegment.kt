package io.github.autotweaker.demo.adapter.napcat.model.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 消息链类型，由多个 [MessageSegment] 组成
 */
typealias MessageChain = List<MessageSegment>

/**
 * OneBot 11 消息段
 *
 * 消息内容由多个消息段组成，每个消息段代表一种类型的内容（文本、图片、@等）。
 * 使用密封类确保类型安全的序列化/反序列化。
 */
@Serializable
sealed class MessageSegment {
    /**
     * 文本消息段
     *
     * @property text 文本内容
     */
    @Serializable
    @SerialName("text")
    data class Text(val text: String) : MessageSegment()

    /**
     * @消息段
     *
     * @property qq 被@的 QQ 号，"all" 表示@全体成员
     */
    @Serializable
    @SerialName("at")
    data class At(val qq: String) : MessageSegment()

    /**
     * 图片消息段
     *
     * @property file 图片文件 ID
     * @property url 图片 URL
     * @property subType 图片子类型
     */
    @Serializable
    @SerialName("image")
    data class Image(
        val file: String,
        val url: String? = null,
        @SerialName("sub_type") val subType: Int? = null
    ) : MessageSegment()

    /**
     * 表情消息段
     *
     * @property id 表情 ID
     */
    @Serializable
    @SerialName("face")
    data class Face(val id: Int) : MessageSegment()

    /**
     * 回复消息段
     *
     * @property id 被回复的消息 ID
     */
    @Serializable
    @SerialName("reply")
    data class Reply(val id: Int) : MessageSegment()

    /**
     * JSON 消息段（卡片消息）
     *
     * @property data JSON 数据字符串
     */
    @Serializable
    @SerialName("json")
    data class Json(val data: String) : MessageSegment()

    /**
     * 语音消息段
     *
     * @property file 语音文件 ID
     */
    @Serializable
    @SerialName("record")
    data class Record(val file: String) : MessageSegment()

    /**
     * 视频消息段
     *
     * @property file 视频文件 ID
     */
    @Serializable
    @SerialName("video")
    data class Video(val file: String) : MessageSegment()

    /**
     * 合并转发消息节点
     *
     * @property userId 发送者 QQ 号
     * @property nickname 发送者昵称
     * @property content 节点内容
     */
    @Serializable
    @SerialName("node")
    data class Node(
        @SerialName("user_id") val userId: String,
        val nickname: String,
        val content: JsonElement? = null
    ) : MessageSegment()

    /**
     * 戳一戳消息段
     *
     * @property type 戳一戳类型
     * @property id 戳一戳 ID
     */
    @Serializable
    @SerialName("poke")
    data class Poke(val type: String, val id: String) : MessageSegment()

    /**
     * 未知类型消息段
     *
     * 用于处理未识别的消息段类型，避免反序列化失败。
     *
     * @property type 消息段类型
     * @property data 原始数据
     */
    @Serializable
    data class Unknown(
        val type: String,
        val data: JsonElement? = null
    ) : MessageSegment()
}
