package dev.triumphteam.docsly.config

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.serializer
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.div
import kotlin.io.path.notExists
import kotlin.io.path.writeText

private val hocon = Hocon {
    encodeDefaults = true
    useConfigNamingConvention = true
}

private val EMPTY_CONFIG = Configuration()

@Serializable
data class Configuration(
    val port: Int = 8080,
    val host: String = "0.0.0.0",
    val postgres: PostgresConfig = PostgresConfig(),
)

@Serializable
data class PostgresConfig(
    val host: String = "0.0.0.0",
    val port: String = "5432",
    val username: String = "",
    val password: String = "",
    val database: String = "",
)

fun createOrGetConfig(): Configuration {
    val dataFolder = Path("data").also { if (it.notExists()) it.createDirectory() }
    val configFile = dataFolder / "configuration.conf"

    if (configFile.notExists()) {
        configFile.createFile()
        val config = hocon.encodeToConfig(Configuration::class.serializer(), EMPTY_CONFIG)
        val options = ConfigRenderOptions.defaults().apply {
            formatted = true
            json = false
            originComments = false
            comments = false
        }

        configFile.writeText(config.root().render(options))
    }

    val config = hocon.decodeFromConfig(Configuration::class.serializer(), ConfigFactory.parseFile(configFile.toFile()))

    if (config == EMPTY_CONFIG) {
        throw AssertionError("Original configuration file has not yet been modifier, please modify it in \"data/configuration.conf\"")
    }

    return config
}
