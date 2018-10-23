package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.*;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.internal.JsonSoknadsmottaker;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks.MetadataInnfyller.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetadataInnfyllerTest {

    private static final String BEHANDLINGSID = "id1234";
    private static final String TILKNYTTET_BEHANDLINGSID = "id5678";
    private static final String EIER = "12345678910";
    private static final String FIKSFORSENDELSEID = "987654";
    private static final String FIKSFORSENDELSEID_METADATA = "111111";
    private static final String ORGNUMMER = "8888888";
    private static final String ORGNUMMER_METADATA = "4444444";
    private static final String ENHETSNAVN = "NAV Moss";
    private static final String ENHETSNAVN_METADATA = "NAV Aremark";
    private static final LocalDateTime SIST_OPPDATERT = now();
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @Mock
    private SendtSoknadRepository sendtSoknadRepository;
    @Mock
    private SoknadUnderArbeidService soknadUnderArbeidService;

    @InjectMocks
    MetadataInnfyller metadataInnfyller;

    @Before
    public void setup() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(lagSoknadUnderArbeidForNySoknad());
        when(soknadMetadataRepository.hent(anyString())).thenReturn(lagSoknadMetadata());
        when(soknadUnderArbeidService.hentJsonInternalSoknadFraSoknadUnderArbeid(any(SoknadUnderArbeid.class)))
                .thenReturn(new JsonInternalSoknad().withMottaker(new JsonSoknadsmottaker()
                        .withOrganisasjonsnummer(ORGNUMMER)
                        .withNavEnhetsnavn(ENHETSNAVN)));
    }

    @Test
    public void byggOppFiksDataHenterMetdataFraSoknadUnderArbeid() {
        FiksData fiksData = new FiksData();
        fiksData.behandlingsId = BEHANDLINGSID;
        fiksData.avsenderFodselsnummer = EIER;

        metadataInnfyller.byggOppFiksData(fiksData);

        assertThat(fiksData.mottakerOrgNr, is(ORGNUMMER));
        assertThat(fiksData.mottakerNavn, is(ENHETSNAVN));
        assertThat(fiksData.innsendtDato, is(SIST_OPPDATERT));
        assertThat(fiksData.ettersendelsePa, nullValue());
    }

    @Test
    public void finnOriginalMottaksinfoVedEttersendelseBrukerFiksIdFraSendtSoknadHvisDenFinnes() {
        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(lagSendtSoknadForEttersendelse());

        final Map<String, String> mottakerinfo = metadataInnfyller.finnOriginalMottaksinfoVedEttersendelse(lagSoknadMetadata(), EIER, TILKNYTTET_BEHANDLINGSID);

        assertThat(mottakerinfo.get(FIKSFORSENDELSEID_KEY), is(FIKSFORSENDELSEID));
        assertThat(mottakerinfo.get(ORGNUMMER_KEY), is(ORGNUMMER));
        assertThat(mottakerinfo.get(NAVENHETSNAVN_KEY), is(ENHETSNAVN));
    }

    @Test
    public void finnOriginalMottaksinfoVedEttersendelseBrukerFiksIdFraSoknadMetadataVedEttersendelsePaGammelSoknad() {
        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(Optional.empty());
        when(soknadMetadataRepository.hent(anyString())).thenReturn(lagSoknadMetadataForEttersendelse());

        final Map<String, String> mottakerinfo = metadataInnfyller.finnOriginalMottaksinfoVedEttersendelse(lagSoknadMetadata(), EIER, TILKNYTTET_BEHANDLINGSID);

        assertThat(mottakerinfo.get(FIKSFORSENDELSEID_KEY), is(FIKSFORSENDELSEID_METADATA));
        assertThat(mottakerinfo.get(ORGNUMMER_KEY), is(ORGNUMMER_METADATA));
        assertThat(mottakerinfo.get(NAVENHETSNAVN_KEY), is(ENHETSNAVN_METADATA));
    }

    @Test
    public void dokumentInfoerBlirRiktig() {
        FiksData fiksData = new FiksData();
        fiksData.behandlingsId = BEHANDLINGSID;
        metadataInnfyller.byggOppDokumentInfo(fiksData, lagSoknadMetadata());

        assertEquals(5, fiksData.dokumentInfoer.size());
        assertEquals("soknad.json", fiksData.dokumentInfoer.get(0).filnavn);
        assertEquals("Soknad.pdf", fiksData.dokumentInfoer.get(1).filnavn);
        assertEquals("vedlegg.json", fiksData.dokumentInfoer.get(2).filnavn);
        assertEquals("Soknad-juridisk.pdf", fiksData.dokumentInfoer.get(3).filnavn);
        assertEquals("uid-v1", fiksData.dokumentInfoer.get(4).uuid);
    }

    private Optional<SoknadUnderArbeid> lagSoknadUnderArbeidForNySoknad() {
        return Optional.ofNullable(new SoknadUnderArbeid()
                .withBehandlingsId(BEHANDLINGSID)
                .withData(new byte[0])
                .withSistEndretDato(SIST_OPPDATERT));
    }

    private Optional<SendtSoknad> lagSendtSoknadForEttersendelse() {
        return Optional.ofNullable(new SendtSoknad()
                .withFiksforsendelseId(FIKSFORSENDELSEID)
                .withOrgnummer(ORGNUMMER)
                .withNavEnhetsnavn(ENHETSNAVN));
    }

    private SoknadMetadata lagSoknadMetadataForEttersendelse() {
        SoknadMetadata soknadMetadata = new SoknadMetadata();
        soknadMetadata.fiksForsendelseId = FIKSFORSENDELSEID_METADATA;
        soknadMetadata.orgnr = ORGNUMMER_METADATA;
        soknadMetadata.navEnhet = ENHETSNAVN_METADATA;
        return soknadMetadata;
    }

    private SoknadMetadata lagSoknadMetadata() {
        SoknadMetadata mockData = new SoknadMetadata();
        mockData.hovedskjema = new HovedskjemaMetadata();
        mockData.hovedskjema.filUuid = "uid-hoved";
        mockData.type = SoknadType.SEND_SOKNAD_KOMMUNAL;

        FilData json = new FilData();
        json.mimetype = "application/json";
        json.filnavn = "soknad.json";
        mockData.hovedskjema.alternativRepresentasjon.add(json);

        FilData vedleggJson = new FilData();
        vedleggJson.mimetype = "application/json";
        vedleggJson.filnavn = "vedlegg.json";
        mockData.hovedskjema.alternativRepresentasjon.add(vedleggJson);

        FilData fullPdf = new FilData();
        fullPdf.mimetype = "application/pdf-fullversjon";
        fullPdf.filnavn = "vedlegg.json";
        mockData.hovedskjema.alternativRepresentasjon.add(fullPdf);

        VedleggMetadata vedlegg1 = new VedleggMetadata();
        vedlegg1.status = Vedlegg.Status.LastetOpp;
        vedlegg1.filUuid = "uid-v1";
        mockData.vedlegg.vedleggListe.add(vedlegg1);

        VedleggMetadata vedlegg2 = new VedleggMetadata();
        vedlegg2.status = Vedlegg.Status.SendesSenere;
        vedlegg2.filUuid = "uid-v2";
        mockData.vedlegg.vedleggListe.add(vedlegg2);
        return mockData;
    }
}