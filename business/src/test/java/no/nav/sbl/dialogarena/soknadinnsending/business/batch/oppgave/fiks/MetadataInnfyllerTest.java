package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.*;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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
    private static final LocalDateTime BRUKER_FERDIG_DATO = now();
    private static final LocalDateTime BRUKER_FERDIG_DATO_METADATA = now().minusDays(1L);
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;
    @Mock
    private SendtSoknadRepository sendtSoknadRepository;

    @InjectMocks
    MetadataInnfyller metadataInnfyller;

    @Before
    public void setup() {
        when(soknadMetadataRepository.hent(anyString())).thenReturn(lagSoknadMetadata());
        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(lagSendtSoknadForNySoknad());
    }

    @Test
    public void byggOppFiksDataHenterMetadataFraSendtSoknadHvisDenFinnes() {
        FiksData fiksData = new FiksData();
        fiksData.behandlingsId = BEHANDLINGSID;
        fiksData.avsenderFodselsnummer = EIER;

        metadataInnfyller.byggOppFiksData(fiksData);

        assertThat(fiksData.mottakerOrgNr, is(ORGNUMMER));
        assertThat(fiksData.mottakerNavn, is(ENHETSNAVN));
        assertThat(fiksData.innsendtDato, is(BRUKER_FERDIG_DATO));
        assertThat(fiksData.ettersendelsePa, nullValue());
    }

    @Test
    public void byggOppFiksDataHenterMetadataFraSoknadMetadataHvisSendtSoknadIkkeFinnes() {
        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(Optional.empty());

        FiksData fiksData = new FiksData();
        fiksData.behandlingsId = BEHANDLINGSID;

        metadataInnfyller.byggOppFiksData(fiksData);

        assertThat(fiksData.avsenderFodselsnummer, is(EIER));
        assertThat(fiksData.mottakerOrgNr, is(ORGNUMMER_METADATA));
        assertThat(fiksData.mottakerNavn, is(ENHETSNAVN_METADATA));
        assertThat(fiksData.innsendtDato, is(BRUKER_FERDIG_DATO_METADATA));
        assertThat(fiksData.ettersendelsePa, nullValue());
    }

    @Test
    public void finnOriginalFiksForsendelseIdVedEttersendelseBrukerFiksIdFraSendtSoknadHvisDenFinnes() {
        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(lagSendtSoknadForEttersendelse());

        final String originalFiksForsendelseId = metadataInnfyller.finnOriginalFiksForsendelseIdVedEttersendelse(TILKNYTTET_BEHANDLINGSID, EIER);

        assertThat(originalFiksForsendelseId, is(FIKSFORSENDELSEID));
    }

    @Test
    public void finnOriginalFiksForsendelseIdVedEttersendelseBrukerFiksIdFraSoknadMetadataVedEttersendelsePaGammelSoknad() {
        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(Optional.empty());
        when(soknadMetadataRepository.hent(anyString())).thenReturn(lagSoknadMetadataForEttersendelse());

        final String originalFiksForsendelseId = metadataInnfyller.finnOriginalFiksForsendelseIdVedEttersendelse(TILKNYTTET_BEHANDLINGSID, EIER);

        assertThat(originalFiksForsendelseId, is(FIKSFORSENDELSEID_METADATA));
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

    private Optional<SendtSoknad> lagSendtSoknadForNySoknad() {
        return Optional.ofNullable(new SendtSoknad()
                .withBehandlingsId(BEHANDLINGSID)
                .withOrgnummer(ORGNUMMER)
                .withNavEnhetsnavn(ENHETSNAVN)
                .withBrukerFerdigDato(BRUKER_FERDIG_DATO));
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
        mockData.orgnr = ORGNUMMER_METADATA;
        mockData.navEnhet = ENHETSNAVN_METADATA;
        mockData.fnr = EIER;
        mockData.innsendtDato = BRUKER_FERDIG_DATO_METADATA;

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