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
import dev.triumphteam.docsly.project.Projects
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.sql.Clob
import kotlin.reflect.KClass

public inline fun <reified T : Any> Table.serializable(name: String, serializer: KSerializer<T>): Column<T> =
    registerColumn(name, SerializableColumnType(T::class, serializer))

public inline fun <reified T : Any> Table.serializable(name: String): Column<T> =
    registerColumn(name, SerializableColumnType(T::class, Projects.JSON.serializersModule.serializer()))

@Suppress("UNCHECKED_CAST")
public class SerializableColumnType<T : Any>(
    private val klass: KClass<T>,
    private val serializer: KSerializer<T>,
) : ColumnType<T>() {

    override fun sqlType(): String = currentDialect.dataTypeProvider.textType()

    /** When writing the value, it can either be a full on list, or individual values. */
    override fun notNullValueToDB(value: T): Any = when {
        klass.isInstance(value) -> Projects.JSON.encodeToString(serializer, value as T)
        else -> error("$value of ${value::class.qualifiedName} is not an instance of ${klass.simpleName}")
    }

    /** When getting the value it can be more than just [PGArray]. */
    override fun valueFromDB(value: Any): T = when (value) {
        is Clob -> Projects.JSON.decodeFromString(serializer, value.characterStream.readText())
        is ByteArray -> Projects.JSON.decodeFromString(serializer, String(value))
        is String -> Projects.JSON.decodeFromString(serializer, value)
        else -> error("Unknown type for value $value")
    }
}

public inline fun <reified T : Any> Table.serializableList(name: String, serializer: KSerializer<T>): Column<List<T>> =
    registerColumn(name, SerializableListColumnType(serializer))

public class SerializableListColumnType<T : Any>(
    private val serializer: KSerializer<T>,
) : CollectionColumnType<T, List<T>>() {

    override fun mapValue(stringValue: String): T? = Projects.JSON.decodeFromString(serializer, stringValue)
    override fun mapToString(value: T): String = Projects.JSON.encodeToString(serializer, value)
    override fun List<T>.mapCollection(): List<T> = this
}
