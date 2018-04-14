package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.FilData;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.HovedskjemaMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetadataInnfyllerTest {

    @Mock
    SoknadMetadataRepository soknadMetadataRepository;

    @InjectMocks
    MetadataInnfyller metadataInnfyller;

    @Before
    public void setup() {
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

        when(soknadMetadataRepository.hent("id1234")).thenReturn(mockData);
    }

    @Test
    public void dokumentInfoerBlirRiktig() {
        FiksData fiksData = new FiksData();
        fiksData.behandlingsId = "id1234";
        metadataInnfyller.byggOppFiksData(fiksData);

        assertEquals(5, fiksData.dokumentInfoer.size());
        assertEquals("soknad.json", fiksData.dokumentInfoer.get(0).filnavn);
        assertEquals("Soknad.pdf", fiksData.dokumentInfoer.get(1).filnavn);
        assertEquals("vedlegg.json", fiksData.dokumentInfoer.get(2).filnavn);
        assertEquals("Soknad-juridisk.pdf", fiksData.dokumentInfoer.get(3).filnavn);
        assertEquals("uid-v1", fiksData.dokumentInfoer.get(4).uuid);
    }

}