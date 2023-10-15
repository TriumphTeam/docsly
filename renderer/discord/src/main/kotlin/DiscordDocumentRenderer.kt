package dev.triumphteam.docsly.renderer

import dev.triumphteam.docsly.elements.BasicType
import dev.triumphteam.docsly.elements.DocElement
import dev.triumphteam.docsly.elements.FunctionType
import dev.triumphteam.docsly.elements.GenericType
import dev.triumphteam.docsly.elements.LiteralAnnotationArgument
import dev.triumphteam.docsly.elements.Modifier
import dev.triumphteam.docsly.elements.SerializableAnnotation
import dev.triumphteam.docsly.elements.SerializableAnnotationArgument
import dev.triumphteam.docsly.elements.SerializableFunction
import dev.triumphteam.docsly.elements.SerializableParameter
import dev.triumphteam.docsly.elements.SerializableType

public class DiscordDocumentRenderer {

    private companion object {
        private const val NEW_LINE = "\n"
        private const val SPACE = " "

        private const val DOCUMENTATION_PH = "{documentation}"
        private const val ABOVE_ANNOTATIONS_PH = "{above_annotations}"
        private const val VISIBILITY_PH = "{visibility}"
        private const val MODIFIERS_PH = "{modifiers}"
        private const val DOC_TYPE_PH = "{doc_type}"
        private const val GENERICS_PH = "{generics}"
        private const val RECEIVER_PH = "{receiver}"
        private const val NAME_PH = "{name}"
        private const val TYPE_GENERICS_PH = "{type_generics}"
        private const val PARAMETERS_PH = "{parameters}"
        private const val RETURN_PH = "{return}"

        private val FUNCTION_TEMPLATE = """
            $DOCUMENTATION_PH
            $ABOVE_ANNOTATIONS_PH
            $VISIBILITY_PH${MODIFIERS_PH}${DOC_TYPE_PH}$GENERICS_PH ${RECEIVER_PH}$NAME_PH(
            $PARAMETERS_PH    
            )$RETURN_PH
        """.trimIndent()
    }

    public fun render(document: DocElement): String {
        return buildString {
            appendLine("```kt")
            appendLine(
                when (document) {
                    is SerializableFunction -> renderFunction(document)

                    else -> TODO("NOT IMPLEMENTED YET")
                }
            )
            appendLine("```")
        }
    }

    private fun renderFunction(function: SerializableFunction): String {
        return FUNCTION_TEMPLATE
            .replace(ABOVE_ANNOTATIONS_PH, renderAnnotationsAbove(function.annotations))
            .replace(VISIBILITY_PH, function.visibility.name.lowercase())
            .replace(
                MODIFIERS_PH,
                renderModifiers(function.modifiers),
            )
            .replace(DOC_TYPE_PH, "fun")
            .replace(GENERICS_PH, " " + renderGenerics(function.generics))
            .replace(RECEIVER_PH, function.receiver?.let { "${renderType(it)}." } ?: "")
            .replace(NAME_PH, function.name)
            .replace(PARAMETERS_PH, function.parameters.joinToString(NEW_LINE) { "    ${renderParameter(it)}," })
            .replace(RETURN_PH, function.type?.let { ": ${renderType(it)}" } ?: "")
    }

    private fun renderModifiers(modifiers: Set<Modifier>): String {
        return modifiers.sortedBy(Modifier::order)
            .joinToString(" ", prefix = " ", postfix = " ") { it.displayName ?: it.name.lowercase() }
    }

    private fun renderAnnotationsAbove(annotations: List<SerializableAnnotation>): String {
        return annotations.joinToString(NEW_LINE) { annotation ->
            buildString {
                append("@")
                append(annotation.type)

                val arguments = annotation.arguments
                if (arguments.isNotEmpty()) {
                    appendLine("(")
                    arguments.entries.forEach { (name, argument) ->
                        appendLine("    $name = ${renderAnnotationArgument(argument)},")
                    }
                    append(")")
                }
            }
        }
    }

    private fun renderParameter(parameter: SerializableParameter): String {
        return "${parameter.name}: ${renderType(parameter.type)}"
    }

    private fun renderGenerics(generics: List<GenericType>): String {
        if (generics.isEmpty()) return ""

        return "<${
        generics.joinToString(", ") {
            it.name // TODO
        }
        }>"
    }

    private fun renderType(type: SerializableType): String {
        return when (type) {
            is BasicType -> buildString {
                val name = type.name
                if (name != null) append(name).append(SPACE)

                val projection = type.projection
                if (projection != null) append(projection.kotlin).append(SPACE) // TODO: JAVA

                append(type.type)

                val parameters = type.parameters
                if (parameters.isNotEmpty()) {
                    append("<")
                    append(
                        parameters.joinToString(", ") {
                            renderType(it)
                        }
                    )
                    append(">")
                }
            }

            is FunctionType -> buildString {
                val parameters = type.params
                append("(")
                if (parameters.isNotEmpty()) {
                    append(parameters.joinToString(", ") { renderType(it) })
                }
                append(")")
                append(" -> ")
                append(type.returnType?.let { renderType(it) } ?: "Unit") // TODO: default not be unit? and java
            }

            else -> "" // TODO
        }
    }

    private fun renderAnnotationArgument(argument: SerializableAnnotationArgument): String {
        return when (argument) {
            is LiteralAnnotationArgument -> "\"${argument.typeName}\""

            else -> ""
        }
    }
}
