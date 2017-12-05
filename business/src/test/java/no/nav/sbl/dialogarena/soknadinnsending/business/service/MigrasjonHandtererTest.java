package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon.FakeMigrasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon.Migrasjon;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MigrasjonHandtererTest {

    @Mock(name = "lokalDb")
    private SoknadRepository lokalDb;

    MigrasjonHandterer handterer;
    WebSoknad innsendtSoknad;

    @Before
    public void setup() {
        List<Migrasjon> testMigrasjoner = new ArrayList<>();
        testMigrasjoner.add(new FakeMigrasjon());
        handterer = new MigrasjonHandterer(testMigrasjoner, lokalDb);
        innsendtSoknad = new WebSoknad().medId(1L).medskjemaNummer("NAV 11-13.05").medVersjon(1);
    }

    @Test
    public void migreringSkjerForFakeSoknadMedEnVersjonLavere() {
        WebSoknad migrertSoknad = handterer.handterMigrasjon(innsendtSoknad);

        assertThat(migrertSoknad.getskjemaNummer()).isEqualTo("NAV 11-13.05");
        assertThat(migrertSoknad.getVersjon()).isEqualTo(2);
        assertThat(migrertSoknad.getDelstegStatus()).isEqualTo(DelstegStatus.UTFYLLING);
    }

    @Test
    public void migreringSkjerIkkeForFakeSoknadMedForSoknaderMedNyereVersjon() {
        innsendtSoknad.medVersjon(3);
        WebSoknad ikkeMigrertSoknad = handterer.handterMigrasjon(innsendtSoknad);

        assertThat(ikkeMigrertSoknad.getVersjon()).isEqualTo(3);
        assertThat(ikkeMigrertSoknad.getDelstegStatus()).isNotEqualTo(DelstegStatus.UTFYLLING);
    }

    @Test
    public void migreringSkjerIkkeForFakeSoknadMedVersjonerLavereEnnEn() {
        innsendtSoknad.medVersjon(0);
        WebSoknad ikkeMigrertSoknad = handterer.handterMigrasjon(innsendtSoknad);

        assertThat(ikkeMigrertSoknad.getVersjon()).isEqualTo(0);
        assertThat(ikkeMigrertSoknad.getDelstegStatus()).isNotEqualTo(DelstegStatus.UTFYLLING);
    }

    @Test
    public void sjekkAtMigreringIkkeSkjerForUkjentSkjemanummer() {
        innsendtSoknad.medskjemaNummer("123HEIHEI");
        WebSoknad migrertSoknad = handterer.handterMigrasjon(innsendtSoknad);

        assertThat(migrertSoknad.getskjemaNummer()).isEqualTo("123HEIHEI");
    }

}
