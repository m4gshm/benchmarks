package m4gshm.benchmark.rest.ktor.graalvm

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonTokenId
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import kotlinx.datetime.Instant


class InstantModule : SimpleModule() {
    init {
        addSerializer(InstantSerializer())
        addDeserializer(Instant::class.java, InstantDeserializer())
    }
}

class InstantSerializer : StdSerializer<Instant>(Instant::class.java) {
    private fun asTimestamp(serializers: SerializerProvider?) = serializers?.isEnabled(WRITE_DATES_AS_TIMESTAMPS)
        ?: throw IllegalArgumentException("Null SerializerProvider passed for " + handledType().name)

    override fun serialize(value: Instant?, gen: JsonGenerator, provider: SerializerProvider?) {
        if (asTimestamp(provider)) {
            gen.writeNumber(
                value?.toEpochMilliseconds() ?: 0L
            )
        } else if (value != null) {
            gen.writeString(value.toString())
        } else gen.writeNull()
    }
}

class InstantDeserializer : StdDeserializer<Instant>(Instant::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant? {
        return when (p.currentTokenId()) {
            JsonTokenId.ID_STRING -> Instant.parse(p.text.trim())
            JsonTokenId.ID_NUMBER_INT -> Instant.fromEpochMilliseconds(p.longValue)
            else -> ctxt.handleUnexpectedToken(_valueClass, p) as Instant?;
        }
    }
}