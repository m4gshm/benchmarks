package m4gshm.benchmark.rest.ktor.graalvm

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

interface Task<D> {
    val id: String?
    val text: String?
    val tags: List<String>?
    val deadline: D?
    fun withId(id: String): Task<D>
}

@Serializable
data class KotlinInstantTask(
    override val id: String? = null,
    override val text: String? = null,
    override val tags: List<String>? = null,
    override val deadline: Instant? = null
) : Task<Instant> {
    override fun withId(id: String) = copy(id = id)
}
