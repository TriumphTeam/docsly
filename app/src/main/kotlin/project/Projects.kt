package dev.triumphteam.docsly.project

import dev.triumphteam.docsly.database.entity.DocEntity
import dev.triumphteam.docsly.defaults.Defaults
import dev.triumphteam.docsly.elements.DocElement
import dev.triumphteam.docsly.meilisearch.Meili
import io.ktor.server.application.Application
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.plugin
import io.ktor.util.AttributeKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

        public fun indexKeyFor(guild: String, project: String, version: String): String {
            return "${guild}_${project}_$version"
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
    public suspend fun setupProjects(guild: String, projects: Map<String, Set<String>>) {
        val mapped = transaction {
            projects.mapValues { (project, versions) ->
                versions.associateWith { version ->
                    val jsonFile = defaults.resolve(project, version.replace(".", "_"))
                    val docs = JSON.decodeFromString<List<DocElement>>(jsonFile.readText())

                    docs.map { doc ->
                        DocEntity.new {
                            this.guild = guild
                            this.project = project
                            this.version = version
                            this.doc = doc
                        }
                    }
                }
            }
        }

        mapped.forEach { (project, versions) ->
            versions.forEach { (version, docs) ->
                docs.chunked(1000).forEach { chunk ->
                    meili.client.index(
                        indexKeyFor(guild, project, version.replace(".", "_")),
                        primaryKey = "id",
                    ).addDocuments(
                        chunk.map { doc ->
                            IndexDocument(doc.id.value, doc.doc.createReferences())
                        }.also { println(JSON.encodeToString(it)) },
                    )
                }
            }
        }
    }
}

@Serializable
public data class IndexDocument(
    public val id: Long,
    public val references: List<String>,
)
