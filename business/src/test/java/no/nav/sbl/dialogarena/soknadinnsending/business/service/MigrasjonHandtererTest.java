package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MigrasjonHandtererTest {

    @Test
    public void sjekkAtMigreringSkjerForFakeSoknad() {
        MigrasjonHandterer handterer = new MigrasjonHandterer();
        WebSoknad innsendtSoknad = new WebSoknad().medId(1L).medskjemaNummer("NAV XO.XO-XO");
        WebSoknad migrertSoknad = handterer.handterMigrasjon(innsendtSoknad);

        assertThat(migrertSoknad.getskjemaNummer()).isEqualTo(null);
    }

    @Test
    public void sjekkAtMigreringIkkeSkjerForUkjentSkjemanummer() {
        MigrasjonHandterer handterer = new MigrasjonHandterer();
        WebSoknad innsendtSoknad = new WebSoknad().medId(1L).medskjemaNummer("123HEIHEI");
        WebSoknad migrertSoknad = handterer.handterMigrasjon(innsendtSoknad);

        assertThat(migrertSoknad.getskjemaNummer()).isEqualTo("123HEIHEI");
    }
}
