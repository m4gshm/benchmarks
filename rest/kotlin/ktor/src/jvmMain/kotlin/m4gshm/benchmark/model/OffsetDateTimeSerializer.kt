package m4gshm.benchmark.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME


object OffsetDateTimeSerializer : KSerializer<OffsetDateTime?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        OffsetDateTime::class.java.simpleName, STRING
    ).nullable

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): OffsetDateTime? = if (descriptor.isNullable
        || decoder.decodeNotNullMark()
    ) OffsetDateTime.parse(
        decoder.decodeString(),
        ISO_OFFSET_DATE_TIME
    ) else decoder.decodeNull()

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: OffsetDateTime?) = when {
        value != null -> encoder.encodeString(value.format(ISO_OFFSET_DATE_TIME))
        else -> encoder.encodeNull()
    }

}
