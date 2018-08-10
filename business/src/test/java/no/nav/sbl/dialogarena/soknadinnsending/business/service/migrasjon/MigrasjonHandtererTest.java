package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MigrasjonHandtererTest {

    @Mock
    private HendelseRepository lokalDb;

    @InjectMocks
    private MigrasjonHandterer handterer;

    WebSoknad innsendtSoknad;

    @Before
    public void setup() {
        innsendtSoknad = new WebSoknad().medId(1L).medVersjon(1);
    }

    @Test
    public void migreringSkjerForSoknadMedEnVersjonLavere() {
        WebSoknad migrertSoknad = handterer.handterMigrasjon(innsendtSoknad);

        assertThat(migrertSoknad.getVersjon(), is(2));
    }

    @Test
    public void toMigreringerSkjerForSoknadMedToVersjonerLavere() {
        handterer.migrasjoner.add(new FakeMigrasjon());

        WebSoknad migrertSoknad = handterer.handterMigrasjon(innsendtSoknad);

        assertThat(migrertSoknad.getVersjon(), is(3));
        assertThat(migrertSoknad.getDelstegStatus(), is(DelstegStatus.UTFYLLING));
    }

    @Test
    public void migreringSkjerIkkeForSoknadMedNyereVersjon() {
        handterer.migrasjoner.add(new FakeMigrasjon());
        innsendtSoknad.medVersjon(4);

        WebSoknad ikkeMigrertSoknad = handterer.handterMigrasjon(innsendtSoknad);

        assertThat(ikkeMigrertSoknad.getVersjon(), is(4));
        assertThat(ikkeMigrertSoknad.getDelstegStatus(), not(DelstegStatus.UTFYLLING));
    }

    @Test
    public void migreringSkjerIkkeForSoknadMedVersjonerLavereEnnEn() {
        handterer.migrasjoner.add(new FakeMigrasjon());
        innsendtSoknad.medVersjon(0);

        WebSoknad ikkeMigrertSoknad = handterer.handterMigrasjon(innsendtSoknad);

        assertThat(ikkeMigrertSoknad.getVersjon(), is(0));
        assertThat(ikkeMigrertSoknad.getDelstegStatus(), not(DelstegStatus.UTFYLLING));
    }
}
