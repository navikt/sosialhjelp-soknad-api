package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.digipost.time.ControllableClock;
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

        hendelseRepository.registrerOpprettetHendelse(new WebSoknad().medskjemaNummer("TEST_1").medBehandlingId("1").medVersjon(1));

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(50)).hasSize(1);
    }

    @Test
    public void skalIkkeHenteNyereUnderArbeidSak() {
        controllableClock.set(LocalDateTime.now().minusDays(1));
        hendelseRepository.registrerOpprettetHendelse(new WebSoknad().medskjemaNummer("TEST_1").medBehandlingId("1").medVersjon(1));

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(50)).isEmpty();

    }


}