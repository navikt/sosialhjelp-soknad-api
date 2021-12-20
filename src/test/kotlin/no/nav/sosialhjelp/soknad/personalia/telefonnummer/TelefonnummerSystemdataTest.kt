package no.nav.sosialhjelp.soknad.personalia.telefonnummer

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonTelefonnummer
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TelefonnummerSystemdataTest {

    private val mobiltelefonService: MobiltelefonService = mockk()
    private val telefonnummerSystemdata = TelefonnummerSystemdata(mobiltelefonService)

    @Test
    fun skalOppdatereTelefonnummerUtenLandkode() {
        every { mobiltelefonService.hent(any()) } returns TELEFONNUMMER_SYSTEM

        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        assertThat(jsonPersonalia.telefonnummer.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(jsonPersonalia.telefonnummer.verdi).isEqualTo("+47$TELEFONNUMMER_SYSTEM")
    }

    @Test
    fun skalOppdatereTelefonnummerMedLandkode() {
        every { mobiltelefonService.hent(any()) } returns "+47$TELEFONNUMMER_SYSTEM"

        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        assertThat(jsonPersonalia.telefonnummer.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(jsonPersonalia.telefonnummer.verdi).isEqualTo("+47$TELEFONNUMMER_SYSTEM")
    }

    @Test
    fun skalIkkeOppdatereTelefonnummerDersomKildeErBruker() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createJsonInternalSoknadWithUserDefinedTelefonnummer())
        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        assertThat(jsonPersonalia.telefonnummer.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(jsonPersonalia.telefonnummer.verdi).isEqualTo(TELEFONNUMMER_BRUKER)
    }

    @Test
    fun skalSetteNullDersomTelefonnummerErTomStreng() {
        every { mobiltelefonService.hent(any()) } returns ""

        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        assertThat(jsonPersonalia.telefonnummer).isNull()
    }

    @Test
    fun skalSetteNullDersomTelefonnummerErNull() {
        every { mobiltelefonService.hent(any()) } returns null

        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        telefonnummerSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        assertThat(jsonPersonalia.telefonnummer).isNull()
    }

    private fun createJsonInternalSoknadWithUserDefinedTelefonnummer(): JsonInternalSoknad {
        val jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER)
        jsonInternalSoknad.soknad.data.personalia.telefonnummer = JsonTelefonnummer()
            .withKilde(JsonKilde.BRUKER)
            .withVerdi(TELEFONNUMMER_BRUKER)
        return jsonInternalSoknad
    }

    companion object {
        private const val EIER = "12345678901"
        private const val TELEFONNUMMER_SYSTEM = "98765432"
        private const val TELEFONNUMMER_BRUKER = "+4723456789"
    }
}
