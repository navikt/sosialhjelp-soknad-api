package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import com.google.common.math.Stats;
import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.AdressebeskyttelseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.FamilierelasjonDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.FoedselDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.FolkeregisterpersonstatusDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.SivilstandDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.StatsborgerskapDto;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static no.nav.common.utils.CollectionUtils.listOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PdlPersonMapperTest {

    private static final String IDENT = "ident";
    private static final String FORNAVN = "fornavn";
    private static final String MELLOMNAVN = "mellomnavn";
    private static final String ETTERNAVN = "etternavn";

    private static final String BARNIDENT = "barnIdent";
    private static final String BARN_ROLLE = "BARN";
    private static final String MOR_ROLLE = "MOR";

    public static final LocalDate FOEDSELSDATO_BARN = LocalDate.now().withMonth(1).withDayOfMonth(1).minusYears(2);

    private static final String EKTEFELLEIDENT = "ektefelleIdent";

    public static final String LAND = "NOR";

    private final PdlPersonMapper mapper = new PdlPersonMapper();

    @Test
    public void fulltUtfyltPdlPerson() {
        PdlPerson pdlPerson = createPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapTilPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getDiskresjonskode(), is(nullValue()));
        assertThat(person.getFornavn(), is(FORNAVN));
        assertThat(person.getMellomnavn(), is(MELLOMNAVN));
        assertThat(person.getEtternavn(), is(ETTERNAVN));
        assertThat(person.getFnr(), is(IDENT));
        assertThat(person.getSivilstatus(), is(SivilstandDto.SivilstandType.GIFT.name()));
        assertThat(person.getStatsborgerskap(), is(LAND));
    }

    @Test
    public void fulltUtfyltPdlEktefelle() {
        PdlPerson pdlPerson = createPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlPerson pdlEktefelle = createEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new FoedselDto(LocalDate.of(1970,1,1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, IDENT))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertNotNull(ektefelle);
        assertFalse(ektefelle.harIkketilgangtilektefelle());
        assertThat(ektefelle.getFornavn(), is(FORNAVN));
        assertThat(ektefelle.getMellomnavn(), is(MELLOMNAVN));
        assertThat(ektefelle.getEtternavn(), is(ETTERNAVN));
        assertThat(ektefelle.getFnr(), is(EKTEFELLEIDENT));
        assertThat(ektefelle.getFodselsdato().toString(), is("1970-01-01"));
        assertTrue(ektefelle.erFolkeregistrertsammen()); // TODO fix
    }

    @Test
    public void ektefelleMedAdressebeskyttelse() {
        PdlPerson pdlPerson = createPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlPerson pdlEktefelle = createEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.STRENGT_FORTROLIG)),
                listOf(new FoedselDto(LocalDate.of(1970,1,1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, IDENT))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertNotNull(ektefelle);
        assertTrue(ektefelle.harIkketilgangtilektefelle());
        assertNull(ektefelle.getFornavn());
        assertNull(ektefelle.getMellomnavn());
        assertNull(ektefelle.getEtternavn());
        assertNull(ektefelle.getFnr());
        assertNull(ektefelle.getFodselsdato());
        assertFalse(ektefelle.erFolkeregistrertsammen()); // TODO fix
    }

    @Test
    public void fulltUtfyltBarn() {
        PdlPerson pdlPerson = createPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlPerson pdlBarn = createBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new FamilierelasjonDto(IDENT, MOR_ROLLE, BARN_ROLLE)), //fjern?
                listOf(new FolkeregisterpersonstatusDto("ikke-doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNotNull(barn);
        assertThat(barn.getFornavn(), is(FORNAVN));
        assertThat(barn.getMellomnavn(), is(emptyString()));
        assertThat(barn.getEtternavn(), is(ETTERNAVN));
        assertThat(barn.getFnr(), is(BARNIDENT));
        assertThat(barn.getFodselsdato().toString(), is(new org.joda.time.LocalDate(FOEDSELSDATO_BARN.getYear(), FOEDSELSDATO_BARN.getMonthValue(), FOEDSELSDATO_BARN.getDayOfMonth()).toString()));
        assertTrue(barn.erFolkeregistrertsammen()); // TODO fix
    }

    @Test
    public void barnMedAdressebeskyttelse() {
        PdlPerson pdlPerson = createPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlPerson pdlBarn = createBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.FORTROLIG)),
                listOf(new FamilierelasjonDto(IDENT, MOR_ROLLE, BARN_ROLLE)), //fjern?
                listOf(new FolkeregisterpersonstatusDto("ikke-doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNull(barn);
    }

    @Test
    public void barnDoed() {
        PdlPerson pdlPerson = createPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlPerson pdlBarn = createBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.FORTROLIG)),
                listOf(new FamilierelasjonDto(IDENT, MOR_ROLLE, BARN_ROLLE)), //fjern?
                listOf(new FolkeregisterpersonstatusDto("doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNull(barn);
    }

    @Test
    public void barnMyndig() {
        PdlPerson pdlPerson = createPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlPerson pdlBarn = createBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.FORTROLIG)),
                listOf(new FamilierelasjonDto(IDENT, MOR_ROLLE, BARN_ROLLE)), //fjern?
                listOf(new FolkeregisterpersonstatusDto("ikke-doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN.minusYears(20))),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNull(barn);
    }

    private PdlPerson createPerson(List<AdressebeskyttelseDto> adressebeskyttelse, List<FamilierelasjonDto> familierelasjoner, List<NavnDto> navn, List<SivilstandDto> sivilstand, List<StatsborgerskapDto> statsborgerskap) {
        return new PdlPerson(
                adressebeskyttelse,
                familierelasjoner,
                Collections.emptyList(), // ingen folkeregisterstatus for Person
                Collections.emptyList(), // ingen foedselsdato for Person
                navn,
                sivilstand,
                statsborgerskap
        );
    }

    private PdlPerson createEktefelle(List<AdressebeskyttelseDto> adressebeskyttelse, List<FoedselDto> foedselsdato, List<NavnDto> navn, List<SivilstandDto> sivilstand) {
        return new PdlPerson(
                adressebeskyttelse,
                Collections.emptyList(), // ingen familierelasjoner for Ektefelle
                Collections.emptyList(), // ingen folkeregisterstatus for Ektefelle
                foedselsdato,
                navn,
                sivilstand, // fjern?
                Collections.emptyList()  // ingen statsborgerskap for Ektefelle
        );
    }

    private PdlPerson createBarn(List<AdressebeskyttelseDto> adressebeskyttelse, List<FamilierelasjonDto> familierelasjoner, List<FolkeregisterpersonstatusDto> folkeregisterpersonstatus, List<FoedselDto> foedselsdato, List<NavnDto> navn) {
        return new PdlPerson(
                adressebeskyttelse,
                familierelasjoner, // fjern?
                folkeregisterpersonstatus,
                foedselsdato,
                navn,
                Collections.emptyList(), // ingen sivilstand for Barn,
                Collections.emptyList()  // ingen statsborgerskap for Barn
        );
    }
}