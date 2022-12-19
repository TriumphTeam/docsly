package dev.triumphteam.docsly.database

import dev.triumphteam.docsly.database.exposed.enumerationSet
import dev.triumphteam.docsly.database.exposed.serializableList
import dev.triumphteam.docsly.serializable.Language
import dev.triumphteam.docsly.serializable.Modifier
import dev.triumphteam.docsly.serializable.SerializableAnnotation
import dev.triumphteam.docsly.serializable.Visibility
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

public object DocsTable : IntIdTable() {
    public val name: Column<String> = varchar("doc_name", 32).uniqueIndex()
    public val version: Column<String> = varchar("doc_version", 32).uniqueIndex()
    public val guild: Column<Long> = long("guild_id").uniqueIndex()
}

public object ObjectTable : IntIdTable() {
    public val language: Column<Language> = enumeration("language")
    public val name: Column<String> = varchar("name", 512)
    public val visibility: Column<Visibility> = enumeration("visibility")

    public val modifiers: Column<Set<Modifier>> = enumerationSet("modifiers")
    // val documentation: Column<String?> = text("documentation").nullable()
    //  val extraDocumentation: List<Documentation>,
    //  val superTypes: List<SuperType>,
}

public object ClassTable : IntIdTable() {
    public val language: Column<Language> = enumeration("language")
    public val name: Column<String> = varchar("name", 512)

    public val companion: Column<EntityID<Int>?> = reference("companion", ObjectTable).nullable()
    public val visibility: Column<Visibility> = enumeration("visibility")

    // val annotations: List<SerializableAnnotation>,
    // val generics: List<GenericType>,
    public val modifiers: Column<Set<Modifier>> = enumerationSet("modifiers")
    // val documentation: Column<String?> = text("documentation").nullable()
    // val extraDocumentation: List<Documentation> = list,
    // val superTypes: List<SuperType>,
}

public object TestTable : IntIdTable() {
    public val annotations:  Column<List<SerializableAnnotation>> = serializableList("test", SerializableAnnotation.serializer())
}
