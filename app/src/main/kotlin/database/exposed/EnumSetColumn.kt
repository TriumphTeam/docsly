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
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.statements.jdbc.JdbcPreparedStatementImpl
import org.jetbrains.exposed.sql.vendors.currentDialect
import kotlin.reflect.KClass

public inline fun <reified T : Enum<T>> Table.enumerationSet(name: String): Column<Set<T>> =
    registerColumn(name, EnumSetColumnType(T::class))

public class EnumSetColumnType<T : Enum<T>>(private val klass: KClass<T>) : ColumnType() {

    // Keep the enum constants for when decoding from the DB
    private val enumConstants by lazy { klass.java.enumConstants.associateBy { it.name.uppercase() } }

    /** The Postgres integer array type. Eg: INTEGER[] */
    override fun sqlType(): String = "${currentDialect.dataTypeProvider.textType()}[]"

    /** When writing the value, it can either be a full on list, or individual values. */
    override fun notNullValueToDB(value: Any): Any = when (value) {
        is Collection<*> -> value.filterIsInstance<Enum<*>>().map(Enum<*>::name)
        is Int -> value
        is Enum<*> -> value.name
        else -> error("$value of ${value::class.qualifiedName} is not valid for enum set ${klass.simpleName}")
    }

    /** When getting the value it can be more than just [PGArray]. */
    override fun valueFromDB(value: Any): Any = when (value) {
        is PGArray -> (value.array as Array<*>).toEnumSet()
        is Array<*> -> value.toEnumSet()
        is Collection<*> -> value.toEnumSet()
        else -> error("Got unexpected array value of type: ${value::class.qualifiedName} ($value)")
    }

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        if (value == null) {
            stmt.setNull(index, this)
        } else {
            val preparedStatement = stmt as? JdbcPreparedStatementImpl ?: error("Currently only JDBC is supported")
            val array = preparedStatement.statement.connection.createArrayOf(
                currentDialect.dataTypeProvider.integerType(),
                (value as Collection<*>).toTypedArray()
            )
            stmt[index] = array
        }
    }

    private fun Array<*>.toEnumSet() = filterIsInstance<String>().map { enumConstants[it.uppercase()] }.toSet()
    private fun Collection<*>.toEnumSet() = filterIsInstance<String>().map { enumConstants[it.uppercase()] }.toSet()
}
