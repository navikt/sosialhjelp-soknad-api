package no.nav.sosialhjelp.soknad.consumer.pdl.person;

import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService;
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
import no.nav.sosialhjelp.soknad.person.domain.Ektefelle;
import no.nav.sosialhjelp.soknad.person.domain.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlPersonMapper.DOED;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto.Gradering.STRENGT_FORTROLIG;
import static no.nav.sosialhjelp.soknad.consumer.pdl.person.dto.AdressebeskyttelseDto.Gradering.UGRADERT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
class PdlPersonMapperTest {

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

    @BeforeEach
    public void setUp() {
        when(kodeverkService.getPoststed(anyString())).thenReturn("Mitt poststed");
    }

    @Test
    void fulltUtfyltPerson() {
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

        assertThat(person).isNotNull();
        assertThat(person.getFornavn()).isEqualTo(FORNAVN.toUpperCase());
        assertThat(person.getMellomnavn()).isEqualTo(MELLOMNAVN.toUpperCase());
        assertThat(person.getEtternavn()).isEqualTo(ETTERNAVN.toUpperCase());
        assertThat(person.getFnr()).isEqualTo(IDENT);
        assertThat(person.getSivilstatus()).isEqualTo("gift");
        assertThat(person.getStatsborgerskap()).hasSize(1);
        assertThat(person.getStatsborgerskap().get(0)).isEqualTo(LAND);
        assertThat(person.getBostedsadresse().getCoAdressenavn()).isNull();
        assertThat(person.getBostedsadresse().getVegadresse().getAdressenavn()).isEqualTo("gateveien".toUpperCase());
        assertThat(person.getBostedsadresse().getVegadresse().getPostnummer()).isEqualTo("1234");
        assertThat(person.getBostedsadresse().getVegadresse().getPoststed()).isEqualTo("Mitt poststed".toUpperCase());
        assertThat(person.getBostedsadresse().getMatrikkeladresse()).isNull();
        assertThat(person.getOppholdsadresse().getCoAdressenavn()).isEqualTo("Test McTest");
        assertThat(person.getOppholdsadresse().getVegadresse().getAdressenavn()).isEqualTo("midlertidig".toUpperCase());
        assertThat(person.getKontaktadresse().getCoAdressenavn()).isNull();
        assertThat(person.getKontaktadresse().getVegadresse().getAdressenavn()).isEqualTo("kontaktveien".toUpperCase());
    }

    @Test
    void personNull() {
        Person person = mapper.mapToPerson(null, IDENT);

        assertThat(person).isNull();
    }

    @Test
    void personMedMatrikkeladresseBostedsadresse() {
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

        assertThat(person).isNotNull();
        assertThat(person.getBostedsadresse().getVegadresse()).isNull();
        assertThat(person.getBostedsadresse().getMatrikkeladresse()).isNotNull();
        assertThat(person.getBostedsadresse().getMatrikkeladresse().getMatrikkelId()).isEqualTo("matrikkelid");
    }

    @Test
    void personMedUkjentBosted() {
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

        assertThat(person).isNotNull();
        assertThat(person.getBostedsadresse()).isNull();
    }

    @Test
    void personMedOppholdsadresseUtenVegadresse() {
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

        assertThat(person).isNotNull();
        assertThat(person.getOppholdsadresse()).isNull();
    }

    @Test
    void personMedOppholdsadresseLikBostedsadresseSkalFiltreresVekk() {
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

        assertThat(person).isNotNull();
        assertThat(person.getBostedsadresse().getCoAdressenavn()).isNull();
        assertThat(person.getBostedsadresse().getVegadresse().getAdressenavn()).isEqualTo(vegadresse.getAdressenavn().toUpperCase());
        assertThat(person.getBostedsadresse().getMatrikkeladresse()).isNull();
        assertThat(person.getOppholdsadresse().getCoAdressenavn()).isNull();
        assertThat(person.getOppholdsadresse().getVegadresse().getAdressenavn()).isEqualTo(annenVegadresse.getAdressenavn().toUpperCase());
    }

