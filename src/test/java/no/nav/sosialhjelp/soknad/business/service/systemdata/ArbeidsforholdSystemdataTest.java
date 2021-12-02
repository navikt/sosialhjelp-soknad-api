package no.nav.sosialhjelp.soknad.business.service.systemdata;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomibekreftelse;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sosialhjelp.soknad.arbeid.ArbeidsforholdService;
import no.nav.sosialhjelp.soknad.arbeid.domain.Arbeidsforhold;
import no.nav.sosialhjelp.soknad.business.service.TextService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkattbarInntektService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.SLUTTOPPGJOER;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.UTBETALING_SKATTEETATEN_SAMTYKKE;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArbeidsforholdSystemdataTest {

    private static final String EIER = "12345678901";

    private static final String tom_lonnslipp = LocalDateTime.now().plusDays(40).format(DateTimeFormatter.ISO_DATE);
    private static final String tom_sluttoppgjor = LocalDateTime.now().plusDays(10).format(DateTimeFormatter.ISO_DATE);

    private static final Arbeidsforhold ARBEIDSFORHOLD_LONNSLIPP = new Arbeidsforhold(null, "Good Corp.", "1337-01-01", tom_lonnslipp, 50L, true);
    private static final Arbeidsforhold ARBEIDSFORHOLD_SLUTTOPPGJOR = new Arbeidsforhold(null, "Evil Corp.", "1337-02-02", tom_sluttoppgjor, 30L, false);

    @Mock
    private ArbeidsforholdService arbeidsforholdService;

    @Mock
    private TextService textService;

    @Mock
    private SkattbarInntektService skattbarInntektService;

    @InjectMocks
    private ArbeidsforholdSystemdata arbeidsforholdSystemdata;

    @InjectMocks
    private SkattetatenSystemdata skattetatenSystemdata;

    @Test
    void skalOppdatereArbeidsforhold() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER)).withEier(EIER);
        List<Arbeidsforhold> arbeidsforholdList = Arrays.asList(ARBEIDSFORHOLD_LONNSLIPP, ARBEIDSFORHOLD_SLUTTOPPGJOR);
        when(arbeidsforholdService.hentArbeidsforhold(anyString())).thenReturn(arbeidsforholdList);
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");

        arbeidsforholdSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonArbeidsforhold> jsonArbeidsforholdList = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid().getForhold();
        JsonArbeidsforhold jsonArbeidsforhold = jsonArbeidsforholdList.get(0);
        JsonArbeidsforhold jsonArbeidsforhold_2 = jsonArbeidsforholdList.get(1);

        assertThat(jsonArbeidsforhold.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThat(jsonArbeidsforhold_2.getKilde()).isEqualTo(JsonKilde.SYSTEM);
        assertThatArbeidsforholdIsCorrectlyConverted(ARBEIDSFORHOLD_LONNSLIPP, jsonArbeidsforhold);
        assertThatArbeidsforholdIsCorrectlyConverted(ARBEIDSFORHOLD_SLUTTOPPGJOR, jsonArbeidsforhold_2);
    }

    @Test
    void skalLeggeTilInntektForLonnslipp() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
                .withEier(EIER);
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setInntektFraSkatteetatenFeilet(true);
        List<Arbeidsforhold> arbeidsforholdList = Collections.singletonList(ARBEIDSFORHOLD_LONNSLIPP);
        when(arbeidsforholdService.hentArbeidsforhold(anyString())).thenReturn(arbeidsforholdList);
        String tittel = "tittel";
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn(tittel);
        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(null);
        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);
        arbeidsforholdSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonOkonomioversiktInntekt inntekt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().getInntekt().get(0);

        assertThat(inntekt.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(inntekt.getType()).isEqualTo(JOBB);
        assertThat(inntekt.getTittel()).isEqualTo(tittel);
        assertThat(inntekt.getOverstyrtAvBruker()).isFalse();
    }

    @Test
    void skalLeggeTilUtbetalingForSluttoppgjor() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
                .withEier(EIER);
        setSamtykke(soknadUnderArbeid.getJsonInternalSoknad(), true);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setInntektFraSkatteetatenFeilet(true);
        List<Arbeidsforhold> arbeidsforholdList = Collections.singletonList(ARBEIDSFORHOLD_SLUTTOPPGJOR);
        when(arbeidsforholdService.hentArbeidsforhold(anyString())).thenReturn(arbeidsforholdList);
        String tittel = "tittel";
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn(tittel);
        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(null);
        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid);
        arbeidsforholdSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonOkonomiOpplysningUtbetaling utbetaling = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling().get(0);

        assertThat(utbetaling.getKilde()).isEqualTo(JsonKilde.BRUKER);
        assertThat(utbetaling.getType()).isEqualTo(SLUTTOPPGJOER);
        assertThat(utbetaling.getTittel()).isEqualTo(tittel);
        assertThat(utbetaling.getOverstyrtAvBruker()).isFalse();
    }

    @Test
    void skalFjerneArbeidsforholdOgFjerneUtbetalingOgInntekt() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createSoknadUnderArbeidWithArbeidsforholdAndSluttOppgjorAndLonnslipp());

        arbeidsforholdSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonArbeidsforhold> jsonArbeidsforholdList = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid().getForhold();
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        List<JsonOkonomioversiktInntekt> inntekter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().getInntekt();

        assertThat(jsonArbeidsforholdList).isEmpty();
        assertThat(utbetalinger).isEmpty();
        assertThat(inntekter).isEmpty();
    }

    private JsonInternalSoknad createSoknadUnderArbeidWithArbeidsforholdAndSluttOppgjorAndLonnslipp() {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        jsonInternalSoknad.getSoknad().getData().getArbeid().getForhold().add(new JsonArbeidsforhold());
        jsonInternalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling().add(new JsonOkonomiOpplysningUtbetaling().withType(SLUTTOPPGJOER));
        jsonInternalSoknad.getSoknad().getData().getOkonomi().getOversikt().getInntekt().add(new JsonOkonomioversiktInntekt().withType(JOBB));
        return jsonInternalSoknad;
    }

    private void setSamtykke(JsonInternalSoknad jsonInternalSoknad, boolean harSamtykke) {
        List<JsonOkonomibekreftelse> bekreftelser = jsonInternalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getBekreftelse();
        bekreftelser.removeIf(bekreftelse -> bekreftelse.getType().equalsIgnoreCase(UTBETALING_SKATTEETATEN_SAMTYKKE));
        bekreftelser
                .add(new JsonOkonomibekreftelse().withKilde(JsonKilde.SYSTEM)
                        .withType(UTBETALING_SKATTEETATEN_SAMTYKKE)
                        .withVerdi(harSamtykke)
                        .withTittel("beskrivelse"));
    }


    private void assertThatArbeidsforholdIsCorrectlyConverted(Arbeidsforhold arbeidsforhold, JsonArbeidsforhold jsonArbeidsforhold) {
        assertThat(jsonArbeidsforhold.getArbeidsgivernavn()).isEqualTo(arbeidsforhold.getArbeidsgivernavn());
        assertThat(jsonArbeidsforhold.getFom()).isEqualTo(arbeidsforhold.getFom());
        assertThat(jsonArbeidsforhold.getTom()).isEqualTo(arbeidsforhold.getTom());
        assertThat(Long.valueOf(jsonArbeidsforhold.getStillingsprosent())).isEqualTo(arbeidsforhold.getFastStillingsprosent());
        if (arbeidsforhold.getHarFastStilling()) {
            assertThat(jsonArbeidsforhold.getStillingstype()).isEqualTo(JsonArbeidsforhold.Stillingstype.FAST);
        } else {
            assertThat(jsonArbeidsforhold.getStillingstype()).isEqualTo(JsonArbeidsforhold.Stillingstype.VARIABEL);
        }
    }
}
