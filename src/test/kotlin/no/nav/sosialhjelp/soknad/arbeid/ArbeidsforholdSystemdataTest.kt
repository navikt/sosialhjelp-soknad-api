package no.nav.sosialhjelp.soknad.arbeid

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold.Stillingstype
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkatteetatenSystemdata
import no.nav.sosialhjelp.soknad.tekster.TextService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class ArbeidsforholdSystemdataTest {

    private val arbeidsforholdService: ArbeidsforholdService = mockk()
    private val textService: TextService = mockk()
    private val skattbarInntektService: SkattbarInntektService = mockk()

    private val arbeidsforholdSystemdata = ArbeidsforholdSystemdata(arbeidsforholdService, textService)
    private val skatteetatenSystemdata = SkatteetatenSystemdata(skattbarInntektService, mockk(), textService)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

    @Test
    fun skalOppdatereArbeidsforhold() {
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
            .withEier(EIER)
        val arbeidsforholdList = listOf(ARBEIDSFORHOLD_LONNSLIPP, ARBEIDSFORHOLD_SLUTTOPPGJOR)
        every { arbeidsforholdService.hentArbeidsforhold(any()) } returns arbeidsforholdList
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"

        arbeidsforholdSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonArbeidsforholdList = soknadUnderArbeid.jsonInternalSoknad.soknad.data.arbeid.forhold
        val jsonArbeidsforhold = jsonArbeidsforholdList[0]
        val jsonarbeidsforhold2 = jsonArbeidsforholdList[1]
        assertThat(jsonArbeidsforhold.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(jsonarbeidsforhold2.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThatArbeidsforholdIsCorrectlyConverted(ARBEIDSFORHOLD_LONNSLIPP, jsonArbeidsforhold)
        assertThatArbeidsforholdIsCorrectlyConverted(ARBEIDSFORHOLD_SLUTTOPPGJOR, jsonarbeidsforhold2)
    }

    @Test
    fun skalLeggeTilInntektForLonnslipp() {
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
            .withEier(EIER)
        setSamtykke(soknadUnderArbeid.jsonInternalSoknad, true)
        soknadUnderArbeid.jsonInternalSoknad.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet = true
        val arbeidsforholdList = listOf(ARBEIDSFORHOLD_LONNSLIPP)

        every { arbeidsforholdService.hentArbeidsforhold(any()) } returns arbeidsforholdList
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"
        every { skattbarInntektService.hentUtbetalinger(any()) } returns emptyList()

        skatteetatenSystemdata.updateSystemdataIn(soknadUnderArbeid)
        arbeidsforholdSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val inntekt = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.inntekt[0]
        assertThat(inntekt.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(inntekt.type).isEqualTo(SoknadJsonTyper.JOBB)
        assertThat(inntekt.tittel).isEqualTo("tittel")
        assertThat(inntekt.overstyrtAvBruker).isFalse
    }

    @Test
    fun skalLeggeTilUtbetalingForSluttoppgjor() {
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
            .withEier(EIER)
        setSamtykke(soknadUnderArbeid.jsonInternalSoknad, true)
        soknadUnderArbeid.jsonInternalSoknad.soknad.driftsinformasjon.inntektFraSkatteetatenFeilet = true
        val arbeidsforholdList = listOf(ARBEIDSFORHOLD_SLUTTOPPGJOR)

        every { arbeidsforholdService.hentArbeidsforhold(any()) } returns arbeidsforholdList
        every { textService.getJsonOkonomiTittel(any()) } returns "tittel"
        every { skattbarInntektService.hentUtbetalinger(any()) } returns emptyList()

        skatteetatenSystemdata.updateSystemdataIn(soknadUnderArbeid)
        arbeidsforholdSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val utbetaling = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling[0]
        assertThat(utbetaling.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(utbetaling.type).isEqualTo(SoknadJsonTyper.SLUTTOPPGJOER)
        assertThat(utbetaling.tittel).isEqualTo("tittel")
        assertThat(utbetaling.overstyrtAvBruker).isFalse
    }

    @Test
    fun skalFjerneArbeidsforholdOgFjerneUtbetalingOgInntekt() {
        val soknadUnderArbeid = SoknadUnderArbeid()
            .withEier(EIER)
            .withJsonInternalSoknad(createSoknadUnderArbeidWithArbeidsforholdAndSluttOppgjorAndLonnslipp())

        every { arbeidsforholdService.hentArbeidsforhold(any()) } returns null

        arbeidsforholdSystemdata.updateSystemdataIn(soknadUnderArbeid)

        val jsonArbeidsforholdList = soknadUnderArbeid.jsonInternalSoknad.soknad.data.arbeid.forhold
        val utbetalinger = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling
        val inntekter = soknadUnderArbeid.jsonInternalSoknad.soknad.data.okonomi.oversikt.inntekt
        assertThat(jsonArbeidsforholdList).isNull()
        assertThat(utbetalinger).isEmpty()
        assertThat(inntekter).isEmpty()
    }

    private fun createSoknadUnderArbeidWithArbeidsforholdAndSluttOppgjorAndLonnslipp(): JsonInternalSoknad {
        val jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER)
        jsonInternalSoknad.soknad.data.arbeid.forhold.add(JsonArbeidsforhold())
        jsonInternalSoknad.soknad.data.okonomi.opplysninger.utbetaling.add(
            JsonOkonomiOpplysningUtbetaling().withType(SoknadJsonTyper.SLUTTOPPGJOER)
        )
        jsonInternalSoknad.soknad.data.okonomi.oversikt.inntekt.add(
            JsonOkonomioversiktInntekt().withType(SoknadJsonTyper.JOBB)
        )
        return jsonInternalSoknad
    }

    private fun setSamtykke(jsonInternalSoknad: JsonInternalSoknad, harSamtykke: Boolean) {
        val bekreftelser = jsonInternalSoknad.soknad.data.okonomi.opplysninger.bekreftelse
        bekreftelser.removeIf { bekreftelse: JsonOkonomibekreftelse ->
            bekreftelse.type.equals(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE, ignoreCase = true)
        }
        bekreftelser
            .add(
                JsonOkonomibekreftelse()
                    .withKilde(JsonKilde.SYSTEM)
                    .withType(SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE)
                    .withVerdi(harSamtykke)
                    .withTittel("beskrivelse")
            )
    }

    private fun assertThatArbeidsforholdIsCorrectlyConverted(
        arbeidsforhold: Arbeidsforhold,
        jsonArbeidsforhold: JsonArbeidsforhold
    ) {
        assertThat(jsonArbeidsforhold.arbeidsgivernavn).isEqualTo(arbeidsforhold.arbeidsgivernavn)
        assertThat(jsonArbeidsforhold.fom).isEqualTo(arbeidsforhold.fom)
        assertThat(jsonArbeidsforhold.tom).isEqualTo(arbeidsforhold.tom)
        assertThat(jsonArbeidsforhold.stillingsprosent.toLong()).isEqualTo(arbeidsforhold.fastStillingsprosent)
        if (arbeidsforhold.harFastStilling == true) {
            assertThat(jsonArbeidsforhold.stillingstype).isEqualTo(Stillingstype.FAST)
        } else {
            assertThat(jsonArbeidsforhold.stillingstype).isEqualTo(Stillingstype.VARIABEL)
        }
    }

    companion object {
        private const val EIER = "12345678901"
        private val tom_lonnslipp = LocalDateTime.now().plusDays(40).format(DateTimeFormatter.ISO_DATE)
        private val tom_sluttoppgjor = LocalDateTime.now().plusDays(10).format(DateTimeFormatter.ISO_DATE)
        private val ARBEIDSFORHOLD_LONNSLIPP =
            Arbeidsforhold(null, "Good Corp.", "1337-01-01", tom_lonnslipp, 50L, true)
        private val ARBEIDSFORHOLD_SLUTTOPPGJOR =
            Arbeidsforhold(null, "Evil Corp.", "1337-02-02", tom_sluttoppgjor, 30L, false)
    }
}
