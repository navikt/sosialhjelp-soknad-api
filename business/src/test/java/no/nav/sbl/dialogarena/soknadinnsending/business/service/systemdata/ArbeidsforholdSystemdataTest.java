package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.ArbeidsforholdService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.skatt.SkattbarInntektService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.utbetaling.UtbetalingService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.SLUTTOPPGJOER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArbeidsforholdSystemdataTest {

    private static final String EIER = "12345678901";

    private static final Arbeidsforhold ARBEIDSFORHOLD_LONNSLIPP = new Arbeidsforhold();
    private static final Arbeidsforhold ARBEIDSFORHOLD_SLUTTOPPGJOR = new Arbeidsforhold();
    private static final String tom_lonnslipp = LocalDateTime.now().plusDays(40).format(DateTimeFormatter.ISO_DATE);
    private static final String tom_sluttoppgjor = LocalDateTime.now().plusDays(10).format(DateTimeFormatter.ISO_DATE);

    static {
        ARBEIDSFORHOLD_LONNSLIPP.arbeidsgivernavn = "Good Corp.";
        ARBEIDSFORHOLD_LONNSLIPP.fom = "1337-01-01";
        ARBEIDSFORHOLD_LONNSLIPP.tom = tom_lonnslipp;
        ARBEIDSFORHOLD_LONNSLIPP.fastStillingsprosent = 50L;
        ARBEIDSFORHOLD_LONNSLIPP.harFastStilling = true;

        ARBEIDSFORHOLD_SLUTTOPPGJOR.arbeidsgivernavn = "Evil Corp.";
        ARBEIDSFORHOLD_SLUTTOPPGJOR.fom = "1337-02-02";
        ARBEIDSFORHOLD_SLUTTOPPGJOR.tom = tom_sluttoppgjor;
        ARBEIDSFORHOLD_SLUTTOPPGJOR.fastStillingsprosent = 30L;
        ARBEIDSFORHOLD_SLUTTOPPGJOR.harFastStilling = false;
    }

    @Mock
    private ArbeidsforholdService arbeidsforholdService;

    @Mock
    private TextService textService;

    @Mock
    private UtbetalingService utbetalingService;

    @Mock
    private SkattbarInntektService skattbarInntektService;

    @InjectMocks
    private ArbeidsforholdSystemdata arbeidsforholdSystemdata;

    @InjectMocks
    private SkattetatenSystemdata skattetatenSystemdata;

    @Test
    public void skalOppdatereArbeidsforhold() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER));
        List<Arbeidsforhold> arbeidsforholdList = Arrays.asList(ARBEIDSFORHOLD_LONNSLIPP, ARBEIDSFORHOLD_SLUTTOPPGJOR);
        when(arbeidsforholdService.hentArbeidsforhold(anyString())).thenReturn(arbeidsforholdList);
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn("tittel");

        arbeidsforholdSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonArbeidsforhold> jsonArbeidsforholdList = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid().getForhold();
        JsonArbeidsforhold jsonArbeidsforhold = jsonArbeidsforholdList.get(0);
        JsonArbeidsforhold jsonArbeidsforhold_2 = jsonArbeidsforholdList.get(1);

        assertThat(jsonArbeidsforhold.getKilde(), is(JsonKilde.SYSTEM));
        assertThat(jsonArbeidsforhold_2.getKilde(), is(JsonKilde.SYSTEM));
        assertThatArbeidsforholdIsCorrectlyConverted(ARBEIDSFORHOLD_LONNSLIPP, jsonArbeidsforhold);
        assertThatArbeidsforholdIsCorrectlyConverted(ARBEIDSFORHOLD_SLUTTOPPGJOR, jsonArbeidsforhold_2);
    }

    @Test
    public void skalLeggeTilInntektForLonnslipp() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
                .withSkattemeldingSamtykke(true);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setInntektFraSkatteetatenFeilet(true);
        List<Arbeidsforhold> arbeidsforholdList = Collections.singletonList(ARBEIDSFORHOLD_LONNSLIPP);
        when(arbeidsforholdService.hentArbeidsforhold(anyString())).thenReturn(arbeidsforholdList);
        String tittel = "tittel";
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn(tittel);
        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(null);
        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid, "");
        arbeidsforholdSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonOkonomioversiktInntekt inntekt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().getInntekt().get(0);

        assertThat(inntekt.getKilde(), is(JsonKilde.BRUKER));
        assertThat(inntekt.getType(), is(JOBB));
        assertThat(inntekt.getTittel(), is(tittel));
        assertThat(inntekt.getOverstyrtAvBruker(), is(false));
    }

    @Test
    public void skalLeggeTilUtbetalingForSluttoppgjor() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid()
                .withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
                .withSkattemeldingSamtykke(true);
        soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getDriftsinformasjon().setInntektFraSkatteetatenFeilet(true);
        List<Arbeidsforhold> arbeidsforholdList = Collections.singletonList(ARBEIDSFORHOLD_SLUTTOPPGJOR);
        when(arbeidsforholdService.hentArbeidsforhold(anyString())).thenReturn(arbeidsforholdList);
        String tittel = "tittel";
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn(tittel);
        when(skattbarInntektService.hentUtbetalinger(anyString())).thenReturn(null);
        skattetatenSystemdata.updateSystemdataIn(soknadUnderArbeid, "");
        arbeidsforholdSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        JsonOkonomiOpplysningUtbetaling utbetaling = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling().get(0);

        assertThat(utbetaling.getKilde(), is(JsonKilde.BRUKER));
        assertThat(utbetaling.getType(), is(SLUTTOPPGJOER));
        assertThat(utbetaling.getTittel(), is(tittel));
        assertThat(utbetaling.getOverstyrtAvBruker(), is(false));
    }

    @Test
    public void skalFjerneArbeidsforholdOgFjerneUtbetalingOgInntekt() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createSoknadUnderArbeidWithArbeidsforholdAndSluttOppgjorAndLonnslipp());
        when(arbeidsforholdService.hentArbeidsforhold(anyString())).thenReturn(new ArrayList<>());
        String tittel = "tittel";
        when(textService.getJsonOkonomiTittel(anyString())).thenReturn(tittel);

        arbeidsforholdSystemdata.updateSystemdataIn(soknadUnderArbeid, "");

        List<JsonArbeidsforhold> jsonArbeidsforholdList = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid().getForhold();
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        List<JsonOkonomioversiktInntekt> inntekter = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt().getInntekt();


        assertThat(jsonArbeidsforholdList.isEmpty(), is(true));
        assertThat(utbetalinger.isEmpty(), is(true));
        assertThat(inntekter.isEmpty(), is(true));
    }

    private JsonInternalSoknad createSoknadUnderArbeidWithArbeidsforholdAndSluttOppgjorAndLonnslipp() {
        JsonInternalSoknad jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER);
        jsonInternalSoknad.getSoknad().getData().getArbeid().getForhold().add(new JsonArbeidsforhold());
        jsonInternalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling().add(new JsonOkonomiOpplysningUtbetaling().withType(SLUTTOPPGJOER));
        jsonInternalSoknad.getSoknad().getData().getOkonomi().getOversikt().getInntekt().add(new JsonOkonomioversiktInntekt().withType(JOBB));
        return jsonInternalSoknad;
    }

    private void assertThatArbeidsforholdIsCorrectlyConverted(Arbeidsforhold arbeidsforhold, JsonArbeidsforhold jsonArbeidsforhold) {
        assertThat("arbeidsgivernavn", jsonArbeidsforhold.getArbeidsgivernavn(), is(arbeidsforhold.arbeidsgivernavn));
        assertThat("fom", jsonArbeidsforhold.getFom(), is(arbeidsforhold.fom));
        assertThat("tom", jsonArbeidsforhold.getTom(), is(arbeidsforhold.tom));
        assertThat("stillingsprosent", new Long(jsonArbeidsforhold.getStillingsprosent()), is(arbeidsforhold.fastStillingsprosent));
        if (arbeidsforhold.harFastStilling) {
            assertThat("harFastStilling", jsonArbeidsforhold.getStillingstype(), is(JsonArbeidsforhold.Stillingstype.FAST));
        } else {
            assertThat("harFastStilling", jsonArbeidsforhold.getStillingstype(), is(JsonArbeidsforhold.Stillingstype.VARIABEL));
        }
    }
}
