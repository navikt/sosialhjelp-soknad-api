package no.nav.sbl.dialogarena.config;

import no.nav.sosialhjelp.soknad.business.service.soknadservice.EttersendingService;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

import javax.inject.Named;

@Configuration
public class DummyHolderConfig {

    @Bean
    public SoknadService soknadService() {
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
