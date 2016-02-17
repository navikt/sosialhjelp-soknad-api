package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.IkkeFunnetException;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.System.setProperty;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static no.nav.sbl.dialogarena.sendsoknad.domain.DelstegStatus.OPPRETTET;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.DAGPENGER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

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
        when(soknadRepository.opprettFaktum(soknadId, faktum)).thenReturn(2L);
        when(soknadRepository.hentFaktum(2L)).thenReturn(faktum);
        faktaService.opprettBrukerFaktum(behandlingsId, faktum);
        verify(soknadRepository, never()).settDelstegstatus(anyLong(), any(DelstegStatus.class));
    }

    @Test
    public void skalIkkeoppdatereDelstegstatusVedSprakFaktum() {
        long soknadId = 1L;
        String behandlingsId = "1000000ABC";
        Faktum faktum = new Faktum().medKey("skjema.sprak").medValue("nb_NO").medFaktumId(soknadId);
        when(soknadRepository.hentSoknad(behandlingsId)).thenReturn(new WebSoknad().medId(soknadId).medDelstegStatus(DelstegStatus.UTFYLLING));
        when(soknadRepository.hentSoknad(soknadId)).thenReturn(new WebSoknad().medId(soknadId).medDelstegStatus(DelstegStatus.UTFYLLING));
        when(soknadRepository.opprettFaktum(soknadId, faktum)).thenReturn(2L);
        when(soknadRepository.hentFaktum(2L)).thenReturn(faktum);
        faktaService.opprettBrukerFaktum(behandlingsId, faktum);
        verify(soknadRepository, never()).settDelstegstatus(anyLong(), any(DelstegStatus.class));
    }

    @Test
    public void skalLagreSoknadFelt() {
        long soknadId = 1L;
        String behandlingsId = "1000000ABC";
        Faktum faktum = new Faktum().medKey("ikkeavtjentverneplikt").medValue("false").medFaktumId(soknadId);
        when(soknadRepository.hentSoknad(behandlingsId)).thenReturn(new WebSoknad().medId(soknadId));
        when(soknadRepository.opprettFaktum(soknadId, faktum)).thenReturn(2L);
        when(soknadRepository.hentFaktum(2L)).thenReturn(faktum);
        faktaService.opprettBrukerFaktum(behandlingsId, faktum);
        verify(soknadRepository).settSistLagretTidspunkt(soknadId);
        when(soknadRepository.hentBarneFakta(soknadId, faktum.getFaktumId())).thenReturn(Collections.singletonList(new Faktum().medKey("subkey")));

        //Verifiser vedlegg sjekker.
        verify(soknadRepository).opprettFaktum(soknadId, faktum);

    }

    @Test(expected = IkkeFunnetException.class)
    public void skalKasteExceptionOmEnProverAaSletteNoeSomIkkeFinnes() {
        when(soknadRepository.hentFaktum(1L)).thenThrow(new IncorrectResultSizeDataAccessException(1, 0));
        faktaService.slettBrukerFaktum(1L);
    }

    @Test
    public void skalSletteBrukerfaktum() {
        Vedlegg vedlegg = new Vedlegg().medVedleggId(111L).medSkjemaNummer("a1").medFaktumId(111L);
        when(soknadRepository.hentSoknad(1L)).thenReturn(new WebSoknad().medId(1L).medDelstegStatus(DelstegStatus.UTFYLLING));
        when(vedleggRepository.hentVedleggForFaktum(1L, 1L)).thenReturn(Collections.singletonList(vedlegg));
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
    public void skalOppretteSystemFaktum() {
        Faktum faktum = new Faktum().medKey("personalia").medSystemProperty("fno", "123").medSoknadId(1L);
        Faktum faktumSjekk = new Faktum().medKey("personalia").medSystemProperty("fno", "123").medSoknadId(1L).medType(Faktum.FaktumType.SYSTEMREGISTRERT);

        when(soknadRepository.oppdaterFaktum(any(Faktum.class), anyBoolean())).thenReturn(2L);
        when(soknadRepository.hentFaktum(2L)).thenReturn(faktum);
        when(soknadRepository.hentSystemFaktumList(1L, faktum.getKey())).thenReturn(Arrays.asList(
                new Faktum().medFaktumId(5L).medKey("personalia").medSystemProperty("fno", "123").medSoknadId(1L),
                new Faktum().medFaktumId(6L).medKey("personalia").medSystemProperty("fno", "124").medSoknadId(1L)));
        faktaService.lagreSystemFaktum(1L, faktum);
        verify(soknadRepository).oppdaterFaktum(faktumSjekk.medFaktumId(5L), true);
    }

    @Test
    public void skalLagreSystemFaktumHvisFaktumId() {
        WebSoknad soknad = new WebSoknad().medId(1L).medFaktum(new Faktum().medKey("personalia").medSoknadId(new Long(1L)).medFaktumId(5L).medValue("gammel"));
        List<Faktum> fakta = Collections.singletonList(new Faktum().medKey("personalia").medSoknadId(1L).medValue("ny"));

        ArgumentCaptor<Faktum> argument = ArgumentCaptor.forClass(Faktum.class);
        faktaService.lagreSystemFakta(soknad, fakta);
        verify(soknadRepository).oppdaterFaktum(argument.capture(), anyBoolean());
        assertEquals(new Long(5L), argument.getValue().getFaktumId());
    }

    @Test
    public void skalOppdatereFaktaMedSammeUnikProperty() {
        WebSoknad soknad = new WebSoknad().medId(1L).medFaktum(lagFaktumMedUnikProperty("123").medValue("gammel").medFaktumId(5L));
        List<Faktum> fakta = Collections.singletonList(lagFaktumMedUnikProperty("123").medValue("ny"));

        ArgumentCaptor<Faktum> argument = ArgumentCaptor.forClass(Faktum.class);
        faktaService.lagreSystemFakta(soknad, fakta);
        verify(soknadRepository).oppdaterFaktum(argument.capture(), anyBoolean());
        assertEquals(new Long(5L), argument.getValue().getFaktumId());
    }

    @Test
    public void skalOppdatereFaktaMedSammeKeyOgUtenUnikProperty() {
        WebSoknad soknad = new WebSoknad().medId(1L).medFaktum(new Faktum().medKey("personalia").medSoknadId(new Long(1L)).medFaktumId(5L).medValue("gammel"));
        List<Faktum> fakta = Collections.singletonList(new Faktum().medKey("personalia").medSoknadId(1L).medValue("ny"));

        ArgumentCaptor<Faktum> argument = ArgumentCaptor.forClass(Faktum.class);
        faktaService.lagreSystemFakta(soknad, fakta);
        verify(soknadRepository).oppdaterFaktum(argument.capture(), anyBoolean());
        assertEquals(new Long(5L), argument.getValue().getFaktumId());
    }

    private Faktum lagFaktumMedUnikProperty(String value) {
        return new Faktum()
                .medSoknadId(1L)
                .medKey("personalia")
                .medUnikProperty("unik")
                .medProperty("unik", value);
    }
}
