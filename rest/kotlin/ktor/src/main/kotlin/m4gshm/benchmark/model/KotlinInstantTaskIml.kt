package m4gshm.benchmark.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import m4gshm.benchmark.rest.java.storage.model.IdAware
import m4gshm.benchmark.rest.java.storage.model.Task
import m4gshm.benchmark.rest.java.storage.model.WithId


@Serializable
data class KotlinInstantTaskIml(
    private val id: String? = null,
    private val text: String? = null,
    private val tags: List<String>? = null,
    private val deadline: Instant? = null
) : Task<Instant>, WithId<KotlinInstantTaskIml, String> {
    override fun getId() = id

    override fun getText() = text

//    override fun getTags() = tags

    override fun getDeadline() = deadline

    override fun withId(id: String) = copy(id = id)
}
