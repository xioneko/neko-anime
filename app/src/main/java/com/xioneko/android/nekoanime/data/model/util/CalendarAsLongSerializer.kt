package com.xioneko.android.nekoanime.data.model.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Calendar

object CalendarAsLongSerializer : KSerializer<Calendar> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Calendar) =
        encoder.encodeLong(value.timeInMillis)

    override fun deserialize(decoder: Decoder): Calendar =
        Calendar.Builder().setInstant(decoder.decodeLong()).build()
}