package no.nav.sbl.sosialhjelp.sendtsoknad;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.RepositoryTestSupport;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DbTestConfig.class})
public class SendtSoknadRepositoryJdbcTest {

    private static final String EIER = "12345678901";
    private static final String EIER2 = "10987654321";
    private static final String BEHANDLINGSID = "1100020";
    private static final String BEHANDLINGSID2 = "1100021";
    private static final String BEHANDLINGSID3 = "1100022";
    private static final String TILKNYTTET_BEHANDLINGSID = "4567";
    private static final String FIKSFORSENDELSEID = "12345";
    private static final String FIKSFORSENDELSEID2 = "12789";
    private static final String FIKSFORSENDELSEID3 = "12652";
    private static final LocalDateTime BRUKER_OPPRETTET_DATO = now().minusDays(2);
    private static final LocalDateTime BRUKER_FERDIG_DATO = now().minusSeconds(50);
    private static final LocalDateTime SENDT_DATO = now();

    @Inject
    private SendtSoknadRepository sendtSoknadRepository;

    @Inject
    private RepositoryTestSupport soknadRepositoryTestSupport;

    @After
    public void cleanUp() {
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from SENDT_SOKNAD");
    }

    @Test
    public void opprettSendtSoknadOppretterSendtSoknadIDatabasen() {
        Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER, BEHANDLINGSID, FIKSFORSENDELSEID), EIER);

        assertThat(sendtSoknadId, notNullValue());
    }

    @Test(expected = RuntimeException.class)
    public void opprettSendtSoknadKasterRuntimeExceptionHvisEierErUlikSoknadseier() {
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER, BEHANDLINGSID, FIKSFORSENDELSEID), EIER2);
    }

    @Test
    public void hentSendtSoknadHenterSendtSoknadForEierOgBehandlingsid() {
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER, BEHANDLINGSID, FIKSFORSENDELSEID), EIER);

        SendtSoknad sendtSoknad = sendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID, EIER).get();

        assertThat(sendtSoknad.getEier(), is(EIER));
        assertThat(sendtSoknad.getSendtSoknadId(), notNullValue());
        assertThat(sendtSoknad.getBehandlingsId(), is(BEHANDLINGSID));
        assertThat(sendtSoknad.getTilknyttetBehandlingsId(), is(TILKNYTTET_BEHANDLINGSID));
        assertThat(sendtSoknad.getFiksforsendelseId(), is(FIKSFORSENDELSEID));
        assertThat(sendtSoknad.getBrukerOpprettetDato(), is(BRUKER_OPPRETTET_DATO));
        assertThat(sendtSoknad.getBrukerFerdigDato(), is(BRUKER_FERDIG_DATO));
        assertThat(sendtSoknad.getSendtDato(), is(SENDT_DATO));
    }

    @Test
    public void hentAlleSendteSoknaderHenterAlleSendteSoknaderForEier() {
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER, BEHANDLINGSID, FIKSFORSENDELSEID), EIER);
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER, BEHANDLINGSID2, FIKSFORSENDELSEID2), EIER);
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER2, BEHANDLINGSID3, FIKSFORSENDELSEID3), EIER2);

        List<SendtSoknad> sendteSoknader = sendtSoknadRepository.hentAlleSendteSoknader(EIER);

        assertThat(sendteSoknader.size(), is(2));
        assertThat(sendteSoknader.get(0).getEier(), is(EIER));
        assertThat(sendteSoknader.get(0).getBehandlingsId(), is(BEHANDLINGSID));
        assertThat(sendteSoknader.get(1).getEier(), is(EIER));
        assertThat(sendteSoknader.get(1).getBehandlingsId(), is(BEHANDLINGSID2));
    }

    @Test
    public void slettSendtSoknadSletterSoknadFraDatabase() {
        SendtSoknad sendtSoknad = lagSendtSoknad(EIER, BEHANDLINGSID, FIKSFORSENDELSEID);
        Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(sendtSoknad, EIER);
        sendtSoknad.setSendtSoknadId(sendtSoknadId);

        sendtSoknadRepository.slettSendtSoknad(sendtSoknad, EIER);

        assertThat(sendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID, EIER).isPresent(), is(false));
    }

    private SendtSoknad lagSendtSoknad(String eier, String behandlingsId, String fiksforsendelseId) {
        return new SendtSoknad().withEier(eier)
                .withBehandlingsId(behandlingsId)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withFiksforsendelseId(fiksforsendelseId)
                .withBrukerOpprettetDato(BRUKER_OPPRETTET_DATO)
                .withBrukerFerdigDato(BRUKER_FERDIG_DATO)
                .withSendtDato(SENDT_DATO);
    }
}