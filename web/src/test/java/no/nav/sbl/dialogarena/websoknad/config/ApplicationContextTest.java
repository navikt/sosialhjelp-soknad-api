package no.nav.sbl.dialogarena.websoknad.config;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.test.SystemProperties.setFrom;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationContext.class})
public class ApplicationContextTest {


    @BeforeClass
    public static void setupStatic() {
        setFrom("jetty-env.properties");
        //Properties target = System.getProperties();
        
        ///target.setProperty("no.nav.modig.security.sts.url", "http://localhost:8080/SecurityTokenServiceProvider/");
        setProperty("no.nav.modig.security.sts.url", "http://localhost:8080/SecurityTokenServiceProvider/");
        setProperty("no.nav.modig.security.systemuser.username", "BD05");
        setProperty("no.nav.modig.security.systemuser.password", "test");
        setProperty("org.apache.cxf.stax.allowInsecureParser", "true");
    }

    @Test
    public void shouldSetupAppContext() { }

}