package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.EndringDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.ForelderBarnRelasjonDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.MetadataDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto;
import no.nav.sosialhjelp.soknad.domain.model.Barn;
import no.nav.sosialhjelp.soknad.person.domain.Ektefelle;
import no.nav.sosialhjelp.soknad.person.domain.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto.SivilstandType.GIFT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    private static final String BARN_IDENT = "11111111111";
    private static final String EKTEFELLE_IDENT = "22222222222";
    private static final String FDAT_IDENT = "11122200000";

    @Mock
    private PdlHentPersonConsumer pdlHentPersonConsumer;
    @Mock
    private PdlPersonMapper pdlPersonMapper;

    @InjectMocks
    private PersonService personService;

    private final PdlPerson mockPdlPerson = mock(PdlPerson.class);
    private final PdlEktefelle mockPdlEktefelle = mock(PdlEktefelle.class);
    private final PdlBarn mockPdlBarn = mock(PdlBarn.class);

    private final Person person = new Person("fornavn", "mellomnavn", "etternavn", "fnr", "ugift", emptyList(), null, null, null, null);
    private final Ektefelle ektefelle = new Ektefelle("fornavn", null, "etternavn", LocalDate.now(), "fnr2", true, false);
    private final Barn barn = new Barn();

    @Test
    void skalHentePersonMedEktefelle() {
        when(pdlHentPersonConsumer.hentPerson(any())).thenReturn(mockPdlPerson);
        when(pdlPersonMapper.mapToPerson(any(), any())).thenReturn(person);

        when(mockPdlPerson.getSivilstand()).thenReturn(singletonList(new SivilstandDto(GIFT, EKTEFELLE_IDENT, new MetadataDto("PDL", singletonList(new EndringDto("PDL", LocalDateTime.now(), null))), null)));

        when(pdlHentPersonConsumer.hentEktefelle(any())).thenReturn(mockPdlEktefelle);
        when(pdlPersonMapper.mapToEktefelle(any(), any(), any())).thenReturn(ektefelle);

        Person result = personService.hentPerson("ident");

        assertThat(result.getEktefelle()).isEqualTo(ektefelle);
    }

    @Test
    void skalHentePersonMenIkkeEktefelleHvisEktefelleidentErNull() {
        when(pdlHentPersonConsumer.hentPerson(any())).thenReturn(mockPdlPerson);
        when(pdlPersonMapper.mapToPerson(any(), any())).thenReturn(person);

        when(mockPdlPerson.getSivilstand()).thenReturn(singletonList(new SivilstandDto(GIFT, null, new MetadataDto("PDL", singletonList(new EndringDto("PDL", LocalDateTime.now(), null))), null)));

        Person result = personService.hentPerson("ident");

        assertThat(result.getEktefelle()).isNull();
        verify(pdlHentPersonConsumer, times(0)).hentEktefelle(anyString());
        verify(pdlPersonMapper, times(0)).mapToEktefelle(any(), anyString(), any());
    }

    @Test
    void skalHentePersonMenIkkeEktefelleHvisEktefelleidentErFDAT() {
        when(pdlHentPersonConsumer.hentPerson(any())).thenReturn(mockPdlPerson);
        when(pdlPersonMapper.mapToPerson(any(), any())).thenReturn(person);

        when(mockPdlPerson.getSivilstand()).thenReturn(singletonList(new SivilstandDto(GIFT, FDAT_IDENT, new MetadataDto("PDL", singletonList(new EndringDto("PDL", LocalDateTime.now(), null))), null)));

        Person result = personService.hentPerson("ident");

        assertThat(result.getEktefelle()).isNotNull();
        assertThat(result.getEktefelle().getFnr()).isEqualTo(FDAT_IDENT);
        assertThat(result.getEktefelle().getFodselsdato()).hasToString(LocalDate.of(1922, 12, 11).toString());
        verify(pdlHentPersonConsumer, times(0)).hentEktefelle(anyString());
        verify(pdlPersonMapper, times(0)).mapToEktefelle(any(), anyString(), any());
    }

    @Test
    void skalHentePersonUtenEktefelle() {
        when(pdlHentPersonConsumer.hentPerson(any())).thenReturn(mockPdlPerson);
        when(pdlPersonMapper.mapToPerson(any(), any())).thenReturn(person);

        when(mockPdlPerson.getSivilstand()).thenReturn(Collections.emptyList());

        Person result = personService.hentPerson("ident");

        assertThat(result.getEktefelle()).isNull();
    }

    @Test
    void skalHenteBarn() {
        when(pdlHentPersonConsumer.hentPerson(any())).thenReturn(mockPdlPerson);

        when(mockPdlPerson.getForelderBarnRelasjon()).thenReturn(asList(new ForelderBarnRelasjonDto(BARN_IDENT, "BARN", "MOR")));

        when(pdlHentPersonConsumer.hentBarn(any())).thenReturn(mockPdlBarn);
        when(pdlPersonMapper.mapToBarn(any(), any(), any())).thenReturn(barn);

        List<Barn> result = personService.hentBarnForPerson("ident");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(barn);
    }

    @Test
    void skalFiltrereVekkNullBarn() {
        when(pdlHentPersonConsumer.hentPerson(any())).thenReturn(mockPdlPerson);

        when(mockPdlPerson.getForelderBarnRelasjon()).thenReturn(asList(new ForelderBarnRelasjonDto(BARN_IDENT, "BARN", "MOR")));

        when(pdlHentPersonConsumer.hentBarn(any())).thenReturn(mockPdlBarn);
        when(pdlPersonMapper.mapToBarn(any(), any(), any())).thenReturn(null);

        List<Barn> result = personService.hentBarnForPerson("ident");

        assertThat(result).isEmpty();
    }

    @Test
    void skalIkkeHenteBarnHvisIdentErNull() {
        when(pdlHentPersonConsumer.hentPerson(any())).thenReturn(mockPdlPerson);

        when(mockPdlPerson.getForelderBarnRelasjon()).thenReturn(asList(new ForelderBarnRelasjonDto(null, "BARN", "MOR")));

        List<Barn> result = personService.hentBarnForPerson("ident");

        assertThat(result).isEmpty();
        verify(pdlHentPersonConsumer, times(0)).hentBarn(anyString());
        verify(pdlPersonMapper, times(0)).mapToBarn(any(), anyString(), any());
    }

    @Test
    void skalIkkeHenteBarnHvisIdentErFDAT() {
        when(pdlHentPersonConsumer.hentPerson(any())).thenReturn(mockPdlPerson);

        when(mockPdlPerson.getForelderBarnRelasjon()).thenReturn(asList(new ForelderBarnRelasjonDto(FDAT_IDENT, "BARN", "MOR")));

        List<Barn> result = personService.hentBarnForPerson("ident");

        assertThat(result).isEmpty();
        verify(pdlHentPersonConsumer, times(0)).hentBarn(anyString());
        verify(pdlPersonMapper, times(0)).mapToBarn(any(), anyString(), any());
    }

    @Test
    void skalHenteAdressebeskyttelse() {
        when(pdlHentPersonConsumer.hentAdressebeskyttelse(any())).thenReturn(mock(PdlAdressebeskyttelse.class));
        when(pdlPersonMapper.mapToAdressebeskyttelse(any())).thenReturn(AdressebeskyttelseDto.Gradering.UGRADERT);

        var result = personService.hentAdressebeskyttelse("ident");

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(AdressebeskyttelseDto.Gradering.UGRADERT);
    }
}