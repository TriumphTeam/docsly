package dev.triumphteam.docsly.database.exposed

import com.impossibl.postgres.jdbc.PGArray
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.jetbrains.exposed.sql.statements.jdbc.JdbcPreparedStatementImpl
import org.jetbrains.exposed.sql.vendors.currentDialect
import kotlin.reflect.KClass

inline fun <reified T : Enum<T>> Table.enumerationSet(name: String) =
    registerColumn<Set<T>>(name, EnumSetColumnType(T::class))

class EnumSetColumnType<T : Enum<T>>(private val klass: KClass<T>) : ColumnType() {

    // Keep the enum constants for when decoding from the DB
    private val enumConstants by lazy { klass.java.enumConstants }

    /** The Postgres integer array type. Eg: INTEGER[] */
    override fun sqlType(): String = "${currentDialect.dataTypeProvider.integerType()}[]"

    /** When writing the value, it can either be a full on list, or individual values. */
    override fun notNullValueToDB(value: Any): Any = when (value) {
        is Collection<*> -> value.filterIsInstance<Enum<*>>().map { it.ordinal }
        is Int -> value
        is Enum<*> -> value.ordinal
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

    private fun Array<*>.toEnumSet() = filterIsInstance<Int>().map { enumConstants[it] }.toSet()
    private fun Collection<*>.toEnumSet() = filterIsInstance<Int>().map { enumConstants[it] }.toSet()
}
