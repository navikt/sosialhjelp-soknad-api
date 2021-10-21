package no.nav.sosialhjelp.soknad.business.pdf.helpers;

import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.pdf.CmsTekst;
import no.nav.sosialhjelp.soknad.business.pdf.HandlebarRegistry;
import no.nav.sosialhjelp.soknad.business.service.Miljovariabler;
import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("rawtypes")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RegistryAwareHelperTest.HandlebarsHelperTestConfig.class})
@ActiveProfiles("RegistryTest")
class RegistryAwareHelperTest {

    private static final Logger LOG = LoggerFactory.getLogger(RegistryAwareHelperTest.class);
    @Inject
    private List<RegistryAwareHelper> helpers;

    @Inject
    private HandlebarRegistry registry;

    @Test
    void listUtRegistrerteHelpers() {
        for (RegistryAwareHelper helper : helpers) {
            LOG.info("Helper: " + helper.getNavn());
        }
    }

    @Test
    void registryKaltMedHelper() {
        verify(registry, atLeastOnce()).registrerHelper(eq(ConcatHelper.NAVN), any(ConcatHelper.class));
        verify(registry, atLeastOnce()).registrerHelper(anyString(), any(RegistryAwareHelper.class));
    }

    @Configuration
    @ComponentScan(basePackageClasses = ConcatHelper.class)
    @Import({
            CmsTekst.class
    })
    @Profile("RegistryTest")
    public static class HandlebarsHelperTestConfig {

        @Bean
        public HandlebarRegistry register() {
            return mock(HandlebarRegistry.class);
        }

        @Bean
        public NavMessageSource navMessageSource() {
            return mock(NavMessageSource.class);
        }

        @Bean
        public KodeverkService kodeverkService() {
            return mock(KodeverkService.class);
        }

        @Bean
        public Miljovariabler informasjonService() {
            return mock(Miljovariabler.class);
        }

        @Bean
        public SoknadMetadataRepository soknadMetadataRepository() {
            return mock(SoknadMetadataRepository.class);
        }

        @Bean
        public Clock clock() {
            return Clock.systemDefaultZone();
        }

    }
}