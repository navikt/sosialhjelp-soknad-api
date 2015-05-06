package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.ETTERSENDING_OPPRETTET;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.OPPRETTET;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.DAGPENGER;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FaktaServiceTest {
    @Mock
    private SoknadRepository soknadRepository;
    @Mock
    private VedleggRepository vedleggRepository;
    @Mock
    private NavMessageSource navMessageSource;
    @Mock
    private WebSoknadConfig config;

    @InjectMocks
    private FaktaService faktaService;

    @Before
    public void before() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        when(soknadRepository.hentSoknadType(anyLong())).thenReturn(DAGPENGER);
        when(soknadRepository.hentSoknad(anyLong())).thenReturn(new WebSoknad().medDelstegStatus(OPPRETTET));
        when(config.hentStruktur(any(Long.class))).thenReturn(new SoknadStruktur());
    }

    @Test
    public void skalIkkeoppdatereDelstegstatusVedEpost() {
        long soknadId = 1L;
        String behandlingsId = "1000000ABC";
        Faktum faktum = new Faktum().medKey("epost").medValue("false").medFaktumId(soknadId);
        when(soknadRepository.hentSoknad(behandlingsId)).thenReturn(new WebSoknad().medId(soknadId).medDelstegStatus(DelstegStatus.UTFYLLING));
        when(soknadRepository.hentSoknad(soknadId)).thenReturn(new WebSoknad().medId(soknadId).medDelstegStatus(DelstegStatus.UTFYLLING));
        when(soknadRepository.lagreFaktum(soknadId, faktum)).thenReturn(2L);
        when(soknadRepository.hentFaktum(2L)).thenReturn(faktum);
        faktaService.lagreSoknadsFelt(behandlingsId, faktum);
        verify(soknadRepository, never()).settDelstegstatus(anyLong(), any(DelstegStatus.class));
    }

    @Test
    public void skalLagreSoknadFelt() {
        long soknadId = 1L;
        String behandlingsId = "1000000ABC";
        Faktum faktum = new Faktum().medKey("ikkeavtjentverneplikt").medValue("false").medFaktumId(soknadId);
        when(soknadRepository.hentSoknad(behandlingsId)).thenReturn(new WebSoknad().medId(soknadId));
        when(soknadRepository.lagreFaktum(soknadId, faktum)).thenReturn(2L);
        when(soknadRepository.hentFaktum(2L)).thenReturn(faktum);
        faktaService.lagreSoknadsFelt(behandlingsId, faktum);
        verify(soknadRepository).settSistLagretTidspunkt(soknadId);
        when(soknadRepository.hentBarneFakta(soknadId, faktum.getFaktumId())).thenReturn(Arrays.asList(new Faktum().medKey("subkey")));

        //Verifiser vedlegg sjekker.
        verify(soknadRepository).lagreFaktum(soknadId, faktum);

    }

    @Test
    public void skalSletteBrukerfaktum() {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(111L).medSkjemaNummer("a1").medFaktumId(111L);
        when(soknadRepository.hentSoknad(1L)).thenReturn(new WebSoknad().medId(1L).medDelstegStatus(DelstegStatus.UTFYLLING));
        when(vedleggRepository.hentVedleggForFaktum(1L, 1L)).thenReturn(Arrays.asList(vedlegg));
        when(soknadRepository.hentFaktum(1L)).thenReturn(new Faktum().medKey("key").medSoknadId(1L));
        faktaService.slettBrukerFaktum(1L);
        verify(vedleggRepository).slettVedleggOgData(1L, vedlegg);
        verify(soknadRepository).slettBrukerFaktum(1L, 1L);
        verify(soknadRepository).settDelstegstatus(1L, DelstegStatus.UTFYLLING);
    }

    @Test
    public void skalSletteVedlegg() {
        Long soknadId = 1L;
        Long faktumId = 1L;
        Faktum arbeidsforholdFaktum = new Faktum().medKey("arbeidsforhold").medProperty("type", "Permittert").medFaktumId(1L).medSoknadId(soknadId);
        Vedlegg permitteringsVedlegg = new Vedlegg().medVedleggId(4L).medSkjemaNummer("G2").medSoknadId(soknadId).medInnsendingsvalg(Vedlegg.Status.UnderBehandling).medFaktumId(faktumId);
        Vedlegg arbeidsgiverVedlegg = new Vedlegg().medVedleggId(4L).medSkjemaNummer("O2").medSoknadId(soknadId).medInnsendingsvalg(Vedlegg.Status.UnderBehandling).medFaktumId(faktumId);

        when(soknadRepository.hentFaktum(faktumId)).thenReturn(arbeidsforholdFaktum);
        when(vedleggRepository.hentVedleggForFaktum(soknadId, faktumId)).thenReturn(Arrays.asList(permitteringsVedlegg, arbeidsgiverVedlegg));
        when(soknadRepository.hentSoknad(1L)).thenReturn(new WebSoknad().medId(1L).medDelstegStatus(DelstegStatus.UTFYLLING));

        faktaService.slettBrukerFaktum(faktumId);

        verify(vedleggRepository, times(1)).slettVedleggOgData(soknadId, permitteringsVedlegg);
        verify(vedleggRepository, times(1)).slettVedleggOgData(soknadId, arbeidsgiverVedlegg);
        verify(soknadRepository, times(1)).slettBrukerFaktum(soknadId, faktumId);
    }

    @Test
    public void skalLagreSystemfaktummedUniqueSomFinnes() {
        Faktum faktum = new Faktum().medKey("personalia").medSystemProperty("fno", "123").medSoknadId(1L);
        Faktum faktumSjekk = new Faktum().medKey("personalia").medSystemProperty("fno", "123").medSoknadId(1L).medType(Faktum.FaktumType.SYSTEMREGISTRERT);

        when(soknadRepository.lagreFaktum(anyLong(), any(Faktum.class), anyBoolean())).thenReturn(2L);
        when(soknadRepository.hentFaktum(2L)).thenReturn(faktum);
        when(soknadRepository.hentSystemFaktumList(1L, faktum.getKey())).thenReturn(Arrays.asList(
                new Faktum().medFaktumId(5L).medKey("personalia").medSystemProperty("fno", "123"),
                new Faktum().medFaktumId(6L).medKey("personalia").medSystemProperty("fno", "124")));
        faktaService.lagreSystemFaktum(1L, faktum, "fno");
        verify(soknadRepository).lagreFaktum(1L, faktumSjekk.medFaktumId(5L), true);
    }

    @Test
    public void skalLagreSystemfaktumUtenUnique() {
        Faktum faktum = new Faktum().medKey("personalia").medValue("tester").medSoknadId(1L);
        when(soknadRepository.lagreFaktum(anyLong(), any(Faktum.class), anyBoolean())).thenReturn(2L);
        when(soknadRepository.hentFaktum(2L)).thenReturn(faktum);
        faktaService.lagreSystemFaktum(1L, faktum, "");
        verify(soknadRepository).lagreFaktum(1L, faktum, true);
    }
}
