/**
 * MIT License
 *
 * Copyright (c) 2019-2022 TriumphTeam and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.triumphteam.docsly.database.exposed

import com.impossibl.postgres.jdbc.PGArray
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType
import java.sql.Clob
import kotlin.reflect.KClass

private val json = Json

public inline fun <reified T : Any> Table.serializable(name: String, serializer: KSerializer<T>): Column<T> =
    registerColumn<T>(name, SerializableColumnType(T::class, serializer))

@Suppress("UNCHECKED_CAST")
public class SerializableColumnType<T : Any>(
    private val klass: KClass<T>,
    private val serializer: KSerializer<T>,
) : TextColumnType() {

    /** When writing the value, it can either be a full on list, or individual values. */
    override fun notNullValueToDB(value: Any): Any = when {
        klass.isInstance(value) -> json.encodeToString(serializer, value as T)
        else -> error("$value of ${value::class.qualifiedName} is not an instance of ${klass.simpleName}")
    }

    /** When getting the value it can be more than just [PGArray]. */
    override fun valueFromDB(value: Any): Any = when (value) {
        is Clob -> json.decodeFromString(serializer, value.characterStream.readText())
        is ByteArray -> json.decodeFromString(serializer, String(value))
        is String -> json.decodeFromString(serializer, value)
        else -> error("$value of ${value::class.qualifiedName} could not be decoded into ${klass.simpleName}")
    }
}

public inline fun <reified T : Any> Table.serializableList(name: String, serializer: KSerializer<T>): Column<List<T>> =
    registerColumn(name, SerializableListColumnType(T::class, ListSerializer(serializer)))

@Suppress("UNCHECKED_CAST")
public class SerializableListColumnType<T : Any>(
    private val klass: KClass<T>,
    private val serializer: KSerializer<List<T>>,
) : TextColumnType() {

    /** When writing the value, it can either be a full on list, or individual values. */
    override fun notNullValueToDB(value: Any): Any = when (value) {
        is List<*> -> json.encodeToString(serializer, value as List<T>)
        else -> error("$value of ${value::class.qualifiedName} is not an instance of ${klass.simpleName}")
    }

    /** When getting the value it can be more than just [PGArray]. */
    override fun valueFromDB(value: Any): Any = when (value) {
        is String -> json.decodeFromString(serializer, value)
        else -> {
            println("Oh boy! ${value.javaClass}")
        }
    }
}
