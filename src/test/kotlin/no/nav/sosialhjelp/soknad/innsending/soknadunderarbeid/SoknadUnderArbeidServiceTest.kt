package no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneErMidlertidigUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.exceptions.SendingTilKommuneUtilgjengeligException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal class SoknadUnderArbeidServiceTest {
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val kommuneInfoService: KommuneInfoService = mockk()
    private val soknadUnderArbeidService = SoknadUnderArbeidService(soknadUnderArbeidRepository, kommuneInfoService)

    @Test
    fun settInnsendingstidspunktPaSoknadSkalHandtereEttersendelse() {
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs

        soknadUnderArbeidService.settInnsendingstidspunktPaSoknad(
            lagSoknadUnderArbeidForEttersendelse(),
        )
    }

    @Test
    internal fun `skalSoknadSendesMedDigisosApi - alle scenarier`() {
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())

        // false - soknadUnderArbeid er ettersendelse
        val soknadUnderArbeid: SoknadUnderArbeid = mockk()
        every { soknadUnderArbeidRepository.hentSoknad(BEHANDLINGSID, any()) } returns soknadUnderArbeid

        // false - mottaker.kommunenummer er null, dvs at bruker ikke har valgt noen adresse enda
        every { soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer } returns null
        assertThat(soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(BEHANDLINGSID)).isFalse

        // kast feil - nedetid for kommune
        every { soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker?.kommunenummer } returns "1234"
        every { kommuneInfoService.getKommuneStatus("1234") } returns KommuneStatus.FIKS_NEDETID_OG_TOM_CACHE
        assertThatExceptionOfType(SendingTilKommuneUtilgjengeligException::class.java)
            .isThrownBy { soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(BEHANDLINGSID) }

        // kast feil - midlertidig nedetid for kommune
        every {
            kommuneInfoService.getKommuneStatus(
                "1234",
            )
        } returns KommuneStatus.SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD
        assertThatExceptionOfType(SendingTilKommuneErMidlertidigUtilgjengeligException::class.java)
            .isThrownBy { soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(BEHANDLINGSID) }

        // false - kommune mangler konfigurasjon hos Fiks
        every { kommuneInfoService.getKommuneStatus("1234") } returns KommuneStatus.MANGLER_KONFIGURASJON
        assertThatExceptionOfType(SendingTilKommuneUtilgjengeligException::class.java)
            .isThrownBy { soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(BEHANDLINGSID) }

        // false - kommune har feil konfigurasjon
        every { kommuneInfoService.getKommuneStatus("1234") } returns KommuneStatus.HAR_KONFIGURASJON_MED_MANGLER
        assertThatExceptionOfType(SendingTilKommuneUtilgjengeligException::class.java)
            .isThrownBy { soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(BEHANDLINGSID) }
    }

    private fun lagSoknadUnderArbeidForEttersendelse(): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            soknadId = SOKNAD_UNDER_ARBEID_ID,
            versjon = 1L,
            behandlingsId = BEHANDLINGSID,
            eier = EIER,
            jsonInternalSoknad = null,
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = OPPRETTET_DATO,
            sistEndretDato = SIST_ENDRET_DATO,
        )
    }

    @Test
    fun `Valider at innsendingstidspunkt blir riktig satt`() {
        val tidspunktRegEx = "^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]T[0-9][0-9]:[0-9][0-9]:[0-9][0-9].[0-9][0-9]*Z\$"

        OffsetDateTime.of(
            2020,
            12,
            31,
            0,
            0,
            0,
            0,
            ZoneOffset.UTC,
        )
            .let { SoknadUnderArbeidService.nowWithForcedMillis(it) }
            .also { assertThat(it.matches(Regex(tidspunktRegEx))).isTrue() }
    }

    companion object {
        private const val EIER = "12345678910"
        private const val SOKNAD_UNDER_ARBEID_ID = 1L
        private const val BEHANDLINGSID = "1100001L"
        private val OPPRETTET_DATO = LocalDateTime.now().minusSeconds(50)
        private val SIST_ENDRET_DATO = LocalDateTime.now()
    }
}
