package no.nav.sosialhjelp.soknad.consumer.restconfig;

import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlHentPersonConsumer;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlHentPersonConsumerImpl;
import no.nav.sosialhjelp.soknad.consumer.sts.apigw.STSConsumer;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable;
import no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.PingMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.feilet;
import static no.nav.sosialhjelp.soknad.web.selftest.Pingable.Ping.lyktes;

public class PdlHentPersonRestConfig extends PdlConfig {

    @Value("${pdl_api_url}")
    private String endpoint;

    @Bean
    public PdlHentPersonConsumer pdlHentPersonConsumer(STSConsumer stsConsumer) {
        return new PdlHentPersonConsumerImpl(pdlClient(), endpoint, stsConsumer);
    }

    // Trenger kun en ping mot PDL
    @Bean
    public Pingable pdlRestPing(PdlHentPersonConsumer pdlHentPersonConsumer) {
        return () -> {
            var metadata = new PingMetadata(endpoint, "Pdl", false);
            try {
                pdlHentPersonConsumer.ping();
                return lyktes(metadata);
            } catch (Exception e) {
                return feilet(metadata, e);
            }
        };
    }
}
