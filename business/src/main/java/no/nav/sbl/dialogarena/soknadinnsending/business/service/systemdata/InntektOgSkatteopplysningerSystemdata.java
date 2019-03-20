package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.inntektsogskatteopplysninger.InntektOgskatteopplysningerConsumer;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;

@Component
public class InntektOgSkatteopplysningerSystemdata implements Systemdata {

    @Inject
    private InntektOgskatteopplysningerConsumer inntektOgskatteopplysningerConsumer;

    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        JsonOkonomioversikt jsonOkonomioversikt = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt();
        String identifikator = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia().getPersonIdentifikator().getVerdi();
        innhentInntektOgSkatteopplysninger(identifikator);

//        jsonOkonomioversikt.setInntekt(innhentInntektOgSkatteopplysninger(identifikator));
    }

    public void innhentInntektOgSkatteopplysninger(String identifikator) {
        InntektOgskatteopplysningerConsumer.Sokedata sokedata = new InntektOgskatteopplysningerConsumer.Sokedata()
                .withIdentifikator(identifikator)
                .withTom(LocalDate.now())
                .withFom(LocalDate.now().minusMonths(3));

      //  InntektOgskatteopplysningerConsumer.InntektOgskatteopplysningerRespons inntektOgskatteopplysningerRespons = inntektOgskatteopplysningerConsumer.sok(sokedata);

    }
}
