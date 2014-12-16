package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.modig.core.context.StaticSubjectHandler;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.Arrays;

import static java.lang.String.format;
import static java.lang.System.setProperty;
import static javax.xml.bind.JAXBContext.newInstance;
import static no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FaktumServiceTest {
    @Mock
    private SoknadRepository soknadRepository;
    @Mock
    private VedleggRepository vedleggRepository;
    @Mock
    private NavMessageSource navMessageSource;
    @Mock
    private SoknadService soknadService;

    @InjectMocks
    private FaktumService faktumService;

    private static final String DAGPENGER = "NAV 04-01.03.xml";

    @Before
    public void before() {
        SoknadStruktur struktur;
        try {
            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class)
                    .createUnmarshaller();
            struktur = (SoknadStruktur) unmarshaller.unmarshal(SoknadStruktur.class
                    .getResourceAsStream(format("/soknader/%s", DAGPENGER)));
        } catch (JAXBException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }

        setProperty(SUBJECTHANDLER_KEY, StaticSubjectHandler.class.getName());
        when(soknadService.hentSoknadStruktur(anyLong())).thenReturn(struktur);
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

        faktumService.lagreSoknadsFelt(soknadId, permitteringsFaktum);

        assertThat(permitteringsVedlegg.getInnsendingsvalg(), is(Vedlegg.Status.IkkeVedlegg));
    }

    @Test
    public void skalIkkeoppdatereDelstegstatusVedEpost() {
        Faktum faktum = new Faktum().medKey("epost").medValue("false").medFaktumId(1L);
        when(soknadRepository.lagreFaktum(1L, faktum)).thenReturn(2L);
        when(soknadRepository.hentFaktum(1L, 2L)).thenReturn(faktum);
        faktumService.lagreSoknadsFelt(1L, faktum);
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
        faktumService.lagreSoknadsFelt(1L, faktum);
        verify(soknadRepository).settSistLagretTidspunkt(1L);
        when(soknadRepository.hentBarneFakta(1L, faktum.getFaktumId())).thenReturn(Arrays.asList(new Faktum().medKey("subkey")));

        //Verifiser vedlegg sjekker.
        verify(soknadRepository).lagreFaktum(1L, faktum);
        verify(vedleggRepository).lagreVedlegg(1L, 4L, vedlegg.medInnsendingsvalg(Vedlegg.Status.VedleggKreves));

    }
}
