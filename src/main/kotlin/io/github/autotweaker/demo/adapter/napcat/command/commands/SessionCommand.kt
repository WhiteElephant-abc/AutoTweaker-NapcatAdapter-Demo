package io.github.autotweaker.demo.adapter.napcat.command.commands

import io.github.autotweaker.api.trace.TraceRecorder
import io.github.autotweaker.api.trace.catching
import io.github.autotweaker.demo.adapter.napcat.command.Command
import io.github.autotweaker.demo.adapter.napcat.command.CommandContext
import io.github.autotweaker.demo.adapter.napcat.permission.Role
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * 会话管理命令
 *
 * 用法：
 *   /session — 显示当前会话
 *   /session list — 列出当前工作区的会话
 *   /session new [标题] — 创建新会话并进入
 *   /session enter <sessionId> — 进入指定会话
 *   /session exit — 退出当前会话
 *   /session remove <sessionId> — 删除指定会话
 */
class SessionCommand : Command {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private lateinit var trace: TraceRecorder

    override val name = "session"
    override val description = "管理会话"
    override val usage =
        "/session [list|new|enter|exit|remove] [参数]"
    override val requiredRole = Role.USER

    override suspend fun execute(context: CommandContext): String {
        if (!::trace.isInitialized) trace = context.core.trace(this::class)
        if (context.args.isEmpty()) {
            return showMySession(context)
        }

        return when (context.args[0].lowercase()) {
            "list", "ls" -> listSessions(context)
            "new", "create" -> newSession(context)
            "enter" -> enterSession(context)
            "exit", "leave" -> exitSession(context)
            "remove", "rm", "delete" -> removeSession(context)
            else -> "未知子命令: ${context.args[0]}\n用法: $usage"
        }
    }

    private suspend fun showMySession(context: CommandContext): String {
        val sessionId = context.sessionManager.getActiveSession(context.userId)
        if (sessionId == null) {
            return "你没有活跃的会话"
        }

        val handle = trace.catching {
            context.core.session.getHandle(sessionId)
        }.getOrElse {
            context.sessionManager.clearActiveSession(context.userId)
            return "你没有活跃的会话"
        }

        val data = handle.data.value
        val workspace = context.core.session.listWorkspaces()
            .find { it.meta.id == data.workspaceId }

        return buildString {
            appendLine("当前会话:")
            appendLine("  ID: ${data.id}")
            appendLine("  标题: ${data.title ?: "未设置"}")
            appendLine("  工作区: ${workspace?.meta?.displayName ?: "未知"}")
        }
    }

    private suspend fun listSessions(context: CommandContext): String {
        val workspaceId = context.sessionManager.getUserWorkspace(context.userId)
            ?: return "你还没有选择工作区，请先 /workspace select"

        val workspace = context.core.session.listWorkspaces()
            .find { it.meta.id == workspaceId }
            ?: return "当前工作区不存在"

        val sessionIds = workspace.sessionIds.orEmpty()
        if (sessionIds.isEmpty()) {
            return "当前工作区（${workspace.meta.displayName}）没有会话"
        }

        val sessions = context.core.session.loadData(sessionIds)
        if (sessions.isEmpty()) {
            return "没有会话"
        }

        val activeSessionId = context.sessionManager.getActiveSession(context.userId)

        return buildString {
            appendLine("工作区「${workspace.meta.displayName}」的会话:")
            sessions.forEach { data ->
                val active = if (data.id == activeSessionId) " ← 当前" else ""
                appendLine("  ${data.title ?: "未设置"}$active")
                appendLine("    ID: ${data.id}")
            }
        }
    }

    /**
     * 创建新会话并进入
     *
     * 用法: /session new [标题]
     */
    private suspend fun newSession(context: CommandContext): String {
        val title = context.args.drop(1).joinToString(" ").take(100).ifEmpty { "新会话" }

        return try {
            val handle = context.sessionManager.autoCreateSession(context.userId, title)
            "会话已创建: ${handle.id}\n标题: $title\n已自动进入此会话"
        } catch (e: IllegalStateException) {
            trace.exception(e)
            e.message ?: "创建会话失败"
        } catch (e: Exception) {
            trace.exception(e)
            "创建会话失败: ${e.message}"
        }
    }

    /**
     * 进入指定会话
     *
     * 用法: /session enter <sessionId>
     */
    private suspend fun enterSession(context: CommandContext): String {
        if (context.args.size < 2) {
            return "用法: /session enter <sessionId>"
        }

        val sessionId = try {
            UUID.fromString(context.args[1])
        } catch (e: IllegalArgumentException) {
            trace.exception(e)
            return "无效的会话 ID: ${context.args[1]}"
        }

        return try {
            context.sessionManager.enterSession(context.userId, sessionId)
            "已进入会话: $sessionId"
        } catch (e: IllegalStateException) {
            trace.exception(e)
            logger.warn("Failed to enter session  sessionId={}", sessionId, e)
            "会话恢复失败: ${e.message}"
        } catch (e: Exception) {
            trace.exception(e)
            logger.warn("Failed to enter session  sessionId={}", sessionId, e)
            "会话不存在: $sessionId"
        }
    }

    /**
     * 退出当前会话
     *
     * 用法: /session exit
     */
    private suspend fun exitSession(context: CommandContext): String {
        val handle = context.sessionManager.getActiveSessionHandle(context.userId)
            ?: return "当前没有活跃会话"

        return trace.catching {
            try {
                context.core.session.stop(handle.id)
            } finally {
                context.sessionManager.exitSession(context.userId)
            }
            "已停止并退出会话"
        }.getOrElse { "退出失败，请稍后重试" }
    }

    /**
     * 删除指定会话
     *
     * 用法: /session remove <sessionId>
     */
    private suspend fun removeSession(context: CommandContext): String {
        if (context.args.size < 2) {
            return "用法: /session remove <sessionId>"
        }

        val sessionId = try {
            UUID.fromString(context.args[1])
        } catch (e: IllegalArgumentException) {
            return "无效的会话 ID: ${context.args[1]}"
        }

        val activeSessionId = context.sessionManager.getActiveSession(context.userId)
        if (sessionId == activeSessionId) {
            return "不能删除当前活跃会话，请先 /session exit"
        }

        return try {
            context.core.session.delete(sessionId)
            "已删除会话: $sessionId"
        } catch (e: Exception) {
            trace.exception(e)
            "删除会话失败: ${e.message}"
        }
    }
}
