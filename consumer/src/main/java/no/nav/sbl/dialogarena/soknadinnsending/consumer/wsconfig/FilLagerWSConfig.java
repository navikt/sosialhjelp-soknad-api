package no.nav.sbl.dialogarena.soknadinnsending.consumer.wsconfig;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.ServiceBuilder;
import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.FilLagerPortType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilLagerWSConfig {
    @Value("${soknad.webservice.henvendelse.fillager.url}")
    private String serviceEndpoint;

    private ServiceBuilder<FilLagerPortType>.PortTypeBuilder<FilLagerPortType> factory() {
        return new ServiceBuilder<>(FilLagerPortType.class)
                .asStandardService()
                .withAddress(serviceEndpoint)
                .withWsdl("classpath:FilLager.wsdl")
                .build()
                .withHttpsMock();
    }

    @Bean
    public FilLagerPortType fillagerEndpoint() {
        return factory().withMDC().withUserSecurity().get();
    }

    @Bean
    public FilLagerPortType fillagerSelftestEndpoint() {
        return factory().withSystemSecurity().get();
    }

    @Bean
    public Pingable fillagerPing() {
        return new Pingable() {
            @Override
            public Ping ping() {
                PingMetadata metadata = new PingMetadata(serviceEndpoint,"Fillager v1 - Lagring mot henvendelse", true);
                try {
                    fillagerSelftestEndpoint().ping();
                    return Pingable.Ping.lyktes(metadata);
                } catch (Exception e) {
                    return Pingable.Ping.feilet(metadata, e);
                }            }
        };
    }
}
