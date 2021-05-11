package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import no.nav.sosialhjelp.soknad.consumer.kodeverk.KodeverkService;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.BostedsadresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.EndringDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.FoedselDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.FolkeregistermetadataDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.FolkeregisterpersonstatusDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.ForelderBarnRelasjonDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.KontaktadresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.MatrikkeladresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.MetadataDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.NavnDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.OppholdsadresseDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.SivilstandDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.StatsborgerskapDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.UkjentBostedDto;
import no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.VegadresseDto;
import no.nav.sosialhjelp.soknad.domain.model.Barn;
import no.nav.sosialhjelp.soknad.domain.model.Ektefelle;
import no.nav.sosialhjelp.soknad.domain.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPersonMapper.DOED;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto.Gradering.STRENGT_FORTROLIG;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto.Gradering.UGRADERT;
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

    private static final MetadataDto METADATA = new MetadataDto("FREG", singletonList(new EndringDto("FREG", LocalDateTime.now().minusDays(15), null)));
    private static final FolkeregistermetadataDto FOLKEREGISTERMETADATA = new FolkeregistermetadataDto(LocalDateTime.now().minusMonths(1), null);

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
                asList(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123", "123456"), null, null)),
                asList(new OppholdsadresseDto(null, "Test McTest", new VegadresseDto("111", "midlertidig", 1, "A", null, "1234", "1212", null, null), null, null)),
                asList(new KontaktadresseDto("Innland", null, new VegadresseDto("222", "kontaktveien", 1, "A", null, "2222", "3333", null, null), null, null)),
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapToPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getFornavn(), is(FORNAVN.toUpperCase()));
        assertThat(person.getMellomnavn(), is(MELLOMNAVN.toUpperCase()));
        assertThat(person.getEtternavn(), is(ETTERNAVN.toUpperCase()));
        assertThat(person.getFnr(), is(IDENT));
        assertThat(person.getSivilstatus(), is("gift"));
        assertThat(person.getStatsborgerskap(), hasSize(1));
        assertThat(person.getStatsborgerskap().get(0), is(LAND));
        assertThat(person.getBostedsadresse().getCoAdressenavn(), is(nullValue()));
        assertThat(person.getBostedsadresse().getVegadresse().getAdressenavn(), is("gateveien".toUpperCase()));
        assertThat(person.getBostedsadresse().getVegadresse().getPostnummer(), is("1234"));
        assertThat(person.getBostedsadresse().getVegadresse().getPoststed(), is("Mitt poststed".toUpperCase()));
        assertThat(person.getBostedsadresse().getMatrikkeladresse(), is(nullValue()));
        assertThat(person.getOppholdsadresse().getCoAdressenavn(), is("Test McTest"));
        assertThat(person.getOppholdsadresse().getVegadresse().getAdressenavn(), is("midlertidig".toUpperCase()));
        assertThat(person.getKontaktadresse().getCoAdressenavn(), is(nullValue()));
        assertThat(person.getKontaktadresse().getVegadresse().getAdressenavn(), is("kontaktveien".toUpperCase()));
    }

    @Test
    public void personNull() {
        Person person = mapper.mapToPerson(null, IDENT);

        assertNull(person);
    }

    @Test
    public void personMedMatrikkeladresseBostedsadresse() {
        PdlPerson pdlPerson = new PdlPerson(
                asList(new BostedsadresseDto(null, null, new MatrikkeladresseDto("matrikkelid", "1111", null, "1111", null), null)),
                null, // ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapToPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getBostedsadresse().getVegadresse(), is(nullValue()));
        assertThat(person.getBostedsadresse().getMatrikkeladresse(), is(notNullValue()));
        assertThat(person.getBostedsadresse().getMatrikkeladresse().getMatrikkelId(), is("matrikkelid"));
    }

    @Test
    public void personMedUkjentBosted() {
        PdlPerson pdlPerson = new PdlPerson(
                asList(new BostedsadresseDto(null, null, null, new UkjentBostedDto("Oslo"))),
                null, // ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapToPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getBostedsadresse(), is(nullValue()));
    }

    @Test
    public void personMedOppholdsadresseUtenVegadresse() {
        PdlPerson pdlPerson = new PdlPerson(
                asList(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123", "123456"), null, null)),
                asList(new OppholdsadresseDto("oppholdAnnetSted", null, null, null, null)),
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapToPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getOppholdsadresse(), is(nullValue()));
    }

    @Test
    public void personMedOppholdsadresseLikBostedsadresseSkalFiltreresVekk() {
        var vegadresse = defaultVegadresse();
        var annenVegadresse = annenVegadresse();

        PdlPerson pdlPerson = new PdlPerson(
                asList(new BostedsadresseDto(null, vegadresse, null, null)),
                asList(
                        new OppholdsadresseDto(null, null, vegadresse, null, null),
                        new OppholdsadresseDto(null, null, annenVegadresse, null, null)
                ),
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapToPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getBostedsadresse().getCoAdressenavn(), is(nullValue()));
        assertThat(person.getBostedsadresse().getVegadresse().getAdressenavn(), is(vegadresse.getAdressenavn().toUpperCase()));
        assertThat(person.getBostedsadresse().getMatrikkeladresse(), is(nullValue()));
        assertThat(person.getOppholdsadresse().getCoAdressenavn(), is(nullValue()));
        assertThat(person.getOppholdsadresse().getVegadresse().getAdressenavn(), is(annenVegadresse.getAdressenavn().toUpperCase()));
    }

    @Test
    public void personMedKontaktadresseLikBostedsadresseSkalFiltreresVekk() {
        var vegadresse = defaultVegadresse();
        var annenVegadresse = annenVegadresse();

        PdlPerson pdlPerson = new PdlPerson(
                asList(new BostedsadresseDto(null, vegadresse, null, null)),
                null, // ingen oppholdsadresse
                asList(
                        new KontaktadresseDto("Innland", null, vegadresse, null, null),
                        new KontaktadresseDto("Innland", null, annenVegadresse, null, null)
                ),
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapToPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getBostedsadresse().getCoAdressenavn(), is(nullValue()));
        assertThat(person.getBostedsadresse().getVegadresse().getAdressenavn(), is(vegadresse.getAdressenavn().toUpperCase()));
        assertThat(person.getBostedsadresse().getMatrikkeladresse(), is(nullValue()));
        assertThat(person.getKontaktadresse().getCoAdressenavn(), is(nullValue()));
        assertThat(person.getKontaktadresse().getVegadresse().getAdressenavn(), is(annenVegadresse.getAdressenavn().toUpperCase()));
    }

    @Test
    public void personMedKontaktadresseUtenKommunenummerLikBostedsadresseSkalFiltreresVekk() {
        var vegadresse = new VegadresseDto("matrikkelId", "gateveien", 1, "A", null, "1234", "1212", null, null);
        var vegadresseUtenKommunenummer = new VegadresseDto("matrikkelId", "gateveien", 1, "A", null, "1234", null, null, null);

        PdlPerson pdlPerson = new PdlPerson(
                asList(new BostedsadresseDto(null, vegadresse, null, null)),
                null, // ingen oppholdsadresse
                asList(new KontaktadresseDto("Innland", null, vegadresseUtenKommunenummer, null, null)),
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        Person person = mapper.mapToPerson(pdlPerson, IDENT);

        assertNotNull(person);
        assertThat(person.getBostedsadresse().getCoAdressenavn(), is(nullValue()));
        assertThat(person.getBostedsadresse().getVegadresse().getAdressenavn(), is(vegadresse.getAdressenavn().toUpperCase()));
        assertThat(person.getBostedsadresse().getMatrikkeladresse(), is(nullValue()));
        assertThat(person.getKontaktadresse(), is(nullValue()));
    }

    @Test
    public void fulltUtfyltEktefelle() {
        PdlPerson pdlPerson = new PdlPerson(
                asList(new BostedsadresseDto(null, defaultVegadresse(), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                asList(new BostedsadresseDto(null, defaultVegadresse(), null, null)),
                asList(new FoedselDto(LocalDate.of(1970, 1, 1))),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Ektefelle ektefelle = mapper.mapToEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

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
                asList(new BostedsadresseDto(null, new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123", "123456"), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                asList(new BostedsadresseDto(null, new VegadresseDto("2matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123", "123456"), null, null)), // kun matrikkelId er ulik
                asList(new FoedselDto(LocalDate.of(1970, 1, 1))),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Ektefelle ektefelle = mapper.mapToEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertNotNull(ektefelle);
        assertFalse(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleOgPersonErFolkeregistrertSammenUtenMatrikkelId() {
        PdlPerson pdlPerson = new PdlPerson(
                asList(new BostedsadresseDto(null, new VegadresseDto(null, "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123", "123456"), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                asList(new BostedsadresseDto(null, new VegadresseDto(null, "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123", "123456"), null, null)),
                asList(new FoedselDto(LocalDate.of(1970, 1, 1))),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Ektefelle ektefelle = mapper.mapToEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertNotNull(ektefelle);
        assertTrue(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleMedAdressebeskyttelse() {
        PdlPerson pdlPerson = new PdlPerson(
                asList(new BostedsadresseDto(null, defaultVegadresse(), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                asList(new AdressebeskyttelseDto(STRENGT_FORTROLIG)),
                asList(new BostedsadresseDto(null, null, null, null)),
                asList(new FoedselDto(LocalDate.of(1970, 1, 1))),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Ektefelle ektefelle = mapper.mapToEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

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
                asList(new BostedsadresseDto(null, defaultVegadresse(), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        Ektefelle ektefelle = mapper.mapToEktefelle(null, EKTEFELLEIDENT, pdlPerson);

        assertNull(ektefelle);
    }

    @Test
    public void ektefelleOgPersonNullAdresse() {
        PdlPerson pdlPerson = new PdlPerson(
                null, // Ingen bostedsadresse
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                null, // Ingen bostedsadresse
                asList(new FoedselDto(LocalDate.of(1970, 1, 1))),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Ektefelle ektefelle = mapper.mapToEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertFalse(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleOgPersonTomAdresse() {
        PdlPerson pdlPerson = new PdlPerson(
                emptyList(),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                emptyList(),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                asList(new FoedselDto(LocalDate.of(1970, 1, 1))),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Ektefelle ektefelle = mapper.mapToEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertFalse(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void ektefelleOgPersonMatrikkelAdresse() {
        PdlPerson pdlPerson = new PdlPerson(
                asList(new BostedsadresseDto(null, null, new MatrikkeladresseDto("matrikkelId", "postnr", "tillegg", "kommunenr", "bruksenhetsnr"), null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                emptyList(),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        PdlEktefelle pdlEktefelle = new PdlEktefelle(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                asList(new BostedsadresseDto(null, null, new MatrikkeladresseDto("matrikkelId", "postnr", "tillegg", "kommunenr", "bruksenhetsnr"), null)),
                asList(new FoedselDto(LocalDate.of(1970, 1, 1))),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Ektefelle ektefelle = mapper.mapToEktefelle(pdlEktefelle, EKTEFELLEIDENT, pdlPerson);

        assertTrue(ektefelle.erFolkeregistrertsammen());
    }

    @Test
    public void fulltUtfyltBarn() {
        PdlPerson pdlPerson = new PdlPerson(
                asList(new BostedsadresseDto(null, defaultVegadresse(), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                asList(new BostedsadresseDto(null, defaultVegadresse(), null, null)),
                asList(new FolkeregisterpersonstatusDto("ikke-doed")),
                asList(new FoedselDto(FOEDSELSDATO_BARN)),
                asList(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Barn barn = mapper.mapToBarn(pdlBarn, BARNIDENT, pdlPerson);

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
                asList(new BostedsadresseDto(null, defaultVegadresse(), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.FORTROLIG)),
                asList(new BostedsadresseDto(null, null, null, null)),
                asList(new FolkeregisterpersonstatusDto("ikke-doed")),
                asList(new FoedselDto(FOEDSELSDATO_BARN)),
                asList(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Barn barn = mapper.mapToBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNull(barn);
    }

    @Test
    public void barnDoed() {
        PdlPerson pdlPerson = new PdlPerson(
                asList(new BostedsadresseDto(null, defaultVegadresse(), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                asList(new BostedsadresseDto(null, defaultVegadresse(), null, null)),
                asList(new FolkeregisterpersonstatusDto(DOED)),
                asList(new FoedselDto(FOEDSELSDATO_BARN)),
                asList(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Barn barn = mapper.mapToBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNull(barn);
    }

    @Test
    public void barnMyndig() {
        PdlPerson pdlPerson = new PdlPerson(
                asList(new BostedsadresseDto(null, defaultVegadresse(), null, null)),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                asList(new BostedsadresseDto(null, defaultVegadresse(), null, null)),
                asList(new FolkeregisterpersonstatusDto("ikke-doed")),
                asList(new FoedselDto(FOEDSELSDATO_BARN_MYNDIG)),
                asList(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Barn barn = mapper.mapToBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertNull(barn);
    }

    @Test
    public void barnOgPersonNullAdresse() {
        PdlPerson pdlPerson = new PdlPerson(
                null, // Ingen bostedsadresse
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                null,
                asList(new FolkeregisterpersonstatusDto("ikke-doed")),
                asList(new FoedselDto(FOEDSELSDATO_BARN)),
                asList(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Barn barn = mapper.mapToBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertFalse(barn.erFolkeregistrertsammen());
    }

    @Test
    public void barnOgPersonTomAdresseliste() {
        PdlPerson pdlPerson = new PdlPerson(
                emptyList(),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        PdlBarn pdlBarn = new PdlBarn(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                asList(new FolkeregisterpersonstatusDto("ikke-doed")),
                asList(new FoedselDto(FOEDSELSDATO_BARN)),
                asList(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        Barn barn = mapper.mapToBarn(pdlBarn, BARNIDENT, pdlPerson);

        assertFalse(barn.erFolkeregistrertsammen());
    }

    @Test
    public void assertMyndighetKorrekthetVedBursdag() {
        var dayBeforeBirthday = LocalDate.now().minusYears(18).plusDays(1);
        var birthday = LocalDate.now().minusYears(18);
        var dayAfterBirthday = LocalDate.now().minusYears(18).minusDays(1);

        var pdlPerson = new PdlPerson(
                emptyList(),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        var pdlBarnDayBeforeBirthday = new PdlBarn(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                asList(new FolkeregisterpersonstatusDto("ikke-doed")),
                asList(new FoedselDto(dayBeforeBirthday)),
                asList(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        var pdlBarnBirthday = new PdlBarn(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                asList(new FolkeregisterpersonstatusDto("ikke-doed")),
                asList(new FoedselDto(birthday)),
                asList(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        var pdlBarnDayAfterBirthday = new PdlBarn(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                asList(new FolkeregisterpersonstatusDto("ikke-doed")),
                asList(new FoedselDto(dayAfterBirthday)),
                asList(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );

        var barnDayBeforeBirthday = mapper.mapToBarn(pdlBarnDayBeforeBirthday, BARNIDENT, pdlPerson);
        var barnBirthday = mapper.mapToBarn(pdlBarnBirthday, BARNIDENT, pdlPerson);
        var barnDayAfterBirthday = mapper.mapToBarn(pdlBarnDayAfterBirthday, BARNIDENT, pdlPerson);

        assertNotNull(barnDayBeforeBirthday);
        assertNull(barnBirthday);
        assertNull(barnDayAfterBirthday);
    }

    @Test
    public void adressebeskyttelseStrengtFortrolig() {
        var pdlAdressebeskyttelse = new PdlAdressebeskyttelse(
                asList(new AdressebeskyttelseDto(STRENGT_FORTROLIG))
        );

        var gradering = mapper.mapToAdressebeskyttelse(pdlAdressebeskyttelse);

        assertNotNull(gradering);
        assertThat(gradering, is(STRENGT_FORTROLIG));
    }

    @Test
    public void adressebeskyttelseStrengtUgradert() {
        var pdlAdressebeskyttelse = new PdlAdressebeskyttelse(
                asList(new AdressebeskyttelseDto(UGRADERT))
        );

        var gradering = mapper.mapToAdressebeskyttelse(pdlAdressebeskyttelse);

        assertNotNull(gradering);
        assertThat(gradering, is(UGRADERT));
    }

    @Test
    public void adressebeskyttelseNull() {
        var pdlAdressebeskyttelse = new PdlAdressebeskyttelse(
                asList(new AdressebeskyttelseDto(null))
        );

        var gradering = mapper.mapToAdressebeskyttelse(pdlAdressebeskyttelse);

        assertNull(gradering);
    }

    private VegadresseDto defaultVegadresse() {
        return new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123", "123456");
    }

    private VegadresseDto annenVegadresse() {
        return new VegadresseDto("matrikkelId2", "stien", 2, "B", null, "1234", "1212", null, null);
    }
}