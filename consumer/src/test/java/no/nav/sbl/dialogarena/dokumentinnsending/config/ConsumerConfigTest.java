package no.nav.sbl.dialogarena.dokumentinnsending.config;

import no.nav.sbl.dialogarena.common.kodeverk.config.KodeverkConfig;
import no.nav.sbl.dialogarena.dokumentinnsending.kodeverk.KodeverkIntegrasjon;
import no.nav.sbl.dialogarena.dokumentinnsending.repository.SoknadRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = KodeverkConfig.class)
public class ConsumerConfigTest {

    @Bean
    public SoknadRepository soknadRepository() {
        return new SoknadRepository();
    }

    @Bean
    public KodeverkIntegrasjon kodeverkClient() {
        return new KodeverkIntegrasjon();
    }
    
}