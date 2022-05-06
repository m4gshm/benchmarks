package m4gshm.benchmark.rest.ktor.graalvm

import kotlinx.serialization.Serializable

@Serializable
data class Status(
    val success: Boolean? = null, val status: Int? = null, val message: String? = null, val type: String? = null
)