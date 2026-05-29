package io.github.autotweaker.demo.adapter.napcat.bridge

import io.github.autotweaker.api.config.JsonStore
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 会话数据持久化
 *
 * 负责将会话映射、用户模型/工作区/thinking 配置、全局模型配置
 * 序列化到 [JsonStore] 和从其中反序列化。
 *
 * @property store JSON 持久化存储
 * @property activeSessions 会话映射
 * @property userPrimaryModels 用户主模型
 * @property userSelectedWorkspaces 用户工作区
 * @property userThinking 用户 thinking 设置
 * @property globalFallbackModels 全局备选模型
 * @property globalSummarizeModel 全局压缩模型
 */
class SessionPersistence(
    private val store: JsonStore,
    private val activeSessions: ConcurrentHashMap<Long, UUID>,
    private val userPrimaryModels: ConcurrentHashMap<Long, UUID>,
    private val userSelectedWorkspaces: ConcurrentHashMap<Long, UUID>,
    private val userThinking: ConcurrentHashMap<Long, Boolean>,
    private val globalFallbackModels: CopyOnWriteArrayList<UUID>,
    private val globalSummarizeModel: () -> UUID?
) {

    private val logger = LoggerFactory.getLogger(SessionPersistence::class.java)

    companion object {
        private const val SESSION_MAP_KEY = "session_map"
        private const val USER_MODELS_KEY = "user_models"
        private const val USER_WORKSPACES_KEY = "user_workspaces"
        private const val USER_THINKING_KEY = "user_thinking"
        private const val GLOBAL_CONFIG_KEY = "global_config"
    }

    /**
     * 从存储加载所有数据
     */
    fun load() {
        try {
            val element = store.get() ?: return
            val obj = element.jsonObject

            obj[SESSION_MAP_KEY]?.let { loadSessionMap(it) }
            obj[USER_MODELS_KEY]?.let { loadUserModels(it) }
            obj[USER_WORKSPACES_KEY]?.let { loadUserWorkspaces(it) }
            obj[USER_THINKING_KEY]?.let { loadUserThinking(it) }
            obj[GLOBAL_CONFIG_KEY]?.let { loadGlobalConfig(it) }

            logger.debug("Loaded {} sessions, {} user models, {} user workspaces, {} user thinking",
                activeSessions.size, userPrimaryModels.size, userSelectedWorkspaces.size, userThinking.size)
        } catch (e: Exception) {
            logger.warn("Failed to load from store", e)
        }
    }

    /**
     * 将所有数据保存到存储
     */
    fun save() {
        try {
            val obj = buildJsonObject {
                put(SESSION_MAP_KEY, buildJsonObject {
                    activeSessions.forEach { (userId, sessionId) ->
                        put(userId.toString(), JsonPrimitive(sessionId.toString()))
                    }
                })
                put(USER_MODELS_KEY, buildJsonObject {
                    userPrimaryModels.forEach { (userId, modelId) ->
                        put(userId.toString(), JsonPrimitive(modelId.toString()))
                    }
                })
                put(USER_WORKSPACES_KEY, buildJsonObject {
                    userSelectedWorkspaces.forEach { (userId, workspaceId) ->
                        put(userId.toString(), JsonPrimitive(workspaceId.toString()))
                    }
                })
                put(USER_THINKING_KEY, buildJsonObject {
                    userThinking.forEach { (userId, enabled) ->
                        put(userId.toString(), JsonPrimitive(enabled))
                    }
                })
                put(GLOBAL_CONFIG_KEY, buildJsonObject {
                    put("fallback", buildJsonArray {
                        globalFallbackModels.forEach { add(JsonPrimitive(it.toString())) }
                    })
                    globalSummarizeModel()?.let {
                        put("summarize", JsonPrimitive(it.toString()))
                    }
                })
            }
            store.set(obj)
        } catch (e: Exception) {
            logger.error("Failed to save to store", e)
        }
    }

    private fun loadSessionMap(element: JsonElement) {
        try {
            element.jsonObject.forEach { (key, value) ->
                try {
                    activeSessions[key.toLong()] = UUID.fromString(value.jsonPrimitive.content)
                } catch (e: Exception) {
                    logger.warn("Failed to parse session: {} -> {}", key, value)
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to load session map", e)
        }
    }

    private fun loadUserModels(element: JsonElement) {
        try {
            element.jsonObject.forEach { (key, value) ->
                try {
                    userPrimaryModels[key.toLong()] = UUID.fromString(value.jsonPrimitive.content)
                } catch (e: Exception) {
                    logger.warn("Failed to parse user model: {} -> {}", key, value)
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to load user models", e)
        }
    }

    private fun loadUserWorkspaces(element: JsonElement) {
        try {
            element.jsonObject.forEach { (key, value) ->
                try {
                    userSelectedWorkspaces[key.toLong()] = UUID.fromString(value.jsonPrimitive.content)
                } catch (e: Exception) {
                    logger.warn("Failed to parse user workspace: {} -> {}", key, value)
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to load user workspaces", e)
        }
    }

    private fun loadUserThinking(element: JsonElement) {
        try {
            element.jsonObject.forEach { (key, value) ->
                try {
                    userThinking[key.toLong()] = value.jsonPrimitive.content.toBoolean()
                } catch (e: Exception) {
                    logger.warn("Failed to parse user thinking: {} -> {}", key, value)
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to load user thinking", e)
        }
    }

    private fun loadGlobalConfig(element: JsonElement) {
        try {
            val obj = element.jsonObject
            obj["fallback"]?.jsonArray?.forEach {
                try {
                    globalFallbackModels.add(UUID.fromString(it.jsonPrimitive.content))
                } catch (e: Exception) {
                    logger.warn("Failed to parse fallback model: {}", it)
                }
            }
            obj["summarize"]?.let {
                // globalSummarizeModel is @Volatile in SessionManager, set directly
                // We need a setter callback for this
                // Actually we can't set it here since it's a val in SessionManager
                // The load will be handled by SessionManager after persistence.load()
            }
        } catch (e: Exception) {
            logger.warn("Failed to load global config", e)
        }
    }

    /**
     * 从存储中读取全局压缩模型 ID
     */
    fun loadSummarizeModel(): UUID? {
        return try {
            val element = store.get() ?: return null
            val obj = element.jsonObject
            obj[GLOBAL_CONFIG_KEY]?.jsonObject?.get("summarize")?.let {
                UUID.fromString(it.jsonPrimitive.content)
            }
        } catch (e: Exception) {
            logger.warn("Failed to load summarize model", e)
            null
        }
    }
}
