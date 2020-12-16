package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdlperson;

import no.finn.unleash.Unleash;
import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.exceptions.PdlApiException;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.person.PersonService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.pdlperson.PdlEllerPersonV1Service.FEATURE_BRUK_PDL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PdlEllerPersonV1ServiceTest {

    private final Person mockPerson = mock(Person.class);
    private final Barn barn1 = mock(Barn.class);
    private final Barn barn2 = mock(Barn.class);

    @Mock
    private PdlService pdlService;

    @Mock
    private PersonService personService;

    @Mock
    private PersonSammenligner personSammenligner;

    @Mock
    private Unleash unleashConsumer;

    @InjectMocks
    private PdlEllerPersonV1Service pdlEllerPersonV1Service;

    @Test
    public void skalHentePersonFraPdl_pdl_enabled() {
        when(unleashConsumer.isEnabled(FEATURE_BRUK_PDL, false)).thenReturn(true);
        when(pdlService.hentPerson(anyString())).thenReturn(mockPerson);

        var person = pdlEllerPersonV1Service.hentPerson("fnr");

        assertThat(person).isEqualTo(mockPerson);

        verify(pdlService, times(1)).hentPerson(anyString());
        verify(personService, times(0)).hentPerson(anyString());
        verify(personSammenligner, times(0)).sammenlign(any(), any());
    }

    @Test
    public void skalHentePersonFraTpsHvisPdlFeiler_pdl_enabled() {
        when(unleashConsumer.isEnabled(FEATURE_BRUK_PDL, false)).thenReturn(true);
        when(pdlService.hentPerson(anyString())).thenThrow(new PdlApiException("Noe feilet"));
        when(personService.hentPerson(any())).thenReturn(mockPerson);

        var person = pdlEllerPersonV1Service.hentPerson("fnr");

        assertThat(person).isEqualTo(mockPerson);

        verify(pdlService, times(1)).hentPerson(anyString());
        verify(personService, times(1)).hentPerson(anyString());
        verify(personSammenligner, times(0)).sammenlign(any(), any());
    }

    @Test
    public void skalHentePersonFraTps_pdl_disabled() {
        when(unleashConsumer.isEnabled(FEATURE_BRUK_PDL, false)).thenReturn(false);
        when(personService.hentPerson(any())).thenReturn(mockPerson);
        when(pdlService.hentPerson(anyString())).thenReturn(mockPerson);

        var person = pdlEllerPersonV1Service.hentPerson("fnr");

        assertThat(person).isEqualTo(mockPerson);

        verify(pdlService, times(1)).hentPerson(anyString());
        verify(personService, times(1)).hentPerson(anyString());
        verify(personSammenligner, times(1)).sammenlign(any(), any());
    }

    @Test
    public void skalHentePersonFraTps_ingenSammenligningPdlFeiler_pdl_disabled() {
        when(unleashConsumer.isEnabled(FEATURE_BRUK_PDL, false)).thenReturn(false);
        when(personService.hentPerson(any())).thenReturn(mockPerson);
        when(pdlService.hentPerson(anyString())).thenThrow(new PdlApiException("Noe feilet"));

        var person = pdlEllerPersonV1Service.hentPerson("fnr");

        assertThat(person).isEqualTo(mockPerson);

        verify(pdlService, times(1)).hentPerson(anyString());
        verify(personService, times(1)).hentPerson(anyString());
        verify(personSammenligner, times(0)).sammenlign(any(), any());
    }

    @Test
    public void skalHenteBarnFraPdl_pdl_enabled() {
        when(unleashConsumer.isEnabled(FEATURE_BRUK_PDL, false)).thenReturn(true);
        when(pdlService.hentBarnForPerson(anyString())).thenReturn(asList(barn1, barn2));

        var alleBarn = pdlEllerPersonV1Service.hentBarn("fnr");

        assertThat(alleBarn).contains(barn1, barn2);

        verify(pdlService, times(1)).hentBarnForPerson(anyString());
        verify(personService, times(0)).hentBarn(anyString());
        verify(personSammenligner, times(0)).sammenlignBarn(anyList(), anyList());
    }

    @Test
    public void skalHenteBarnFraTpsHvisPdlFeiler_pdl_enabled() {
        when(unleashConsumer.isEnabled(FEATURE_BRUK_PDL, false)).thenReturn(true);
        when(pdlService.hentBarnForPerson(anyString())).thenThrow(new PdlApiException("Noe feilet"));
        when(personService.hentBarn(anyString())).thenReturn(asList(barn1, barn2));

        var alleBarn = pdlEllerPersonV1Service.hentBarn("fnr");

        assertThat(alleBarn).contains(barn1, barn2);

        verify(pdlService, times(1)).hentBarnForPerson(anyString());
        verify(personService, times(1)).hentBarn(anyString());
        verify(personSammenligner, times(0)).sammenlignBarn(anyList(), anyList());
    }

    @Test
    public void skalHenteBarnFraTps_pdl_disabled() {
        when(unleashConsumer.isEnabled(FEATURE_BRUK_PDL, false)).thenReturn(false);
        when(personService.hentBarn(any())).thenReturn(asList(barn1, barn2));
        when(pdlService.hentBarnForPerson(anyString())).thenReturn(asList(barn1, barn2));

        var alleBarn = pdlEllerPersonV1Service.hentBarn("fnr");

        assertThat(alleBarn).contains(barn1, barn2);

        verify(pdlService, times(1)).hentBarnForPerson(anyString());
        verify(personService, times(1)).hentBarn(anyString());
        verify(personSammenligner, times(1)).sammenlignBarn(anyList(), anyList());
    }

    @Test
    public void skalHenteBarnFraTps_ingenSammenligningPdlFeiler_pdl_disabled() {
        when(unleashConsumer.isEnabled(FEATURE_BRUK_PDL, false)).thenReturn(false);
        when(personService.hentBarn(any())).thenReturn(asList(barn1, barn2));
        when(pdlService.hentBarnForPerson(anyString())).thenThrow(new PdlApiException("Noe feilet"));

        var alleBarn = pdlEllerPersonV1Service.hentBarn("fnr");

        assertThat(alleBarn).contains(barn1, barn2);

        verify(pdlService, times(1)).hentBarnForPerson(anyString());
        verify(personService, times(1)).hentBarn(anyString());
        verify(personSammenligner, times(0)).sammenlignBarn(anyList(), anyList());
    }
}