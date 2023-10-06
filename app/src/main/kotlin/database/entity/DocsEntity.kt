package dev.triumphteam.docsly.database.entity

import dev.triumphteam.docsly.database.exposed.serializable
import dev.triumphteam.docsly.elements.DocElement
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

public object DocsTable : LongIdTable() {

    public val guild: Column<String> = text("guild_id")
    public val project: Column<String> = text("project")
    public val version: Column<String> = text("version")
    public val doc: Column<DocElement> = serializable("doc")
}

public class DocEntity(entityId: EntityID<Long>) : LongEntity(entityId) {
    public companion object : LongEntityClass<DocEntity>(DocsTable)

    public var guild: String by DocsTable.guild
    public var project: String by DocsTable.project
    public var version: String by DocsTable.version
    public var doc: DocElement by DocsTable.doc
}
