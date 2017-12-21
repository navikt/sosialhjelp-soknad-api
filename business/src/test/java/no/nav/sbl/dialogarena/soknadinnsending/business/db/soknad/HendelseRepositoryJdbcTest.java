package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.digipost.time.ControllableClock;
import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class HendelseRepositoryJdbcTest {

    @Inject
    private HendelseRepository hendelseRepository;

    @Inject
    private Clock systemClock;
    private ControllableClock controllableClock;


    @Before
    public void setUp() throws Exception {
        controllableClock = (ControllableClock) systemClock;
    }

    @Test
    public void skalHenteForGammelUnderArbeidSak() {
        controllableClock.set(LocalDateTime.now().minusDays(56));

        hendelseRepository.registrerOpprettetHendelse(soknad());

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(50)).hasSize(1);
    }

    @Test
    public void skalIkkeHenteNyereUnderArbeidSak() {
        controllableClock.set(LocalDateTime.now().minusDays(1));
        hendelseRepository.registrerOpprettetHendelse(soknad());

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(50)).isEmpty();
    }

    @Test
    public void skalBehandleMigertSoknadSomUnderArbeid() {
        controllableClock.set(LocalDateTime.now().minusDays(56));
        hendelseRepository.registrerOpprettetHendelse(soknad());
        hendelseRepository.registrerMigrertHendelse(soknad().medVersjon(2));

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(50)).hasSize(1);
    }

    @Test
    public void skalBehandleMellomlagretSoknadSomUnderArbeid() {
        controllableClock.set(LocalDateTime.now().minusDays(56));
        hendelseRepository.registrerOpprettetHendelse(soknad());
        hendelseRepository.registrerHendelse(soknad(), HendelseType.LAGRET_I_HENVENDELSE);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(50)).hasSize(1);
    }

    @Test
    public void skalBehandleOpphentetSoknadSomUnderArbeid() {
        controllableClock.set(LocalDateTime.now().minusDays(56));
        hendelseRepository.registrerOpprettetHendelse(soknad());
        hendelseRepository.registrerHendelse(soknad(), HendelseType.LAGRET_I_HENVENDELSE);
        hendelseRepository.registrerHendelse(soknad(), HendelseType.HENTET_FRA_HENVENDELSE);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(50)).hasSize(1);
    }
    @Test
    public void skalIkkeHenteSakHvorSisteHendelseErAvbruttAvBruker() {
        controllableClock.set(LocalDateTime.now().minusDays(56));
        hendelseRepository.registrerOpprettetHendelse(soknad().medBehandlingId("1"));
        hendelseRepository.registrerHendelse(soknad().medBehandlingId("1"), HendelseType.AVBRUTT_AV_BRUKER);

        controllableClock.set(LocalDateTime.now().minusDays(1));
        hendelseRepository.registrerOpprettetHendelse(soknad().medBehandlingId("2"));
        hendelseRepository.registrerHendelse(soknad().medBehandlingId("2"), HendelseType.AVBRUTT_AV_BRUKER);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(50)).isEmpty();
    }

    @Test
    public void skalIkkeHenteSakHvorSisteHendelseErAvbruttAutomatisk() {
        controllableClock.set(LocalDateTime.now().minusDays(56));
        hendelseRepository.registrerOpprettetHendelse(soknad().medBehandlingId("1"));
        hendelseRepository.registrerHendelse(soknad().medBehandlingId("1"), HendelseType.LAGRET_I_HENVENDELSE);
        hendelseRepository.registrerAutomatiskAvsluttetHendelse("1");

        controllableClock.set(LocalDateTime.now().minusDays(1));
        hendelseRepository.registrerOpprettetHendelse(soknad().medBehandlingId("2"));
        hendelseRepository.registrerHendelse(soknad().medBehandlingId("2"), HendelseType.LAGRET_I_HENVENDELSE);
        hendelseRepository.registrerAutomatiskAvsluttetHendelse("2");

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(50)).isEmpty();
    }

    @Test
    public void skalIkkeHenteSakHvorSisteHendelseErInnsendt() {
        controllableClock.set(LocalDateTime.now().minusDays(56));
        hendelseRepository.registrerOpprettetHendelse(soknad().medBehandlingId("1"));
        hendelseRepository.registrerHendelse(soknad().medBehandlingId("1"), HendelseType.INNSENDT);

        controllableClock.set(LocalDateTime.now().minusDays(1));
        hendelseRepository.registrerOpprettetHendelse(soknad().medBehandlingId("2"));
        hendelseRepository.registrerHendelse(soknad().medBehandlingId("2"), HendelseType.INNSENDT);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(50)).isEmpty();
    }


    private WebSoknad soknad() {
        return new WebSoknad().medskjemaNummer("NAV-01").medBehandlingId("1").medVersjon(1);
    }


}