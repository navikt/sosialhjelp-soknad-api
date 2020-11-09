package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.barn.PdlBarn;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FamilierelasjonDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.ektefelle.PdlEktefelle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static no.nav.common.utils.CollectionUtils.listOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PdlServiceTest {

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
        when(pdlPersonMapper.mapTilPerson(any(), any())).thenReturn(person);

        when(mockPdlPerson.getSivilstand()).thenReturn(listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, "ident")));

        when(pdlConsumer.hentEktefelle(any())).thenReturn(mockPdlEktefelle);
        when(pdlPersonMapper.mapTilEktefelle(any(), any(), any())).thenReturn(ektefelle);

        Person result = pdlService.hentPerson("ident");

        assertThat(result.getEktefelle(), is(ektefelle));
    }

    @Test
    public void skalHentePersonUtenEktefelle() {
        when(pdlConsumer.hentPerson(any())).thenReturn(mockPdlPerson);
        when(pdlPersonMapper.mapTilPerson(any(), any())).thenReturn(person);

        when(mockPdlPerson.getSivilstand()).thenReturn(Collections.emptyList());

        Person result = pdlService.hentPerson("ident");

        assertNull(result.getEktefelle());
    }

    @Test
    public void skalHenteBarn() {
        when(pdlConsumer.hentPerson(any())).thenReturn(mockPdlPerson);

        when(mockPdlPerson.getFamilierelasjoner()).thenReturn(listOf(new FamilierelasjonDto("barnIdent", "BARN", "MOR")));

        when(pdlConsumer.hentBarn(any())).thenReturn(mockPdlBarn);
        when(pdlPersonMapper.mapTilBarn(any(), any(), any())).thenReturn(barn);

        List<Barn> result = pdlService.hentBarnForPerson("ident");

        assertNotNull(result);
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is(barn));
    }

    @Test
    public void skalFiltrereVekkNullBarn() {
        when(pdlConsumer.hentPerson(any())).thenReturn(mockPdlPerson);

        when(mockPdlPerson.getFamilierelasjoner()).thenReturn(listOf(new FamilierelasjonDto("barnIdent", "BARN", "MOR")));

        when(pdlConsumer.hentBarn(any())).thenReturn(mockPdlBarn);
        when(pdlPersonMapper.mapTilBarn(any(), any(), any())).thenReturn(null);

        List<Barn> result = pdlService.hentBarnForPerson("ident");

        assertNotNull(result);
        assertThat(result, hasSize(0));
    }
}