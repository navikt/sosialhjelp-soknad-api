package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.digipost.time.ControllableClock;
import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.TestSupport;
import org.junit.After;
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

    public static final int FEMTISEKS_DAGER = 56;
    public static final int EN_DAG = 1;
    public static final int FEMTI_DAGER = 50;
    public static final String BEHANDLINGS_ID_1 = "99";
    @Inject
    private HendelseRepository hendelseRepository;

    @Inject
    private TestSupport support;

    @Inject
    private Clock systemClock;
    private ControllableClock controllableClock;


    @Before
    public void setUp() throws Exception {
        controllableClock = (ControllableClock) systemClock;
    }

    @After
    public void teardown() throws Exception {
        support.getJdbcTemplate().execute("DELETE FROM hendelse");
    }

    @Test
    public void skalHenteVersjonForOpprettetSoknad() {
        hendelseRepository.registrerOpprettetHendelse(BEHANDLINGS_ID_1, 1);

        assertThat(hendelseRepository.hentVersjon(BEHANDLINGS_ID_1)).isEqualTo(1);
    }

    @Test
    public void skalGiDefaultversjonForSoknadUtenHendelser() {
        assertThat(hendelseRepository.hentVersjon(BEHANDLINGS_ID_1)).isEqualTo(0);
        assertThat(hendelseRepository.hentVersjon("XX")).isEqualTo(0);
    }


    @Test
    public void skalHenteForGammelUnderArbeidSak() {
        stillTidenTilbake(FEMTISEKS_DAGER);

        hendelseRepository.registrerOpprettetHendelse(BEHANDLINGS_ID_1, 0);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).hasSize(1);
    }



    @Test
    public void skalIkkeHenteNyereUnderArbeidSak() {
        stillTidenTilbake(EN_DAG);
        hendelseRepository.registrerOpprettetHendelse(BEHANDLINGS_ID_1, 0);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).isEmpty();
    }

    @Test
    public void skalBehandleMigertSoknadSomUnderArbeid() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(BEHANDLINGS_ID_1, 0);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).hasSize(1);
    }

    @Test
    public void skalBehandleMellomlagretSoknadSomUnderArbeid() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(BEHANDLINGS_ID_1, 0);
        hendelseRepository.registrerHendelse(BEHANDLINGS_ID_1, HendelseType.LAGRET_I_HENVENDELSE, 1);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).hasSize(1);
    }

    @Test
    public void skalBehandleOpphentetSoknadSomUnderArbeid() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(BEHANDLINGS_ID_1, 0);
        hendelseRepository.registrerHendelse(BEHANDLINGS_ID_1, HendelseType.LAGRET_I_HENVENDELSE, 1);
        hendelseRepository.registrerHendelse(BEHANDLINGS_ID_1, HendelseType.HENTET_FRA_HENVENDELSE, 2);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).hasSize(1);
    }
    @Test
    public void skalIkkeHenteSakHvorSisteHendelseErAvbruttAvBruker() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(BEHANDLINGS_ID_1, 0);
        hendelseRepository.registrerHendelse(BEHANDLINGS_ID_1, HendelseType.AVBRUTT_AV_BRUKER, 1);

        stillTidenTilbake(EN_DAG);
        hendelseRepository.registrerOpprettetHendelse("2", 0);
        hendelseRepository.registrerHendelse("2", HendelseType.AVBRUTT_AV_BRUKER, 1);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).isEmpty();
    }

    @Test
    public void skalIkkeHenteSakHvorSisteHendelseErAvbruttAutomatisk() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(BEHANDLINGS_ID_1, 0);
        hendelseRepository.registrerHendelse(BEHANDLINGS_ID_1, HendelseType.LAGRET_I_HENVENDELSE, 1);
        hendelseRepository.registrerAutomatiskAvsluttetHendelse(BEHANDLINGS_ID_1);

        stillTidenTilbake(EN_DAG);
        hendelseRepository.registrerOpprettetHendelse("2", 0);
        hendelseRepository.registrerHendelse("2", HendelseType.LAGRET_I_HENVENDELSE, 1);
        hendelseRepository.registrerAutomatiskAvsluttetHendelse("2");

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).isEmpty();
    }

    @Test
    public void skalIkkeHenteSakHvorSisteHendelseErInnsendt() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(BEHANDLINGS_ID_1, 0);
        hendelseRepository.registrerHendelse(BEHANDLINGS_ID_1, HendelseType.INNSENDT, 1);

        stillTidenTilbake(EN_DAG);
        hendelseRepository.registrerOpprettetHendelse("2", 0);
        hendelseRepository.registrerHendelse("2", HendelseType.INNSENDT, 1);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).isEmpty();
    }

    private void stillTidenTilbake(int antallDager) {
        controllableClock.set(LocalDateTime.now().minusDays(antallDager));
    }


}