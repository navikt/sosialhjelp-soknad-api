package no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad;

import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class HendelseRepositoryJdbcTest {

    @Inject
    private HendelseRepository hendelseRepository;

    @Test
    @Ignore("Avventer Rune for en SQL som funker både for hsql og plsql")
    public void skalHenteIkkeAvsluttede() {
        setupHendelser();

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(-1).size(), is(2));

        assertThat(hendelseRepository.hentSoknaderUnderArbeidEldreEnn(1).size(), is(0));

    }


    private void setupHendelser() {
        WebSoknad webSoknad = new WebSoknad().medskjemaNummer("TEST_1").medBehandlingId("1").medVersjon(1);
        hendelseRepository.registrerOpprettetHendelse(webSoknad);
        hendelseRepository.registrerHendelse(webSoknad, LAGRET_I_HENVENDELSE);
        hendelseRepository.registrerMigrertHendelse(webSoknad.medVersjon(2));

        WebSoknad webSoknad2 = new WebSoknad().medskjemaNummer("TEST_2").medBehandlingId("2").medVersjon(2);
        hendelseRepository.registrerOpprettetHendelse(webSoknad2);

        WebSoknad webSoknad3 = new WebSoknad().medskjemaNummer("TEST_1").medBehandlingId("3").medVersjon(2);
        hendelseRepository.registrerOpprettetHendelse(webSoknad3);
        hendelseRepository.registrerHendelse(webSoknad3, LAGRET_I_HENVENDELSE);
        hendelseRepository.registrerHendelse(webSoknad3, INNSENDT);
    }

}