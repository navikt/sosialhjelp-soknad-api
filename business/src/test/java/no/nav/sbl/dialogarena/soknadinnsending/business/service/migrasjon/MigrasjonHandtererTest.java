package no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon;

import static org.assertj.core.api.Assertions.assertThat;

import no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon.MigrasjonHandterer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.HendelseRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.migrasjon.FakeMigrasjon;

@RunWith(MockitoJUnitRunner.class)
public class MigrasjonHandtererTest {

    @Mock
    private HendelseRepository lokalDb;

    @InjectMocks
    private MigrasjonHandterer handterer;

    WebSoknad innsendtSoknad;

    @Before
    public void setup() {
        handterer.migrasjoner.add(new FakeMigrasjon());
        innsendtSoknad = new WebSoknad().medId(1L).medskjemaNummer(SosialhjelpInformasjon.SKJEMANUMMER).medVersjon(1);
    }

    @Test
    @Ignore("Aktiveres straks det er en migrasjon på søknadsosialhjelp")
    public void migreringSkjerForFakeSoknadMedEnVersjonLavere() {
        WebSoknad migrertSoknad = handterer.handterMigrasjon(innsendtSoknad);

        assertThat(migrertSoknad.getskjemaNummer()).isEqualTo(SosialhjelpInformasjon.SKJEMANUMMER);
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
