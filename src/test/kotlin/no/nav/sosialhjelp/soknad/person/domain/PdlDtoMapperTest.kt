package no.nav.sosialhjelp.soknad.person.domain

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.person.domain.PdlDtoMapper.Companion.DOED
import no.nav.sosialhjelp.soknad.person.dto.AdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.person.dto.BostedsadresseDto
import no.nav.sosialhjelp.soknad.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.person.dto.EndringDto
import no.nav.sosialhjelp.soknad.person.dto.FoedselDto
import no.nav.sosialhjelp.soknad.person.dto.FolkeregisterMetadataDto
import no.nav.sosialhjelp.soknad.person.dto.FolkeregisterpersonstatusDto
import no.nav.sosialhjelp.soknad.person.dto.ForelderBarnRelasjonDto
import no.nav.sosialhjelp.soknad.person.dto.Gradering
import no.nav.sosialhjelp.soknad.person.dto.KontaktadresseDto
import no.nav.sosialhjelp.soknad.person.dto.MatrikkeladresseDto
import no.nav.sosialhjelp.soknad.person.dto.MetadataDto
import no.nav.sosialhjelp.soknad.person.dto.NavnDto
import no.nav.sosialhjelp.soknad.person.dto.OppholdsadresseDto
import no.nav.sosialhjelp.soknad.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.person.dto.SivilstandDto
import no.nav.sosialhjelp.soknad.person.dto.SivilstandType
import no.nav.sosialhjelp.soknad.person.dto.StatsborgerskapDto
import no.nav.sosialhjelp.soknad.person.dto.UkjentBostedDto
import no.nav.sosialhjelp.soknad.person.dto.VegadresseDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class PdlDtoMapperTest {

    private val IDENT = "ident"
    private val FORNAVN = "fornavn"
    private val MELLOMNAVN = "mellomnavn"
    private val ETTERNAVN = "etternavn"

    private val BARNIDENT = "barnIdent"
    private val BARN_ROLLE = "BARN"
    private val MOR_ROLLE = "MOR"

    private val FOEDSELSDATO_BARN = LocalDate.now().withMonth(1).withDayOfMonth(1).minusYears(2)
    private val FOEDSELSDATO_BARN_MYNDIG = LocalDate.now().withMonth(1).withDayOfMonth(1).minusYears(19)

    private val EKTEFELLEIDENT = "ektefelleIdent"

    private val LAND = "NOR"

    private val METADATA = MetadataDto("FREG", listOf(EndringDto("FREG", LocalDateTime.now().minusDays(15), "type")))
    private val FOLKEREGISTERMETADATA = FolkeregisterMetadataDto(LocalDateTime.now().minusMonths(1), null)

    private val kodeverkService: KodeverkService = mockk()
    private val helper = MapperHelper()
    private val mapper = PdlDtoMapper(kodeverkService, helper)

    @BeforeEach
    internal fun setUp() {
        every { kodeverkService.getPoststed(any()) } returns "Mitt poststed"
    }

    @Test
    fun fulltUtfyltPerson() {
        val pdlPerson = PersonDto(
            listOf(
                BostedsadresseDto(
                    null,
                    VegadresseDto(
                        "matrikkelId",
                        "gateveien",
                        1,
                        "A",
                        "tilleggsnavn",
                        "1234",
                        "1212",
                        "U123123",
                        "123456"
                    ),
                    null,
                    null
                )
            ),
            listOf(
                OppholdsadresseDto(
                    null,
                    "Test McTest",
                    VegadresseDto("111", "midlertidig", 1, "A", null, "1234", "1212", null, null),
                    null,
                    null
                )
            ),
            listOf(
                KontaktadresseDto(
                    "Innland",
                    null,
                    VegadresseDto("222", "kontaktveien", 1, "A", null, "2222", "3333", null, null),
                    null,
                    null
                )
            ),
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val person = mapper.personDtoToDomain(pdlPerson, IDENT)
        assertThat(person).isNotNull
        assertThat(person!!.fornavn).isEqualTo(FORNAVN.uppercase())
        assertThat(person.mellomnavn).isEqualTo(MELLOMNAVN.uppercase())
        assertThat(person.etternavn).isEqualTo(ETTERNAVN.uppercase())
        assertThat(person.fnr).isEqualTo(IDENT)
        assertThat(person.sivilstatus).isEqualTo("gift")
        assertThat(person.statsborgerskap).hasSize(1)
        assertThat(person.statsborgerskap!![0]).isEqualTo(LAND)
        assertThat(person.bostedsadresse!!.coAdressenavn).isNull()
        assertThat(person.bostedsadresse!!.vegadresse!!.adressenavn).isEqualTo("gateveien".uppercase())
        assertThat(person.bostedsadresse!!.vegadresse!!.postnummer).isEqualTo("1234")
        assertThat(person.bostedsadresse!!.vegadresse!!.poststed).isEqualTo("Mitt poststed".uppercase())
        assertThat(person.bostedsadresse!!.matrikkeladresse).isNull()
        assertThat(person.oppholdsadresse!!.coAdressenavn).isEqualTo("Test McTest")
        assertThat(person.oppholdsadresse!!.vegadresse!!.adressenavn).isEqualTo("midlertidig".uppercase())
        assertThat(person.kontaktadresse!!.coAdressenavn).isNull()
        assertThat(person.kontaktadresse!!.vegadresse!!.adressenavn).isEqualTo("kontaktveien".uppercase())
    }

    @Test
    fun personNull() {
        val person = mapper.personDtoToDomain(null, IDENT)
        assertThat(person).isNull()
    }

    @Test
    fun personMedMatrikkeladresseBostedsadresse() {
        val pdlPerson = PersonDto(
            listOf(
                BostedsadresseDto(
                    null,
                    null,
                    MatrikkeladresseDto("matrikkelid", "1111", null, "1111", null),
                    null
                )
            ),
            null, // ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val person = mapper.personDtoToDomain(pdlPerson, IDENT)
        assertThat(person).isNotNull
        assertThat(person!!.bostedsadresse!!.vegadresse).isNull()
        assertThat(person.bostedsadresse!!.matrikkeladresse).isNotNull
        assertThat(person.bostedsadresse!!.matrikkeladresse!!.matrikkelId).isEqualTo("matrikkelid")
    }

    @Test
    fun personMedUkjentBosted() {
        val pdlPerson = PersonDto(
            listOf(BostedsadresseDto(null, null, null, UkjentBostedDto("Oslo"))),
            null, // ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val person = mapper.personDtoToDomain(pdlPerson, IDENT)
        assertThat(person).isNotNull
        assertThat(person!!.bostedsadresse).isNull()
    }

    @Test
    fun personMedOppholdsadresseUtenVegadresse() {
        val pdlPerson = PersonDto(
            listOf(
                BostedsadresseDto(
                    null,
                    VegadresseDto(
                        "matrikkelId",
                        "gateveien",
                        1,
                        "A",
                        "tilleggsnavn",
                        "1234",
                        "1212",
                        "U123123",
                        "123456"
                    ),
                    null,
                    null
                )
            ),
            listOf(OppholdsadresseDto("oppholdAnnetSted", null, null, null, null)),
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val person = mapper.personDtoToDomain(pdlPerson, IDENT)
        assertThat(person).isNotNull
        assertThat(person!!.oppholdsadresse).isNull()
    }

    @Test
    fun personMedOppholdsadresseLikBostedsadresseSkalFiltreresVekk() {
        val vegadresse = defaultVegadresse()
        val annenVegadresse = annenVegadresse()
        val pdlPerson = PersonDto(
            listOf(BostedsadresseDto(null, vegadresse, null, null)),
            listOf(
                OppholdsadresseDto(null, null, vegadresse, null, null),
                OppholdsadresseDto(null, null, annenVegadresse, null, null)
            ),
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val person = mapper.personDtoToDomain(pdlPerson, IDENT)
        assertThat(person).isNotNull
        assertThat(person!!.bostedsadresse!!.coAdressenavn).isNull()
        assertThat(person.bostedsadresse!!.vegadresse!!.adressenavn)
            .isEqualTo(vegadresse.adressenavn?.uppercase())
        assertThat(person.bostedsadresse!!.matrikkeladresse).isNull()
        assertThat(person.oppholdsadresse!!.coAdressenavn).isNull()
        assertThat(person.oppholdsadresse!!.vegadresse!!.adressenavn)
            .isEqualTo(annenVegadresse.adressenavn?.uppercase())
    }

    @Test
    fun personMedKontaktadresseLikBostedsadresseSkalFiltreresVekk() {
        val vegadresse = defaultVegadresse()
        val annenVegadresse = annenVegadresse()
        val pdlPerson = PersonDto(
            listOf(BostedsadresseDto(null, vegadresse, null, null)),
            null, // ingen oppholdsadresse
            listOf(
                KontaktadresseDto("Innland", null, vegadresse, null, null),
                KontaktadresseDto("Innland", null, annenVegadresse, null, null)
            ),
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val person = mapper.personDtoToDomain(pdlPerson, IDENT)
        assertThat(person).isNotNull
        assertThat(person!!.bostedsadresse!!.coAdressenavn).isNull()
        assertThat(person.bostedsadresse!!.vegadresse!!.adressenavn)
            .isEqualTo(vegadresse.adressenavn?.uppercase())
        assertThat(person.bostedsadresse!!.matrikkeladresse).isNull()
        assertThat(person.kontaktadresse!!.coAdressenavn).isNull()
        assertThat(person.kontaktadresse!!.vegadresse!!.adressenavn)
            .isEqualTo(annenVegadresse.adressenavn?.uppercase())
    }

    @Test
    fun personMedKontaktadresseUtenKommunenummerLikBostedsadresseSkalFiltreresVekk() {
        val vegadresse = VegadresseDto("matrikkelId", "gateveien", 1, "A", null, "1234", "1212", null, null)
        val vegadresseUtenKommunenummer =
            VegadresseDto("matrikkelId", "gateveien", 1, "A", null, "1234", null, null, null)
        val pdlPerson = PersonDto(
            listOf(BostedsadresseDto(null, vegadresse, null, null)),
            null, // ingen oppholdsadresse
            listOf(KontaktadresseDto("Innland", null, vegadresseUtenKommunenummer, null, null)),
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val person = mapper.personDtoToDomain(pdlPerson, IDENT)
        assertThat(person).isNotNull
        assertThat(person!!.bostedsadresse!!.coAdressenavn).isNull()
        assertThat(person.bostedsadresse!!.vegadresse!!.adressenavn)
            .isEqualTo(vegadresse.adressenavn?.uppercase())
        assertThat(person.bostedsadresse!!.matrikkeladresse).isNull()
        assertThat(person.kontaktadresse).isNull()
    }

    @Test
    fun fulltUtfyltEktefelle() {
        val pdlPerson = PersonDto(
            listOf(BostedsadresseDto(null, defaultVegadresse(), null, null)),
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlEktefelle = EktefelleDto(
            listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            listOf(BostedsadresseDto(null, defaultVegadresse(), null, null)),
            listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
        val ektefelle = mapper.ektefelleDtoToDomain(pdlEktefelle, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle).isNotNull
        assertThat(ektefelle!!.ikkeTilgangTilEktefelle).isFalse
        assertThat(ektefelle.fornavn).isEqualTo(FORNAVN.uppercase())
        assertThat(ektefelle.mellomnavn).isEqualTo(MELLOMNAVN.uppercase())
        assertThat(ektefelle.etternavn).isEqualTo(ETTERNAVN.uppercase())
        assertThat(ektefelle.fnr).isEqualTo(EKTEFELLEIDENT)
        assertThat(ektefelle.fodselsdato).hasToString("1970-01-01")
        assertThat(ektefelle.folkeregistrertSammen).isTrue
    }

    @Test
    fun ektefelleOgPersonErIkkeFolkeregistrertSammenMedUlikMatrikkelId() {
        val pdlPerson = PersonDto(
            listOf(
                BostedsadresseDto(
                    null,
                    VegadresseDto(
                        "matrikkelId",
                        "gateveien",
                        1,
                        "A",
                        "tilleggsnavn",
                        "1234",
                        "1212",
                        "U123123",
                        "123456"
                    ),
                    null,
                    null
                )
            ),
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlEktefelle = EktefelleDto(
            listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            listOf(
                BostedsadresseDto(
                    null,
                    VegadresseDto(
                        "2matrikkelId",
                        "gateveien",
                        1,
                        "A",
                        "tilleggsnavn",
                        "1234",
                        "1212",
                        "U123123",
                        "123456"
                    ),
                    null,
                    null
                )
            ), // kun matrikkelId er ulik
            listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
        val ektefelle = mapper.ektefelleDtoToDomain(pdlEktefelle, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle).isNotNull
        assertThat(ektefelle!!.folkeregistrertSammen).isFalse
    }

    @Test
    fun ektefelleOgPersonErFolkeregistrertSammenUtenMatrikkelId() {
        val pdlPerson = PersonDto(
            listOf(
                BostedsadresseDto(
                    null,
                    VegadresseDto(null, "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123", "123456"),
                    null,
                    null
                )
            ),
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlEktefelle = EktefelleDto(
            listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            listOf(
                BostedsadresseDto(
                    null,
                    VegadresseDto(null, "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123", "123456"),
                    null,
                    null
                )
            ),
            listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
        val ektefelle = mapper.ektefelleDtoToDomain(pdlEktefelle, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle).isNotNull
        assertThat(ektefelle!!.folkeregistrertSammen).isTrue
    }

    @Test
    fun ektefelleMedAdressebeskyttelse() {
        val pdlPerson = PersonDto(
            listOf(BostedsadresseDto(null, defaultVegadresse(), null, null)),
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlEktefelle = EktefelleDto(
            listOf(AdressebeskyttelseDto(Gradering.STRENGT_FORTROLIG)),
            listOf(BostedsadresseDto(null, null, null, null)),
            listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
        val ektefelle = mapper.ektefelleDtoToDomain(pdlEktefelle, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle).isNotNull
        assertThat(ektefelle!!.ikkeTilgangTilEktefelle).isTrue
        assertThat(ektefelle.fornavn).isNull()
        assertThat(ektefelle.mellomnavn).isNull()
        assertThat(ektefelle.etternavn).isNull()
        assertThat(ektefelle.fnr).isNull()
        assertThat(ektefelle.fodselsdato).isNull()
        assertThat(ektefelle.folkeregistrertSammen).isFalse
    }

    @Test
    fun ektefelleNull() {
        val pdlPerson = PersonDto(
            listOf(BostedsadresseDto(null, defaultVegadresse(), null, null)),
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val ektefelle = mapper.ektefelleDtoToDomain(null, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle).isNull()
    }

    @Test
    fun ektefelleOgPersonNullAdresse() {
        val pdlPerson = PersonDto(
            null, // Ingen bostedsadresse
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlEktefelle = EktefelleDto(
            listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            null, // Ingen bostedsadresse
            listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
        val ektefelle = mapper.ektefelleDtoToDomain(pdlEktefelle, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle!!.folkeregistrertSammen).isFalse
    }

    @Test
    fun ektefelleOgPersonTomAdresse() {
        val pdlPerson = PersonDto(
            emptyList(),
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            emptyList(),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlEktefelle = EktefelleDto(
            listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            emptyList(),
            listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
        val ektefelle = mapper.ektefelleDtoToDomain(pdlEktefelle, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle!!.folkeregistrertSammen).isFalse
    }

    @Test
    fun ektefelleOgPersonMatrikkelAdresse() {
        val pdlPerson = PersonDto(
            listOf(
                BostedsadresseDto(
                    null,
                    null,
                    MatrikkeladresseDto("matrikkelId", "postnr", "tillegg", "kommunenr", "bruksenhetsnr"),
                    null
                )
            ),
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            emptyList(),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlEktefelle = EktefelleDto(
            listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            listOf(
                BostedsadresseDto(
                    null,
                    null,
                    MatrikkeladresseDto("matrikkelId", "postnr", "tillegg", "kommunenr", "bruksenhetsnr"),
                    null
                )
            ),
            listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
        val ektefelle = mapper.ektefelleDtoToDomain(pdlEktefelle, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle!!.folkeregistrertSammen).isTrue
    }

    @Test
    fun fulltUtfyltBarn() {
        val pdlPerson = PersonDto(
            listOf(BostedsadresseDto(null, defaultVegadresse(), null, null)),
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlBarn = BarnDto(
            listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            listOf(BostedsadresseDto(null, defaultVegadresse(), null, null)),
            listOf(FolkeregisterpersonstatusDto("ikke-doed")),
            listOf(FoedselDto(FOEDSELSDATO_BARN)),
            listOf(
                NavnDto(
                    FORNAVN,
                    null,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
        val barn = mapper.barnDtoToDomain(pdlBarn, BARNIDENT, pdlPerson)
        assertThat(barn).isNotNull
        assertThat(barn!!.fornavn).isEqualTo(FORNAVN.uppercase())
        assertThat(barn.mellomnavn).isBlank
        assertThat(barn.etternavn).isEqualTo(ETTERNAVN.uppercase())
        assertThat(barn.fnr).isEqualTo(BARNIDENT)
        assertThat(barn.fodselsdato).isEqualTo(
            LocalDate.of(
                FOEDSELSDATO_BARN.year,
                FOEDSELSDATO_BARN.monthValue,
                FOEDSELSDATO_BARN.dayOfMonth
            )
        )
        assertThat(barn.folkeregistrertSammen).isTrue
    }

    @Test
    fun barnMedAdressebeskyttelse() {
        val pdlPerson = PersonDto(
            listOf(BostedsadresseDto(null, defaultVegadresse(), null, null)),
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlBarn = BarnDto(
            listOf(AdressebeskyttelseDto(Gradering.FORTROLIG)),
            listOf(BostedsadresseDto(null, null, null, null)),
            listOf(FolkeregisterpersonstatusDto("ikke-doed")),
            listOf(FoedselDto(FOEDSELSDATO_BARN)),
            listOf(
                NavnDto(
                    FORNAVN,
                    null,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
        val barn = mapper.barnDtoToDomain(pdlBarn, BARNIDENT, pdlPerson)
        assertThat(barn).isNull()
    }

    @Test
    fun barnDoed() {
        val pdlPerson = PersonDto(
            listOf(BostedsadresseDto(null, defaultVegadresse(), null, null)),
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlBarn = BarnDto(
            listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            listOf(BostedsadresseDto(null, defaultVegadresse(), null, null)),
            listOf(FolkeregisterpersonstatusDto(DOED)),
            listOf(FoedselDto(FOEDSELSDATO_BARN)),
            listOf(
                NavnDto(
                    FORNAVN,
                    null,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
        val barn = mapper.barnDtoToDomain(pdlBarn, BARNIDENT, pdlPerson)
        assertThat(barn).isNull()
    }

    @Test
    fun barnMyndig() {
        val pdlPerson = PersonDto(
            listOf(BostedsadresseDto(null, defaultVegadresse(), null, null)),
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlBarn = BarnDto(
            listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            listOf(BostedsadresseDto(null, defaultVegadresse(), null, null)),
            listOf(FolkeregisterpersonstatusDto("ikke-doed")),
            listOf(FoedselDto(FOEDSELSDATO_BARN_MYNDIG)),
            listOf(
                NavnDto(
                    FORNAVN,
                    null,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
        val barn = mapper.barnDtoToDomain(pdlBarn, BARNIDENT, pdlPerson)
        assertThat(barn).isNull()
    }

    @Test
    fun barnOgPersonNullAdresse() {
        val pdlPerson = PersonDto(
            null, // Ingen bostedsadresse
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlBarn = BarnDto(
            listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            null,
            listOf(FolkeregisterpersonstatusDto("ikke-doed")),
            listOf(FoedselDto(FOEDSELSDATO_BARN)),
            listOf(
                NavnDto(
                    FORNAVN,
                    null,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
        val barn = mapper.barnDtoToDomain(pdlBarn, BARNIDENT, pdlPerson)
        assertThat(barn!!.folkeregistrertSammen).isFalse
    }

    @Test
    fun barnOgPersonTomAdresseliste() {
        val pdlPerson = PersonDto(
            emptyList(),
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlBarn = BarnDto(
            listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            emptyList(),
            listOf(FolkeregisterpersonstatusDto("ikke-doed")),
            listOf(FoedselDto(FOEDSELSDATO_BARN)),
            listOf(
                NavnDto(
                    FORNAVN,
                    null,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
        val barn = mapper.barnDtoToDomain(pdlBarn, BARNIDENT, pdlPerson)
        assertThat(barn!!.folkeregistrertSammen).isFalse
    }

    @Test
    fun assertUtledingAvMyndighetErKorrekt() {
        val dagenFoerBarnBlirMyndig = LocalDate.now().minusYears(18).plusDays(1)
        val dagenBarnBlirMyndig = LocalDate.now().minusYears(18)
        val dagenEtterBarnBlirMyndig = LocalDate.now().minusYears(18).minusDays(1)
        val pdlPerson = PersonDto(
            emptyList(),
            null, // Ingen oppholdsadresse
            null, // ingen kontaktadresse
            listOf(
                ForelderBarnRelasjonDto(
                    BARNIDENT,
                    BARN_ROLLE,
                    MOR_ROLLE
                )
            ),
            listOf(
                NavnDto(
                    FORNAVN,
                    MELLOMNAVN,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(
                SivilstandDto(
                    SivilstandType.GIFT,
                    EKTEFELLEIDENT,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            ),
            listOf(StatsborgerskapDto(LAND))
        )
        val pdlBarn_dagenFoerBarnBlirMyndig = createBarnMedFoedselsdato(dagenFoerBarnBlirMyndig)
        val pdlBarn_dagenBarnBlirMyndig = createBarnMedFoedselsdato(dagenBarnBlirMyndig)
        val pdlBarn_dagenEtterBarnBlirMyndig = createBarnMedFoedselsdato(dagenEtterBarnBlirMyndig)
        assertThat<Barn>(
            mapper.barnDtoToDomain(
                pdlBarn_dagenFoerBarnBlirMyndig,
                BARNIDENT,
                pdlPerson
            )
        ).isNotNull
        assertThat<Barn>(
            mapper.barnDtoToDomain(
                pdlBarn_dagenBarnBlirMyndig,
                BARNIDENT,
                pdlPerson
            )
        ).isNull()
        assertThat<Barn>(
            mapper.barnDtoToDomain(
                pdlBarn_dagenEtterBarnBlirMyndig,
                BARNIDENT,
                pdlPerson
            )
        ).isNull()
    }

    @Test
    fun adressebeskyttelseStrengtFortrolig() {
        val pdlAdressebeskyttelse = PersonAdressebeskyttelseDto(
            listOf(AdressebeskyttelseDto(Gradering.STRENGT_FORTROLIG))
        )
        val gradering = mapper.personAdressebeskyttelseDtoToGradering(pdlAdressebeskyttelse)
        assertThat(gradering).isNotNull
        assertThat(gradering).isEqualTo(Gradering.STRENGT_FORTROLIG)
    }

    @Test
    fun adressebeskyttelseStrengtUgradert() {
        val pdlAdressebeskyttelse = PersonAdressebeskyttelseDto(
            listOf(AdressebeskyttelseDto(Gradering.UGRADERT))
        )
        val gradering = mapper.personAdressebeskyttelseDtoToGradering(pdlAdressebeskyttelse)
        assertThat(gradering).isNotNull
        assertThat(gradering).isEqualTo(Gradering.UGRADERT)
    }

    @Test
    fun adressebeskyttelseNull() {
        val pdlAdressebeskyttelse = PersonAdressebeskyttelseDto(
            listOf(AdressebeskyttelseDto(null))
        )
        val gradering = mapper.personAdressebeskyttelseDtoToGradering(pdlAdressebeskyttelse)
        assertThat(gradering).isNull()
    }

    private fun defaultVegadresse(): VegadresseDto {
        return VegadresseDto("matrikkelId", "gateveien", 1, "A", "tilleggsnavn", "1234", "1212", "U123123", "123456")
    }

    private fun annenVegadresse(): VegadresseDto {
        return VegadresseDto("matrikkelId2", "stien", 2, "B", null, "1234", "1212", null, null)
    }

    private fun createBarnMedFoedselsdato(foedselsdato: LocalDate): BarnDto {
        return BarnDto(
            listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            emptyList(),
            listOf(FolkeregisterpersonstatusDto("ikke-doed")),
            listOf(FoedselDto(foedselsdato)),
            listOf(
                NavnDto(
                    FORNAVN,
                    null,
                    ETTERNAVN,
                    METADATA,
                    FOLKEREGISTERMETADATA
                )
            )
        )
    }
}
