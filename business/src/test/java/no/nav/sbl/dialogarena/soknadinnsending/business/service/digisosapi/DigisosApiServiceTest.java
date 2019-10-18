package no.nav.sbl.dialogarena.soknadinnsending.business.service.digisosapi;

import no.nav.sbl.dialogarena.soknadinnsending.business.SoknadServiceIntegrationTestContext;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.FilMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.digisosapi.FilOpplasting;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.domain.VedleggType;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.pdf.PDFService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService.createEmptyJsonInternalSoknad;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = SoknadServiceIntegrationTestContext.class)
public class DigisosApiServiceTest {

    @Mock
    PDFService pdfService;

    @Mock
    InnsendingService innsendingService;

    @InjectMocks
    private DigisosApiService digisosApiService;

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty("idporten_config_url", "https://oidc-ver2.difi.no/idporten-oidc-provider/.well-known/openid-configuration");
        System.setProperty("idporten_scope", "ks:fiks");
        System.setProperty("idporten_clientid", "1c3631f4-dbf2-4c12-bdc4-156cbd53c625");
        System.setProperty("idporten_token_url", "https://oidc-ver2.difi.no/idporten-oidc-provider/token");
        System.setProperty("digisos_api_baseurl", "https://api.fiks.test.ks.no/");
        System.setProperty("integrasjonsid_fiks", "c4bf2682-327f-4535-a087-c248d35978e1");
    }

    @Before
    public void setUpBefore() throws Exception {
        when(pdfService.genererSaksbehandlerPdf(any(JsonInternalSoknad.class), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(pdfService.genererJuridiskPdf(any(JsonInternalSoknad.class), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(pdfService.genererBrukerkvitteringPdf(any(JsonInternalSoknad.class), anyString(), anyBoolean(), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(pdfService.genererEttersendelsePdf(any(JsonInternalSoknad.class), anyString(), anyString())).thenReturn(new byte[]{1, 2, 3});
        when(innsendingService.hentSoknadUnderArbeid(anyString(), anyString())).thenReturn(new SoknadUnderArbeid());
    }

    @Test
    public void skalLageOpplastingsListeMedDokumenterForSoknad() {
        SoknadUnderArbeid soknadUnderArbeid = new SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad("12345678910"));

        List<FilOpplasting> filOpplastings = digisosApiService.lagDokumentListe(soknadUnderArbeid);

        FilMetadata metadataFil1 = filOpplastings.get(0).metadata;
        assertThat(metadataFil1.filnavn).isEqualTo("soknad.pdf");
        assertThat(metadataFil1.mimetype).isEqualTo("application/pdf");


        FilMetadata metadataFil3 = filOpplastings.get(1).metadata;
        assertThat(metadataFil3.filnavn).isEqualTo("Soknad-juridisk.pdf");
        assertThat(metadataFil3.mimetype).isEqualTo("application/pdf");

        FilMetadata metadataFil4 = filOpplastings.get(2).metadata;
        assertThat(metadataFil4.filnavn).isEqualTo("Brukerkvittering.pdf");
        assertThat(metadataFil4.mimetype).isEqualTo("application/pdf");
    }

    @Test
    public void hentDokumenterFraSoknadReturnererTreDokumenterForEttersendingMedEtVedlegg() {
        when(innsendingService.hentAlleOpplastedeVedleggForSoknad(any(SoknadUnderArbeid.class))).thenReturn(lagOpplastetVedlegg());

        List<FilOpplasting> fiksDokumenter = digisosApiService.lagDokumentListe(new SoknadUnderArbeid()
                .withTilknyttetBehandlingsId("123")
                .withJsonInternalSoknad(lagInternalSoknadForEttersending()));

        Assert.assertThat(fiksDokumenter.size(), is(3));
        Assert.assertThat(fiksDokumenter.get(0).metadata.filnavn, is("ettersendelse.pdf"));
        Assert.assertThat(fiksDokumenter.get(1).metadata.filnavn, is("Brukerkvittering.pdf"));
        Assert.assertThat(fiksDokumenter.get(2).metadata.filnavn, is("FILNAVN"));
    }

    private JsonInternalSoknad lagInternalSoknadForEttersending() {
        List<JsonFiler> jsonFiler = new ArrayList<>();
        jsonFiler.add(new JsonFiler().withFilnavn("FILNAVN").withSha512("sha512"));
        List<JsonVedlegg> jsonVedlegg = new ArrayList<>();
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name())
                .withType("type")
                .withTilleggsinfo("tilleggsinfo")
                .withFiler(jsonFiler));
        return new JsonInternalSoknad()
                .withVedlegg(new JsonVedleggSpesifikasjon().withVedlegg(jsonVedlegg));
    }


    private List<OpplastetVedlegg> lagOpplastetVedlegg() {
        List<OpplastetVedlegg> opplastedeVedlegg = new ArrayList<>();
        opplastedeVedlegg.add(new OpplastetVedlegg()
                .withFilnavn("FILNAVN")
                .withSha512("sha512")
                .withVedleggType(new VedleggType("type|tilleggsinfo"))
                .withData(new byte[]{1, 2, 3}));
        return opplastedeVedlegg;
    }

}
