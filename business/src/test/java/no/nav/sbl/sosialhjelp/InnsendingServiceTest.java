package no.nav.sbl.sosialhjelp;

import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sosialhjelp.soknad.domain.SendtSoknad;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import no.nav.sosialhjelp.soknad.domain.VedleggType;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.OpplastetVedleggRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InnsendingServiceTest {
    private static final Long SOKNAD_UNDER_ARBEID_ID = 1L;
    private static final Long SENDT_SOKNAD_ID = 2L;
    private static final String EIER = "12345678910";
    private static final VedleggType VEDLEGGTYPE = new VedleggType("bostotte|annetboutgift");
    private static final String BEHANDLINGSID = "1100001L";
    private static final String TILKNYTTET_BEHANDLINGSID = "1100002K";
    private static final String FIKSFORSENDELSEID = "12345";
    private static final String ORGNR = "012345678";
    private static final String ORGNR_METADATA = "8888";
    private static final String NAVENHETSNAVN = "NAV Enhet";
    private static final String NAVENHETSNAVN_METADATA = "NAV Enhet2";
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
    private SoknadUnderArbeidService soknadUnderArbeidService;
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;
    @InjectMocks
    private InnsendingService innsendingService;

    @Before
    public void setUp() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            TransactionCallbackWithoutResult arg = (TransactionCallbackWithoutResult) args[0];
            return arg.doInTransaction(new SimpleTransactionStatus());
        });
        when(sendtSoknadRepository.opprettSendtSoknad(any(), anyString())).thenReturn(SENDT_SOKNAD_ID);
        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(createSendtSoknad());
    }

    @Test
    public void opprettSendtSoknadOppretterSendtSoknadOgVedleggstatus() {
        innsendingService.opprettSendtSoknad(createSoknadUnderArbeid().
                withJsonInternalSoknad(createJsonInternalSoknadWithOrgnrAndNavEnhetsnavn()));

        verify(soknadUnderArbeidRepository, times(1)).oppdaterInnsendingStatus(any(SoknadUnderArbeid.class), eq(EIER));
        verify(sendtSoknadRepository, times(1)).opprettSendtSoknad(any(SendtSoknad.class), eq(EIER));
    }

    @Test
    public void mapSoknadUnderArbeidTilSendtSoknadMapperInfoRiktig() {
        SendtSoknad sendtSoknad = innsendingService.mapSoknadUnderArbeidTilSendtSoknad(createSoknadUnderArbeid().withJsonInternalSoknad(
                createJsonInternalSoknadWithOrgnrAndNavEnhetsnavn()
        ));

        assertThat(sendtSoknad.getBehandlingsId(), is(BEHANDLINGSID));
        assertThat(sendtSoknad.getTilknyttetBehandlingsId(), nullValue());
        assertThat(sendtSoknad.getEier(), is(EIER));
        assertThat(sendtSoknad.getOrgnummer(), is(ORGNR));
        assertThat(sendtSoknad.getNavEnhetsnavn(), is(NAVENHETSNAVN));
        assertThat(sendtSoknad.getBrukerOpprettetDato(), is(OPPRETTET_DATO));
        assertThat(sendtSoknad.getBrukerFerdigDato(), is(SIST_ENDRET_DATO));
        assertThat(sendtSoknad.getSendtDato(), nullValue());
        assertThat(sendtSoknad.getFiksforsendelseId(), nullValue());
    }

    @Test(expected = IllegalStateException.class)
    public void mapSoknadUnderArbeidTilSendtSoknadKasterFeilHvisIkkeEttersendingOgMottakerinfoMangler() {
        innsendingService.mapSoknadUnderArbeidTilSendtSoknad(createSoknadUnderArbeidUtenTilknyttetBehandlingsid()
                .withJsonInternalSoknad(new JsonInternalSoknad().withMottaker(null)));
    }

    @Test
    public void finnSendtSoknadForEttersendelseHenterMottakerinfoFraSendtSoknadVedEttersendelse() {
        SendtSoknad sendtSoknad = innsendingService.finnSendtSoknadForEttersendelse(createSoknadUnderArbeidForEttersendelse());

        assertThat(sendtSoknad.getOrgnummer(), is(ORGNR));
        assertThat(sendtSoknad.getNavEnhetsnavn(), is(NAVENHETSNAVN));
        assertThat(sendtSoknad.getTilknyttetBehandlingsId(), is(TILKNYTTET_BEHANDLINGSID));
    }

    @Test
    public void finnSendtSoknadForEttersendelseHenterInfoFraSoknadMetadataHvisSendtSoknadMangler() {
        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(Optional.empty());
        when(soknadMetadataRepository.hent(anyString())).thenReturn(createSoknadMetadata());

        SendtSoknad soknadMedMottaksinfoFraMetadata = innsendingService.finnSendtSoknadForEttersendelse(createSoknadUnderArbeidForEttersendelse());

        assertThat(soknadMedMottaksinfoFraMetadata.getOrgnummer(), is(ORGNR_METADATA));
        assertThat(soknadMedMottaksinfoFraMetadata.getNavEnhetsnavn(), is(NAVENHETSNAVN_METADATA));
    }

    @Test(expected = IllegalStateException.class)
    public void finnSendtSoknadForEttersendelseKasterFeilHvisSendtSoknadOgMetadataManglerForEttersendelse() {
        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(Optional.empty());
        when(soknadMetadataRepository.hent(anyString())).thenReturn(null);

        innsendingService.finnSendtSoknadForEttersendelse(createSoknadUnderArbeidForEttersendelse());
    }

    private SoknadUnderArbeid createSoknadUnderArbeid() {
        return new SoknadUnderArbeid()
                .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
                .withBehandlingsId(BEHANDLINGSID)
                .withEier(EIER)
                .withOpprettetDato(OPPRETTET_DATO)
                .withSistEndretDato(SIST_ENDRET_DATO);
    }

    private JsonInternalSoknad createJsonInternalSoknadWithOrgnrAndNavEnhetsnavn() {
        return new JsonInternalSoknad().withMottaker(new JsonSoknadsmottaker()
                .withOrganisasjonsnummer(ORGNR)
                .withNavEnhetsnavn(NAVENHETSNAVN));
    }

    private SoknadUnderArbeid createSoknadUnderArbeidForEttersendelse() {
        return new SoknadUnderArbeid()
                .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
                .withBehandlingsId(BEHANDLINGSID)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withEier(EIER)
                .withOpprettetDato(OPPRETTET_DATO)
                .withSistEndretDato(SIST_ENDRET_DATO);
    }

    private SoknadUnderArbeid createSoknadUnderArbeidUtenTilknyttetBehandlingsid() {
        return new SoknadUnderArbeid()
                .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
                .withBehandlingsId(BEHANDLINGSID)
                .withEier(EIER)
                .withOpprettetDato(OPPRETTET_DATO)
                .withSistEndretDato(SIST_ENDRET_DATO);
    }

    private Optional<SendtSoknad> createSendtSoknad() {
        return Optional.of(new SendtSoknad().withEier(EIER)
                .withBehandlingsId(BEHANDLINGSID)
                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
                .withFiksforsendelseId(FIKSFORSENDELSEID)
                .withOrgnummer(ORGNR)
                .withNavEnhetsnavn(NAVENHETSNAVN)
                .withBrukerOpprettetDato(OPPRETTET_DATO)
                .withBrukerFerdigDato(SIST_ENDRET_DATO)
                .withSendtDato(now()));
    }

    private SoknadMetadata createSoknadMetadata() {
        SoknadMetadata soknadMetadata = new SoknadMetadata();
        soknadMetadata.orgnr = ORGNR_METADATA;
        soknadMetadata.navEnhet = NAVENHETSNAVN_METADATA;
        return soknadMetadata;
    }
}