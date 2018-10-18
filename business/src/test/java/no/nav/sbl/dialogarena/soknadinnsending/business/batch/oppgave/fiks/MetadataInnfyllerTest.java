package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.FilData;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.HovedskjemaMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadata;
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
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
    private static final LocalDateTime SIST_OPPDATERT = now();
    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;
    @Mock
    private SendtSoknadRepository sendtSoknadRepository;

    @InjectMocks
    MetadataInnfyller metadataInnfyller;

    @Before
    public void setup() {
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(lagSoknadUnderArbeidForNySoknad());
        when(soknadMetadataRepository.hent(anyString())).thenReturn(lagSoknadMetadata());
    }

    @Test
    public void byggOppFiksDataHenterMetdataFraSoknadUnderArbeid() {
        FiksData fiksData = new FiksData();
        fiksData.behandlingsId = BEHANDLINGSID;
        fiksData.avsenderFodselsnummer = EIER;

        metadataInnfyller.byggOppFiksData(fiksData);

        //assertThat(fiksData.mottakerOrgNr, notNullValue());
        //assertThat(fiksData.mottakerNavn, notNullValue());
        assertThat(fiksData.innsendtDato, is(SIST_OPPDATERT));
        assertThat(fiksData.ettersendelsePa, nullValue());
    }

    @Test
    public void finnOriginalFiksforsendelseIdBrukerFiksIdFraSendtSoknadHvisDenFinnes() {
        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(lagSendtSoknadForEttersendelse());

        final String fiksforsendelseId = metadataInnfyller.finnOriginalFiksforsendelseId(lagSoknadMetadata(), EIER, TILKNYTTET_BEHANDLINGSID);

        assertThat(fiksforsendelseId, is(FIKSFORSENDELSEID));
    }

    @Test
    public void finnOriginalFiksforsendelseIdBrukerFiksIdFraSoknadMetadataVedEttersendelsePaGammelSoknad() {
        when(sendtSoknadRepository.hentSendtSoknad(anyString(), anyString())).thenReturn(Optional.empty());
        when(soknadMetadataRepository.hent(anyString())).thenReturn(lagSoknadMetadataForEttersendelse());

        final String fiksforsendelseId = metadataInnfyller.finnOriginalFiksforsendelseId(lagSoknadMetadata(), EIER, TILKNYTTET_BEHANDLINGSID);

        assertThat(fiksforsendelseId, is(FIKSFORSENDELSEID_METADATA));
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
                .withSistEndretDato(SIST_OPPDATERT));
    }

    private Optional<SendtSoknad> lagSendtSoknadForEttersendelse() {
        return Optional.ofNullable(new SendtSoknad()
                .withFiksforsendelseId(FIKSFORSENDELSEID));
    }

    private SoknadMetadata lagSoknadMetadataForEttersendelse() {
        SoknadMetadata soknadMetadata = new SoknadMetadata();
        soknadMetadata.fiksForsendelseId = FIKSFORSENDELSEID_METADATA;
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