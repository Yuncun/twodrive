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

package codes.fixmy.twodrive

import codes.fixmy.twodrive.ui.accountInitials
import org.junit.Test
import kotlin.test.assertEquals

class AccountAvatarTest {

    @Test
    fun twoWordNameGivesBothInitials() {
        assertEquals("DU", accountInitials("Demo User"))
    }

    @Test
    fun longerNameUsesFirstAndLastWords() {
        assertEquals("AL", accountInitials("  ada   king lovelace "))
    }

    @Test
    fun singleWordGivesOneInitial() {
        assertEquals("A", accountInitials("ada"))
    }

    @Test
    fun blankNameGivesNoInitials() {
        assertEquals("", accountInitials("   "))
    }

    @Test
    fun characterOutsideTheBasicPlaneIsKeptWhole() {
        assertEquals("𝐀B", accountInitials("𝐀x Bee"))
    }
}
