package no.nav.sbl.sosialhjelp.midlertidig;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.VedleggType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VedleggConverterTest {
    private static final Long SOKNAD_UNDER_ARBEID_ID = 1L;
    private static final String EIER = "12345678910";
    private static final byte[] DATA = {1, 2, 3, 4};
    private static final String SHA512 = ServiceUtils.getSha512FromByteArray(DATA);
    private static final String TYPE = "bostotte|annetboutgift";
    private static final VedleggType VEDLEGG_TYPE = new VedleggType(TYPE);
    private static final String FILNAVN = "dokumentasjon.pdf";

    @Mock
    private VedleggService vedleggService;

    @InjectMocks
    private VedleggConverter vedleggConverter;

    @Before
    public void setUp() {
        when(vedleggService.hentVedlegg(anyLong(), anyBoolean())).thenReturn(lagOpplastetVedleggMedData());
    }

    @Test
    public void mapVedleggListeTilOpplastetVedleggListeReturnererListeMedEtVedleggHvisEtVedleggErLastetOpp() {
        List<Vedlegg> vedlegg = new ArrayList<>();
        vedlegg.add(lagOpplastetVedleggUtenData());
        vedlegg.add(lagVedleggMedStatusVedleggKreves());

        List<OpplastetVedlegg> opplastedeVedlegg = vedleggConverter.mapVedleggListeTilOpplastetVedleggListe(SOKNAD_UNDER_ARBEID_ID, EIER, vedlegg);

        assertThat(opplastedeVedlegg.size(), is(1));
        assertThat(opplastedeVedlegg.get(0).getFilnavn(), is(FILNAVN));
    }

    @Test
    public void mapVedleggListeTilOpplastetVedleggListeReturnererTomListeHvisVedlegglistenErNull() {
        List<Vedlegg> vedlegg = new ArrayList<>();

        List<OpplastetVedlegg> opplastedeVedlegg = vedleggConverter.mapVedleggListeTilOpplastetVedleggListe(SOKNAD_UNDER_ARBEID_ID, EIER, vedlegg);

        assertThat(opplastedeVedlegg.size(), is(0));
    }

    @Test
    public void mapVedleggListeTilOpplastetVedleggListeReturnererNullHvisSoknadIdMangler() {
        List<Vedlegg> vedlegg = new ArrayList<>();
        vedlegg.add(lagOpplastetVedleggUtenData());

        List<OpplastetVedlegg> opplastedeVedlegg = vedleggConverter.mapVedleggListeTilOpplastetVedleggListe(null, EIER, vedlegg);

        assertThat(opplastedeVedlegg, nullValue());
    }

    @Test
    public void mapVedleggTilOpplastetVedleggMapperInformasjonRiktig() {
        OpplastetVedlegg opplastetVedlegg = vedleggConverter.mapVedleggTilOpplastetVedlegg(SOKNAD_UNDER_ARBEID_ID, EIER,
                lagOpplastetVedleggUtenData());

        assertThat(opplastetVedlegg.getSoknadId(), is(SOKNAD_UNDER_ARBEID_ID));
        assertThat(opplastetVedlegg.getVedleggType().getSammensattType(), is(TYPE));
        assertThat(opplastetVedlegg.getData(), is(DATA));
        assertThat(opplastetVedlegg.getEier(), is(EIER));
        assertThat(opplastetVedlegg.getFilnavn(), is(FILNAVN));
        assertThat(opplastetVedlegg.getSha512(), is(SHA512));
    }

    @Test
    public void mapVedleggTilOpplastetVedleggReturnererNullHvisTjenestenReturnererNull() {
        when(vedleggService.hentVedlegg(anyLong(), anyBoolean())).thenReturn(null);

        OpplastetVedlegg opplastetVedlegg = vedleggConverter.mapVedleggTilOpplastetVedlegg(SOKNAD_UNDER_ARBEID_ID, EIER, new Vedlegg());

        assertThat(opplastetVedlegg, nullValue());
    }

    private Vedlegg lagOpplastetVedleggUtenData() {
        return new Vedlegg().medVedleggId(1L)
                .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
                .medFilnavn(FILNAVN)
                .medSkjemaNummer(VEDLEGG_TYPE.getType())
                .medSkjemanummerTillegg(VEDLEGG_TYPE.getTilleggsinfo())
                .medSha512(SHA512);
    }

    private Vedlegg lagOpplastetVedleggMedData() {
        return lagOpplastetVedleggUtenData()
                .medData(DATA);
    }

    private Vedlegg lagVedleggMedStatusVedleggKreves() {
        return new Vedlegg().medVedleggId(2L)
                .medInnsendingsvalg(Vedlegg.Status.VedleggKreves)
                .medFilnavn("ikke i bruk");
    }
}