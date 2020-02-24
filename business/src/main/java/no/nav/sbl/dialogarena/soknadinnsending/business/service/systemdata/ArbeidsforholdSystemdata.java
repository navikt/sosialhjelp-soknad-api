package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.ArbeidsforholdService;
import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.*;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.TitleKeyMapper.soknadTypeToTitleKey;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.ArbeidsforholdService.Sokeperiode;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.JOBB;
import static no.nav.sbl.soknadsosialhjelp.json.SoknadJsonTyper.SLUTTOPPGJOER;

@Component
public class ArbeidsforholdSystemdata implements Systemdata {

    private static final Logger LOG = LoggerFactory.getLogger(ArbeidsforholdSystemdata.class);

    @Inject
    private ArbeidsforholdService arbeidsforholdService;

    @Inject
    private TextService textService;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid, String token) {
        String eier = soknadUnderArbeid.getEier();
        JsonInternalSoknad internalSoknad = soknadUnderArbeid.getJsonInternalSoknad();
        internalSoknad.getSoknad().getData().getArbeid().setForhold(innhentSystemArbeidsforhold(eier));

        updateVedleggForventninger(internalSoknad, textService);
    }

    public static void updateVedleggForventninger(JsonInternalSoknad internalSoknad, TextService textService) {
        List<JsonOkonomiOpplysningUtbetaling> utbetalinger = internalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getUtbetaling();
        List<JsonOkonomioversiktInntekt> inntekter = internalSoknad.getSoknad().getData().getOkonomi().getOversikt().getInntekt();
        List<JsonVedlegg> jsonVedleggs = VedleggsforventningMaster.finnPaakrevdeVedleggForArbeid(internalSoknad);

        if (typeIsInList(jsonVedleggs, "sluttoppgjor")){
            String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(SLUTTOPPGJOER));
            addUtbetalingIfNotPresentInOpplysninger(utbetalinger, SLUTTOPPGJOER, tittel);
        } else {
            removeUtbetalingIfPresentInOpplysninger(utbetalinger, SLUTTOPPGJOER);
        }

        if (typeIsInList(jsonVedleggs, "lonnslipp")){
            String tittel = textService.getJsonOkonomiTittel(soknadTypeToTitleKey.get(JOBB));
            addInntektIfNotPresentInOversikt(inntekter, JOBB, tittel);
        } else {
            removeInntektIfPresentInOversikt(inntekter, JOBB);
        }
    }

    private static boolean typeIsInList(List<JsonVedlegg> jsonVedleggs, String vedleggstype) {
        return jsonVedleggs.stream().anyMatch(jsonVedlegg -> jsonVedlegg.getType().equals(vedleggstype));
    }

    public List<JsonArbeidsforhold> innhentSystemArbeidsforhold(final String personIdentifikator) {
        Sokeperiode sokeperiode = getSoekeperiode();
        List<Arbeidsforhold> arbeidsforholds;
        try {
            arbeidsforholds = arbeidsforholdService.hentArbeidsforhold(personIdentifikator, sokeperiode);
        } catch (Exception e) {
            LOG.warn("Kunne ikke hente arbeidsforhold: " + e, e);
            arbeidsforholds = null;
        }

        if (arbeidsforholds == null){
            return null;
        }
        return arbeidsforholds.stream()
                .map(this::mapToJsonArbeidsforhold)
                .collect(Collectors.toList());
    }

    private JsonArbeidsforhold mapToJsonArbeidsforhold(Arbeidsforhold arbeidsforhold) {
        return new JsonArbeidsforhold()
                .withArbeidsgivernavn(arbeidsforhold.arbeidsgivernavn)
                .withFom(arbeidsforhold.fom)
                .withTom(arbeidsforhold.tom)
                .withKilde(JsonKilde.SYSTEM)
                .withStillingsprosent(Math.toIntExact(arbeidsforhold.fastStillingsprosent))
                .withStillingstype(tilJsonStillingstype(arbeidsforhold.harFastStilling))
                .withOverstyrtAvBruker(Boolean.FALSE);
    }

    private ArbeidsforholdService.Sokeperiode getSoekeperiode() {
        return new ArbeidsforholdService.Sokeperiode(new DateTime().minusMonths(3), new DateTime());
    }

    private static JsonArbeidsforhold.Stillingstype tilJsonStillingstype(boolean harFastStilling) {
        return harFastStilling ? JsonArbeidsforhold.Stillingstype.FAST : JsonArbeidsforhold.Stillingstype.VARIABEL;
    }

}
