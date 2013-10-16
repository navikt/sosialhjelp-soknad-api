package no.nav.sbl.dialogarena.websoknad.config;

import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsumerConfigTest {

	@Bean
	public SendSoknadPortType sendSoknadService() {
//		return new SendSoknadPortTypeMock();
		return null;
	}

}
