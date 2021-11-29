package no.nav.sosialhjelp.soknad.web.service;

import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.EttersendingsSoknad;
import no.nav.sbl.soknadsosialhjelp.tjeneste.saksoversikt.InnsendtSoknad;
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata.VedleggMetadata;
import no.nav.sosialhjelp.soknad.business.service.soknadservice.EttersendingService;
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus;
import no.nav.sosialhjelp.soknad.domain.model.kravdialoginformasjon.SoknadType;
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static no.nav.sosialhjelp.soknad.business.service.soknadservice.EttersendingService.ETTERSENDELSE_FRIST_DAGER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class SaksoversiktMetadataServiceTest {

    @Mock
    private SoknadMetadataRepository soknadMetadataRepository;

    @Mock
    private EttersendingService ettersendingService;

    @Mock
    private NavMessageSource navMessageSource;

    @Mock
    private Clock clock;

    @InjectMocks
    private SaksoversiktMetadataService saksoversiktMetadataService;

    @Captor
    private ArgumentCaptor<LocalDateTime> timeCaptor;

    private SoknadMetadata soknadMetadata;

    @BeforeEach
    public void setUp() {
        Properties props = mock(Properties.class);
        when(props.getProperty(anyString())).then(new ReturnsArgumentAt(0));

        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(LocalDateTime.of(2018, 5, 31, 13, 33, 37).atZone(ZoneId.systemDefault()).toInstant());
        when(navMessageSource.getBundleFor(anyString(), any())).thenReturn(props);

        soknadMetadata = new SoknadMetadata();
        soknadMetadata.fnr = "12345";
        soknadMetadata.behandlingsId = "beh123";
        soknadMetadata.type = SoknadType.SEND_SOKNAD_KOMMUNAL;
        soknadMetadata.innsendtDato = LocalDateTime.of(2018, 4, 11, 13, 30, 0);

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
    void henterInnsendteForBruker() throws ParseException {
        when(soknadMetadataRepository.hentAlleInnsendteSoknaderForBruker("12345"))
                .thenReturn(asList(soknadMetadata));

        List<InnsendtSoknad> resultat = saksoversiktMetadataService.hentInnsendteSoknaderForFnr("12345");

        assertThat(resultat).hasSize(1);
        InnsendtSoknad soknad = resultat.get(0);
        assertThat(soknad.getBehandlingsId()).isEqualTo("beh123");
        assertThat(soknad.getHoveddokument().getTittel()).isEqualTo("saksoversikt.soknadsnavn");
        assertThat(soknad.getVedlegg()).hasSize(1);
        assertThat(soknad.getVedlegg().get(0).getTittel()).isEqualTo("vedlegg.skjema1.tillegg1.tittel");
        assertThat(soknad.getInnsendtDato()).isEqualTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-04-11 13:30:00"));
    }

    @Test
    void hentForEttersendelse() {
        when(soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(anyString(), any())).thenReturn(asList(soknadMetadata));
        when(ettersendingService.hentNyesteSoknadIKjede(any())).thenReturn(soknadMetadata);

        List<EttersendingsSoknad> resultat = saksoversiktMetadataService.hentSoknaderBrukerKanEttersendePa("12345");

        assertThat(resultat).hasSize(1);
        EttersendingsSoknad soknad = resultat.get(0);
        assertThat(soknad.getTittel()).contains("saksoversikt.soknadsnavn");
        assertThat(soknad.getVedlegg()).hasSize(1);
        assertThat(soknad.getVedlegg().get(0).getTittel()).isEqualTo("vedlegg.skjema2.tillegg1.tittel");
    }

    @Test
    void hentForEttersendelseHarRiktigInterval() {
        when(soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(anyString(), timeCaptor.capture())).thenReturn(asList(soknadMetadata));
        when(ettersendingService.hentNyesteSoknadIKjede(any())).thenReturn(soknadMetadata);

        saksoversiktMetadataService.hentSoknaderBrukerKanEttersendePa("12345");

        assertThat(timeCaptor.getValue()).isEqualTo(LocalDateTime.of(2018, 5, 31, 13, 33, 37).minusDays(ETTERSENDELSE_FRIST_DAGER));
    }
}