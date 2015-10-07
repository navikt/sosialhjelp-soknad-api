package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.FunksjonalitetBryter;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.detect.Detect.IS_PDF;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.ETTERSENDING_OPPRETTET;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.OPPRETTET;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.SKJEMA_VALIDERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg.Status.LastetOpp;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg.Status.VedleggKreves;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.DAGPENGER;
import static no.nav.sbl.dialogarena.test.match.Matchers.match;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VedleggServiceTest {
    @Mock
    private SoknadRepository soknadRepository;
    @Mock
    private VedleggRepository vedleggRepository;
    @Mock
    private SoknadService soknadService;
    @Mock
    private SoknadDataFletter soknadDataFletter;
    @Mock
    private FillagerService fillagerService;
    @Mock
    private Kodeverk kodeverk;

    @InjectMocks
    private VedleggService vedleggService;

    @Before
    public void before() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        when(soknadRepository.hentSoknadType(anyLong())).thenReturn(DAGPENGER);
    }

    @Test
    public void skalAKonvertereFilerVedOpplasting() throws IOException {
        Vedlegg vedlegg = new Vedlegg()
                .medVedleggId(1L)
                .medSoknadId(1L)
                .medFaktumId(1L)
                .medSkjemaNummer("1")
                .medNavn("")
                .medStorrelse(1L)
                .medAntallSider(1)
                .medFillagerReferanse(null)
                .medData(null)
                .medOpprettetDato(DateTime.now().getMillis())
                .medInnsendingsvalg(VedleggKreves);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(vedleggRepository.opprettEllerEndreVedlegg(any(Vedlegg.class), captor.capture())).thenReturn(11L);

        ByteArrayInputStream bais = new ByteArrayInputStream(getBytesFromFile("/images/bilde.jpg"));
        List<Long> ids = vedleggService.splitOgLagreVedlegg(vedlegg, bais);
        assertThat(captor.getValue(), match(IS_PDF));
        assertThat(ids, contains(11L));
    }

    @Test
    public void skalHenteVedlegg() {
        vedleggService.hentVedlegg(1L, false);
        verify(vedleggRepository).hentVedlegg(1L);
        vedleggService.hentVedlegg(1L, true);
        verify(vedleggRepository).hentVedleggMedInnhold(1L);
    }

    @Test
    public void skalKonverterePdfVedOpplasting() throws IOException {
        Vedlegg vedlegg = new Vedlegg()
                .medVedleggId(1L)
                .medSoknadId(1L)
                .medFaktumId(1L)
                .medSkjemaNummer("1")
                .medNavn("")
                .medStorrelse(1L)
                .medAntallSider(1)
                .medFillagerReferanse(null)
                .medData(null)
                .medOpprettetDato(DateTime.now().getMillis())
                .medInnsendingsvalg(VedleggKreves);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(vedleggRepository.opprettEllerEndreVedlegg(any(Vedlegg.class), captor.capture())).thenReturn(10L, 11L, 12L, 13L, 14L);

        ByteArrayInputStream bais = new ByteArrayInputStream(getBytesFromFile("/pdfs/navskjema.pdf"));
        List<Long> ids = vedleggService.splitOgLagreVedlegg(vedlegg, bais);
        assertThat(captor.getValue(), match(IS_PDF));
        assertThat(ids, contains(10L, 11L, 12L, 13L, 14L));
    }

    @Test
    public void skalGenerereVedleggFaktum() throws IOException {
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer("L6").medSoknadId(1L).medVedleggId(2L);
        byte[] bytes = getBytesFromFile("/pdfs/minimal.pdf");
        Vedlegg vedleggSjekk = new Vedlegg().medSkjemaNummer("L6").medInnsendingsvalg(LastetOpp).medSoknadId(1L).medAntallSider(1).medVedleggId(2L).medFillagerReferanse(vedlegg.getFillagerReferanse()).medData(bytes);
        when(vedleggRepository.hentVedlegg(2L)).thenReturn(vedlegg);
        when(vedleggRepository.hentVedleggUnderBehandling("ABC", vedlegg.getFillagerReferanse())).thenReturn(Arrays.asList(new Vedlegg().medVedleggId(10L)));
        when(vedleggRepository.hentVedleggData(10L)).thenReturn(bytes);
        when(soknadRepository.hentSoknad("ABC")).thenReturn(new WebSoknad().medBehandlingId("ABC").medAktorId("234").medId(1L));
        vedleggService.genererVedleggFaktum("ABC", 2L);
        vedleggSjekk.setData(vedlegg.getData());
        vedleggSjekk.medStorrelse((long) vedlegg.getData().length);
        verify(vedleggRepository).lagreVedleggMedData(1L, 2L, vedleggSjekk);
        verify(fillagerService).lagreFil(eq("ABC"), eq(vedleggSjekk.getFillagerReferanse()), eq("234"), any(InputStream.class));
    }

    @Test
    public void skalGenererForhandsvisning() throws IOException {
        when(vedleggRepository.hentVedleggData(1L)).thenReturn(getBytesFromFile("/pdfs/minimal.pdf"));
        byte[] bytes = vedleggService.lagForhandsvisning(1L, 0);
        assertThat(bytes, instanceOf(byte[].class));
    }

    @Test
    public void skalSletteVedlegg() {
        when(soknadService.hentSoknadFraLokalDb(1L)).thenReturn(new WebSoknad().medBehandlingId("123").medAktorId("234").medDelstegStatus(DelstegStatus.OPPRETTET).medId(1L));
        when(vedleggService.hentVedlegg(2L, false)).thenReturn(new Vedlegg().medSoknadId(1L));

        vedleggService.slettVedlegg(2L);

        verify(vedleggRepository).slettVedlegg(1L, 2L);
        verify(soknadRepository).settDelstegstatus(1L, SKJEMA_VALIDERT);
    }

    //Tester gammel vedleggslogikk
    @Test
    public void skalHentePaakrevdeVedlegg() {
        System.setProperty(FunksjonalitetBryter.GammelVedleggsLogikk.nokkel, "true");
        Map<Kodeverk.Nokkel, String> map = new HashMap<>();
        map.put(Kodeverk.Nokkel.TITTEL, "tittel");
        map.put(Kodeverk.Nokkel.URL, "url");
        when(kodeverk.getKoder("L6")).thenReturn(map);
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer("L6").medInnsendingsvalg(VedleggKreves);
        Vedlegg vedleggSjekk = new Vedlegg().medSkjemaNummer("L6").medTittel("tittel").medUrl("URL", "url")
                .medFillagerReferanse(vedlegg.getFillagerReferanse()).medInnsendingsvalg(VedleggKreves);
        when(vedleggRepository.hentVedlegg(anyString())).thenReturn(Arrays.asList(vedlegg));
        List<Vedlegg> vedleggs = vedleggService.hentPaakrevdeVedlegg("10000000ABC");
        assertThat(vedleggs.get(0), is(equalTo(vedleggSjekk)));
    }

    @Test
    public void skalLagreVedlegg() {
        when(soknadService.hentSoknadFraLokalDb(11L)).thenReturn(new WebSoknad().medDelstegStatus(OPPRETTET));
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L).medSoknadId(11L);
        vedleggService.lagreVedlegg(1L, vedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, vedlegg);
    }

    @Test(expected=ApplicationException.class)
    public void skalIkkeKunneLagreVedleggMedNegradertInnsendingsStatus() {
        Vedlegg opplastetVedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.LastetOpp);

        opplastetVedlegg.setInnsendingsvalg(Vedlegg.Status.SendesIkke);
        vedleggService.lagreVedlegg(1L, opplastetVedlegg);
        verify(vedleggRepository, never()).lagreVedlegg(11L, 1L, opplastetVedlegg);
    }

    @Test
    public void skalKunneLagreVedleggMedSammeInnsendinsStatus() {
        when(soknadService.hentSoknadFraLokalDb(11L)).thenReturn(new WebSoknad().medDelstegStatus(OPPRETTET));
        Vedlegg opplastetVedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.LastetOpp).medSoknadId(11L);

        opplastetVedlegg.setInnsendingsvalg(Vedlegg.Status.LastetOpp);
        vedleggService.lagreVedlegg(1L, opplastetVedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, opplastetVedlegg);
    }

    @Test
    public void skalIkkeSetteDelstegDersomVedleggLagresPaaEttersending() {
        when(soknadService.hentSoknadFraLokalDb(11L)).thenReturn(new WebSoknad().medDelstegStatus(ETTERSENDING_OPPRETTET));
        Vedlegg opplastetVedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.LastetOpp).medSoknadId(11L);

        opplastetVedlegg.setInnsendingsvalg(Vedlegg.Status.LastetOpp);
        vedleggService.lagreVedlegg(1L, opplastetVedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, opplastetVedlegg);
        verify(soknadRepository, never()).settDelstegstatus(11L, DelstegStatus.SKJEMA_VALIDERT);
    }

    @Test
    public void skalKunneLagreVedleggMedOppgradertInnsendingsStatus() {
        when(soknadService.hentSoknadFraLokalDb(11L)).thenReturn(new WebSoknad().medDelstegStatus(OPPRETTET));
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.SendesIkke).medSoknadId(11L);

        vedlegg.setInnsendingsvalg(Vedlegg.Status.SendesSenere);
        vedleggService.lagreVedlegg(1L, vedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, vedlegg);
    }

    @Test(expected=ApplicationException.class)
    public void skalIkkeKunneLagreVedleggMedPrioritetMindreEllerLik1() {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(VedleggKreves);

        vedlegg.setInnsendingsvalg(VedleggKreves);
        vedleggService.lagreVedlegg(1L, vedlegg);
        verify(vedleggRepository, never()).lagreVedlegg(11L, 1L, vedlegg);
    }

    public static byte[] getBytesFromFile(String path) throws IOException {
        InputStream resourceAsStream = SoknadServiceTest.class.getResourceAsStream(path);
        return IOUtils.toByteArray(resourceAsStream);
    }
}
