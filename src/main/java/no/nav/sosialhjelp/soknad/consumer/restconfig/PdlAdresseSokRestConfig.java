package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.consumer.pdl.adressesok.PdlAdresseSokConsumer;
import no.nav.sosialhjelp.soknad.consumer.sts.apigw.STSConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class PdlAdresseSokRestConfig extends PdlConfig {

    @Value("${pdl_api_url}")
    private String endpoint;

    @Bean
    public PdlAdresseSokConsumer pdlAdresseSokConsumer(STSConsumer stsConsumer) {
        return new PdlAdresseSokConsumer(pdlClient(), endpoint, stsConsumer);
    }

}
