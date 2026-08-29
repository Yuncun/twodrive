/*
 * Copyright 2026 Eric Shen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package codes.fixmy.twodrive.core.network.model

import kotlinx.serialization.Serializable

/**
 * Network representation of a Graph [drive](https://learn.microsoft.com/graph/api/resources/drive).
 */
@Serializable
data class NetworkDrive(
    val id: String,
    val driveType: String? = null,
    val quota: NetworkQuota? = null,
)

@Serializable
data class NetworkQuota(
    val used: Long = 0,
    val total: Long = 0,
    val remaining: Long = 0,
    val state: String? = null,
)
