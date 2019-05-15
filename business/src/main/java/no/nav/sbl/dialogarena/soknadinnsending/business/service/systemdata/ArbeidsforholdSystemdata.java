package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService;
import no.nav.sbl.soknadsosialhjelp.json.VedleggsforventningMaster;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.FaktumNoklerOgBelopNavnMapper.soknadTypeToFaktumKey;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.*;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService.Sokeperiode;

@Component
public class ArbeidsforholdSystemdata implements Systemdata {

    @Inject
    private ArbeidsforholdService arbeidsforholdService;

    @Inject
    private TextService textService;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        final String eier = soknadUnderArbeid.getEier();
        final JsonData jsonData = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData();
        jsonData.getArbeid().setForhold(innhentSystemArbeidsforhold(eier));

        updateVedleggForventninger(jsonData);
    }

    private void updateVedleggForventninger(JsonData jsonData) {
        final List<JsonOkonomiOpplysningUtbetaling> utbetalinger = jsonData.getOkonomi().getOpplysninger().getUtbetaling();
        final List<JsonOkonomioversiktInntekt> inntekter = jsonData.getOkonomi().getOversikt().getInntekt();
        List<JsonVedlegg> jsonVedleggs = VedleggsforventningMaster.finnPaakrevdeVedleggForArbeid(jsonData.getArbeid());

        String soknadstype = "sluttoppgjoer";
        if (typeIsInList(jsonVedleggs, "sluttoppgjor")){
            final String tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(soknadstype));
            addUtbetalingIfNotPresentInOpplysninger(utbetalinger, soknadstype, tittel);
        } else {
            removeUtbetalingIfPresentInOpplysninger(utbetalinger, soknadstype);
        }

        soknadstype = "jobb";
        if (typeIsInList(jsonVedleggs, "lonnslipp")){
            final String tittel = textService.getJsonOkonomiTittel(soknadTypeToFaktumKey.get(soknadstype));
            addInntektIfNotPresentInOversikt(inntekter, soknadstype, tittel);
        } else {
            removeInntektIfPresentInOversikt(inntekter, soknadstype);
        }
    }

    private boolean typeIsInList(List<JsonVedlegg> jsonVedleggs, String vedleggstype) {
        return jsonVedleggs.stream().anyMatch(jsonVedlegg -> jsonVedlegg.getType().equals(vedleggstype));
    }

    public List<JsonArbeidsforhold> innhentSystemArbeidsforhold(final String personIdentifikator) {
        Sokeperiode sokeperiode = getSoekeperiode();
        List<Arbeidsforhold> arbeidsforholds = arbeidsforholdService.hentArbeidsforhold(personIdentifikator, sokeperiode);

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
        return new ArbeidsforholdService.Sokeperiode(new DateTime().minusMonths(10), new DateTime());
    }

    private static JsonArbeidsforhold.Stillingstype tilJsonStillingstype(boolean harFastStilling) {
        return harFastStilling ? JsonArbeidsforhold.Stillingstype.FAST : JsonArbeidsforhold.Stillingstype.VARIABEL;
    }

}
