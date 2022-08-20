package m4gshm.benchmark.model

import kotlinx.serialization.Serializable
import m4gshm.benchmark.rest.java.model.Task
import java.time.OffsetDateTime

@Serializable
data class JavaOffsetDateTimeTask(
    private val id: String? = null,
    private val text: String? = null,
    private val tags: List<String>? = null,
    @Serializable(with = OffsetDateTimeSerializer::class)
    private val deadline: OffsetDateTime? = null
) : Task<JavaOffsetDateTimeTask, OffsetDateTime> {
    override fun getId() = id

    override fun getText() = text

    override fun getTags() = tags

    override fun getDeadline() = deadline

    override fun withId(id: String) = copy(id = id)
}