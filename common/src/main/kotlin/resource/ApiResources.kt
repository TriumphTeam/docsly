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
package dev.triumphteam.docsly.resource

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

/** Resource location for "/api". */
@Serializable
@Resource("/api/guild")
public class GuildApi {

    /** Resource location for "/api/[guild]". */
    @Serializable
    @Resource("{guild}")
    public class Guild(public val parent: GuildApi = GuildApi(), public val guild: String) {

        /** Resource location for "/api/[guild]/setup". */
        @Serializable
        @Resource("setup")
        public class Setup(public val parent: Guild)

        /** Resource location for "/api/[guild]/[index]/[version]". *//*
        @Serializable
        @Resource("{project}/{version}")
        public class Project(
            public val parent: Guild,
            public val index: String,
            public val version: String,
        ) {

            *//** Resource location for "/api/[guild]/[index]/[version]/search". *//*
            @Serializable
            @Resource("search")
            public class Search(public val parent: Project)
        }*/
    }
}