    @Test
    void personMedKontaktadresseLikBostedsadresseSkalFiltreresVekk() {
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

        assertThat(person).isNotNull();
        assertThat(person.getBostedsadresse().getCoAdressenavn()).isNull();
        assertThat(person.getBostedsadresse().getVegadresse().getAdressenavn()).isEqualTo(vegadresse.getAdressenavn().toUpperCase());
        assertThat(person.getBostedsadresse().getMatrikkeladresse()).isNull();
        assertThat(person.getKontaktadresse().getCoAdressenavn()).isNull();
        assertThat(person.getKontaktadresse().getVegadresse().getAdressenavn()).isEqualTo(annenVegadresse.getAdressenavn().toUpperCase());
    }

    @Test
    void personMedKontaktadresseUtenKommunenummerLikBostedsadresseSkalFiltreresVekk() {
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

        assertThat(person).isNotNull();
        assertThat(person.getBostedsadresse().getCoAdressenavn()).isNull();
        assertThat(person.getBostedsadresse().getVegadresse().getAdressenavn()).isEqualTo(vegadresse.getAdressenavn().toUpperCase());
        assertThat(person.getBostedsadresse().getMatrikkeladresse()).isNull();
        assertThat(person.getKontaktadresse()).isNull();
    }

    @Test
    void fulltUtfyltEktefelle() {
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

        assertThat(ektefelle).isNotNull();
        assertThat(ektefelle.getIkkeTilgangTilEktefelle()).isFalse();
        assertThat(ektefelle.getFornavn()).isEqualTo(FORNAVN.toUpperCase());
        assertThat(ektefelle.getMellomnavn()).isEqualTo(MELLOMNAVN.toUpperCase());
        assertThat(ektefelle.getEtternavn()).isEqualTo(ETTERNAVN.toUpperCase());
        assertThat(ektefelle.getFnr()).isEqualTo(EKTEFELLEIDENT);
        assertThat(ektefelle.getFodselsdato()).hasToString("1970-01-01");
        assertThat(ektefelle.getFolkeregistrertSammen()).isTrue();
    }

    @Test
    void ektefelleOgPersonErIkkeFolkeregistrertSammenMedUlikMatrikkelId() {
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

        assertThat(ektefelle).isNotNull();
        assertThat(ektefelle.getFolkeregistrertSammen()).isFalse();
    }

    @Test
    void ektefelleOgPersonErFolkeregistrertSammenUtenMatrikkelId() {
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

        assertThat(ektefelle).isNotNull();
        assertThat(ektefelle.getFolkeregistrertSammen()).isTrue();
    }

    @Test
    void ektefelleMedAdressebeskyttelse() {
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

        assertThat(ektefelle).isNotNull();
        assertThat(ektefelle.getIkkeTilgangTilEktefelle()).isTrue();
        assertThat(ektefelle.getFornavn()).isNull();
        assertThat(ektefelle.getMellomnavn()).isNull();
        assertThat(ektefelle.getEtternavn()).isNull();
        assertThat(ektefelle.getFnr()).isNull();
        assertThat(ektefelle.getFodselsdato()).isNull();
        assertThat(ektefelle.getFolkeregistrertSammen()).isFalse();
    }

    @Test
    void ektefelleNull() {
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

        assertThat(ektefelle).isNull();
    }

    @Test
    void ektefelleOgPersonNullAdresse() {
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

        assertThat(ektefelle.getFolkeregistrertSammen()).isFalse();
    }

    @Test
    void ektefelleOgPersonTomAdresse() {
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

        assertThat(ektefelle.getFolkeregistrertSammen()).isFalse();
    }

    @Test
    void ektefelleOgPersonMatrikkelAdresse() {
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

        assertThat(ektefelle.getFolkeregistrertSammen()).isTrue();
    }

    @Test
    void fulltUtfyltBarn() {
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

        assertThat(barn).isNotNull();
        assertThat(barn.getFornavn()).isEqualTo(FORNAVN.toUpperCase());
        assertThat(barn.getMellomnavn()).isBlank();
        assertThat(barn.getEtternavn()).isEqualTo(ETTERNAVN.toUpperCase());
        assertThat(barn.getFnr()).isEqualTo(BARNIDENT);
        assertThat(barn.getFodselsdato()).isEqualTo(LocalDate.of(FOEDSELSDATO_BARN.getYear(), FOEDSELSDATO_BARN.getMonthValue(), FOEDSELSDATO_BARN.getDayOfMonth()));
        assertThat(barn.erFolkeregistrertsammen()).isTrue();
    }

    @Test
    void barnMedAdressebeskyttelse() {
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

        assertThat(barn).isNull();
    }

