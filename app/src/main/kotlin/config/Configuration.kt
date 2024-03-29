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
package dev.triumphteam.docsly.config

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import com.zaxxer.hikari.HikariConfig
import dev.triumphteam.docsly.config.serializer.ProtocolSerializer
import io.ktor.http.URLProtocol
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.modules.SerializersModule
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

    serializersModule = SerializersModule {
        contextual(URLProtocol::class, ProtocolSerializer())
    }
}

private val EMPTY_CONFIG = Configuration()

@Serializable
public data class Configuration(
    public val port: Int = 8080,
    public val host: String = "0.0.0.0",
    public val postgres: PostgresConfig = PostgresConfig(),
    public val meili: MeiliConfig = MeiliConfig(),
)

@Serializable
public data class PostgresConfig(
    public val host: String = "0.0.0.0",
    public val port: Int = 5432,
    public val username: String = "",
    public val password: String = "",
    public val database: String = "",
) {

    /** Turns this serializable config into one that can be used by Hikari. */
    public fun toHikariConfig(): HikariConfig = HikariConfig().apply {
        dataSourceClassName = "com.impossibl.postgres.jdbc.PGDataSource"
        addDataSourceProperty("host", host)
        addDataSourceProperty("port", port)
        addDataSourceProperty("user", this@PostgresConfig.username)
        addDataSourceProperty("password", this@PostgresConfig.password)
        addDataSourceProperty("databaseName", database)
    }
}

@Serializable
public data class MeiliConfig(
    public val host: String = "0.0.0.0",
    public val port: Int = 7700,
    public val apiKey: String = "masterKey",
    @Contextual public val protocol: URLProtocol = URLProtocol.HTTP,
)

/** Get a config if it exists, or create a new one with default values, which will always result in the app failing the run. */
public fun createOrGetConfig(): Configuration {
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
