package dev.triumphteam.docsly.database.entity

import dev.triumphteam.docsly.database.entity.DocumentsTable.document
import dev.triumphteam.docsly.database.entity.DocumentsTable.projectId
import dev.triumphteam.docsly.database.exposed.serializable
import dev.triumphteam.docsly.elements.DocElement
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

/**
 * Represents a table that stores documents associated with a project.
 *
 * This class extends the `LongIdTable` class and provides columns to store
 * the project ID and document contents.
 *
 * @property projectId The column to store the ID of the project associated with the document.
 * @property document The column to store the document contents.
 */
public object DocumentsTable : LongIdTable("docsly_documents") {

    public val projectId: Column<Int> = integer("project_id").references(ProjectsTable.id)
    public val document: Column<DocElement> = serializable("document")
}

/** Document entity referencing the table [DocumentsTable]. */
public class DocumentEntity(entityId: EntityID<Long>) : LongEntity(entityId) {

    public companion object : LongEntityClass<DocumentEntity>(DocumentsTable)

    public var projectId: Int by DocumentsTable.projectId
    public var document: DocElement by DocumentsTable.document
}
