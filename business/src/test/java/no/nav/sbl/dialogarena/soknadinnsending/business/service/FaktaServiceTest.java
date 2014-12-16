package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
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
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.WebSoknadUtils.DAGPENGER;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
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

    @InjectMocks
    private FaktaService faktaService;

    @Before
    public void before() {
        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        when(soknadRepository.hentSoknadType(anyLong())).thenReturn(DAGPENGER);
    }

    @Test
    public void oppdatereFaktum() {
        Long soknadId = 1L;
        Long arbeidsforholdFaktumId = 1L;
        Long permitteringFaktumId = 2L;
        Faktum arbeidsforholdFaktum = new Faktum().medKey("arbeidsforhold").medProperty("type", "Konkurs").medFaktumId(1L);
        Faktum permitteringsFaktum = new Faktum().medKey("arbeidsforhold.permitteringsperiode").medProperty("permitteringsperiodefra", "1111-11-11").medFaktumId(permitteringFaktumId).medParrentFaktumId(arbeidsforholdFaktumId);
        Vedlegg permitteringsVedlegg = new Vedlegg().medVedleggId(4L).medSkjemaNummer("G2").medSoknadId(soknadId).medInnsendingsvalg(Vedlegg.Status.UnderBehandling).medFaktumId(permitteringFaktumId);
        Vedlegg arbeidsgiverVedlegg = new Vedlegg().medVedleggId(4L).medSkjemaNummer("O2").medSoknadId(soknadId).medInnsendingsvalg(Vedlegg.Status.UnderBehandling).medFaktumId(arbeidsforholdFaktumId);

        when(soknadRepository.lagreFaktum(soknadId, permitteringsFaktum)).thenReturn(permitteringFaktumId);
        when(soknadRepository.hentFaktum(soknadId, permitteringFaktumId)).thenReturn(permitteringsFaktum);
        when(soknadRepository.hentFaktum(soknadId, arbeidsforholdFaktumId)).thenReturn(arbeidsforholdFaktum);
        when(vedleggRepository.hentVedleggForskjemaNummer(soknadId, permitteringFaktumId, "G2")).thenReturn(permitteringsVedlegg);
        when(vedleggRepository.hentVedleggForskjemaNummer(soknadId, arbeidsforholdFaktumId, "O2")).thenReturn(arbeidsgiverVedlegg);

        faktaService.lagreSoknadsFelt(soknadId, permitteringsFaktum);

        assertThat(permitteringsVedlegg.getInnsendingsvalg(), is(Vedlegg.Status.IkkeVedlegg));
    }

    @Test
    public void skalIkkeoppdatereDelstegstatusVedEpost() {
        Faktum faktum = new Faktum().medKey("epost").medValue("false").medFaktumId(1L);
        when(soknadRepository.lagreFaktum(1L, faktum)).thenReturn(2L);
        when(soknadRepository.hentFaktum(1L, 2L)).thenReturn(faktum);
        faktaService.lagreSoknadsFelt(1L, faktum);
        verify(soknadRepository, never()).settDelstegstatus(anyLong(), any(DelstegStatus.class));
    }

    @Test
    public void skalLagreSoknadFelt() {
        Faktum faktum = new Faktum().medKey("ikkeavtjentverneplikt").medValue("false").medFaktumId(1L);
        when(soknadRepository.lagreFaktum(1L, faktum)).thenReturn(2L);
        when(soknadRepository.hentFaktum(1L, 2L)).thenReturn(faktum);
        Vedlegg vedlegg = new Vedlegg().medVedleggId(4L).medSkjemaNummer("T3").medSoknadId(1L).medInnsendingsvalg(Vedlegg.Status.IkkeVedlegg);
        when(vedleggRepository.hentVedleggForskjemaNummer(1L, null, "T3")).thenReturn(vedlegg);
        when(vedleggRepository.opprettVedlegg(any(Vedlegg.class), any(byte[].class))).thenReturn(4L);
        faktaService.lagreSoknadsFelt(1L, faktum);
        verify(soknadRepository).settSistLagretTidspunkt(1L);
        when(soknadRepository.hentBarneFakta(1L, faktum.getFaktumId())).thenReturn(Arrays.asList(new Faktum().medKey("subkey")));

        //Verifiser vedlegg sjekker.
        verify(soknadRepository).lagreFaktum(1L, faktum);
        verify(vedleggRepository).lagreVedlegg(1L, 4L, vedlegg.medInnsendingsvalg(Vedlegg.Status.VedleggKreves));

    }

    @Test
    public void skalSletteBrukerfaktum() {
        when(vedleggRepository.hentVedleggForFaktum(1L, 1L)).thenReturn(Arrays.asList(new Vedlegg().medVedleggId(111L).medSkjemaNummer("a1").medFaktumId(111L)));
        when(soknadRepository.hentFaktum(1L, 1L)).thenReturn(new Faktum().medKey("key"));
        faktaService.slettBrukerFaktum(1L, 1L);
        verify(vedleggRepository).slettVedleggOgData(1L, 111L, "a1");
        verify(soknadRepository).slettBrukerFaktum(1L, 1L);
        verify(soknadRepository).settDelstegstatus(1L, DelstegStatus.UTFYLLING);
    }

    @Test
    public void skalSletteVedlegg() {
        Long soknadId = 1L;
        Long faktumId = 1L;
        Faktum arbeidsforholdFaktum = new Faktum().medKey("arbeidsforhold").medProperty("type", "Permittert").medFaktumId(1L);
        Vedlegg permitteringsVedlegg = new Vedlegg().medVedleggId(4L).medSkjemaNummer("G2").medSoknadId(soknadId).medInnsendingsvalg(Vedlegg.Status.UnderBehandling).medFaktumId(faktumId);
        Vedlegg arbeidsgiverVedlegg = new Vedlegg().medVedleggId(4L).medSkjemaNummer("O2").medSoknadId(soknadId).medInnsendingsvalg(Vedlegg.Status.UnderBehandling).medFaktumId(faktumId);

        when(soknadRepository.hentFaktum(soknadId, faktumId)).thenReturn(arbeidsforholdFaktum);
        when(vedleggRepository.hentVedleggForFaktum(soknadId, faktumId)).thenReturn(Arrays.asList(permitteringsVedlegg, arbeidsgiverVedlegg));

        faktaService.slettBrukerFaktum(soknadId, faktumId);

        verify(vedleggRepository, times(1)).slettVedleggOgData(soknadId, faktumId, "G2");
        verify(vedleggRepository, times(1)).slettVedleggOgData(soknadId, faktumId, "O2");
        verify(soknadRepository, times(1)).slettBrukerFaktum(soknadId, faktumId);
    }

    @Test
    public void skalLagreSystemfaktummedUniqueSomFinnes() {
        Faktum faktum = new Faktum().medKey("personalia").medSystemProperty("fno", "123").medSoknadId(1L);
        Faktum faktumSjekk = new Faktum().medKey("personalia").medSystemProperty("fno", "123").medSoknadId(1L).medType(Faktum.FaktumType.SYSTEMREGISTRERT);

        when(soknadRepository.lagreFaktum(anyLong(), any(Faktum.class), anyBoolean())).thenReturn(2L);
        when(soknadRepository.hentFaktum(1L, 2L)).thenReturn(faktum);
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
        when(soknadRepository.hentFaktum(1L, 2L)).thenReturn(faktum);
        faktaService.lagreSystemFaktum(1L, faktum, "");
        verify(soknadRepository).lagreFaktum(1L, faktum, true);
    }
}
