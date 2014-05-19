package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerConnector;
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
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.WebSoknadUtils.DAGPENGER;
import static no.nav.sbl.dialogarena.test.match.Matchers.match;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
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
    private SendSoknadService soknadService;
    @Mock
    private FillagerConnector fillagerConnector;
    @Mock
    private Kodeverk kodeverk;

    @InjectMocks
    private DefaultVedleggService vedleggService;

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
                .medInnsendingsvalg(Vedlegg.Status.VedleggKreves);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(vedleggRepository.opprettVedlegg(any(Vedlegg.class), captor.capture())).thenReturn(11L);

        ByteArrayInputStream bais = new ByteArrayInputStream(getBytesFromFile("/images/bilde.jpg"));
        List<Long> ids = vedleggService.splitOgLagreVedlegg(vedlegg, bais);
        assertThat(captor.getValue(), match(IS_PDF));
        assertThat(ids, contains(11L));
    }

    @Test
    public void skalHenteVedlegg() {
        vedleggService.hentVedlegg(11L, 1L, false);
        verify(vedleggRepository).hentVedlegg(11L, 1L);
        vedleggService.hentVedlegg(11L, 1L, true);
        verify(vedleggRepository).hentVedleggMedInnhold(11L, 1L);
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
                .medInnsendingsvalg(Vedlegg.Status.VedleggKreves);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        when(vedleggRepository.opprettVedlegg(any(Vedlegg.class), captor.capture())).thenReturn(10L, 11L, 12L, 13L, 14L);

        ByteArrayInputStream bais = new ByteArrayInputStream(getBytesFromFile("/pdfs/navskjema.pdf"));
        List<Long> ids = vedleggService.splitOgLagreVedlegg(vedlegg, bais);
        assertThat(captor.getValue(), match(IS_PDF));
        assertThat(ids, contains(10L, 11L, 12L, 13L, 14L));
    }

    @Test
    public void skalGenerereVedleggFaktum() throws IOException {
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer("L6").medSoknadId(1L).medVedleggId(2L);
        byte[] bytes = getBytesFromFile("/pdfs/minimal.pdf");
        Vedlegg vedleggSjekk = new Vedlegg().medSkjemaNummer("L6").medSoknadId(1L).medAntallSider(1).medVedleggId(2L).medFillagerReferanse(vedlegg.getFillagerReferanse()).medData(bytes);
        when(vedleggRepository.hentVedlegg(1L, 2L)).thenReturn(vedlegg);
        when(vedleggRepository.hentVedleggUnderBehandling(1L, vedlegg.getFillagerReferanse())).thenReturn(Arrays.asList(new Vedlegg().medVedleggId(10L)));
        when(vedleggRepository.hentVedleggData(1L, 10L)).thenReturn(bytes);
        when(soknadRepository.hentSoknad(1L)).thenReturn(new WebSoknad().medBehandlingId("123").medAktorId("234"));
        vedleggService.genererVedleggFaktum(1L, 2L);
        vedleggSjekk.setData(vedlegg.getData());
        vedleggSjekk.medStorrelse((long) vedlegg.getData().length);
        verify(vedleggRepository).lagreVedleggMedData(1L, 2L, vedleggSjekk);
        verify(fillagerConnector).lagreFil(eq("123"), eq(vedleggSjekk.getFillagerReferanse()), eq("234"), any(InputStream.class));
    }

    @Test
    public void skalGenererForhandsvisning() throws IOException {
        when(vedleggRepository.hentVedleggData(11L, 1L)).thenReturn(getBytesFromFile("/pdfs/minimal.pdf"));
        byte[] bytes = vedleggService.lagForhandsvisning(11L, 1L, 0);
        assertThat(bytes, instanceOf(byte[].class));
    }

    @Test
    public void skalSletteVedlegg() {
        when(soknadService.hentSoknad(1L)).thenReturn(new WebSoknad().medBehandlingId("123").medAktorId("234").medDelstegStatus(DelstegStatus.OPPRETTET));

        vedleggService.slettVedlegg(1L, 2L);

        verify(vedleggRepository).slettVedlegg(1L, 2L);
        verify(soknadRepository).settDelstegstatus(1L, SKJEMA_VALIDERT);
    }

    @Test
    public void skalHentePaakrevdeVedlegg() {
        Map<Kodeverk.Nokkel, String> map = new HashMap<>();
        map.put(Kodeverk.Nokkel.TITTEL, "tittel");
        map.put(Kodeverk.Nokkel.URL, "url");
        when(kodeverk.getKoder("L6")).thenReturn(map);
        Vedlegg vedlegg = new Vedlegg().medSkjemaNummer("L6");
        Vedlegg vedleggSjekk = new Vedlegg().medSkjemaNummer("L6").medTittel("tittel").medUrl("URL", "url")
                .medFillagerReferanse(vedlegg.getFillagerReferanse());
        when(vedleggRepository.hentPaakrevdeVedlegg(1L)).thenReturn(Arrays.asList(vedlegg));
        List<Vedlegg> vedleggs = vedleggService.hentPaakrevdeVedlegg(1L);
        assertThat(vedleggs.get(0), is(equalTo(vedleggSjekk)));
    }

    @Test
    public void skalLagreVedlegg() {
        when(soknadService.hentSoknad(11L)).thenReturn(new WebSoknad().medDelstegStatus(OPPRETTET));
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L);
        vedleggService.lagreVedlegg(11L, 1L, vedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, vedlegg);
    }

    @Test(expected=ApplicationException.class)
    public void skalIkkeKunneLagreVedleggMedNegradertInnsendingsStatus() {
        Vedlegg opplastetVedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.LastetOpp);

        opplastetVedlegg.setInnsendingsvalg(Vedlegg.Status.SendesIkke);
        vedleggService.lagreVedlegg(11L, 1L, opplastetVedlegg);
        verify(vedleggRepository, never()).lagreVedlegg(11L, 1L, opplastetVedlegg);
    }

    @Test
    public void skalKunneLagreVedleggMedSammeInnsendinsStatus() {
        when(soknadService.hentSoknad(11L)).thenReturn(new WebSoknad().medDelstegStatus(OPPRETTET));
        Vedlegg opplastetVedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.LastetOpp);

        opplastetVedlegg.setInnsendingsvalg(Vedlegg.Status.LastetOpp);
        vedleggService.lagreVedlegg(11L, 1L, opplastetVedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, opplastetVedlegg);
    }

    @Test
    public void skalIkkeSetteDelstegDersomVedleggLagresPaaEttersending() {
        when(soknadService.hentSoknad(11L)).thenReturn(new WebSoknad().medDelstegStatus(ETTERSENDING_OPPRETTET));
        Vedlegg opplastetVedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.LastetOpp);

        opplastetVedlegg.setInnsendingsvalg(Vedlegg.Status.LastetOpp);
        vedleggService.lagreVedlegg(11L, 1L, opplastetVedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, opplastetVedlegg);
        verify(soknadRepository, never()).settDelstegstatus(11L, DelstegStatus.SKJEMA_VALIDERT);
    }

    @Test
    public void skalKunneLagreVedleggMedOppgradertInnsendingsStatus() {
        when(soknadService.hentSoknad(11L)).thenReturn(new WebSoknad().medDelstegStatus(OPPRETTET));
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.SendesIkke);

        vedlegg.setInnsendingsvalg(Vedlegg.Status.SendesSenere);
        vedleggService.lagreVedlegg(11L, 1L, vedlegg);
        verify(vedleggRepository).lagreVedlegg(11L, 1L, vedlegg);
    }

    @Test(expected=ApplicationException.class)
    public void skalIkkeKunneLagreVedleggMedPrioritetMindreEllerLik1() {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(1L).medOpprinneligInnsendingsvalg(Vedlegg.Status.VedleggKreves);

        vedlegg.setInnsendingsvalg(Vedlegg.Status.VedleggKreves);
        vedleggService.lagreVedlegg(11L, 1L, vedlegg);
        verify(vedleggRepository, never()).lagreVedlegg(11L, 1L, vedlegg);
    }

    public static byte[] getBytesFromFile(String path) throws IOException {
        InputStream resourceAsStream = SoknadServiceTest.class.getResourceAsStream(path);
        return IOUtils.toByteArray(resourceAsStream);
    }
}
