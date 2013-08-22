package no.nav.sbl.dialogarena.dokumentinnsending.service;

import no.nav.modig.content.CmsContentRetriever;
import no.nav.modig.content.ValueRetriever;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentInnhold;
import no.nav.sbl.dialogarena.dokumentinnsending.kodeverk.KodeverkClient;
import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingOppsummering;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokument;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventning;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransformersTest {


    @InjectMocks
    private CmsContentRetriever cmsContentRetriever = new CmsContentRetriever();
    @Mock
    private ValueRetriever valueRetriever;

    @Before
    public void setup() {
        cmsContentRetriever.setTeksterRetriever(valueRetriever);
    }

    @Test
    public void testTransformerTilBehandlingsId() {
        WSBrukerBehandlingOppsummering wsBrukerBehandlingOppsummering = new WSBrukerBehandlingOppsummering();
        String behandlingsId = "1";
        wsBrukerBehandlingOppsummering.setBehandlingsId(behandlingsId);
        assertThat(Transformers.BEHANDLINGSID.transform(wsBrukerBehandlingOppsummering), is(behandlingsId));
    }

    @Test
    public void testTransformerTilDokumentForventning() {
        String kodeverkId = "1";
        WSDokumentForventning wsDokumentForventning = Transformers.TIL_DOKUMENTFORVENTNING.transform(kodeverkId);
        assertThat(wsDokumentForventning.getKodeverkId(), is(kodeverkId));
        assertThat(wsDokumentForventning.getInnsendingsValg(), is(WSInnsendingsValg.IKKE_VALGT));
        assertThat(wsDokumentForventning.isHovedskjema(), is(false));
    }

    @Test
    public void testTilDokumentMedNav() {
        KodeverkClient kodeverkClient = mock(KodeverkClient.class);
        String behandlingsId = "1";
        String kodeverkId = "NAV 04-03.07";
        WSDokumentForventning wsDokumentForventning = Transformers.TIL_DOKUMENTFORVENTNING.transform(kodeverkId);
        KodeverkSkjema kodeverkSkjema = byggOppKodeverkskjema();

        when(kodeverkClient.hentKodeverkSkjemaForVedleggsid(kodeverkId)).thenReturn(kodeverkSkjema);

        Dokument dokument = Transformers.tilDokument(kodeverkClient, behandlingsId).transform(wsDokumentForventning);
        verifiserDokumentVerdier(behandlingsId, kodeverkId, dokument, "");
    }

    @Test
    public void testTilDokumentMedForventningSomErHovedskjema() {
        KodeverkClient kodeverkClient = mock(KodeverkClient.class);
        String behandlingsId = "1";
        String kodeverkId = "NAV 04-03.07";
        WSDokumentForventning wsDokumentForventning = Transformers.TIL_DOKUMENTFORVENTNING.transform(kodeverkId);
        wsDokumentForventning.setHovedskjema(true);
        KodeverkSkjema kodeverkSkjema = byggOppKodeverkskjema();

        when(kodeverkClient.hentKodeverkSkjemaForSkjemanummer(kodeverkId)).thenReturn(kodeverkSkjema);

        Dokument dokument = Transformers.tilDokument(kodeverkClient, behandlingsId).transform(wsDokumentForventning);
        verifiserDokumentVerdier(behandlingsId, kodeverkId, dokument, "");
    }

    @Test
    public void testTilDokumentMedForventningSomErAvTypenAnnet() {
        KodeverkClient kodeverkClient = mock(KodeverkClient.class);
        String behandlingsId = "1";
        String kodeverkId = "N6";
        WSDokumentForventning wsDokumentForventning = Transformers.TIL_DOKUMENTFORVENTNING.transform(kodeverkId);
        String dokForventningFritekst = "Annet";
        wsDokumentForventning.setFriTekst(dokForventningFritekst);
        KodeverkSkjema kodeverkSkjema = byggOppKodeverkskjema();

        when(kodeverkClient.hentKodeverkSkjemaForVedleggsid(kodeverkId)).thenReturn(kodeverkSkjema);

        Dokument dokument = Transformers.tilDokument(kodeverkClient, behandlingsId).transform(wsDokumentForventning);
        verifiserDokumentVerdier(behandlingsId, kodeverkId, dokument, dokForventningFritekst);
    }

    @Test
    public void testTilDokumentMedForventningSomErAvTypenEksterntVedlegg() {
        KodeverkClient kodeverkClient = mock(KodeverkClient.class);
        String behandlingsId = "1";
        String kodeverkId = "Ekstern vedlegg";
        WSDokumentForventning wsDokumentForventning = Transformers.TIL_DOKUMENTFORVENTNING.transform(kodeverkId);
        KodeverkSkjema kodeverkSkjema = byggOppKodeverkskjema();

        when(kodeverkClient.hentKodeverkSkjemaForVedleggsid(kodeverkId)).thenReturn(kodeverkSkjema);

        Dokument dokument = Transformers.tilDokument(kodeverkClient, behandlingsId).transform(wsDokumentForventning);
        verifiserDokumentVerdier(behandlingsId, kodeverkId, dokument, "");
    }

    @Test
    public void testTilWsDokument() {
        DokumentInnhold dokumentInnhold = new DokumentInnhold();
        DateTime opplastetDato = new DateTime();
        dokumentInnhold.setOpplastetDato(opplastetDato.toDate());
        String filnavn = "test.jpg";
        dokumentInnhold.setNavn(filnavn);
        dokumentInnhold.setInnhold(new byte[]{'a'});

        WSDokument wsDokument = Transformers.DOKUMENT_TIL_WS_DOKUMENT.transform(dokumentInnhold);
        assertThat(wsDokument.getOpplastetDato(), is(opplastetDato));
        assertThat(wsDokument.getFilnavn(), is(filnavn));
    }

    @Test
    public void testWSDokumentTilDokumentInnhold() {
        WSDokument wsDokument = new WSDokument();
        long id = 1;
        wsDokument.setId(id);
        String filnavn = "test.jpg";
        wsDokument.setFilnavn(filnavn);
        DateTime opplastetDato = new DateTime();
        wsDokument.setOpplastetDato(opplastetDato);
        wsDokument.setInnhold(new DataHandler(new ByteArrayDataSource(new byte[]{'a'}, "application/octet-stream")));
        DokumentInnhold dokumentInnhold = Transformers.WS_DOKUMENT_TIL_DOKUMENT_INNHOLD.transform(wsDokument);
        assertThat(dokumentInnhold.getId(), is(id));
        assertThat(dokumentInnhold.getNavn(), is(filnavn));
        assertThat(dokumentInnhold.getOpplastetDato(), is(opplastetDato));
    }

    private KodeverkSkjema byggOppKodeverkskjema() {
        KodeverkSkjema kodeverkSkjema = new KodeverkSkjema();
        kodeverkSkjema.setTittel("Egenerklæring - overdragelse av lønnskrav");
        kodeverkSkjema.setSkjemanummer("NAV 04-03.07");
        kodeverkSkjema.setBeskrivelse("123");
        kodeverkSkjema.setUrl("https://www-t8.nav.no:443/skjema/Skjemaer/Alle+skjemaer+JSON/_attachment/805329612?_ts=12084ac1f70&download=true");
        return kodeverkSkjema;
    }

    private void verifiserDokumentVerdier(String behandlingsId, String kodeverkId, Dokument dokument, String dokForventningFritekst) {
        assertThat(dokument, notNullValue());
        assertThat(dokument.getKodeverkId(), is(kodeverkId));
        if (StringUtils.isNotBlank(dokForventningFritekst)) {
            assertThat(dokument.getNavn(), is("Egenerklæring - overdragelse av lønnskrav: " + dokForventningFritekst));
        } else {
            assertThat(dokument.getNavn(), is("Egenerklæring - overdragelse av lønnskrav" + dokForventningFritekst));
        }
        assertThat(dokument.getLink(), is("https://www-t8.nav.no:443/skjema/Skjemaer/Alle+skjemaer+JSON/_attachment/805329612?_ts=12084ac1f70&download=true"));
        assertThat(dokument.getBehandlingsId(), is(behandlingsId));
    }
}