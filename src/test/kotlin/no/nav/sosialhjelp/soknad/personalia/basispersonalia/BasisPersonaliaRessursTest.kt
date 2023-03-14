package no.nav.sosialhjelp.soknad.personalia.basispersonalia

import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonNordiskBorger
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.personalia.basispersonalia.dto.BasisPersonaliaFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class BasisPersonaliaRessursTest {

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val kodeverkService: KodeverkService = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()

    private val basisPersonaliaRessurs =
        BasisPersonaliaRessurs(kodeverkService, soknadUnderArbeidRepository, tilgangskontroll)

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun basisPersonaliaSkalReturnereSystemBasisPersonalia() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithBasisPersonalia(
                withStatsborgerskap = true,
                withNordiskBorger = true,
                erNordisk = true
            )
        every { kodeverkService.getLand("NOR") } returns "Norge"

        val basisPersonaliaFrontend = basisPersonaliaRessurs.hentBasisPersonalia(BEHANDLINGSID)
        assertThatPersonaliaIsCorrectlyConverted(basisPersonaliaFrontend, JSON_PERSONALIA)
    }

    @Test
    fun basisPersonaliaSkalReturnereBasisPersonaliaUtenStatsborgerskapOgNordiskBorger() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithBasisPersonalia(
                withStatsborgerskap = false,
                withNordiskBorger = false,
                erNordisk = true
            )

        val basisPersonaliaFrontend = basisPersonaliaRessurs.hentBasisPersonalia(BEHANDLINGSID)
        assertThatPersonaliaIsCorrectlyConverted(basisPersonaliaFrontend, JSON_PERSONALIA_UTEN_STAT_OG_NORDISK)
    }

    @Test
    fun basisPersonaliaSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { basisPersonaliaRessurs.hentBasisPersonalia(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    private fun assertThatPersonaliaIsCorrectlyConverted(
        personaliaFrontend: BasisPersonaliaFrontend,
        jsonPersonalia: JsonPersonalia
    ) {
        assertThat(personaliaFrontend.fodselsnummer).isEqualTo(jsonPersonalia.personIdentifikator.verdi)
        assertThat(personaliaFrontend.navn?.fornavn).isEqualTo(jsonPersonalia.navn.fornavn)
        assertThat(personaliaFrontend.navn?.mellomnavn).isEqualTo(jsonPersonalia.navn.mellomnavn)
        assertThat(personaliaFrontend.navn?.etternavn).isEqualTo(jsonPersonalia.navn.etternavn)
        assertThat(personaliaFrontend.navn?.fulltNavn).isEqualTo(FULLT_NAVN)
        assertThat(personaliaFrontend.statsborgerskap)
            .isEqualTo(if (jsonPersonalia.statsborgerskap?.verdi == "NOR") "Norge" else jsonPersonalia.statsborgerskap?.verdi)
        assertThat(personaliaFrontend.nordiskBorger).isEqualTo(jsonPersonalia.nordiskBorger?.verdi)
    }

    private fun createJsonInternalSoknadWithBasisPersonalia(
        withStatsborgerskap: Boolean,
        withNordiskBorger: Boolean,
        erNordisk: Boolean
    ): SoknadUnderArbeid {
        val soknadUnderArbeid = SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = BEHANDLINGSID,
            tilknyttetBehandlingsId = null,
            eier = EIER,
            jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )

        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
            .withNavn(
                JsonSokernavn()
                    .withKilde(JsonSokernavn.Kilde.SYSTEM)
                    .withFornavn(FORNAVN)
                    .withMellomnavn(MELLOMNAVN)
                    .withEtternavn(ETTERNAVN)
            )
            .withStatsborgerskap(
                if (!withStatsborgerskap) null else JsonStatsborgerskap()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(if (erNordisk) NORDISK_STATSBORGERSKAP else IKKE_NORDISK_STATSBORGERSKAP)
            )
            .withNordiskBorger(
                if (!withNordiskBorger) null else JsonNordiskBorger()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(erNordisk)
            )
        return soknadUnderArbeid
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "123456789101"
        private const val FORNAVN = "Aragorn"
        private const val MELLOMNAVN = "Elessar"
        private const val ETTERNAVN = "Telcontar"
        private const val FULLT_NAVN = "Aragorn Elessar Telcontar"
        private const val NORDISK_STATSBORGERSKAP = "NOR"
        private const val IKKE_NORDISK_STATSBORGERSKAP = "GER"
        private val JSON_PERSONALIA = JsonPersonalia()
            .withPersonIdentifikator(
                JsonPersonIdentifikator()
                    .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                    .withVerdi(EIER)
            )
            .withNavn(
                JsonSokernavn()
                    .withKilde(JsonSokernavn.Kilde.SYSTEM)
                    .withFornavn(FORNAVN)
                    .withMellomnavn(MELLOMNAVN)
                    .withEtternavn(ETTERNAVN)
            )
            .withStatsborgerskap(
                JsonStatsborgerskap()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi("NOR")
            )
            .withNordiskBorger(
                JsonNordiskBorger()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(true)
            )
        private val JSON_PERSONALIA_UTEN_STAT_OG_NORDISK = JsonPersonalia()
            .withPersonIdentifikator(
                JsonPersonIdentifikator()
                    .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
                    .withVerdi(EIER)
            )
            .withNavn(
                JsonSokernavn()
                    .withKilde(JsonSokernavn.Kilde.SYSTEM)
                    .withFornavn(FORNAVN)
                    .withMellomnavn(MELLOMNAVN)
                    .withEtternavn(ETTERNAVN)
            )
    }
}
