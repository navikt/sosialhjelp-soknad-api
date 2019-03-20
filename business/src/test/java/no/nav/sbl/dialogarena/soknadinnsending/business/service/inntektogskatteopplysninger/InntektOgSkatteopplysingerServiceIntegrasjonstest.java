package no.nav.sbl.dialogarena.soknadinnsending.business.service.inntektogskatteopplysninger;

import no.nav.sbl.dialogarena.sendsoknad.domain.inntektsogskatteopplysninger.InntektOgskatteopplysningerConsumer;
import no.nav.sbl.dialogarena.soknadinnsending.business.SoknadDataFletterIntegrationTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig.InntektsOgSkatteopplysningerRestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
//hvordan sette opp context
@ContextConfiguration(classes = SoknadDataFletterIntegrationTestContext.class)
public class InntektOgSkatteopplysingerServiceIntegrasjonstest {

    @Inject
    private InntektsOgSkatteopplysningerRestConfig config;

//    @Test
//    public void skalReturnereInformasjonFraSkatteetaten() {
//        config.endpoint = "https://api-gw-q1.adeo.no/ekstern/skatt/datasamarbeid/api/innrapportert/inntektsmottaker";
//        InntektOgskatteopplysningerConsumer inntektOgskatteopplysningerConsumer = config.inntektOgskatteopplysningerConsumer();
//
//        InntektOgskatteopplysningerConsumer.InntektOgskatteopplysningerRespons repons
//                = inntektOgskatteopplysningerConsumer.sok(
//                        new InntektOgskatteopplysningerConsumer.Sokedata().withIdentifikator("01029413157").withFom(LocalDate.now().minusMonths(3)).withTom(LocalDate.now()));
//
//        System.out.println(repons);
//
//        InntektOgskatteopplysningerConsumer.OppgaveInntektsmottaker oppgaveInntektsmottaker = repons.oppgaveInntektsmottaker.get(0);
//        assertThat(oppgaveInntektsmottaker.inntekt);
//    }
}
