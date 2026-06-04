# Trace 类型

## TraceRecorder

追踪记录器接口，用于记录追踪信息。

```kotlin
interface TraceRecorder {
    suspend fun add(namespace: String, content: String)
}
```

### add

```kotlin
suspend fun add(namespace: String, content: String)
```

添加一条追踪记录。

**参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| `namespace` | `String` | 命名空间，用于分类追踪记录 |
| `content` | `String` | 追踪内容 |

**示例：**

```kotlin
val trace = core.trace(NapCatAdapter::class)

// 记录 LLM 请求
trace.add("request", "request=$request, model=${request.model}, chatId=$chatId")

// 记录 LLM 响应
trace.add("response", "result=$result, chatId=$chatId")
```
