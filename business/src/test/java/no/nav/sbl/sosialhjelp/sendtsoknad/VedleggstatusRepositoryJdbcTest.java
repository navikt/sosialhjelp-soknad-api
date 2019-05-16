package no.nav.sbl.sosialhjelp.sendtsoknad;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.DbTestConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.RepositoryTestSupport;
import no.nav.sbl.sosialhjelp.domain.*;
import org.junit.*;
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
public class VedleggstatusRepositoryJdbcTest {

    private static final String EIER = "12345678901";
    private static final String BEHANDLINGSID = "1100020";
    private static final String BEHANDLINGSID2 = "1100021";
    private static final String FIKSFORSENDELSEID = "12345";
    private static final String FIKSFORSENDELSEID2 = "12789";
    private static final String ORGNUMMER = "987654";
    private static final String NAVENHETSNAVN = "NAV Enhet";
    private static final LocalDateTime BRUKER_OPPRETTET_DATO = now().minusDays(2);
    private static final LocalDateTime BRUKER_FERDIG_DATO = now().minusSeconds(50);
    private static final LocalDateTime SENDT_DATO = now();
    private static final String TYPE = "bostotte|annetboutgift";
    private static final String TYPE2 = "dokumentasjon|aksjer";

    @Inject
    private VedleggstatusRepository vedleggstatusRepository;

    @Inject
    private SendtSoknadRepository sendtSoknadRepository;

    @Inject
    private RepositoryTestSupport soknadRepositoryTestSupport;

    @After
    public void cleanUp() {
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from VEDLEGGSTATUS");
        soknadRepositoryTestSupport.getJdbcTemplate().update("delete from SENDT_SOKNAD");
    }

