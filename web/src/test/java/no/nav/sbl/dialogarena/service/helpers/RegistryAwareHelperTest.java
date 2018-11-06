package no.nav.sbl.dialogarena.service.helpers;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
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

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.internal.Files;
import com.github.jknack.handlebars.io.StringTemplateSource;
import com.github.jknack.handlebars.io.TemplateSource;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.service.CmsTekst;
import no.nav.sbl.dialogarena.service.HandlebarRegistry;
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
    @Ignore("Ignoreres inntil det har blitt vurdert om dette har verdi eller ikke.")
    // TODO Oskar før pull request.
    public void skrivRegistrerteHelpersTilReadme() throws Exception {
        List<Map<String, String>> helpersListe = new ArrayList<>();

        for (RegistryAwareHelper helper : helpers) {
            HashMap<String, String> helperInformasjon = new HashMap<>();
            helperInformasjon.put(NAVN, helper.getNavn());
            helperInformasjon.put("beskrivelse", helper.getBeskrivelse());
            helperInformasjon.put("eksempel", hentEksempelfil(helper.getNavn()));
            helpersListe.add(helperInformasjon);
        }

        Collections.sort(helpersListe, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                return o1.get(NAVN).compareTo(o2.get(NAVN));
            }
        });

        Map<String, List> handlebarsObject = new HashMap();
        handlebarsObject.put("helpers", helpersListe);
        Handlebars handlebars = new Handlebars();
        TemplateSource utf8TemplateSource = new StringTemplateSource("Handlebars-helpers.hbs", Files.read("/readme/Handlebars-helpers.hbs"));
        String apply = handlebars.compile(utf8TemplateSource).apply(handlebarsObject);

        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("Handlebars-helpers.md"), "UTF-8");
        writer.write(apply);
        writer.close();
    }

    private String hentEksempelfil(String name) {
        URL url = this.getClass().getResource("/readme/" + name + ".hbs");
        try {
            return FileUtils.readFileToString(new File(url.toURI()), "UTF-8");
        } catch (Exception e) {
            fail("Helperen " + name + " har ingen eksempelfil under /readme. Det må finnes en hbs-fil med dette navnet her.");
        }
        return "";
    }

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