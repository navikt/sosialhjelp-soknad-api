package no.nav.sosialhjelp.soknad.personalia.kontonummer

import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonKontonummer
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class KontonummerSystemdataTest {

    private val kontonummerService: KontonummerService = mockk()
    private val kontonummerSystemdata = KontonummerSystemdata(kontonummerService)

    @Test
    fun skalOppdatereKontonummer() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        every { kontonummerService.getKontonummer(any()) } returns KONTONUMMER_SYSTEM
        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        assertThat(jsonPersonalia.kontonummer.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(jsonPersonalia.kontonummer.verdi).isEqualTo(KONTONUMMER_SYSTEM)
    }

    @Test
    fun skalOppdatereKontonummerOgFjerneUlovligeSymboler() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        every { kontonummerService.getKontonummer(any()) } returns KONTONUMMER_SYSTEM + " !#¤%&/()=?`-<>|§,.-* "
        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        assertThat(jsonPersonalia.kontonummer.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(jsonPersonalia.kontonummer.verdi).isEqualTo(KONTONUMMER_SYSTEM)
    }

    @Test
    fun skalIkkeOppdatereKontonummerDersomKildeErBruker() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            createJsonInternalSoknadWithUserDefinedKontonummer()
        )
        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        assertThat(jsonPersonalia.kontonummer.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(jsonPersonalia.kontonummer.verdi).isEqualTo(KONTONUMMER_BRUKER)
    }

    @Test
    fun skalSetteNullDersomKontonummerErTomStreng() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        every { kontonummerService.getKontonummer(any()) } returns ""
        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        assertThat(jsonPersonalia.kontonummer.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(jsonPersonalia.kontonummer.verdi).isNull()
    }

    @Test
    fun skalSetteNullDersomKontonummerErNull() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        every { kontonummerService.getKontonummer(any()) } returns null
        kontonummerSystemdata.updateSystemdataIn(soknadUnderArbeid, "")
        val jsonPersonalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        assertThat(jsonPersonalia.kontonummer.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(jsonPersonalia.kontonummer.verdi).isNull()
    }

    private fun createJsonInternalSoknadWithUserDefinedKontonummer(): JsonInternalSoknad {
        val jsonInternalSoknad = SoknadService.createEmptyJsonInternalSoknad(EIER)
        jsonInternalSoknad.soknad.data.personalia.kontonummer = JsonKontonummer().withKilde(JsonKilde.BRUKER).withVerdi(
            KONTONUMMER_BRUKER
        )
        return jsonInternalSoknad
    }

    companion object {
        private const val EIER = "12345678901"
        private const val KONTONUMMER_SYSTEM = "12345678903"
        private const val KONTONUMMER_BRUKER = "11223344556"
    }
}
