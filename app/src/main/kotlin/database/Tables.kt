package dev.triumphteam.docsly.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object DocsTable : IntIdTable() {
    val name: Column<String> = varchar("doc_name", 32).uniqueIndex()
    val version: Column<String> = varchar("doc_version", 32).uniqueIndex()
    val guild: Column<Long> = long("guild_id").uniqueIndex()
}

object EntryTable : IntIdTable() {
    // val type: Column<> = varchar("entry_type", 32).uniqueIndex()
    val name: Column<String> = varchar("doc_name", 32).uniqueIndex()
}