    @Test
    public void opprettVedleggstatusOppretterVedleggstatusIDatabasen() {
        final Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER, BEHANDLINGSID, FIKSFORSENDELSEID), EIER);

        Long vedleggstatusId = vedleggstatusRepository.opprettVedlegg(lagVedleggstatus(EIER, sendtSoknadId), EIER);

        assertThat(vedleggstatusId, notNullValue());
    }

    @Test
    public void hentVedleggHenterVedleggstatusSomFinnesForGittIdOgEier() {
        final Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER, BEHANDLINGSID, FIKSFORSENDELSEID), EIER);
        final Long vedleggstatusId = vedleggstatusRepository.opprettVedlegg(lagVedleggstatus(EIER, sendtSoknadId), EIER);

        Vedleggstatus vedleggstatus = vedleggstatusRepository.hentVedlegg(vedleggstatusId, EIER).get();

        assertThat(vedleggstatus.getVedleggstatusId(), is(vedleggstatusId));
        assertThat(vedleggstatus.getEier(), is(EIER));
        assertThat(vedleggstatus.getStatus(), is(Vedleggstatus.Status.VedleggKreves));
        assertThat(vedleggstatus.getVedleggType().getSammensattType(), is(TYPE));
        assertThat(vedleggstatus.getSendtSoknadId(), is(sendtSoknadId));
    }

    @Test
    public void hentVedleggforSendtSoknadHenterKunVedleggstatusForAngittSoknad() {
        final Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER, BEHANDLINGSID, FIKSFORSENDELSEID), EIER);
        final Long vedleggstatusId = vedleggstatusRepository.opprettVedlegg(lagVedleggstatus(EIER, sendtSoknadId), EIER);
        final Long sendtSoknadId2 = sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER, BEHANDLINGSID2, FIKSFORSENDELSEID2), EIER);
        vedleggstatusRepository.opprettVedlegg(lagVedleggstatus(EIER, sendtSoknadId2), EIER);

        List<Vedleggstatus> vedleggstatuser = vedleggstatusRepository.hentVedleggForSendtSoknad(sendtSoknadId, EIER);

        assertThat(vedleggstatuser.size(), is(1));
        assertThat(vedleggstatuser.get(0).getVedleggstatusId(), is(vedleggstatusId));
    }

    @Test
    public void hentVedleggForSendtSoknadMedStatusHenterKunVedleggstatusForAngittStatus() {
        final Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER, BEHANDLINGSID, FIKSFORSENDELSEID), EIER);
        final Long vedleggstatusId = vedleggstatusRepository.opprettVedlegg(
                lagVedleggstatus(EIER, sendtSoknadId, Vedleggstatus.Status.VedleggAlleredeSendt, TYPE2), EIER);
        vedleggstatusRepository.opprettVedlegg(lagVedleggstatus(EIER, sendtSoknadId), EIER);

        List<Vedleggstatus> vedleggstatuser = vedleggstatusRepository.hentVedleggForSendtSoknadMedStatus(
                sendtSoknadId, Vedleggstatus.Status.VedleggAlleredeSendt.VedleggAlleredeSendt.toString(), EIER);

        assertThat(vedleggstatuser.size(), is(1));
        assertThat(vedleggstatuser.get(0).getVedleggstatusId(), is(vedleggstatusId));
    }

    @Test
    public void endreStatusForVedleggOppdatererStatusForVedleggstatus() {
        final Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER, BEHANDLINGSID, FIKSFORSENDELSEID), EIER);
        final Long vedleggstatusId = vedleggstatusRepository.opprettVedlegg(lagVedleggstatus(EIER, sendtSoknadId), EIER);

        vedleggstatusRepository.endreStatusForVedlegg(vedleggstatusId, Vedleggstatus.Status.LastetOpp.toString(), EIER);

        Vedleggstatus oppdatertVedleggstatus = vedleggstatusRepository.hentVedlegg(vedleggstatusId, EIER).get();
        assertThat(oppdatertVedleggstatus.getStatus(), is(Vedleggstatus.Status.LastetOpp));
    }

    @Test
    public void slettVedleggSletterVedleggstatusFraDatabase() {
        final Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER, BEHANDLINGSID, FIKSFORSENDELSEID), EIER);
        final Long vedleggstatusId = vedleggstatusRepository.opprettVedlegg(lagVedleggstatus(EIER, sendtSoknadId), EIER);

        vedleggstatusRepository.slettVedlegg(vedleggstatusId, EIER);

        assertThat(vedleggstatusRepository.hentVedlegg(vedleggstatusId, EIER).isPresent(), is(false));
    }

    @Test
    public void slettAlleVedleggForSendtSoknadSletterAlleVedleggForGittSoknad() {
        final Long sendtSoknadId = sendtSoknadRepository.opprettSendtSoknad(lagSendtSoknad(EIER, BEHANDLINGSID, FIKSFORSENDELSEID), EIER);
        final Long vedleggstatusId = vedleggstatusRepository.opprettVedlegg(
                lagVedleggstatus(EIER, sendtSoknadId, Vedleggstatus.Status.VedleggAlleredeSendt, TYPE2), EIER);
        final Long vedleggstatusId2 = vedleggstatusRepository.opprettVedlegg(lagVedleggstatus(EIER, sendtSoknadId), EIER);

        vedleggstatusRepository.slettAlleVedleggForSendtSoknad(sendtSoknadId, EIER);

        assertThat(vedleggstatusRepository.hentVedlegg(vedleggstatusId, EIER).isPresent(), is(false));
        assertThat(vedleggstatusRepository.hentVedlegg(vedleggstatusId2, EIER).isPresent(), is(false));
    }

    private Vedleggstatus lagVedleggstatus(String eier, Long sendtSoknadId) {
        return lagVedleggstatus(eier, sendtSoknadId, Vedleggstatus.Status.VedleggKreves, TYPE);
    }

    private Vedleggstatus lagVedleggstatus(String eier, Long sendtSoknadId, Vedleggstatus.Status status, String type) {
        return new Vedleggstatus().withEier(eier)
                .withStatus(status)
                .withVedleggType(new VedleggType(type))
                .withSendtSoknadId(sendtSoknadId);
    }

    private SendtSoknad lagSendtSoknad(String eier, String behandlingsId, String fiksforsendelseId) {
        return new SendtSoknad().withEier(eier)
                .withBehandlingsId(behandlingsId)
                .withFiksforsendelseId(fiksforsendelseId)
                .withOrgnummer(ORGNUMMER)
                .withNavEnhetsnavn(NAVENHETSNAVN)
                .withBrukerOpprettetDato(BRUKER_OPPRETTET_DATO)
                .withBrukerFerdigDato(BRUKER_FERDIG_DATO)
                .withSendtDato(SENDT_DATO);
    }
}