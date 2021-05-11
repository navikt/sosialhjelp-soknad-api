package no.nav.sosialhjelp.soknad.business.sendtsoknad;

import no.nav.sosialhjelp.soknad.business.db.DbTestConfig;
import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport;
import no.nav.sosialhjelp.soknad.domain.SendtSoknad;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.LocalDateTime.now;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class SendtSoknadRepositoryJdbcTest {

    private static final String EIER = "12345678901";
    private static final String EIER2 = "22222222222";
    private static final String BEHANDLINGSID = "1100020";
    private static final String BEHANDLINGSID2 = "1100021";
    private static final String BEHANDLINGSID3 = "1100022";
    private static final String TILKNYTTET_BEHANDLINGSID = "4567";
    private static final String FIKSFORSENDELSEID = "12345";
    private static final String FIKSFORSENDELSEID2 = "12789";
    private static final String FIKSFORSENDELSEID3 = "12652";
    private static final String ORGNUMMER = "987654";
    private static final String NAVENHETSNAVN = "NAV Enhet";
    private static final LocalDateTime BRUKER_OPPRETTET_DATO = now().minusDays(2).truncatedTo(ChronoUnit.MILLIS);
    private static final LocalDateTime BRUKER_FERDIG_DATO = now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS);
    private static final LocalDateTime SENDT_DATO = now().truncatedTo(ChronoUnit.MILLIS);

    @Inject
    private SendtSoknadRepository sendtSoknadRepository;

    @Inject
    private RepositoryTestSupport soknadRepositoryTestSupport;

    @After
    public void tearDown() {
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from SENDT_SOKNAD");
    }

    @Test
    public void opprettSendtSoknadOppretterSendtSoknadIDatabasen() {
        Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), EIER);

        assertThat(sendtSoknadId, notNullValue());
    }

    @Test(expected = RuntimeException.class)
    public void opprettSendtSoknadKasterRuntimeExceptionHvisEierErUlikSoknadseier() {
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), EIER2);
    }

    @Test(expected = RuntimeException.class)
    public void opprettSendtSoknadKasterRuntimeExceptionHvisEierErNull() {
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), null);
    }

    @Test
    public void hentSendtSoknadHenterSendtSoknadForEierOgBehandlingsid() {
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), EIER);

        SendtSoknad sendtSoknad = sendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID, EIER).get();

        assertThat(sendtSoknad.getEier(), is(EIER));
        assertThat(sendtSoknad.getSendtSoknadId(), notNullValue());
        assertThat(sendtSoknad.getBehandlingsId(), is(BEHANDLINGSID));
        assertThat(sendtSoknad.getTilknyttetBehandlingsId(), is(TILKNYTTET_BEHANDLINGSID));
        assertThat(sendtSoknad.getFiksforsendelseId(), is(FIKSFORSENDELSEID));
        assertThat(sendtSoknad.getOrgnummer(), is(ORGNUMMER));
        assertThat(sendtSoknad.getNavEnhetsnavn(), is(NAVENHETSNAVN));
        assertThat(sendtSoknad.getBrukerOpprettetDato(), is(BRUKER_OPPRETTET_DATO));
        assertThat(sendtSoknad.getBrukerFerdigDato(), is(BRUKER_FERDIG_DATO));
        assertThat(sendtSoknad.getSendtDato(), is(SENDT_DATO));
    }

    @Test
    public void oppdaterSendtSoknadVedSendingTilFiksOppdatererFiksIdOgSendtDato() {
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknadSomIkkeErSendtTilFiks(), EIER);

        sendtSoknadRepository.oppdaterSendtSoknadVedSendingTilFiks(FIKSFORSENDELSEID, BEHANDLINGSID, EIER);

        SendtSoknad oppdatertSendtSoknad = sendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID, EIER).get();
        assertThat(oppdatertSendtSoknad.getFiksforsendelseId(), is(FIKSFORSENDELSEID));
        assertThat(oppdatertSendtSoknad.getSendtDato(), notNullValue());
    }

    @Test
    public void slettSendtSoknadSletterSoknadFraDatabase() {
        SendtSoknad sendtSoknad = lagSendtSoknad(EIER);
        Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(sendtSoknad, EIER);
        sendtSoknad.setSendtSoknadId(sendtSoknadId);

        sendtSoknadRepository.slettSendtSoknad(sendtSoknad, EIER);

        assertThat(sendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID, EIER).isPresent(), is(false));
    }

    private SendtSoknad lagSendtSoknad(String eier) {
        return lagSendtSoknad(eier, BEHANDLINGSID, FIKSFORSENDELSEID);
    }

    private SendtSoknad lagSendtSoknad(String eier, String behandlingsId, String fiksforsendelseId) {
        return new SendtSoknad().withEier(eier)
                .withBehandlingsId(behandlingsId)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withFiksforsendelseId(fiksforsendelseId)
                .withOrgnummer(ORGNUMMER)
                .withNavEnhetsnavn(NAVENHETSNAVN)
                .withBrukerOpprettetDato(BRUKER_OPPRETTET_DATO)
                .withBrukerFerdigDato(BRUKER_FERDIG_DATO)
                .withSendtDato(SENDT_DATO);
    }

    private SendtSoknad lagSendtSoknadSomIkkeErSendtTilFiks() {
        return new SendtSoknad().withEier(EIER)
                .withBehandlingsId(BEHANDLINGSID)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withOrgnummer(ORGNUMMER)
                .withNavEnhetsnavn(NAVENHETSNAVN)
                .withBrukerOpprettetDato(BRUKER_OPPRETTET_DATO)
                .withBrukerFerdigDato(BRUKER_FERDIG_DATO);
    }
}