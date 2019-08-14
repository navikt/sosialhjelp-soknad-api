package no.nav.sbl.dialogarena.service;

import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SoknadType;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknadmetadata.SoknadMetadataRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.VedleggMetadata;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.EttersendingService;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.EttersendingsSoknad;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.InnsendtSoknad;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.domain.Vedleggstatus;
import no.nav.sbl.sosialhjelp.sendtsoknad.SendtSoknadRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.EttersendingService.ETTERSENDELSE_FRIST_DAGER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SaksoversiktMetadataServiceTest {

    private static final String EIER = "12345";
    private static final String BEHANDLINGS_ID = "beh123";

    @Mock
    SoknadMetadataRepository soknadMetadataRepository;

    @Mock
    SendtSoknadRepository sendtSoknadRepository;

    @Mock
    EttersendingService ettersendingService;

    @Mock
    NavMessageSource navMessageSource;

    @InjectMocks
    SaksoversiktMetadataService saksoversiktMetadataService;

    @Captor
    ArgumentCaptor<LocalDateTime> timeCaptor;


    SoknadMetadata soknadMetadata;
    SendtSoknad sendtSoknad;

    @Before
    public void setUp() {
        Properties props = mock(Properties.class);
        when(props.getProperty(anyString())).then(new ReturnsArgumentAt(0));

        saksoversiktMetadataService.clock = Clock.fixed(LocalDateTime.of(2018, 5, 31, 13, 33, 37).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        when(navMessageSource.getBundleFor(anyString(), any())).thenReturn(props);

        sendtSoknad = new SendtSoknad()
                .withEier(EIER)
                .withBehandlingsId(BEHANDLINGS_ID)
                .withSendtDato(LocalDateTime.of(2018, 4, 11, 13, 30, 0));

        soknadMetadata = new SoknadMetadata();
        soknadMetadata.fnr = EIER;
        soknadMetadata.behandlingsId = BEHANDLINGS_ID;
        soknadMetadata.type = SoknadType.SEND_SOKNAD_KOMMUNAL;

        VedleggMetadata v = new VedleggMetadata();
        v.skjema = "skjema1";
        v.tillegg = "tillegg1";
        v.status = Vedleggstatus.LastetOpp;
        VedleggMetadata v2 = new VedleggMetadata();
        v2.skjema = "skjema1";
        v2.tillegg = "tillegg1";
        v2.status = Vedleggstatus.LastetOpp;
        VedleggMetadata v3 = new VedleggMetadata();
        v3.skjema = "skjema2";
        v3.tillegg = "tillegg1";
        v3.status = Vedleggstatus.VedleggKreves;
        VedleggMetadata v4 = new VedleggMetadata();
        v4.skjema = "annet";
        v4.tillegg = "annet";
        v4.status = Vedleggstatus.VedleggKreves;

        List<VedleggMetadata> vedleggListe = soknadMetadata.vedlegg.vedleggListe;
        vedleggListe.add(v);
        vedleggListe.add(v2);
        vedleggListe.add(v3);
        vedleggListe.add(v4);
    }

    @Test
    public void henterInnsendteForBruker() throws ParseException {
        when(soknadMetadataRepository.hent(BEHANDLINGS_ID)).thenReturn(soknadMetadata);
        when(sendtSoknadRepository.hentAlleSendteSoknader(EIER)).thenReturn(asList(sendtSoknad));

        List<InnsendtSoknad> resultat = saksoversiktMetadataService.hentInnsendteSoknaderForFnr(EIER);

        assertEquals(1, resultat.size());
        InnsendtSoknad soknad = resultat.get(0);
        assertEquals("beh123", soknad.getBehandlingsId());
        assertEquals("saksoversikt.soknadsnavn", soknad.getHoveddokument().getTittel());
        assertEquals(1, soknad.getVedlegg().size());
        assertEquals("vedlegg.skjema1.tillegg1.tittel", soknad.getVedlegg().get(0).getTittel());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-04-11 13:30:00"), soknad.getInnsendtDato());
    }

    @Test
    public void hentForEttersendelse() {
        when(sendtSoknadRepository.hentSoknaderForEttersending(anyString(), any())).thenReturn(asList(sendtSoknad));
        when(ettersendingService.hentVedleggForNyesteSoknadIKjede(any())).thenReturn(soknadMetadata.vedlegg.vedleggListe);

        List<EttersendingsSoknad> resultat = saksoversiktMetadataService.hentSoknaderBrukerKanEttersendePa("12345");

        assertEquals(1, resultat.size());
        EttersendingsSoknad soknad = resultat.get(0);
        assertEquals("saksoversikt.soknadsnavn", soknad.getTittel());
        assertEquals(1, soknad.getVedlegg().size());
        assertEquals("vedlegg.skjema2.tillegg1.tittel", soknad.getVedlegg().get(0).getTittel());
    }

    @Test
    public void hentForEttersendelseHarRiktigInterval() {
        when(sendtSoknadRepository.hentSoknaderForEttersending(anyString(), timeCaptor.capture())).thenReturn(asList(sendtSoknad));
        when(ettersendingService.hentVedleggForNyesteSoknadIKjede(any())).thenReturn(soknadMetadata.vedlegg.vedleggListe);

        saksoversiktMetadataService.hentSoknaderBrukerKanEttersendePa("12345");

        assertEquals(LocalDateTime.of(2018, 5, 31, 13, 33, 37).minusDays(ETTERSENDELSE_FRIST_DAGER), timeCaptor.getValue());
    }
}