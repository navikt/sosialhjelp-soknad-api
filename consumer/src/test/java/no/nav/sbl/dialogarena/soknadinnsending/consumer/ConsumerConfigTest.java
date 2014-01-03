package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class ConsumerConfigTest {

    @Bean
    public SendSoknadPortType sendSoknadService() {
//		return new SendSoknadPortTypeMock();
        return mock(SendSoknadPortType.class);
    }

    @Bean
    public FilLagerPortType fillagerService() {
//		return new SendSoknadPortTypeMock();
        return mock(FilLagerPortType.class);
    }

}
