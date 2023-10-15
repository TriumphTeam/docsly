package dev.triumphteam.docsly.project

import dev.triumphteam.docsly.database.entity.DocumentEntity
import dev.triumphteam.docsly.database.entity.ProjectEntity
import dev.triumphteam.docsly.database.entity.ProjectsTable
import dev.triumphteam.docsly.defaults.Defaults
import dev.triumphteam.docsly.elements.DocElement
import dev.triumphteam.docsly.meilisearch.Meili
import io.ktor.server.application.Application
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.plugin
import io.ktor.util.AttributeKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * A class representing a Projects plugin.
 * Used for setting up projects, deleting, etc.
 *
 * @property meili The Meili plugin instance.
 * @property defaults The Defaults plugin instance.
 */
public class Projects(private val meili: Meili, private val defaults: Defaults) {

    public companion object Plugin : BaseApplicationPlugin<Application, Projects, Projects> {

        override val key: AttributeKey<Projects> = AttributeKey("Projects")

        override fun install(pipeline: Application, configure: Projects.() -> Unit): Projects {
            return Projects(pipeline.plugin(Meili), pipeline.plugin(Defaults))
        }

        public fun indexKeyFor(guild: String, projectEntity: ProjectEntity): String {
            return "${guild}_${projectEntity.id.value}"
        }

        public val JSON: Json = Json {
            explicitNulls = false
            ignoreUnknownKeys = true
        }
    }

    /**
     * Sets up projects for a specified guild with their projects.
     *
     * @param guild The name of the guild.
     * @param projects A map containing project names as keys and sets of versions as values.
     */
    public suspend fun setup(guild: String, defaults: Defaults) {
        // TODO: REPLACE WITH BETTER SETUP LATER

        val mapped = transaction {
            defaults.defaults.entries.associate { (versionData, documentsFile) ->

                val docs = JSON.decodeFromString<List<DocElement>>(documentsFile.readText())

                val project = transaction {
                    ProjectEntity.new {
                        this.guild = guild
                        this.name = versionData.project
                        this.version = versionData.version
                        this.latest = versionData.latest
                    }
                }

                project to docs.map { doc ->
                    DocumentEntity.new {
                        this.projectId = project.id.value
                        this.document = doc
                    }
                }
            }
        }

        mapped.forEach { (project, documents) ->
            documents.chunked(500).forEach { chunk ->
                meili.client.index(
                    indexKeyFor(guild, project),
                    primaryKey = "id",
                ).addDocuments(
                    chunk.map { doc ->
                        IndexDocument(doc.id.value, doc.document.createReferences())
                    },
                )
            }
        }
    }

    public fun getProjects(guild: String): List<ProjectData> {
        return transaction {
            ProjectEntity.find {
                ProjectsTable.guild eq guild
            }.groupBy(ProjectEntity::name)
                .map { (project, entity) ->
                    ProjectData(project, entity.map(ProjectEntity::version))
                }
        }
    }

    public fun getProject(guild: String, project: String, version: String?): ProjectEntity? {
        return transaction {
            val query: SqlExpressionBuilder.() -> Op<Boolean> = if (version == null) {
                {
                    (ProjectsTable.guild eq guild) and (ProjectsTable.name eq project) and (ProjectsTable.latest eq true)
                }
            } else {
                {
                    (ProjectsTable.guild eq guild) and (ProjectsTable.name eq project) and (ProjectsTable.version eq version)
                }
            }

            ProjectEntity.find(query).firstOrNull()
        }
    }
}

@Serializable
public data class IndexDocument(
    public val id: Long,
    public val references: List<String>,
)