    @Test
    void barnDoed() {
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

        assertThat(barn).isNull();
    }

    @Test
    void barnMyndig() {
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

        assertThat(barn).isNull();
    }

    @Test
    void barnOgPersonNullAdresse() {
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

        assertThat(barn.erFolkeregistrertsammen()).isFalse();
    }

    @Test
    void barnOgPersonTomAdresseliste() {
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

        assertThat(barn.erFolkeregistrertsammen()).isFalse();
    }

    @Test
    void assertUtledingAvMyndighetErKorrekt() {
        var dagenFoerBarnBlirMyndig = LocalDate.now().minusYears(18).plusDays(1);
        var dagenBarnBlirMyndig = LocalDate.now().minusYears(18);
        var dagenEtterBarnBlirMyndig = LocalDate.now().minusYears(18).minusDays(1);

        var pdlPerson = new PdlPerson(
                emptyList(),
                null, // Ingen oppholdsadresse
                null, // ingen kontaktadresse
                asList(new ForelderBarnRelasjonDto(BARNIDENT, BARN_ROLLE, MOR_ROLLE)),
                asList(new NavnDto(FORNAVN, MELLOMNAVN, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA)),
                asList(new SivilstandDto(SivilstandDto.SivilstandType.GIFT, EKTEFELLEIDENT, METADATA, FOLKEREGISTERMETADATA)),
                asList(new StatsborgerskapDto(LAND))
        );

        var pdlBarn_dagenFoerBarnBlirMyndig = createBarnMedFoedselsdato(dagenFoerBarnBlirMyndig);
        var pdlBarn_dagenBarnBlirMyndig = createBarnMedFoedselsdato(dagenBarnBlirMyndig);
        var pdlBarn_dagenEtterBarnBlirMyndig = createBarnMedFoedselsdato(dagenEtterBarnBlirMyndig);

        assertThat(mapper.mapToBarn(pdlBarn_dagenFoerBarnBlirMyndig, BARNIDENT, pdlPerson)).isNotNull();
        assertThat(mapper.mapToBarn(pdlBarn_dagenBarnBlirMyndig, BARNIDENT, pdlPerson)).isNull();
        assertThat(mapper.mapToBarn(pdlBarn_dagenEtterBarnBlirMyndig, BARNIDENT, pdlPerson)).isNull();
    }

    @Test
    void adressebeskyttelseStrengtFortrolig() {
        var pdlAdressebeskyttelse = new PdlAdressebeskyttelse(
                asList(new AdressebeskyttelseDto(STRENGT_FORTROLIG))
        );

        var gradering = mapper.mapToAdressebeskyttelse(pdlAdressebeskyttelse);

        assertThat(gradering).isNotNull();
        assertThat(gradering).isEqualTo(STRENGT_FORTROLIG);
    }

    @Test
    void adressebeskyttelseStrengtUgradert() {
        var pdlAdressebeskyttelse = new PdlAdressebeskyttelse(
                asList(new AdressebeskyttelseDto(UGRADERT))
        );

        var gradering = mapper.mapToAdressebeskyttelse(pdlAdressebeskyttelse);

        assertThat(gradering).isNotNull();
        assertThat(gradering).isEqualTo(UGRADERT);
    }

    @Test
    void adressebeskyttelseNull() {
        var pdlAdressebeskyttelse = new PdlAdressebeskyttelse(
                asList(new AdressebeskyttelseDto(null))
        );

        var gradering = mapper.mapToAdressebeskyttelse(pdlAdressebeskyttelse);

        assertThat(gradering).isNull();
    }

    private VegadresseDto defaultVegadresse() {
        return new VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123", "123456");
    }

    private VegadresseDto annenVegadresse() {
        return new VegadresseDto("matrikkelId2", "stien", 2, "B", null, "1234", "1212", null, null);
    }

    private PdlBarn createBarnMedFoedselsdato(LocalDate foedselsdato) {
        return new PdlBarn(
                asList(new AdressebeskyttelseDto(AdressebeskyttelseDto.Gradering.UGRADERT)),
                emptyList(),
                asList(new FolkeregisterpersonstatusDto("ikke-doed")),
                asList(new FoedselDto(foedselsdato)),
                asList(new NavnDto(FORNAVN, null, ETTERNAVN, METADATA, FOLKEREGISTERMETADATA))
        );
    }
}