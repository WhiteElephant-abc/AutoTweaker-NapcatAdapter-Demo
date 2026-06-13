package io.github.autotweaker.demo.adapter.napcat.command.commands

import io.github.autotweaker.demo.adapter.napcat.command.Command
import io.github.autotweaker.demo.adapter.napcat.command.CommandContext
import io.github.autotweaker.demo.adapter.napcat.permission.Role

class BackCommand : Command {

    override val name = "back"
    override val description = "回到上一个会话"
    override val usage = "/back"
    override val requiredRole = Role.USER

    override suspend fun execute(context: CommandContext): String {
        val previousId = context.sessionManager.getPreviousSession(context.userId)
            ?: return "没有上一个会话"

        val handle = try {
            context.sessionManager.enterSession(context.userId, previousId)
        } catch (e: Exception) {
            context.sessionManager.clearPreviousSession(context.userId)
            return "上一个会话已不存在"
        }
        val data = handle.data.value
        return "已切换到: ${data.title ?: "未设置"}"
    }
}
