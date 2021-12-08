//package no.nav.sosialhjelp.soknad.business;
//
//import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
//import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
//import no.nav.sosialhjelp.soknad.business.db.repositories.opplastetvedlegg.OpplastetVedleggRepository;
//import no.nav.sosialhjelp.soknad.business.db.repositories.sendtsoknad.SendtSoknadRepository;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
//import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository;
//import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
//import no.nav.sosialhjelp.soknad.domain.SendtSoknad;
//import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
//import no.nav.sosialhjelp.soknad.domain.VedleggType;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import org.springframework.transaction.support.SimpleTransactionStatus;
//import org.springframework.transaction.support.TransactionCallbackWithoutResult;
//import org.springframework.transaction.support.TransactionTemplate;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static java.time.LocalDateTime.now;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@MockitoSettings(strictness = Strictness.LENIENT)
//class InnsendingServiceTest {
//    private static final Long SOKNAD_UNDER_ARBEID_ID = 1L;
//    private static final Long SENDT_SOKNAD_ID = 2L;
//    private static final String EIER = "12345678910";
//    private static final VedleggType VEDLEGGTYPE = new VedleggType("bostotte|annetboutgift");
//    private static final String BEHANDLINGSID = "1100001L";
//    private static final String TILKNYTTET_BEHANDLINGSID = "1100002K";
//    private static final String FIKSFORSENDELSEID = "12345";
//    private static final String ORGNR = "012345678";
//    private static final String ORGNR_METADATA = "8888";
//    private static final String NAVENHETSNAVN = "NAV Enhet";
//    private static final String NAVENHETSNAVN_METADATA = "NAV Enhet2";
//    private static final LocalDateTime OPPRETTET_DATO = now().minusSeconds(50);
//    private static final LocalDateTime SIST_ENDRET_DATO = now();
//    @Mock
//    private TransactionTemplate transactionTemplate;
//    @Mock
//    private SendtSoknadRepository sendtSoknadRepository;
//    @Mock
//    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
//    @Mock
//    private OpplastetVedleggRepository opplastetVedleggRepository;
//    @Mock
//    private SoknadUnderArbeidService soknadUnderArbeidService;
//    @Mock
//    private SoknadMetadataRepository soknadMetadataRepository;
//    @InjectMocks
//    private InnsendingService innsendingService;
//
//    @BeforeEach
//    public void setUp() {
//        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
//            Object[] args = invocation.getArguments();
//            TransactionCallbackWithoutResult arg = (TransactionCallbackWithoutResult) args[0];
//            return arg.doInTransaction(new SimpleTransactionStatus());
//        });
//        when(sendtSoknadRepository.opprettSendtSoknad(any(), anyString())).thenReturn(SENDT_SOKNAD_ID);
//        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(createSendtSoknad());
//    }
//
//    @Test
//    void opprettSendtSoknadOppretterSendtSoknadOgVedleggstatus() {
//        innsendingService.opprettSendtSoknad(createSoknadUnderArbeid().
//                withJsonInternalSoknad(createJsonInternalSoknadWithOrgnrAndNavEnhetsnavn()));
//
//        verify(soknadUnderArbeidRepository, times(1)).oppdaterInnsendingStatus(any(SoknadUnderArbeid.class), eq(EIER));
//        verify(sendtSoknadRepository, times(1)).opprettSendtSoknad(any(SendtSoknad.class), eq(EIER));
//    }
//
//    @Test
//    void mapSoknadUnderArbeidTilSendtSoknadMapperInfoRiktig() {
//        SendtSoknad sendtSoknad = innsendingService.mapSoknadUnderArbeidTilSendtSoknad(createSoknadUnderArbeid().withJsonInternalSoknad(
//                createJsonInternalSoknadWithOrgnrAndNavEnhetsnavn()
//        ));
//
//        assertThat(sendtSoknad.getBehandlingsId()).isEqualTo(BEHANDLINGSID);
//        assertThat(sendtSoknad.getTilknyttetBehandlingsId()).isNull();
//        assertThat(sendtSoknad.getEier()).isEqualTo(EIER);
//        assertThat(sendtSoknad.getOrgnummer()).isEqualTo(ORGNR);
//        assertThat(sendtSoknad.getNavEnhetsnavn()).isEqualTo(NAVENHETSNAVN);
//        assertThat(sendtSoknad.getBrukerOpprettetDato()).isEqualTo(OPPRETTET_DATO);
//        assertThat(sendtSoknad.getBrukerFerdigDato()).isEqualTo(SIST_ENDRET_DATO);
//        assertThat(sendtSoknad.getSendtDato()).isNull();
//        assertThat(sendtSoknad.getFiksforsendelseId()).isNull();
//    }
//
//    @Test
//    void mapSoknadUnderArbeidTilSendtSoknadKasterFeilHvisIkkeEttersendingOgMottakerinfoMangler() {
//        var soknadUnderArbeid = createSoknadUnderArbeidUtenTilknyttetBehandlingsid().withJsonInternalSoknad(new JsonInternalSoknad().withMottaker(null));
//        assertThatExceptionOfType(IllegalStateException.class)
//                .isThrownBy(() -> innsendingService.mapSoknadUnderArbeidTilSendtSoknad(soknadUnderArbeid));
//    }
//
//    @Test
//    void finnSendtSoknadForEttersendelseHenterMottakerinfoFraSendtSoknadVedEttersendelse() {
//        SendtSoknad sendtSoknad = innsendingService.finnSendtSoknadForEttersendelse(createSoknadUnderArbeidForEttersendelse());
//
//        assertThat(sendtSoknad.getOrgnummer()).isEqualTo(ORGNR);
//        assertThat(sendtSoknad.getNavEnhetsnavn()).isEqualTo(NAVENHETSNAVN);
//        assertThat(sendtSoknad.getTilknyttetBehandlingsId()).isEqualTo(TILKNYTTET_BEHANDLINGSID);
//    }
//
//    @Test
//    void finnSendtSoknadForEttersendelseHenterInfoFraSoknadMetadataHvisSendtSoknadMangler() {
//        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(Optional.empty());
//        when(soknadMetadataRepository.hent(anyString())).thenReturn(createSoknadMetadata());
//
//        SendtSoknad soknadMedMottaksinfoFraMetadata = innsendingService.finnSendtSoknadForEttersendelse(createSoknadUnderArbeidForEttersendelse());
//
//        assertThat(soknadMedMottaksinfoFraMetadata.getOrgnummer()).isEqualTo(ORGNR_METADATA);
//        assertThat(soknadMedMottaksinfoFraMetadata.getNavEnhetsnavn()).isEqualTo(NAVENHETSNAVN_METADATA);
//    }
//
//    @Test
//    void finnSendtSoknadForEttersendelseKasterFeilHvisSendtSoknadOgMetadataManglerForEttersendelse() {
//        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(Optional.empty());
//        when(soknadMetadataRepository.hent(anyString())).thenReturn(null);
//
//        assertThatExceptionOfType(IllegalStateException.class)
//                .isThrownBy(() -> innsendingService.finnSendtSoknadForEttersendelse(createSoknadUnderArbeidForEttersendelse()));
//    }
//
//    private SoknadUnderArbeid createSoknadUnderArbeid() {
//        return new SoknadUnderArbeid()
//                .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
//                .withBehandlingsId(BEHANDLINGSID)
//                .withEier(EIER)
//                .withOpprettetDato(OPPRETTET_DATO)
//                .withSistEndretDato(SIST_ENDRET_DATO);
//    }
//
//    private JsonInternalSoknad createJsonInternalSoknadWithOrgnrAndNavEnhetsnavn() {
//        return new JsonInternalSoknad().withMottaker(new JsonSoknadsmottaker()
//                .withOrganisasjonsnummer(ORGNR)
//                .withNavEnhetsnavn(NAVENHETSNAVN));
//    }
//
//    private SoknadUnderArbeid createSoknadUnderArbeidForEttersendelse() {
//        return new SoknadUnderArbeid()
//                .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
//                .withBehandlingsId(BEHANDLINGSID)
//                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
//                .withEier(EIER)
//                .withOpprettetDato(OPPRETTET_DATO)
//                .withSistEndretDato(SIST_ENDRET_DATO);
//    }
//
//    private SoknadUnderArbeid createSoknadUnderArbeidUtenTilknyttetBehandlingsid() {
//        return new SoknadUnderArbeid()
//                .withSoknadId(SOKNAD_UNDER_ARBEID_ID)
//                .withBehandlingsId(BEHANDLINGSID)
//                .withEier(EIER)
//                .withOpprettetDato(OPPRETTET_DATO)
//                .withSistEndretDato(SIST_ENDRET_DATO);
//    }
//
//    private Optional<SendtSoknad> createSendtSoknad() {
//        return Optional.of(new SendtSoknad().withEier(EIER)
//                .withBehandlingsId(BEHANDLINGSID)
//                .withTilknyttetBehandlingsId(TILKNYTTET_BEHANDLINGSID)
//                .withFiksforsendelseId(FIKSFORSENDELSEID)
//                .withOrgnummer(ORGNR)
//                .withNavEnhetsnavn(NAVENHETSNAVN)
//                .withBrukerOpprettetDato(OPPRETTET_DATO)
//                .withBrukerFerdigDato(SIST_ENDRET_DATO)
//                .withSendtDato(now()));
//    }
//
//    private SoknadMetadata createSoknadMetadata() {
//        SoknadMetadata soknadMetadata = new SoknadMetadata();
//        soknadMetadata.orgnr = ORGNR_METADATA;
//        soknadMetadata.navEnhet = NAVENHETSNAVN_METADATA;
//        return soknadMetadata;
//    }
//}