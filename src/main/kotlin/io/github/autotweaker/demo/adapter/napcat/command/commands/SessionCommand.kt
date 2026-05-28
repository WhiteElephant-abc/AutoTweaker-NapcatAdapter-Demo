package io.github.autotweaker.demo.adapter.napcat.command.commands

import io.github.autotweaker.demo.adapter.napcat.command.Command
import io.github.autotweaker.demo.adapter.napcat.command.CommandContext
import io.github.autotweaker.demo.adapter.napcat.permission.Role

/**
 * 会话管理命令
 *
 * 用户权限：
 *   /session - 列出当前用户的活跃会话
 *   /session list - 列出所有活跃会话（需要操作员权限）
 */
class SessionCommand : Command {

    override val name = "session"
    override val description = "管理会话"
    override val usage = "/session [list]"
    override val requiredRole = Role.USER

    override suspend fun execute(context: CommandContext): String {
        if (context.args.isEmpty()) {
            return showMySession(context)
        }

        return when (context.args[0].lowercase()) {
            "list", "ls" -> listAllSessions(context)
            else -> "未知子命令: ${context.args[0]}\n用法: $usage"
        }
    }

    private fun showMySession(context: CommandContext): String {
        val sessionId = context.sessionManager.getActiveSession(context.userId)
        if (sessionId == null) {
            return "你没有活跃的会话"
        }

        val handle = context.core.session.getHandle(sessionId)
        if (handle == null) {
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

    private fun listAllSessions(context: CommandContext): String {
        // 列出所有会话需要操作员权限
        val role = context.role
        if (role == null || role.ordinal > Role.OPERATOR.ordinal) {
            return "权限不足，需要操作员角色"
        }

        val activeSessions = context.sessionManager.getAllActiveSessions()
        if (activeSessions.isEmpty()) {
            return "没有活跃的会话"
        }

        return buildString {
            appendLine("活跃会话:")
            activeSessions.forEach { (userId, sessionId) ->
                val handle = context.core.session.getHandle(sessionId)
                if (handle != null) {
                    val data = handle.data.value
                    val workspace = context.core.session.listWorkspaces()
                        .find { it.meta.id == data.workspaceId }
                    appendLine("  用户 $userId:")
                    appendLine("    会话ID: ${data.id}")
                    appendLine("    标题: ${data.title ?: "未设置"}")
                    appendLine("    工作区: ${workspace?.meta?.displayName ?: "未知"}")
                }
            }
        }
    }
}
