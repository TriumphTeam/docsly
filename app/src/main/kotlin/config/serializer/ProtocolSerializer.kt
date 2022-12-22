package dev.triumphteam.docsly.config.serializer

import io.ktor.http.URLProtocol
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public class ProtocolSerializer : KSerializer<URLProtocol> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Protocol", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: URLProtocol) {
        encoder.encodeString(value.name.uppercase())
    }

    override fun deserialize(decoder: Decoder): URLProtocol {
        return URLProtocol.byName[decoder.decodeString().lowercase()] ?: URLProtocol.HTTP
    }
}
