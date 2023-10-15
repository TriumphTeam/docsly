package dev.triumphteam.docsly.defaults

import io.ktor.server.application.Application
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.util.AttributeKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

public class Defaults {

    public companion object Plugin : BaseApplicationPlugin<Application, Defaults, Defaults> {

        override val key: AttributeKey<Defaults> = AttributeKey("defaults")

        override fun install(pipeline: Application, configure: Defaults.() -> Unit): Defaults {
            return Defaults()
        }
    }

    private val logger: Logger = LoggerFactory.getLogger(Defaults::class.java)

    // Move to separate repo
    private val defaultsFolder: Path = Path("data/defaults")
    public val defaults: Map<FileProjectVersion, File> = defaultsFolder.toFile().listFiles()?.flatMap { project ->
        if (!project.isDirectory) return@flatMap emptyList()

        project.listFiles()?.mapNotNull inner@{ version ->
            if (!version.isDirectory) return@inner null

            val files = version.listFiles() ?: return@inner run {
                logger.warn("Ignoring folder ${version.name}, as it is empty.")
                null
            }

            val versionFile = files.find { it.name == "version.json" } ?: return@inner run {
                logger.warn("Ignoring folder ${version.name}, as it does not have a version.json file.")
                null
            }

            val documentsFile = files.find { it.name == "documents.json" } ?: return@inner run {
                logger.warn("Ignoring folder ${version.name}, as it does not have a documents.json file.")
                null
            }

            Json.decodeFromString<FileProjectVersion>(versionFile.readText()) to documentsFile
        } ?: emptyList()
    }?.toMap() ?: emptyMap()

    @Serializable
    public data class FileProjectVersion(
        public val project: String,
        public val version: String,
        public val latest: Boolean,
    )
}
