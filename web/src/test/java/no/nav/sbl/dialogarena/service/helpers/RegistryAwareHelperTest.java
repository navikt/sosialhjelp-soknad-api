package no.nav.sbl.dialogarena.service.helpers;

import no.nav.sbl.dialogarena.config.HandlebarsHelperConfig;
import no.nav.sbl.dialogarena.service.HandlebarRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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