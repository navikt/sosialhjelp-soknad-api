package no.nav.sbl.dialogarena.config;

import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.HenvendelseService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.EttersendingService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import javax.inject.Named;

@Configuration
public class DummyHolderConfig {

    @Bean
    public Kodeverk kodeverk() {
        return null;
    }

    @Bean
    public SoknadDataFletter soknadDataFletter() {
        return null;
    }

    @Bean
    public HenvendelseService henvendelseService() {
        return null;
    }

    @Bean
    public EttersendingService ettersendingService() {
        return null;
    }

    @Bean
    @Named("threadPoolTaskExecutor")
    public TaskExecutor threadPoolTaskExecutor() {
        return null;
    }

}
