package no.nav.sbl.dialogarena.service.helpers;

import com.github.jknack.handlebars.Handlebars;
import no.nav.sbl.dialogarena.config.HandlebarsHelperConfig;
import no.nav.sbl.dialogarena.service.HandlebarRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {RegistryAwareHelperTest.HandlebarsHelperTestConfig.class, HandlebarsHelperConfig.class})
public class RegistryAwareHelperTest {

    private static final Logger LOG = LoggerFactory.getLogger(RegistryAwareHelperTest.class);

    @Inject
    List<RegistryAwareHelper> helpers;

    @Inject
    HandlebarRegistry registry;

    @Test
    public void listUtRegisterteHelpers() throws Exception {
        for (RegistryAwareHelper helper : helpers) {
            LOG.info("Helper: "+ helper.getName());
        }
    }

    @Test
    public void skrivRegisterteHelpersTilReadme() throws Exception {
        List<String> list = new ArrayList<>();
        for (RegistryAwareHelper helper : helpers) {
            list.add(helper.getName());
        }
        Collections.sort(list);
        Map<String, List> map = new HashMap();
        map.put("helpers", list);
        Handlebars handlebars = new Handlebars();
        String apply = handlebars.compile("/readme/Handlebars-helpers").apply(map);

        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("Handlebars-helpers.md"), "UTF-8");
        writer.write(apply);
        writer.close();
    }

    @Test
    public void registryKalltMedHelper() throws Exception {
        verify(registry, atLeastOnce()).registrerHelper(eq(VariabelHelper.NAME), any(VariabelHelper.class));
        verify(registry, atLeastOnce()).registrerHelper(anyString(), any(RegistryAwareHelper.class));
    }



    public static class HandlebarsHelperTestConfig {

        @Bean
        public HandlebarRegistry handleBarKjoerer(){
            return mock(HandlebarRegistry.class);
        }

    }



}