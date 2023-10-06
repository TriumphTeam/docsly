package dev.triumphteam.docsly.project

import dev.triumphteam.docsly.defaults.Defaults
import dev.triumphteam.docsly.elements.DocElement
import dev.triumphteam.docsly.meilisearch.Meili
import dev.triumphteam.docsly.meilisearch.annotation.PrimaryKey
import io.ktor.server.application.Application
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.plugin
import io.ktor.util.AttributeKey
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

public class Projects(private val meili: Meili, private val defaults: Defaults) {

    public companion object Plugin : BaseApplicationPlugin<Application, Projects, Projects> {

        override val key: AttributeKey<Projects> = AttributeKey("Projects")

        override fun install(pipeline: Application, configure: Projects.() -> Unit): Projects {
            return Projects(pipeline.plugin(Meili), pipeline.plugin(Defaults))
        }

        public fun indexKeyFor(guild: String, project: String, version: String): String {
            return "$guild:$project:$version"
        }
    }

    private val json = Json {
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    public suspend fun setupProjects(guild: String, projects: Map<String, Set<String>>) {
        // transaction {
        val mapped = projects.mapValues { (project, versions) ->
            versions.associateWith { version ->
                val jsonFile = defaults.resolve(project, version.replace(".", "_"))
                json.decodeFromString<List<DocElement>>(jsonFile.readText())
            }
        }

        runBlocking {
            mapped.forEach { (project, versions) ->
                versions.forEach { (version, docs) ->
                    meili.client.index(indexKeyFor(guild, project, version)).addDocuments(
                        docs.map { doc ->
                            IndexDocument(doc.location, doc.createReferences())
                        }
                    )
                }
            }
        }

        // TODO: Postgres
        // }
    }
}

@Serializable
public data class IndexDocument(
    @PrimaryKey public val location: String,
    public val references: List<String>,
)
