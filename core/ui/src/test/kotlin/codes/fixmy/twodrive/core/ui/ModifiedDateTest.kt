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

package codes.fixmy.twodrive.core.ui

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class ModifiedDateTest {

    private val today = LocalDate(2026, 8, 30)

    @Test
    fun datesInTheCurrentYearDropTheYear() {
        val instant = Instant.parse("2026-02-04T12:00:00Z")
        assertEquals("Feb 4", formatModifiedDate(instant, Locale.US, TimeZone.UTC, today))
    }

    @Test
    fun datesInOtherYearsKeepTheYear() {
        val instant = Instant.parse("2024-07-12T12:00:00Z")
        assertEquals("Jul 12, 2024", formatModifiedDate(instant, Locale.US, TimeZone.UTC, today))
    }

    @Test
    fun orderAndSeparatorsFollowTheLocale() {
        val instant = Instant.parse("2024-07-12T12:00:00Z")
        assertEquals("12 juil. 2024", formatModifiedDate(instant, Locale.FRANCE, TimeZone.UTC, today))
    }

    @Test
    fun theDayIsTakenInTheGivenTimeZone() {
        // 23:30 UTC on Feb 4 is already Feb 5 in Tokyo.
        val instant = Instant.parse("2026-02-04T23:30:00Z")
        assertEquals("Feb 5", formatModifiedDate(instant, Locale.US, TimeZone.of("Asia/Tokyo"), today))
    }
}
