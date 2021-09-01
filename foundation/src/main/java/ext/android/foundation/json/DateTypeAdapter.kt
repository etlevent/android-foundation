package ext.android.foundation.json

import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.util.*

class DateTypeAdapter : TypeAdapter<Date>() {
    override fun write(out: JsonWriter?, value: Date?) {
        if (value == null) {
            out?.nullValue()
        } else {
            out?.value(value.time)
        }
    }

    override fun read(`in`: JsonReader?): Date? {
        return when (val peek = `in`?.peek()) {
            JsonToken.NUMBER -> {
                val time = `in`.nextLong()
                Date(time)
            }
            JsonToken.NULL -> {
                `in`.nextNull()
                null
            }
            else -> throw JsonParseException("Expected Long but was $peek")
        }
    }
}