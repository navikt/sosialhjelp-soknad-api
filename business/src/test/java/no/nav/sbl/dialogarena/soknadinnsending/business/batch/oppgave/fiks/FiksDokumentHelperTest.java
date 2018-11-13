package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.ks.svarut.servicesv9.Dokument;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import no.nav.sbl.soknadsosialhjelp.soknad.*;
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid;
import no.nav.sbl.soknadsosialhjelp.soknad.begrunnelse.JsonBegrunnelse;
import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.*;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.*;
import no.nav.sbl.soknadsosialhjelp.soknad.utdanning.JsonUtdanning;
import no.nav.sbl.soknadsosialhjelp.vedlegg.*;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.*;
import org.apache.cxf.attachment.ByteDataSource;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FiksDokumentHelperTest {
    private static final String FILNAVN = "vedlegg.pdf";
    private static final String ANNET_FILNAVN = "annetVedlegg.jpg";
    private static final String SHA512 = "sha512";
    private static final String ANNEN_SHA512 = "annensha512";
    private static final String TREDJE_FILNAVN = "tredjeVedlegg.jpg";
    private static final String TREDJE_SHA512 = "tredjesha512";
    private static final String TYPE = "bostotte";
    private static final String TILLEGGSINFO = "annetboutgift";
    private static final String TYPE2 = "dokumentasjon";
    private static final String TILLEGGSINFO2 = "aksjer";
    private static final byte[] DATA = {1, 2, 3};

    private DokumentKrypterer dokumentKrypterer = mock(DokumentKrypterer.class);
    private InnsendingService innsendingService = mock(InnsendingService.class);

    private FiksDokumentHelper fiksDokumentHelper;

    @Before
    public void setup() {
        when(dokumentKrypterer.krypterData(any())).thenReturn(new byte[]{3, 2, 1});
        when(innsendingService.hentAlleOpplastedeVedleggForSoknad(any(SoknadUnderArbeid.class))).thenReturn(lagOpplastedeVedlegg());
        fiksDokumentHelper = new FiksDokumentHelper(false, dokumentKrypterer, innsendingService);
    }

    @Test
    public void lagDokumentForSoknadJsonLagerKorrektDokument() {
        Dokument vedlegg = fiksDokumentHelper.lagDokumentForSoknadJson(lagInternalSoknad());

        assertThat(vedlegg.getFilnavn(), is("soknad.json"));
        assertThat(vedlegg.getMimetype(), is("application/json"));
        assertThat(vedlegg.isEkskluderesFraPrint(), is(true));
        assertThat(vedlegg.getData(), notNullValue());
    }

    @Test
    public void lagDokumentForVedleggJsonLagerKorrektDokument() {
        Dokument vedlegg = fiksDokumentHelper.lagDokumentForVedleggJson(lagInternalSoknadForVedlegg());

        assertThat(vedlegg.getFilnavn(), is("vedlegg.json"));
        assertThat(vedlegg.getMimetype(), is("application/json"));
        assertThat(vedlegg.isEkskluderesFraPrint(), is(true));
        assertThat(vedlegg.getData(), notNullValue());
    }

    @Test
    public void lagDokumentListeForVedleggReturnererRiktigeVedlegg() {
        List<Dokument> dokumenter = fiksDokumentHelper.lagDokumentListeForVedlegg(new SoknadUnderArbeid(), lagInternalSoknadForVedlegg());

        assertThat(dokumenter.size(), is(3));
        assertThat(dokumenter.get(0).getFilnavn(), is(FILNAVN));
        assertThat(dokumenter.get(1).getFilnavn(), is(ANNET_FILNAVN));
        assertThat(dokumenter.get(2).getFilnavn(), is(TREDJE_FILNAVN));
    }

    @Test
    public void jsonFilOgOpplastetVedleggErDetSammeVedleggetHvisFilnavnOgShaErLik() {
        OpplastetVedlegg opplastetVedlegg = new OpplastetVedlegg().withFilnavn(FILNAVN).withSha512(SHA512);
        JsonFiler jsonFiler = new JsonFiler().withFilnavn(FILNAVN).withSha512(SHA512);

        boolean sammeVedlegg = fiksDokumentHelper.jsonFilOgOpplastetVedleggErDetSammeVedlegget(jsonFiler, opplastetVedlegg);

        assertThat(sammeVedlegg, is(true));
    }

    @Test
    public void jsonFilOgOpplastetVedleggErIkkeDetSammeVedleggetHvisFilnavnErLiktOgShaErUlik() {
        OpplastetVedlegg opplastetVedlegg = new OpplastetVedlegg().withFilnavn(FILNAVN).withSha512(SHA512);
        JsonFiler jsonFiler = new JsonFiler().withFilnavn(FILNAVN).withSha512(ANNEN_SHA512);

        boolean sammeVedlegg = fiksDokumentHelper.jsonFilOgOpplastetVedleggErDetSammeVedlegget(jsonFiler, opplastetVedlegg);

        assertThat(sammeVedlegg, is(false));
    }

    @Test
    public void opprettDokumentForVedleggOppretterDokumentKorrekt() {
        OpplastetVedlegg opplastetVedlegg = new OpplastetVedlegg().withData(DATA);
        JsonFiler jsonFiler = new JsonFiler().withFilnavn(FILNAVN);

        Dokument dokument = fiksDokumentHelper.opprettDokumentForVedlegg(opplastetVedlegg, jsonFiler);

        assertThat(dokument.getFilnavn(), is(FILNAVN));
        assertThat(dokument.getData(), notNullValue());
        assertThat(dokument.getMimetype(), notNullValue());
        assertThat(dokument.isEkskluderesFraPrint(), is(true));
    }

    @Test
    public void krypterOgOpprettByteDatasourceKryptererHvisSkalKryptereErTrue() {
        fiksDokumentHelper = new FiksDokumentHelper(true, dokumentKrypterer, innsendingService);

        ByteDataSource dataSource = fiksDokumentHelper.krypterOgOpprettByteDatasource(FILNAVN, DATA);

        assertThat(dataSource.getName(), is(FILNAVN));
        assertThat(dataSource.getContentType(), is("application/octet-stream"));
        assertThat(dataSource.getData()[0], is((byte) 3));
    }

    @Test
    public void krypterOgOpprettByteDatasourceKryptererIkkeHvisSkalKryptereErFalse() {
        ByteDataSource dataSource = fiksDokumentHelper.krypterOgOpprettByteDatasource(FILNAVN, DATA);

        assertThat(dataSource.getData()[0], is((byte) 1));
    }

    private List<OpplastetVedlegg> lagOpplastedeVedlegg() {
        List<OpplastetVedlegg> opplastedeVedlegg = new ArrayList<>();
        opplastedeVedlegg.add(new OpplastetVedlegg()
                .withFilnavn(FILNAVN)
                .withSha512(SHA512)
                .withData(DATA)
                .withVedleggType(new VedleggType(TYPE, TILLEGGSINFO)));
        opplastedeVedlegg.add(new OpplastetVedlegg()
                .withFilnavn(ANNET_FILNAVN)
                .withSha512(ANNEN_SHA512)
                .withData(DATA)
                .withVedleggType(new VedleggType(TYPE2, TILLEGGSINFO2)));
        opplastedeVedlegg.add(new OpplastetVedlegg()
                .withFilnavn(TREDJE_FILNAVN)
                .withSha512(TREDJE_SHA512)
                .withData(DATA)
                .withVedleggType(new VedleggType(TYPE2, TILLEGGSINFO2)));
        return opplastedeVedlegg;
    }

    private JsonInternalSoknad lagInternalSoknad() {
        return new JsonInternalSoknad()
                .withSoknad(new JsonSoknad()
                        .withVersion("1.0.0")
                        .withKompatibilitet(emptyList())
                        .withDriftsinformasjon("")
                        .withData(new JsonData()
                                .withArbeid(new JsonArbeid())
                                .withBegrunnelse(new JsonBegrunnelse()
                                        .withHvaSokesOm("")
                                        .withHvorforSoke(""))
                                .withBosituasjon(new JsonBosituasjon())
                                .withFamilie(new JsonFamilie()
                                        .withForsorgerplikt(new JsonForsorgerplikt()))
                                .withOkonomi(new JsonOkonomi()
                                        .withOpplysninger(new JsonOkonomiopplysninger())
                                        .withOversikt(new JsonOkonomioversikt()))
                                .withPersonalia(new JsonPersonalia()
                                        .withKontonummer(new JsonKontonummer()
                                                .withKilde(JsonKilde.BRUKER))
                                        .withNavn(new JsonSokernavn()
                                                .withFornavn("Fornavn")
                                                .withMellomnavn("")
                                                .withEtternavn("Etternavn")
                                                .withKilde(JsonSokernavn.Kilde.SYSTEM))
                                        .withPersonIdentifikator(new JsonPersonIdentifikator()
                                                .withVerdi("12345678910")
                                                .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)))
                                .withUtdanning(new JsonUtdanning()
                                        .withKilde(JsonKilde.BRUKER))));
    }

    private JsonInternalSoknad lagInternalSoknadForVedlegg() {
        List<JsonVedlegg> jsonVedlegg = new ArrayList<>();
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedlegg.Status.VedleggKreves.name())
                .withType(TYPE)
                .withTilleggsinfo(TILLEGGSINFO2));
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedlegg.Status.LastetOpp.name())
                .withType(TYPE)
                .withTilleggsinfo(TILLEGGSINFO)
                .withFiler(lagJsonFiler(FILNAVN, SHA512)));
        jsonVedlegg.add(new JsonVedlegg()
                .withStatus(Vedlegg.Status.LastetOpp.name())
                .withType(TYPE2)
                .withTilleggsinfo(TILLEGGSINFO2)
                .withFiler(lagJsonFilerMedToFiler(ANNET_FILNAVN, ANNEN_SHA512, TREDJE_FILNAVN, TREDJE_SHA512)));
        return new JsonInternalSoknad()
                .withVedlegg(new JsonVedleggSpesifikasjon()
                        .withVedlegg(jsonVedlegg));
    }

    private List<JsonFiler> lagJsonFiler(String filnavn, String sha512) {
        List<JsonFiler> filer = new ArrayList<>();
        filer.add(new JsonFiler()
                .withFilnavn(filnavn)
                .withSha512(sha512));
        return filer;
    }

    private List<JsonFiler> lagJsonFilerMedToFiler(String filnavn, String sha, String filnavn2, String sha2) {
        List<JsonFiler> jsonFiler = lagJsonFiler(filnavn, sha);
        jsonFiler.add(new JsonFiler()
                .withFilnavn(filnavn2)
                .withSha512(sha2));
        return jsonFiler;
    }
}