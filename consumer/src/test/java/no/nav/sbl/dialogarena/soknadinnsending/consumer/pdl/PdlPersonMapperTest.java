package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.barn.PdlBarn;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.AdressebeskyttelseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.BostedsadresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FamilierelasjonDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FoedselDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FolkeregisterpersonstatusDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.MatrikkeladresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.StatsborgerskapDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.VegadresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.ektefelle.PdlEktefelle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;
import org.junit.Test;

import java.time.LocalDate;

import static java.util.Collections.emptyList;
import static no.nav.common.utils.CollectionUtils.listOf;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlPersonMapper.KODE_6;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlPersonMapper.KODE_7;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
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

    private static final LocalDate FOEDSELSDATO_BARN = LocalDate.now().withMonth(1).withDayOfMonth(1).minusYears(2);
    private static final LocalDate FOEDSELSDATO_BARN_MYNDIG = LocalDate.now().withMonth(1).withDayOfMonth(1).minusYears(19);

    private static final String EKTEFELLEIDENT = "ektefelleIdent";

    private static final String LAND = "NOR";

    private final PdlPersonMapper mapper = new PdlPersonMapper();

    @Test
    public void fulltUtfyltPerson() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
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
        assertThat(person.getSivilstatus(), is("gift"));
        assertThat(person.getStatsborgerskap(), is(LAND));
    }

    @Test
    public void personNull() {
        Person person = mapper.mapTilPerson(null, IDENT);

        assertNull(person);
    }

    @Test
    public void personMedAdressebeskyttelse() {
        PdlPerson nullAdressebeskyttelse = new PdlPerson(null, emptyList(), emptyList(), emptyList(), emptyList(), emptyList());
        PdlPerson tomAdressebeskyttelse = new PdlPerson(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList());
        PdlPerson ugradert = new PdlPerson(listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)), emptyList(), emptyList(), emptyList(), emptyList(), emptyList());
        PdlPerson kode6 = new PdlPerson(listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.STRENGT_FORTROLIG)), emptyList(), emptyList(), emptyList(), emptyList(), emptyList());
        PdlPerson kode7 = new PdlPerson(listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.FORTROLIG)), emptyList(), emptyList(), emptyList(), emptyList(), emptyList());
        PdlPerson listeMedUgradertOgkode7 = new PdlPerson(listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT), new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.FORTROLIG)), emptyList(), emptyList(), emptyList(), emptyList(), emptyList());

        Person nullAdressebeskyttelsePerson = mapper.mapTilPerson(nullAdressebeskyttelse, IDENT);
        Person tomAdressebeskyttelsePerson = mapper.mapTilPerson(tomAdressebeskyttelse, IDENT);
        Person ugradertPerson = mapper.mapTilPerson(ugradert, IDENT);
        Person kode6Person = mapper.mapTilPerson(kode6, IDENT);
        Person kode7Person = mapper.mapTilPerson(kode7, IDENT);
        Person listeMedUgradertOgkode7Person = mapper.mapTilPerson(listeMedUgradertOgkode7, IDENT);

        assertNull(nullAdressebeskyttelsePerson.getDiskresjonskode());
        assertNull(tomAdressebeskyttelsePerson.getDiskresjonskode());
        assertNull(ugradertPerson.getDiskresjonskode());
        assertThat(kode6Person.getDiskresjonskode(), is(KODE_6));
        assertThat(kode7Person.getDiskresjonskode(), is(KODE_7));
        assertThat(listeMedUgradertOgkode7Person.getDiskresjonskode(), is(KODE_7));
    }

    @Test
    public void fulltUtfyltEktefelle() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FoedselDto(LocalDate.of(1970, 1, 1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertNotNull(ektefelle);
        assertFalse(ektefelle.harIkketilgangtilektefelle());
        assertThat(ektefelle.getFornavn(), is(FORNAVN));
        assertThat(ektefelle.getMellomnavn(), is(MELLOMNAVN));
        assertThat(ektefelle.getEtternavn(), is(ETTERNAVN));
        assertThat(ektefelle.getFnr(), is(EKTEFELLEIDENT));
        assertThat(ektefelle.getFodselsdato().toString(), is("1970-01-01"));
        assertTrue(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleMedAnnenBostedsadresse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("annen_", "gårdsplassen", 42, "B", null, "9999", "8888", null), null, null)),
                listOf(new FoedselDto(LocalDate.of(1970, 1, 1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertNotNull(ektefelle);
        assertFalse(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleMedAdressebeskyttelse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.STRENGT_FORTROLIG)),
                listOf(new BostedsadresseDto(null, null, null)),
                listOf(new FoedselDto(LocalDate.of(1970, 1, 1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertNotNull(ektefelle);
        assertTrue(ektefelle.harIkketilgangtilektefelle());
        assertNull(ektefelle.getFornavn());
        assertNull(ektefelle.getMellomnavn());
        assertNull(ektefelle.getEtternavn());
        assertNull(ektefelle.getFnr());
        assertNull(ektefelle.getFodselsdato());
        assertFalse(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleNull() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(null, EKTEFELLEIDENT, pdlPerson);

        assertNull(ektefelle);
    }

    @Test
    public void ektefelleOgPersonNullAdresse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                null,
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                null,
                listOf(new FoedselDto(LocalDate.of(1970, 1, 1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertFalse(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleOgPersonTomAdresse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                emptyList(),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                listOf(new FoedselDto(LocalDate.of(1970, 1, 1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertFalse(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleOgPersonMatrikkelAdresse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new MatrikkeladresseDto("matrikkelId", "postnr", "tillegg", "kommunenr", "bruksenhetsnr"), null)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                emptyList(),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new MatrikkeladresseDto("matrikkelId", "postnr", "tillegg", "kommunenr", "bruksenhetsnr"), null)),
                listOf(new FoedselDto(LocalDate.of(1970, 1, 1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertTrue(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void fulltUtfyltBarn() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
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
        assertTrue(barn.erFolkeregistrertsammen());
    }

    @Test
    public void barnMedAdressebeskyttelse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.FORTROLIG)),
                listOf(new BostedsadresseDto(null, null, null)),
                listOf(new FolkeregisterpersonstatusDto("ikke-doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNull(barn);
    }

    @Test
    public void barnDoed() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FolkeregisterpersonstatusDto("doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNull(barn);
    }

    @Test
    public void barnMyndig() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FolkeregisterpersonstatusDto("ikke-doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN_MYNDIG)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNull(barn);
    }

    @Test
    public void barnOgPersonNullAdresse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                null,
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                null,
                listOf(new FolkeregisterpersonstatusDto("ikke-doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertFalse(barn.erFolkeregistrertsammen());
    }

    @Test
    public void barnOgPersonTomAdresseliste() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                listOf(new FolkeregisterpersonstatusDto("ikke-doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertFalse(barn.erFolkeregistrertsammen());
    }

}