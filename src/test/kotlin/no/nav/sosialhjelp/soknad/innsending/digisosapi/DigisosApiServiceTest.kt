package no.nav.sosialhjelp.soknad.innsending.digisosapi

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.TimestampFixer
import no.nav.sosialhjelp.soknad.v2.json.createEmptyJsonInternalSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

// TODO Dette er strengt talt ikke en DigisosApiServiceTest lenger etter opprydding
internal class DigisosApiServiceTest {
    private val eier = "12345678910"

    @BeforeEach
    fun setUpBefore() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(MiljoUtils)
    }

    @Test
    fun `Verifiser at broken timestamps fikses`() {
        val json = createJsonInternalSoknadWithInvalidTimestamps()

        TimestampFixer.fixBrokenTimestamps(json)
            .also { isAnyTimestampsChanged ->
                assertThat(isAnyTimestampsChanged).isTrue()
                assertThat(json.soknad.innsendingstidspunkt).matches(REGEX)
                json.soknad.data.okonomi.opplysninger.bekreftelse
                    .forEach { assertThat(it.bekreftelsesDato).matches(REGEX) }
            }
    }

    @Test
    fun `Skal ikke fikse godkjente timestamps`() {
        val validTimestamp = "2023-10-10T10:10:10.999Z"

        val json =
            JsonInternalSoknad().withSoknad(
                JsonSoknad()
                    .withInnsendingstidspunkt(validTimestamp)
                    .withData(
                        JsonData().withOkonomi(
                            JsonOkonomi().withOpplysninger(
                                JsonOkonomiopplysninger().withBekreftelse(
                                    listOf(
                                        JsonOkonomibekreftelse().withBekreftelsesDato("2024-09-09T09:09:09Z"),
                                    ),
                                ),
                            ),
                        ),
                    ),
            )

        TimestampFixer.fixBrokenTimestamps(json)
            .also { anyTimestampFixed ->
                assertThat(anyTimestampFixed).isTrue()
                assertThat(json.soknad.innsendingstidspunkt).isEqualTo(validTimestamp)
                assertThat(json.soknad.innsendingstidspunkt).matches(REGEX)

                json.soknad.data.okonomi.opplysninger.bekreftelse.forEach {
                    assertThat(it.bekreftelsesDato).matches(REGEX)
                }
            }
    }

    @Test
    fun `Fikse tidspunkt aksepterer null-objekter eller ingen bekreftelser`() {
        val json =
            JsonInternalSoknad().withSoknad(
                JsonSoknad().withInnsendingstidspunkt("2023-10-10T10:10:10.041Z"),
            )

        TimestampFixer.fixBrokenTimestamps(json)
            .also { anyTimestampChanged ->
                assertThat(anyTimestampChanged).isFalse()
                assertThat(json.soknad.innsendingstidspunkt).matches(REGEX)
                assertThat(json.soknad.data).isNull()
            }
    }

    companion object {
        private const val REGEX =
            "^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]T[0-9][0-9]:[0-9][0-9]" +
                ":[0-9][0-9].[0-9][0-9]*Z$"
    }

    private fun createJsonInternalSoknadWithInvalidTimestamps(): JsonInternalSoknad {
        return createEmptyJsonInternalSoknad(eier, false)
            .withSoknad(
                JsonSoknad()
                    .withInnsendingstidspunkt("2024-09-05T21:34:37Z")
                    .withData(
                        JsonData()
                            .withOkonomi(
                                JsonOkonomi()
                                    .withOpplysninger(
                                        JsonOkonomiopplysninger()
                                            .withBekreftelse(
                                                listOf(
                                                    JsonOkonomibekreftelse().withBekreftelsesDato("2024-09-05T21:34:37Z"),
                                                    JsonOkonomibekreftelse().withBekreftelsesDato("2024-09-05T21:34:37Z"),
                                                ),
                                            ),
                                    ),
                            ),
                    ),
            )
    }
}
