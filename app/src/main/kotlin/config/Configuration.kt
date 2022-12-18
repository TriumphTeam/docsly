package dev.triumphteam.docsly.config

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.zaxxer.hikari.HikariConfig
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
) {

    /** Turns this serializable config into one that can be used by Hikari. */
    fun toHikariConfig() = HikariConfig().apply {
        dataSourceClassName = "com.impossibl.postgres.jdbc.PGDataSource"
        addDataSourceProperty("host", host)
        addDataSourceProperty("port", port)
        addDataSourceProperty("user", username)
        addDataSourceProperty("password", password)
        addDataSourceProperty("databaseName", database)
    }
}

/** Get a config if it exists, or create a new one with default values, which will always result in the app failing the run. */
fun createOrGetConfig(): Configuration {
    val dataFolder = Path("data").also { if (it.notExists()) it.createDirectory() }
    val configFile = dataFolder / "configuration.conf"

    if (configFile.notExists()) {
        // If the config doesn't exist we create a new file
        configFile.createFile()
        // Then serialize a new empty config
        val config = hocon.encodeToConfig(Configuration::class.serializer(), EMPTY_CONFIG)
        val options = ConfigRenderOptions.defaults().apply {
            formatted = true
            json = false
            originComments = false
            comments = false
        }

        // And write to the file
        configFile.writeText(config.root().render(options))
    }

    // We decode the config from the file
    val config = hocon.decodeFromConfig(Configuration::class.serializer(), ConfigFactory.parseFile(configFile.toFile()))

    // We need the config to be edited before continuing
    if (config == EMPTY_CONFIG) {
        throw AssertionError("Original configuration file has not yet been modifier, please modify it in \"data/configuration.conf\"")
    }

    return config
}
