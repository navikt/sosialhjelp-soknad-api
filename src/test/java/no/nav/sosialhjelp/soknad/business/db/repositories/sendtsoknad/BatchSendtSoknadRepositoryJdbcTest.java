package no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad;

import no.nav.sosialhjelp.soknad.business.db.RepositoryTestSupport;
import no.nav.sosialhjelp.soknad.business.db.config.DbTestConfig;
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
public class BatchSendtSoknadRepositoryJdbcTest {

    private static final String EIER = "12345678901";
    private static final String BEHANDLINGSID = "1100020";
    private static final String TILKNYTTET_BEHANDLINGSID = "4567";
    private static final String FIKSFORSENDELSEID = "12345";
    private static final String ORGNUMMER = "987654";
    private static final String NAVENHETSNAVN = "NAV Enhet";
    private static final LocalDateTime BRUKER_OPPRETTET_DATO = now().minusDays(2).truncatedTo(ChronoUnit.MILLIS);
    private static final LocalDateTime BRUKER_FERDIG_DATO = now().minusSeconds(50).truncatedTo(ChronoUnit.MILLIS);
    private static final LocalDateTime SENDT_DATO = now().truncatedTo(ChronoUnit.MILLIS);

    @Inject
    private SendtSoknadRepository sendtSoknadRepository;

    @Inject
    private BatchSendtSoknadRepository batchSendtSoknadRepository;

    @Inject
    private RepositoryTestSupport soknadRepositoryTestSupport;

    @After
    public void tearDown() {
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from SENDT_SOKNAD");
    }

    @Test
    public void hentSendtSoknadHenterSendtSoknadForEierOgBehandlingsid() {
        sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER), EIER);

        Long sendtSoknadId = batchSendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID).get();

        assertThat(sendtSoknadId, notNullValue());
    }

    @Test
    public void slettSendtSoknadSletterSoknadFraDatabase() {
        SendtSoknad sendtSoknad = lagSendtSoknad(EIER);
        Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(sendtSoknad, EIER);
        sendtSoknad.setSendtSoknadId(sendtSoknadId);

        batchSendtSoknadRepository.slettSendtSoknad(sendtSoknadId);

        assertThat(batchSendtSoknadRepository.hentSendtSoknad(BEHANDLINGSID).isPresent(), is(false));
    }

    private SendtSoknad lagSendtSoknad(String eier) {
        return new SendtSoknad().withEier(eier)
                .withBehandlingsId(BEHANDLINGSID)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withFiksforsendelseId(FIKSFORSENDELSEID)
                .withOrgnummer(ORGNUMMER)
                .withNavEnhetsnavn(NAVENHETSNAVN)
                .withBrukerOpprettetDato(BRUKER_OPPRETTET_DATO)
                .withBrukerFerdigDato(BRUKER_FERDIG_DATO)
                .withSendtDato(SENDT_DATO);
    }

}