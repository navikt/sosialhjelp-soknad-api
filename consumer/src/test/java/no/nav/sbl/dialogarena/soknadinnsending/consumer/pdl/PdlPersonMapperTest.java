package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl;

import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Barn;
import no.nav.sbl.dialogarena.sendsoknad.domain.Ektefelle;
import no.nav.sbl.dialogarena.sendsoknad.domain.Person;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.kodeverk.KodeverkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.barn.PdlBarn;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.AdressebeskyttelseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.BostedsadresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.EndringDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FamilierelasjonDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FoedselDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FolkeregistermetadataDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.FolkeregisterpersonstatusDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.KontaktadresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.MatrikkeladresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.MetadataDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.NavnDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.OppholdsadresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.SivilstandDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.StatsborgerskapDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.UkjentBostedDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.common.VegadresseDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.ektefelle.PdlEktefelle;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person.PdlPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.common.utils.CollectionUtils.listOf;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlPersonMapper.DOED;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlPersonMapper.KODE_6;
import static no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.PdlPersonMapper.KODE_7;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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

    private static final MetadataDto METADATA = new MetadataDto("FREG", singletonList(new EndringDto("FREG", LocalDateTime.now().minusDays(15), null, null, null)));
    private static final FolkeregistermetadataDto FOLKEREGISTERMETADATA = new FolkeregistermetadataDto(LocalDateTime.now().minusMonths(1), null, null, null);

    @Mock
    private KodeverkService kodeverkService;

    @InjectMocks
    private PdlPersonMapper mapper;

    @Before
    public void setUp() {
        when(kodeverkService.getPoststed(anyString())).thenReturn("Mitt poststed");
    }

    @Test
    public void fulltUtfyltPerson() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new OppholdsadresseDto(null, "Test McTest", new VegadresseDto("111", "midlertidig", 1, "A", null, "1234", "1212", null), null, null)),
                listOf(new KontaktadresseDto(null, new VegadresseDto("222", "kontaktveien", 1, "A", null, "2222", "3333", null), null, null)),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapTilPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getDiskresjonskode(), is(nullValue()));
        assertThat(person.getFornavn(), is(FORNAVN.toUpperCase()));
        assertThat(person.getMellomnavn(), is(MELLOMNAVN.toUpperCase()));
        assertThat(person.getEtternavn(), is(ETTERNAVN.toUpperCase()));
        assertThat(person.getFnr(), is(IDENT));
        assertThat(person.getSivilstatus(), is("gift"));
        assertThat(person.getStatsborgerskap(), hasSize(1));
        assertThat(person.getStatsborgerskap().get(0), is(LAND));
        assertThat(person.getBostedsadresse().getCoAdressenavn(), is(nullValue()));
        assertThat(person.getBostedsadresse().getVegadresse().getAdressenavn(), is("gateveien"));
        assertThat(person.getBostedsadresse().getVegadresse().getPostnummer(), is("1234"));
        assertThat(person.getBostedsadresse().getVegadresse().getPoststed(), is("Mitt poststed"));
        assertThat(person.getBostedsadresse().getMatrikkeladresse(), is(nullValue()));
        assertThat(person.getOppholdsadresse().getCoAdressenavn(), is("Test McTest"));
        assertThat(person.getOppholdsadresse().getVegadresse().getAdressenavn(), is("midlertidig"));
        assertThat(person.getKontaktadresse().getCoAdressenavn(), is(nullValue()));
        assertThat(person.getKontaktadresse().getVegadresse().getAdressenavn(), is("kontaktveien"));
    }

    @Test
    public void personNull() {
        Person person = mapper.mapTilPerson(null, IDENT);

        assertNull(person);
    }

    @Test
    public void personMedAdressebeskyttelse() {
        PdlPerson nullAdressebeskyttelse = createPdlPersonMedAdressebeskyttelse(null);
        PdlPerson tomAdressebeskyttelse = createPdlPersonMedAdressebeskyttelse(emptyList());
        PdlPerson ugradert = createPdlPersonMedAdressebeskyttelse(listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)));
        PdlPerson kode6 = createPdlPersonMedAdressebeskyttelse(listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.STRENGT_FORTROLIG)));
        PdlPerson kode7 = createPdlPersonMedAdressebeskyttelse(listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.FORTROLIG)));
        PdlPerson listeMedUgradertOgkode7 = createPdlPersonMedAdressebeskyttelse(listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT), new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.FORTROLIG)));

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

    private PdlPerson createPdlPersonMedAdressebeskyttelse(List<AdressebeskyttelseDto> adressebeskyttelse) {
        return new PdlPerson(adressebeskyttelse, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList());
    }

    @Test
    public void personMedMatrikkeladresseBostedsadresse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, null, new MatrikkeladresseDto("matrikkelid", "1111", null, "1111", null), null)),
                null, // ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapTilPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getBostedsadresse().getVegadresse(), is(nullValue()));
        assertThat(person.getBostedsadresse().getMatrikkeladresse(), is(notNullValue()));
        assertThat(person.getBostedsadresse().getMatrikkeladresse().getMatrikkelId(), is("matrikkelid"));
    }

    @Test
    public void personMedUkjentBosted() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, null, null, new UkjentBostedDto("Oslo"))),
                null, // ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapTilPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getBostedsadresse(), is(nullValue()));
    }

    @Test
    public void personMedOppholdsadresseUtenVegadresse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new OppholdsadresseDto("oppholdAnnetSted", null, null, null, null)),
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapTilPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getOppholdsadresse(), is(nullValue()));
    }

    @Test
    public void personMedOppholdsadresseLikBostedsadresseSkalFiltreresVekk() {
        var vegadresse = new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123");
        var annenVegadresse = new VegadresseDto("matrikkelId2", "stien", 2, "B", null, "1234", "1212", null);

        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, vegadresse, null, null)),
                listOf(
                        new OppholdsadresseDto(null, null, vegadresse, null, null),
                        new OppholdsadresseDto(null, null, annenVegadresse, null, null)
                ),
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapTilPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getBostedsadresse().getCoAdressenavn(), is(nullValue()));
        assertThat(person.getBostedsadresse().getVegadresse().getAdressenavn(), is(vegadresse.getAdressenavn()));
        assertThat(person.getBostedsadresse().getMatrikkeladresse(), is(nullValue()));
        assertThat(person.getOppholdsadresse().getCoAdressenavn(), is(nullValue()));
        assertThat(person.getOppholdsadresse().getVegadresse().getAdressenavn(), is(annenVegadresse.getAdressenavn()));
    }

    @Test
    public void personMedKontaktadresseLikBostedsadresseSkalFiltreresVekk() {
        var vegadresse = new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123");
        var annenVegadresse = new VegadresseDto("matrikkelId2", "stien", 2, "B", null, "1234", "1212", null);

        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, vegadresse, null, null)),
                null, // ingen oppholdsadresse
                listOf(
                        new KontaktadresseDto(null, vegadresse, null, null),
                        new KontaktadresseDto(null, annenVegadresse, null, null)
                ),
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapTilPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getBostedsadresse().getCoAdressenavn(), is(nullValue()));
        assertThat(person.getBostedsadresse().getVegadresse().getAdressenavn(), is(vegadresse.getAdressenavn()));
        assertThat(person.getBostedsadresse().getMatrikkeladresse(), is(nullValue()));
        assertThat(person.getKontaktadresse().getCoAdressenavn(), is(nullValue()));
        assertThat(person.getKontaktadresse().getVegadresse().getAdressenavn(), is(annenVegadresse.getAdressenavn()));
    }

    @Test
    public void fulltUtfyltEktefelle() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FoedselDto(LocalDate.of(1970, 1, 1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertNotNull(ektefelle);
        assertFalse(ektefelle.harIkketilgangtilektefelle());
        assertThat(ektefelle.getFornavn(), is(FORNAVN.toUpperCase()));
        assertThat(ektefelle.getMellomnavn(), is(MELLOMNAVN.toUpperCase()));
        assertThat(ektefelle.getEtternavn(), is(ETTERNAVN.toUpperCase()));
        assertThat(ektefelle.getFnr(), is(EKTEFELLEIDENT));
        assertThat(ektefelle.getFodselsdato().toString(), is("1970-01-01"));
        assertTrue(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleOgPersonErIkkeFolkeregistrertSammenMedUlikMatrikkelId() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("2matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)), // kun matrikkelId er ulik
                listOf(new FoedselDto(LocalDate.of(1970, 1, 1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertNotNull(ektefelle);
        assertFalse(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleOgPersonErFolkeregistrertSammenUtenMatrikkelId() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto(null, "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto(null, "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FoedselDto(LocalDate.of(1970, 1, 1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertNotNull(ektefelle);
        assertTrue(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleMedAdressebeskyttelse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.STRENGT_FORTROLIG)),
                listOf(new BostedsadresseDto(null, null, null, null)),
                listOf(new FoedselDto(LocalDate.of(1970, 1, 1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
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
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(null, EKTEFELLEIDENT, pdlPerson);

        assertNull(ektefelle);
    }

    @Test
    public void ektefelleOgPersonNullAdresse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                null, // Ingen bostedsadresse
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                null, // Ingen bostedsadresse
                listOf(new FoedselDto(LocalDate.of(1970, 1, 1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertFalse(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleOgPersonTomAdresse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                emptyList(),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                listOf(new FoedselDto(LocalDate.of(1970, 1, 1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertFalse(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleOgPersonMatrikkelAdresse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, null, new MatrikkeladresseDto("matrikkelId", "postnr", "tillegg", "kommunenr", "bruksenhetsnr"), null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                emptyList(),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, null, new MatrikkeladresseDto("matrikkelId", "postnr", "tillegg", "kommunenr", "bruksenhetsnr"), null)),
                listOf(new FoedselDto(LocalDate.of(1970, 1, 1))),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Ektefelle ektefelle = mapper.mapTilEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertTrue(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void fulltUtfyltBarn() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FolkeregisterpersonstatusDto("ikke-doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNotNull(barn);
        assertThat(barn.getFornavn(), is(FORNAVN.toUpperCase()));
        assertThat(barn.getMellomnavn(), is(emptyString()));
        assertThat(barn.getEtternavn(), is(ETTERNAVN.toUpperCase()));
        assertThat(barn.getFnr(), is(BARNIDENT));
        assertThat(barn.getFodselsdato().toString(), is(new org.joda.time.LocalDate(FOEDSELSDATO_BARN.getYear(), FOEDSELSDATO_BARN.getMonthValue(), FOEDSELSDATO_BARN.getDayOfMonth()).toString()));
        assertTrue(barn.erFolkeregistrertsammen());
    }

    @Test
    public void barnMedAdressebeskyttelse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.FORTROLIG)),
                listOf(new BostedsadresseDto(null, null, null, null)),
                listOf(new FolkeregisterpersonstatusDto("ikke-doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNull(barn);
    }

    @Test
    public void barnDoed() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FolkeregisterpersonstatusDto(DOED)),
                listOf(new FoedselDto(FOEDSELSDATO_BARN)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNull(barn);
    }

    @Test
    public void barnMyndig() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                listOf(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123"), null, null)),
                listOf(new FolkeregisterpersonstatusDto("ikke-doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN_MYNDIG)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNull(barn);
    }

    @Test
    public void barnOgPersonNullAdresse() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                null, // Ingen bostedsadresse
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                null,
                listOf(new FolkeregisterpersonstatusDto("ikke-doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertFalse(barn.erFolkeregistrertsammen());
    }

    @Test
    public void barnOgPersonTomAdresseliste() {
        PdlPerson pdlPerson = new PdlPerson(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                listOf(new FamilierelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                listOf(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                listOf(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                listOf(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                listOf(new FolkeregisterpersonstatusDto("ikke-doed")),
                listOf(new FoedselDto(FOEDSELSDATO_BARN)),
                listOf(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Barn barn = mapper.mapTilBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertFalse(barn.erFolkeregistrertsammen());
    }

}