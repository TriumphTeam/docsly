/**
 * MIT License
 *
 * Copyright (c) 2019-2022 TriumphTeam and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
private const val DOCSLY_DOKKA_PLUGIN_VERSION = "dev.triumphteam:docsly-dokka-plugin:0.0.3"

public open class DocslyGradlePlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        if (!plugins.hasPlugin(DOKKA_ID)) {
            throw RuntimeException("Docsly depends on Dokka to generate files, please make sure you have it on your project!")
        }

        setupDokkaTask("dokkaDocsly") {
            plugins.dependencies.add(project.dependencies.create(DOCSLY_DOKKA_PLUGIN_VERSION))
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
