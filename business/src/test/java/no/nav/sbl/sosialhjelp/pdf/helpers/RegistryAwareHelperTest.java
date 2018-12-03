package no.nav.sbl.sosialhjelp.pdf.helpers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.util.List;

import javax.inject.Inject;

import no.nav.sbl.sosialhjelp.pdf.CmsTekst;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.sosialhjelp.pdf.HandlebarRegistry;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.Miljovariabler;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RegistryAwareHelperTest.HandlebarsHelperTestConfig.class})
@ActiveProfiles("RegistryTest")
public class RegistryAwareHelperTest {



    private static final Logger LOG = LoggerFactory.getLogger(RegistryAwareHelperTest.class);
    public static final String NAVN = "navn";

    @Inject
    List<RegistryAwareHelper> helpers;

    @Inject
    HandlebarRegistry registry;

    @Test
    public void listUtRegistrerteHelpers() throws Exception {
        for (RegistryAwareHelper helper : helpers) {
            LOG.info("Helper: " + helper.getNavn());
        }
    }

    /*
    * Denne testen har ingen assertions, men genererer fila Handlebars-helpers.md.
    * Se beskrivelse i Handlebars-helpers.md for å få bakgrunn for testen.
    * */


    @Test
    public void registryKaltMedHelper() throws Exception {
        verify(registry, atLeastOnce()).registrerHelper(eq(VariabelHelper.NAVN), any(VariabelHelper.class));
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

        @Bean()
        public KravdialogInformasjonHolder kravdialogInformasjonHolder() {
            return mock(KravdialogInformasjonHolder.class);
        }

        @Bean()
        public Kodeverk kodeverk() {
            return mock(Kodeverk.class);
        }

        @Bean()
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