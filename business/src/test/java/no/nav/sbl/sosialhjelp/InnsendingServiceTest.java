package no.nav.sbl.sosialhjelp;

import no.nav.sbl.sosialhjelp.domain.*;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.sendtsoknad.VedleggstatusRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.support.*;

import java.time.LocalDateTime;
import java.util.*;

import static java.time.LocalDateTime.now;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InnsendingServiceTest {
    private static final Long SOKNAD_UNDER_ARBEID_ID = 1L;
    private static final Long SENDT_SOKNAD_ID = 2L;
    private static final String EIER = "12345678910";
    private static final String TYPE = "bostotte";
    private static final String TILLEGGSINFO = "annetboutgift";
    private static final VedleggType VEDLEGGTYPE = new VedleggType(TYPE, TILLEGGSINFO);
    private static final String BEHANDLINGSID = "1100001L";
    private static final String TILKNYTTET_BEHANDLINGSID = "1100002K";
    private static final String FIKSFORSENDELSEID = "12345";
    private static final String ORGNR = "012345678";
    private static final LocalDateTime OPPRETTET_DATO = now().minusSeconds(50);
    private static final LocalDateTime SIST_ENDRET_DATO = now();
    @Mock
    private TransactionTemplate transactionTemplate;
    @Mock
    private SendtSoknadRepository sendtSoknadRepository;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @Mock
    private OpplastetVedleggRepository opplastetVedleggRepository;
    @Mock
    private VedleggstatusRepository vedleggstatusRepository;
    @InjectMocks
    private InnsendingService innsendingService;

    @Before
    public void setUp() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            TransactionCallbackWithoutResult arg = (TransactionCallbackWithoutResult) args[0];
            return arg.doInTransaction(new SimpleTransactionStatus());
        });
        when(opplastetVedleggRepository.hentVedleggForSoknad(anyLong(), anyString())).thenReturn(lagOpplastetVedleggListe());
        when(sendtSoknadRepository.opprettSendtSoknad(any(), anyString())).thenReturn(SENDT_SOKNAD_ID);
        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(lagSendtSoknad());
        when(vedleggstatusRepository.opprettVedlegg(any(), anyString())).thenReturn(5L);
    }

    @Test
    public void sendSoknadSletterSoknadFraSoknadUnderArbeid() {
        innsendingService.sendSoknad(lagSoknadUnderArbeid(), new ArrayList<>(), ORGNR);

        verify(soknadUnderArbeidRepository, times(1)).slettSoknad(any(SoknadUnderArbeid.class), eq(EIER));
    }

    @Test
    public void finnAlleVedleggHenterAlleOpplastedeOgManglendeVedlegg() {
        when(opplastetVedleggRepository.hentVedleggForSoknad(anyLong(), anyString())).thenReturn(lagOpplastetVedleggListe());

        List<Vedleggstatus> alleVedlegg = innsendingService.finnAlleVedlegg(lagSoknadUnderArbeid(), lagIkkeOpplastedePaakrevdeVedlegg());

        assertThat(alleVedlegg.size(), is(2));
    }

    @Test
    public void mapSoknadUnderArbeidTilSendtSoknadMapperInfoRiktig() {
        SendtSoknad sendtSoknad = innsendingService.mapSoknadUnderArbeidTilSendtSoknad(lagSoknadUnderArbeid(), ORGNR);

        assertThat(sendtSoknad.getBehandlingsId(), is(BEHANDLINGSID));
        assertThat(sendtSoknad.getTilknyttetBehandlingsId(), is(TILKNYTTET_BEHANDLINGSID));
        assertThat(sendtSoknad.getEier(), is(EIER));
        assertThat(sendtSoknad.getOrgnummer(), is(ORGNR));
        assertThat(sendtSoknad.getBrukerOpprettetDato(), is(OPPRETTET_DATO));
        assertThat(sendtSoknad.getBrukerFerdigDato(), is(SIST_ENDRET_DATO));
        assertThat(sendtSoknad.getSendtDato(), nullValue());
        assertThat(sendtSoknad.getFiksforsendelseId(), nullValue());
    }

    @Test
    public void mapSoknadUnderArbeidTilSendtSoknadHenterOrgNummerFraSendtSoknadVedEttersendelse() {
        SendtSoknad sendtSoknad = innsendingService.mapSoknadUnderArbeidTilSendtSoknad(lagSoknadUnderArbeid(), null);

        assertThat(sendtSoknad.getOrgnummer(), is(ORGNR));
    }

    @Test(expected = IllegalStateException.class)
    public void mapSoknadUnderArbeidTilSendtSoknadKasterFeilHvisOrgnrOgTilknyttetIdMangler() {
        innsendingService.mapSoknadUnderArbeidTilSendtSoknad(lagSoknadUnderArbeidUtenOrgnrOgUtenTilknyttetBehandlingsid(), null);
    }

    @Test
    public void mapOpplastedeVedleggTilVedleggstatusListeMapperInfoRiktig() {
        List<OpplastetVedlegg> opplastedeVedlegg = lagOpplastetVedleggListe();

        List<Vedleggstatus> vedleggstatuser = innsendingService.mapOpplastedeVedleggTilVedleggstatusListe(opplastedeVedlegg);
        Vedleggstatus vedleggstatus = vedleggstatuser.get(0);

        assertThat(vedleggstatuser.size(), is(1));
        assertThat(vedleggstatus.getStatus(), is(Vedleggstatus.Status.LastetOpp));
        assertThat(vedleggstatus.getEier(), is(EIER));
        assertThat(vedleggstatus.getVedleggType().getType(), is(TYPE));
        assertThat(vedleggstatus.getVedleggType().getTilleggsinfo(), is(TILLEGGSINFO));
    }

    private List<OpplastetVedlegg> lagOpplastetVedleggListe() {
        List<OpplastetVedlegg> opplastedeVedlegg = new ArrayList<>();
        opplastedeVedlegg.add(lagOpplastetVedlegg());
        opplastedeVedlegg.add(null);
        return opplastedeVedlegg;
    }

    private SoknadUnderArbeid lagSoknadUnderArbeid() {
        return new SoknadUnderArbeid()
                .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
                .withBehandlingsId(BEHANDLINGSID)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withEier(EIER)
                .withOpprettetDato(OPPRETTET_DATO)
                .withSistEndretDato(SIST_ENDRET_DATO);
    }

    private SoknadUnderArbeid lagSoknadUnderArbeidUtenOrgnrOgUtenTilknyttetBehandlingsid() {
        return new SoknadUnderArbeid()
                .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
                .withBehandlingsId(BEHANDLINGSID)
                .withEier(EIER)
                .withOpprettetDato(OPPRETTET_DATO)
                .withSistEndretDato(SIST_ENDRET_DATO);
    }

    private OpplastetVedlegg lagOpplastetVedlegg() {
        return new OpplastetVedlegg()
                .withVedleggType(VEDLEGGTYPE)
                .withEier(EIER);
    }

    private List<Vedleggstatus> lagIkkeOpplastedePaakrevdeVedlegg() {
        List<Vedleggstatus> paakrevdeVedlegg = new ArrayList<>();
        paakrevdeVedlegg.add(new Vedleggstatus()
                .withEier(EIER)
                .withStatus(Vedleggstatus.Status.VedleggKreves)
                .withVedleggType(VEDLEGGTYPE)
                .withSendtSoknadId(SOKNAD_UNDER_ARBEID_ID));
        paakrevdeVedlegg.add(null);
        return paakrevdeVedlegg;
    }

    private Optional<SendtSoknad> lagSendtSoknad() {
        return Optional.of(new SendtSoknad().withEier(EIER)
                .withBehandlingsId(BEHANDLINGSID)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withFiksforsendelseId(FIKSFORSENDELSEID)
                .withOrgnummer(ORGNR)
                .withBrukerOpprettetDato(OPPRETTET_DATO)
                .withBrukerFerdigDato(SIST_ENDRET_DATO)
                .withSendtDato(now()));
    }
}