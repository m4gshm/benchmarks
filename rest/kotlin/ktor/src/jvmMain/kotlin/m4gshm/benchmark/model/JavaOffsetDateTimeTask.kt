package m4gshm.benchmark.model

import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class JavaOffsetDateTimeTask(
    override val id: String? = null,
    override val text: String? = null,
    override val tags: List<String>? = null,
    @Serializable(with = OffsetDateTimeSerializer::class)
    override val deadline: OffsetDateTime? = null
) : Task<OffsetDateTime> {
    override fun withId(id: String) = copy(id = id)
}