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

    public static final int FEMTISEKS_DAGER = 56;
    public static final int EN_DAG = 1;
    public static final int FEMTI_DAGER = 50;
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
        stillTidenTilbake(FEMTISEKS_DAGER);

        hendelseRepository.registrerOpprettetHendelse(soknad("1"));

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).hasSize(1);
    }



    @Test
    public void skalIkkeHenteNyereUnderArbeidSak() {
        stillTidenTilbake(EN_DAG);
        hendelseRepository.registrerOpprettetHendelse(soknad("1"));

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).isEmpty();
    }

    @Test
    public void skalBehandleMigertSoknadSomUnderArbeid() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(soknad("1"));
        hendelseRepository.registrerMigrertHendelse(soknad("1").medVersjon(2));

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).hasSize(1);
    }

    @Test
    public void skalBehandleMellomlagretSoknadSomUnderArbeid() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(soknad("1"));
        hendelseRepository.registrerHendelse(soknad("1"), HendelseType.LAGRET_I_HENVENDELSE);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).hasSize(1);
    }

    @Test
    public void skalBehandleOpphentetSoknadSomUnderArbeid() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(soknad("1"));
        hendelseRepository.registrerHendelse(soknad("1"), HendelseType.LAGRET_I_HENVENDELSE);
        hendelseRepository.registrerHendelse(soknad("1"), HendelseType.HENTET_FRA_HENVENDELSE);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).hasSize(1);
    }
    @Test
    public void skalIkkeHenteSakHvorSisteHendelseErAvbruttAvBruker() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(soknad("1"));
        hendelseRepository.registrerHendelse(soknad("1"), HendelseType.AVBRUTT_AV_BRUKER);

        stillTidenTilbake(EN_DAG);
        hendelseRepository.registrerOpprettetHendelse(soknad("2"));
        hendelseRepository.registrerHendelse(soknad("2"), HendelseType.AVBRUTT_AV_BRUKER);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).isEmpty();
    }

    @Test
    public void skalIkkeHenteSakHvorSisteHendelseErAvbruttAutomatisk() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(soknad("1"));
        hendelseRepository.registrerHendelse(soknad("1"), HendelseType.LAGRET_I_HENVENDELSE);
        hendelseRepository.registrerAutomatiskAvsluttetHendelse("1");

        stillTidenTilbake(EN_DAG);
        hendelseRepository.registrerOpprettetHendelse(soknad("2"));
        hendelseRepository.registrerHendelse(soknad("2"), HendelseType.LAGRET_I_HENVENDELSE);
        hendelseRepository.registrerAutomatiskAvsluttetHendelse("2");

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).isEmpty();
    }

    @Test
    public void skalIkkeHenteSakHvorSisteHendelseErInnsendt() {
        stillTidenTilbake(FEMTISEKS_DAGER);
        hendelseRepository.registrerOpprettetHendelse(soknad("1"));
        hendelseRepository.registrerHendelse(soknad("1"), HendelseType.INNSENDT);

        stillTidenTilbake(EN_DAG);
        hendelseRepository.registrerOpprettetHendelse(soknad("2"));
        hendelseRepository.registrerHendelse(soknad("2"), HendelseType.INNSENDT);

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(FEMTI_DAGER)).isEmpty();
    }


    private WebSoknad soknad(String behandlingsId) {
        return new WebSoknad().medskjemaNummer("NAV-01").medBehandlingId(behandlingsId).medVersjon(1);
    }

    private void stillTidenTilbake(int antallDager) {
        controllableClock.set(LocalDateTime.now().minusDays(antallDager));
    }


}