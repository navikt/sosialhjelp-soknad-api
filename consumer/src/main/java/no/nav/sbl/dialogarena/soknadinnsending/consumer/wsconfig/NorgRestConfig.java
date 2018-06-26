package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.sendsoknad.domain.norg.NorgConsumer;
import no.nav.sbl.dialogarena.sendsoknad.mockmodul.norg.NorgConsumerMock;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.norg.NorgConsumerImpl;
import no.nav.sbl.rest.RestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.sbl.dialogarena.common.cxf.InstanceSwitcher.createSwitcher;


@Configuration
public class NorgRestConfig {

    public static final String NORG_KEY = "start.norg.withmock";

    @Value("${norg.rest.url}")
    private String endpoint;

    @Bean
    public NorgConsumer norgConsumer() {
        NorgConsumer prod = new NorgConsumerImpl(RestUtils.createClient(), endpoint);
        NorgConsumer mock = new NorgConsumerMock();
        return createSwitcher(prod, mock, NORG_KEY, NorgConsumer.class);
    }
}
