package dev.triumphteam.docsly.database.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

/**
 * A database table for storing projects information.
 *
 * The ProjectsTable class extends the IntIdTable class to inherit the primary key column 'id'.
 * It has columns to store the guild id, name, version, and a boolean flag for the latest version.
 * The table has a unique constraint on the combination of guild, name, and version columns.
 */
public object ProjectsTable : IntIdTable("docsly_projects") {

    public val guild: Column<String> = text("guild_id")
    public val name: Column<String> = text("name")
    public val version: Column<String> = text("version")
    public val latest: Column<Boolean> = bool("latest")

    init {
        uniqueIndex(
            columns = arrayOf(guild, name, version),
            customIndexName = "docsly_guild_name_version_uq"
        )
    }
}

/** Project entity referencing the table [ProjectsTable]. */
public class ProjectEntity(entityId: EntityID<Int>) : IntEntity(entityId) {

    public companion object : IntEntityClass<ProjectEntity>(ProjectsTable)

    public var guild: String by ProjectsTable.guild
    public var name: String by ProjectsTable.name
    public var version: String by ProjectsTable.version
    public var latest: Boolean by ProjectsTable.latest
}
