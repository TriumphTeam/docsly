package dev.triumphteam.docsly

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.dokka.gradle.DokkaCollectorTask
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial

private const val DOKKA_ID = "org.jetbrains.dokka"
private const val DOCLOPEDIA_DOKKA = "dev.triumphteam:docsly-dokka-plugin:0.0.2"

public open class DocslyGradlePlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        if (!plugins.hasPlugin(DOKKA_ID)) {
            throw RuntimeException("Docsly depends on Dokka to generate files, please make sure you have it on your project!")
        }

        setupDokkaTask("dokkaDocsly") {
            plugins.dependencies.add(project.dependencies.create(DOCLOPEDIA_DOKKA))
            description = "Generates JSON documentation to be used by Docsly."
        }
    }

    /** Stolen from Dokka's own plugin. */
    private fun Project.setupDokkaTask(
        name: String,
        configuration: AbstractDokkaTask.() -> Unit = {},
    ) {
        project.tasks.register<DokkaTask>(name) {
            configuration()
        }

        if (project.parent != null) {
            val partialName = "${name}Partial"
            project.tasks.register<DokkaTaskPartial>(partialName) {
                configuration()
            }
        }

        if (project.subprojects.isNotEmpty()) {
            val multiModuleName = "${name}MultiModule"

            project.tasks.register<DokkaMultiModuleTask>(multiModuleName) {
                addSubprojectChildTasks("${name}Partial")
                configuration()
                description = "Runs all subprojects '$name' tasks and generates module navigation page"
            }

            project.tasks.register<DefaultTask>("${name}Multimodule") {
                dependsOn(multiModuleName)
                doLast {
                    logger.warn("'Multimodule' is deprecated. Use 'MultiModule' instead")
                }
            }

            project.tasks.register<DokkaCollectorTask>("${name}Collector") {
                addSubprojectChildTasks(name)
                description = "Generates documentation merging all subprojects '$name' tasks into one virtual module"
            }
        }
    }
}
