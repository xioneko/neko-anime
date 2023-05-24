package com.xioneko.android.nekoanime.data.datastore.serializer

import androidx.datastore.core.Serializer
import com.xioneko.android.nekoanime.data.datastore.model.UserDataProto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class LocalStorageSerializer @Inject constructor() : Serializer<UserDataProto> {
    /**
     * Value to return if there is no data on disk.
     */
    override val defaultValue: UserDataProto
        get() = UserDataProto()

    /**
     * Unmarshal object from stream.
     *
     * @param input the InputStream with the data to deserialize
     */
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): UserDataProto {
        return ProtoBuf.decodeFromByteArray(input.readBytes())
    }

    /**
     *  Marshal object to a stream. Closing the provided OutputStream is a no-op.
     *
     *  @param t the data to write to output
     *  @output the OutputStream to serialize data to
     */
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun writeTo(t: UserDataProto, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(ProtoBuf.encodeToByteArray(t))
        }
    }
}