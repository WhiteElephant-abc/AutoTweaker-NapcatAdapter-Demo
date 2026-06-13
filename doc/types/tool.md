# 工具参数类型

内置工具的参数数据类，位于 `io.github.autotweaker.api.types.tool.args` 包。

---

## BashArgs

Bash 工具参数。

```kotlin
@Serializable
data class BashArgs(
    val command: String,
    val timeoutSeconds: Int = 60,
    val envIds: List<String> = emptyList(),
)
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `command` | `String` | 要执行的命令 |
| `timeoutSeconds` | `Int` | 超时时间（秒），默认 60 |
| `envIds` | `List<String>` | 环境 ID 列表，默认空 |

---

## ReadArgs

Read 工具参数，密封类，支持多种读取模式。

```kotlin
@Serializable
sealed class ReadArgs {
    @Serializable
    data class File(...) : ReadArgs()

    @Serializable
    data class Summarize(...) : ReadArgs()

    @Serializable
    data class Unicode(...) : ReadArgs()
}
```

### ReadArgs.File

按行范围读取文件。

```kotlin
data class File(
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val lineNumber: Boolean = true,
)
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `filePath` | `String` | 文件路径 |
| `startLine` | `Int` | 起始行号 |
| `endLine` | `Int` | 结束行号 |
| `lineNumber` | `Boolean` | 是否显示行号，默认 `true` |

### ReadArgs.Summarize

读取文件范围并用 LLM 摘要。

```kotlin
data class Summarize(
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val prompt: String? = null,
)
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `filePath` | `String` | 文件路径 |
| `startLine` | `Int` | 起始行号 |
| `endLine` | `Int` | 结束行号 |
| `prompt` | `String?` | 自定义摘要提示词 |

### ReadArgs.Unicode

按字符数读取文件（适合二进制/大文件预览）。

```kotlin
data class Unicode(
    val filePath: String,
    val maxChars: Int,
)
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `filePath` | `String` | 文件路径 |
| `maxChars` | `Int` | 最大读取字符数 |
