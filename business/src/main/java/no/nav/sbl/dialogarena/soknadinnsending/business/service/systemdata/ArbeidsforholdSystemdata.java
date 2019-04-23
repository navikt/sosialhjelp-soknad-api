package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService.Sokeperiode;

@Component
public class ArbeidsforholdSystemdata implements Systemdata {

    @Inject
    private ArbeidsforholdService arbeidsforholdService;


    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        String eier = soknadUnderArbeid.getEier();
        JsonArbeid arbeid = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getArbeid();
        arbeid.setForhold(innhentSystemArbeidsforhold(eier));
    }

    public List<JsonArbeidsforhold> innhentSystemArbeidsforhold(String personIdentifikator) {
        Sokeperiode sokeperiode = getSoekeperiode();
        List<Arbeidsforhold> arbeidsforholds = arbeidsforholdService.hentArbeidsforhold(personIdentifikator, sokeperiode);

        if (arbeidsforholds == null){
            return null;
        }
        return arbeidsforholds.stream()
                .map(arbeidsforhold -> mapToJsonArbeidsforhold(arbeidsforhold))
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
        return new ArbeidsforholdService.Sokeperiode(OffsetDateTime.now().minusMonths(10), OffsetDateTime.now());
    }

    private static JsonArbeidsforhold.Stillingstype tilJsonStillingstype(boolean harFastStilling) {
        return harFastStilling ? JsonArbeidsforhold.Stillingstype.FAST : JsonArbeidsforhold.Stillingstype.VARIABEL;
    }

}
