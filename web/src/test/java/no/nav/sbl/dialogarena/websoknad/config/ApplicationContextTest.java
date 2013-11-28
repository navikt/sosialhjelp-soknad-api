package no.nav.sbl.dialogarena.websoknad.config;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static no.nav.sbl.dialogarena.test.SystemProperties.setFrom;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationContext.class})
public class ApplicationContextTest {


    @BeforeClass
    public static void setupStatic() {
        setFrom("jetty-env.properties");
       
    }

    @Test
    public void shouldSetupAppContext() { }

}