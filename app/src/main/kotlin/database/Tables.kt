package dev.triumphteam.docsly.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

public object DocsTable : IntIdTable() {
    public val name: Column<String> = varchar("doc_name", 32).uniqueIndex()
    public val version: Column<String> = varchar("doc_version", 32).uniqueIndex()
    public val guild: Column<Long> = long("guild_id").uniqueIndex()
}
