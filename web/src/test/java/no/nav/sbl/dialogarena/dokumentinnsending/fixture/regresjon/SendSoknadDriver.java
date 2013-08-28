package no.nav.sbl.dialogarena.dokumentinnsending.fixture.regresjon;

import no.nav.modig.test.fitnesse.fixture.SpringAwareDoFixture;
import no.nav.sbl.dialogarena.dokumentinnsending.config.FitNesseApplicationConfig;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = FitNesseApplicationConfig.class)
public class SendSoknadDriver extends SpringAwareDoFixture {

    public SendSoknadDriver() throws Exception {
        super.setUp();
    }

    public long startSoeknad() {
        return 1L;
    }
}