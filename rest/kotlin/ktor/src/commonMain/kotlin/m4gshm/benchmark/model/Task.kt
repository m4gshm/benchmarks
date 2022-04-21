package m4gshm.benchmark.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String? = null, val text: String? = null, val tags: List<String>? = null,
    val deadline: Instant? = null
)