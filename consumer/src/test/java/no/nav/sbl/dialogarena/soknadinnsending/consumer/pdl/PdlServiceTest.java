package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.barn.PdlBarn;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.EndringDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FamilierelasjonDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.MetadataDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.ektefelle.PdlEktefelle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.common.utils.CollectionUtils.listOf;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto.SivilstandType.GIFT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PdlServiceTest {

    private static final String BARN_IDENT = "11111111111";
    private static final String EKTEFELLE_IDENT = "22222222222";
    private static final String FDAT_IDENT = "11122200000";

    @Mock
    private PdlConsumer pdlConsumer;
    @Mock
    private PdlPersonMapper pdlPersonMapper;

    @InjectMocks
    private PdlService pdlService;

    private final PdlPerson mockPdlPerson = mock(PdlPerson.class);
    private final PdlEktefelle mockPdlEktefelle = mock(PdlEktefelle.class);
    private final PdlBarn mockPdlBarn = mock(PdlBarn.class);

    private final Person person = new Person();
    private final Ektefelle ektefelle = new Ektefelle();
    private final Barn barn = new Barn();

    @Test
    public void skalHentePersonMedEktefelle() {
        when(pdlConsumer.hentPerson(any())).thenReturn(mockPdlPerson);
        when(pdlPersonMapper.mapToPerson(any(), any())).thenReturn(person);

        when(mockPdlPerson.getSivilstand()).thenReturn(singletonList(new SivilstandDto(GIFT, EKTEFELLE_IDENT, new MetadataDto("PDL", singletonList(new EndringDto("PDL", LocalDateTime.now(), null, null, null))), null)));

        when(pdlConsumer.hentEktefelle(any())).thenReturn(mockPdlEktefelle);
        when(pdlPersonMapper.mapToEktefelle(any(), any(), any())).thenReturn(ektefelle);

        Person result = pdlService.hentPerson("ident");

        assertThat(result.getEktefelle(), is(ektefelle));
    }

    @Test
    public void skalHentePersonMenIkkeEktefelleHvisEktefelleidentErNull() {
        when(pdlConsumer.hentPerson(any())).thenReturn(mockPdlPerson);
        when(pdlPersonMapper.mapToPerson(any(), any())).thenReturn(person);

        when(mockPdlPerson.getSivilstand()).thenReturn(singletonList(new SivilstandDto(GIFT, null, new MetadataDto("PDL", singletonList(new EndringDto("PDL", LocalDateTime.now(), null, null, null))), null)));

        Person result = pdlService.hentPerson("ident");

        assertThat(result.getEktefelle(), is(nullValue()));
        verify(pdlConsumer, times(0)).hentEktefelle(anyString());
        verify(pdlPersonMapper, times(0)).mapToEktefelle(any(), anyString(), any());
    }

    @Test
    public void skalHentePersonMenIkkeEktefelleHvisEktefelleidentErFDAT() {
        when(pdlConsumer.hentPerson(any())).thenReturn(mockPdlPerson);
        when(pdlPersonMapper.mapToPerson(any(), any())).thenReturn(person);

        when(mockPdlPerson.getSivilstand()).thenReturn(singletonList(new SivilstandDto(GIFT, FDAT_IDENT, new MetadataDto("PDL", singletonList(new EndringDto("PDL", LocalDateTime.now(), null, null, null))), null)));

        Person result = pdlService.hentPerson("ident");

        assertThat(result.getEktefelle(), is(notNullValue()));
        assertThat(result.getEktefelle().getFnr(), is(FDAT_IDENT));
        assertThat(result.getEktefelle().getFodselsdato().toString(), is(LocalDate.of(1922, 12, 11).toString()));
        verify(pdlConsumer, times(0)).hentEktefelle(anyString());
        verify(pdlPersonMapper, times(0)).mapToEktefelle(any(), anyString(), any());
    }

    @Test
    public void skalHentePersonUtenEktefelle() {
        when(pdlConsumer.hentPerson(any())).thenReturn(mockPdlPerson);
        when(pdlPersonMapper.mapToPerson(any(), any())).thenReturn(person);

        when(mockPdlPerson.getSivilstand()).thenReturn(Collections.emptyList());

        Person result = pdlService.hentPerson("ident");

        assertNull(result.getEktefelle());
    }

    @Test
    public void skalHenteBarn() {
        when(pdlConsumer.hentPerson(any())).thenReturn(mockPdlPerson);

        when(mockPdlPerson.getFamilierelasjoner()).thenReturn(listOf(new FamilierelasjonDto(BARN_IDENT, "BARN", "MOR")));

        when(pdlConsumer.hentBarn(any())).thenReturn(mockPdlBarn);
        when(pdlPersonMapper.mapToBarn(any(), any(), any())).thenReturn(barn);

        List<Barn> result = pdlService.hentBarnForPerson("ident");

        assertNotNull(result);
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is(barn));
    }

    @Test
    public void skalFiltrereVekkNullBarn() {
        when(pdlConsumer.hentPerson(any())).thenReturn(mockPdlPerson);

        when(mockPdlPerson.getFamilierelasjoner()).thenReturn(listOf(new FamilierelasjonDto(BARN_IDENT, "BARN", "MOR")));

        when(pdlConsumer.hentBarn(any())).thenReturn(mockPdlBarn);
        when(pdlPersonMapper.mapToBarn(any(), any(), any())).thenReturn(null);

        List<Barn> result = pdlService.hentBarnForPerson("ident");

        assertNotNull(result);
        assertThat(result, hasSize(0));
    }

    @Test
    public void skalIkkeHenteBarnHvisIdentErNull() {
        when(pdlConsumer.hentPerson(any())).thenReturn(mockPdlPerson);

        when(mockPdlPerson.getFamilierelasjoner()).thenReturn(listOf(new FamilierelasjonDto(null, "BARN", "MOR")));

        List<Barn> result = pdlService.hentBarnForPerson("ident");

        assertNotNull(result);
        assertThat(result, hasSize(0));
        verify(pdlConsumer, times(0)).hentBarn(anyString());
        verify(pdlPersonMapper, times(0)).mapToBarn(any(), anyString(), any());
    }

    @Test
    public void skalIkkeHenteBarnHvisIdentErFDAT() {
        when(pdlConsumer.hentPerson(any())).thenReturn(mockPdlPerson);

        when(mockPdlPerson.getFamilierelasjoner()).thenReturn(listOf(new FamilierelasjonDto(FDAT_IDENT, "BARN", "MOR")));

        List<Barn> result = pdlService.hentBarnForPerson("ident");

        assertNotNull(result);
        assertThat(result, hasSize(0));
        verify(pdlConsumer, times(0)).hentBarn(anyString());
        verify(pdlPersonMapper, times(0)).mapToBarn(any(), anyString(), any());
    }
}