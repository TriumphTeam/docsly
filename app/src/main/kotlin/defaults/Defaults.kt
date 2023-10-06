package dev.triumphteam.docsly.defaults

import io.ktor.server.application.Application
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.util.AttributeKey
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists

public class Defaults {

    // Move to separate repo
    private val defaultsFolder: Path = Path("data/defaults")
    private val defaults: Map<String, Set<String>> = defaultsFolder.toFile().listFiles()?.mapNotNull { project ->
        if (!project.isDirectory) return@mapNotNull null

        val versions = project.listFiles()?.mapNotNull { version ->
            if (!version.isDirectory) null else version.name
        }?.toSet() ?: emptySet()

        project.name to versions
    }?.toMap() ?: emptyMap()

    public fun defaultProjects(): Map<String, Set<String>> {
        return defaults
    }

    public fun resolve(project: String, version: String): File {
        val folder = defaultsFolder.resolve("$project/$version")
        if (folder.notExists() || !folder.isDirectory()) {
            throw FileNotFoundException("Could not find file with path '${"$project/$version"}'")
        }

        return folder.toFile().listFiles()?.find { it.name.endsWith(".json") }
            ?: throw FileNotFoundException("Could not find json file for '${"$project/$version"}'")
    }

    public companion object Plugin : BaseApplicationPlugin<Application, Defaults, Defaults> {

        override val key: AttributeKey<Defaults> = AttributeKey("defaults")

        override fun install(pipeline: Application, configure: Defaults.() -> Unit): Defaults {
            return Defaults()
        }
    }
}
