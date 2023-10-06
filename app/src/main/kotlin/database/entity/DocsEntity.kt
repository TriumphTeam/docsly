package dev.triumphteam.docsly.database.entity

import dev.triumphteam.docsly.database.exposed.serializable
import dev.triumphteam.docsly.elements.DocElement
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

public object DocsTable : IdTable<Long>() {
    public val guild: Column<Long> = long("guild_id").uniqueIndex()
    public val project: Column<String> = text("defaults").uniqueIndex()
    public val version: Column<String> = text("version").uniqueIndex()
    public val location: Column<String> = text("location").uniqueIndex()
    public val doc: Column<DocElement> = serializable("doc")

    override val primaryKey: PrimaryKey = PrimaryKey(guild, version, location)
    override val id: Column<EntityID<Long>> = guild.entityId()
}

public class DocEntity(entityId: EntityID<Long>) : LongEntity(entityId) {
    public companion object : LongEntityClass<DocEntity>(DocsTable)

    public var version: String by DocsTable.version
    public var guild: Long by DocsTable.guild
}
