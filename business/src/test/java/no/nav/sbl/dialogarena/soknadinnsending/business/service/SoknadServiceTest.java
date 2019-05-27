package no.nav.sbl.dialogarena.soknadinnsending.business.service;


import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.HendelseType;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.SosialhjelpInformasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadDataFletter;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadMetricsService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.StartDatoUtil;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.OPPRETTET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SoknadServiceTest {

    public static final String SKJEMA_NUMMER = "NAV 04-01.03";
    private static final Vedlegg KVITTERING_REF = new Vedlegg()
            .medFillagerReferanse("kvitteringRef")
            .medSkjemaNummer(Kodeverk.KVITTERING)
            .medInnsendingsvalg(Vedlegg.Status.LastetOpp)
            .medStorrelse(3L)
            .medAntallSider(1);
    @Mock
    private SoknadRepository soknadRepository;
    @Mock
    private VedleggRepository vedleggRepository;
    @Mock
    private HenvendelseService henvendelsesConnector;
    @Mock
    private FillagerService fillagerService;
    @Mock
    private Kodeverk kodeverk;
    @Mock
    private NavMessageSource navMessageSource;
    @Mock
    private StartDatoUtil startDatoUtil;
    @Mock
    private FaktaService faktaService;
    @Mock
    private WebSoknadConfig config;
    @Mock
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;
    @Mock
    ApplicationContext applicationContex;
    @Mock
    SoknadDataFletter soknadServiceUtil;
    @Mock
    SoknadMetricsService soknadMetricsService;
    @Mock
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @InjectMocks
    private SoknadService soknadService;

    public static byte[] getBytesFromFile(String path) throws IOException {
        InputStream resourceAsStream = SoknadServiceTest.class.getResourceAsStream(path);
        return IOUtils.toByteArray(resourceAsStream);
    }

    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        when(soknadRepository.hentSoknadType(anyLong())).thenReturn(SosialhjelpInformasjon.SKJEMANUMMER);
        when(kravdialogInformasjonHolder.hentAlleSkjemanumre()).thenReturn(new KravdialogInformasjonHolder().hentAlleSkjemanumre());
        when(soknadUnderArbeidRepository.hentSoknad(anyString(), anyString())).thenReturn(Optional.of(new SoknadUnderArbeid()));
    }

    @Test
    public void skalSetteDelsteg() {
        soknadService.settDelsteg("1", OPPRETTET);
        verify(soknadRepository).settDelstegstatus("1", OPPRETTET);
    }

    @Test
    public void skalSetteJournalforendeEnhet() {
        soknadService.settJournalforendeEnhet("1", "1234");
        verify(soknadRepository).settJournalforendeEnhet("1", "1234");
    }

    @Test
    public void skalHenteSoknad() {
        when(soknadRepository.hentSoknad(1L)).thenReturn(new WebSoknad().medId(1L).medskjemaNummer("NAV 04-01.03"));
        assertThat(soknadService.hentSoknadFraLokalDb(1L)).isEqualTo(new WebSoknad().medId(1L).medskjemaNummer("NAV 04-01.03"));
    }

    @Test
    public void skalAvbryteSoknad() {
        WebSoknad soknad = new WebSoknad().medBehandlingId("123").medId(11L);
        when(soknadRepository.hentSoknad("123")).thenReturn(soknad);

        soknadService.avbrytSoknad("123");

        verify(soknadRepository).slettSoknad(soknad, HendelseType.AVBRUTT_AV_BRUKER);
        verify(henvendelsesConnector).avbrytSoknad("123", false);
        verify(soknadUnderArbeidRepository).slettSoknad(any(SoknadUnderArbeid.class), anyString());
    }

    @Test
    public void skalHenteSoknadsIdForEttersendingTilBehandlingskjedeId() {
        WebSoknad soknad = new WebSoknad();
        soknad.setSoknadId(1L);
        when(soknadRepository.hentEttersendingMedBehandlingskjedeId(anyString())).thenReturn(Optional.of(soknad));

        WebSoknad webSoknad = soknadService.hentEttersendingForBehandlingskjedeId("123");

        assertThat(webSoknad.getSoknadId()).isEqualTo(1L);
    }

    @Test
    public void skalFaNullNarManProverAHenteEttersendingMedBehandlingskjedeIdSomIkkeHarNoenEttersending() {
        WebSoknad soknad = new WebSoknad();
        soknad.setSoknadId(1L);
        when(soknadRepository.hentEttersendingMedBehandlingskjedeId(anyString())).thenReturn(Optional.empty());

        WebSoknad webSoknad = soknadService.hentEttersendingForBehandlingskjedeId("123");

        assertThat(webSoknad).isNull();
    }

    private static List<Vedlegg> mockHentVedleggForventninger(WebSoknad soknad) {

        List<Vedlegg> vedleggForventninger = soknad.getVedlegg();
        Vedlegg kvittering = KVITTERING_REF;
        if (kvittering != null) {
            vedleggForventninger.add(kvittering);
        }
        return vedleggForventninger;
    }
}
