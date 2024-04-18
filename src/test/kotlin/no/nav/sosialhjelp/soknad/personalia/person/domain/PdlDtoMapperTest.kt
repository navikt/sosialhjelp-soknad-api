package no.nav.sosialhjelp.soknad.personalia.person.domain

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.personalia.person.domain.PdlDtoMapper.Companion.DOED
import no.nav.sosialhjelp.soknad.personalia.person.dto.AdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.BostedsadresseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EndringDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.FoedselDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.FolkeregisterMetadataDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.FolkeregisterpersonstatusDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.ForelderBarnRelasjonDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import no.nav.sosialhjelp.soknad.personalia.person.dto.MatrikkeladresseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.MetadataDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.NavnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.OppholdsadresseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandType
import no.nav.sosialhjelp.soknad.personalia.person.dto.StatsborgerskapDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.UkjentBostedDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.VegadresseDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class PdlDtoMapperTest {
    companion object {
        private const val IDENT = "ident"
        private const val FORNAVN = "Fornavn"
        private const val MELLOMNAVN = "Mellomnavn"
        private const val ETTERNAVN = "Etternavn"

        private const val BARNIDENT = "barnIdent"
        private const val BARN_ROLLE = "BARN"
        private const val MOR_ROLLE = "MOR"

        private val FOEDSELSDATO_BARN = LocalDate.now().withMonth(1).withDayOfMonth(1).minusYears(2)
        private val FOEDSELSDATO_BARN_MYNDIG = LocalDate.now().withMonth(1).withDayOfMonth(1).minusYears(19)

        private const val EKTEFELLEIDENT = "ektefelleIdent"

        private const val LAND = "NOR"

        private val METADATA = MetadataDto("FREG", listOf(EndringDto("FREG", LocalDateTime.now().minusDays(15), "type")))
        private val FOLKEREGISTERMETADATA = FolkeregisterMetadataDto(LocalDateTime.now().minusMonths(1), null)
    }

    private val kodeverkService: KodeverkService = mockk()
    private val helper = MapperHelper()
    private val mapper = PdlDtoMapper(kodeverkService, helper)

    private val defaultVegadresse =
        VegadresseDto(
            matrikkelId = "matrikkelId",
            adressenavn = "Gateveien",
            husnummer = 1,
            husbokstav = "A",
            tilleggsnavn = "tilleggsnavn",
            postnummer = "1234",
            kommunenummer = "1212",
            bruksenhetsnummer = "U123123",
            bydelsnummer = "123456",
        )

    private val annenVegadresse =
        VegadresseDto(
            matrikkelId = "matrikkelId2",
            adressenavn = "Stien",
            husnummer = 2,
            husbokstav = "B",
            tilleggsnavn = null,
            postnummer = "1234",
            kommunenummer = "1212",
            bruksenhetsnummer = null,
            bydelsnummer = null,
        )

    private val defaultBarn =
        BarnDto(
            adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            bostedsadresse = emptyList(),
            folkeregisterpersonstatus = listOf(FolkeregisterpersonstatusDto("ikke-doed")),
            foedsel = listOf(FoedselDto(LocalDate.now().minusYears(12))),
            navn =
                listOf(
                    NavnDto(
                        fornavn = FORNAVN,
                        mellomnavn = null,
                        etternavn = ETTERNAVN,
                        metadata = METADATA,
                        folkeregistermetadata = FOLKEREGISTERMETADATA,
                    ),
                ),
        )

    private val defaultForelderBarnRelasjonDto =
        ForelderBarnRelasjonDto(
            relatertPersonsIdent = BARNIDENT,
            relatertPersonsRolle = BARN_ROLLE,
            minRolleForPerson = MOR_ROLLE,
        )

    private val defaultNavnDto =
        NavnDto(
            fornavn = FORNAVN,
            mellomnavn = MELLOMNAVN,
            etternavn = ETTERNAVN,
            metadata = METADATA,
            folkeregistermetadata = FOLKEREGISTERMETADATA,
        )

    private val defaultSivilstandDto =
        SivilstandDto(
            type = SivilstandType.GIFT,
            relatertVedSivilstand = EKTEFELLEIDENT,
            metadata = METADATA,
            folkeregistermetadata = FOLKEREGISTERMETADATA,
        )

    @BeforeEach
    internal fun setUp() {
        every { kodeverkService.getPoststed(any()) } returns "Mitt poststed"
    }

    @Test
    fun fulltUtfyltPerson() {
        val pdlPerson =
            PersonDto(
                bostedsadresse =
                    listOf(
                        BostedsadresseDto(
                            coAdressenavn = null,
                            vegadresse = defaultVegadresse,
                            matrikkeladresse = null,
                            ukjentBosted = null,
                        ),
                    ),
                oppholdsadresse =
                    listOf(
                        OppholdsadresseDto(
                            oppholdAnnetSted = null,
                            coAdressenavn = "Test McTest",
                            vegadresse = VegadresseDto("111", "midlertidig", 1, "A", null, "1234", "1212", null, null),
                            metadata = null,
                            folkeregistermetadata = null,
                        ),
                    ),
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val person = mapper.personDtoToDomain(pdlPerson, IDENT)
        assertThat(person).isNotNull
        assertThat(person!!.fornavn).isEqualTo(FORNAVN)
        assertThat(person.mellomnavn).isEqualTo(MELLOMNAVN)
        assertThat(person.etternavn).isEqualTo(ETTERNAVN)
        assertThat(person.fnr).isEqualTo(IDENT)
        assertThat(person.sivilstatus).isEqualTo("gift")
        assertThat(person.statsborgerskap).hasSize(1)
        assertThat(person.statsborgerskap!![0]).isEqualTo(LAND)
        assertThat(person.bostedsadresse!!.coAdressenavn).isNull()
        assertThat(person.bostedsadresse!!.vegadresse!!.adressenavn).isEqualTo("Gateveien")
        assertThat(person.bostedsadresse!!.vegadresse!!.postnummer).isEqualTo("1234")
        assertThat(person.bostedsadresse!!.vegadresse!!.poststed).isEqualTo("Mitt poststed")
        assertThat(person.bostedsadresse!!.matrikkeladresse).isNull()
        assertThat(person.oppholdsadresse!!.coAdressenavn).isEqualTo("Test McTest")
        assertThat(person.oppholdsadresse!!.vegadresse!!.adressenavn).isEqualTo("midlertidig")
    }

    @Test
    fun personNull() {
        val person = mapper.personDtoToDomain(null, IDENT)
        assertThat(person).isNull()
    }

    @Test
    fun personMedMatrikkeladresseBostedsadresse() {
        val pdlPerson =
            PersonDto(
                bostedsadresse =
                    listOf(
                        BostedsadresseDto(
                            coAdressenavn = null,
                            vegadresse = null,
                            matrikkeladresse = MatrikkeladresseDto("matrikkelid", "1111", null, "1111", null),
                            ukjentBosted = null,
                        ),
                    ),
                oppholdsadresse = null,
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val person = mapper.personDtoToDomain(pdlPerson, IDENT)
        assertThat(person).isNotNull
        assertThat(person!!.bostedsadresse!!.vegadresse).isNull()
        assertThat(person.bostedsadresse!!.matrikkeladresse).isNotNull
        assertThat(person.bostedsadresse!!.matrikkeladresse!!.matrikkelId).isEqualTo("matrikkelid")
    }

    @Test
    fun personMedUkjentBosted() {
        val pdlPerson =
            PersonDto(
                bostedsadresse = listOf(BostedsadresseDto(null, null, null, UkjentBostedDto("Oslo"))),
                oppholdsadresse = null,
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val person = mapper.personDtoToDomain(pdlPerson, IDENT)
        assertThat(person).isNotNull
        assertThat(person!!.bostedsadresse).isNull()
    }

    @Test
    fun personMedOppholdsadresseUtenVegadresse() {
        val pdlPerson =
            PersonDto(
                bostedsadresse =
                    listOf(
                        BostedsadresseDto(
                            coAdressenavn = null,
                            vegadresse = defaultVegadresse,
                            matrikkeladresse = null,
                            ukjentBosted = null,
                        ),
                    ),
                oppholdsadresse =
                    listOf(
                        OppholdsadresseDto(
                            oppholdAnnetSted = "oppholdAnnetSted",
                            coAdressenavn = null,
                            vegadresse = null,
                            metadata = null,
                            folkeregistermetadata = null,
                        ),
                    ),
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val person = mapper.personDtoToDomain(pdlPerson, IDENT)
        assertThat(person).isNotNull
        assertThat(person!!.oppholdsadresse).isNull()
    }

    @Test
    fun personMedOppholdsadresseLikBostedsadresseSkalFiltreresVekk() {
        val pdlPerson =
            PersonDto(
                bostedsadresse =
                    listOf(
                        BostedsadresseDto(
                            coAdressenavn = null,
                            vegadresse = defaultVegadresse,
                            matrikkeladresse = null,
                            ukjentBosted = null,
                        ),
                    ),
                oppholdsadresse =
                    listOf(
                        OppholdsadresseDto(
                            oppholdAnnetSted = null,
                            coAdressenavn = null,
                            vegadresse = defaultVegadresse,
                            metadata = null,
                            folkeregistermetadata = null,
                        ),
                        OppholdsadresseDto(
                            oppholdAnnetSted = null,
                            coAdressenavn = null,
                            vegadresse = annenVegadresse,
                            metadata = null,
                            folkeregistermetadata = null,
                        ),
                    ),
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val person = mapper.personDtoToDomain(pdlPerson, IDENT)
        assertThat(person).isNotNull
        assertThat(person!!.bostedsadresse!!.coAdressenavn).isNull()
        assertThat(person.bostedsadresse!!.vegadresse!!.adressenavn).isEqualTo(defaultVegadresse.adressenavn)
        assertThat(person.bostedsadresse!!.matrikkeladresse).isNull()
        assertThat(person.oppholdsadresse!!.coAdressenavn).isNull()
        assertThat(person.oppholdsadresse!!.vegadresse!!.adressenavn).isEqualTo(annenVegadresse.adressenavn)
    }

    @Test
    fun personMedOppholdsadresseUtenBostedsadresse() {
        val pdlPerson =
            PersonDto(
                bostedsadresse = null, // ingen bostedsadresse
                oppholdsadresse =
                    listOf(
                        OppholdsadresseDto(
                            oppholdAnnetSted = null,
                            coAdressenavn = null,
                            vegadresse = defaultVegadresse,
                            metadata = null,
                            folkeregistermetadata = null,
                        ),
                    ),
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val person = mapper.personDtoToDomain(pdlPerson, IDENT)
        assertThat(person).isNotNull
        assertThat(person!!.bostedsadresse).isNull()
        assertThat(person.oppholdsadresse).isNotNull
        assertThat(person.oppholdsadresse!!.vegadresse!!.adressenavn).isEqualTo(defaultVegadresse.adressenavn)
    }

    @Test
    fun fulltUtfyltEktefelle() {
        val pdlPerson =
            PersonDto(
                bostedsadresse =
                    listOf(
                        BostedsadresseDto(
                            coAdressenavn = null,
                            vegadresse = defaultVegadresse,
                            matrikkeladresse = null,
                            ukjentBosted = null,
                        ),
                    ),
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlEktefelle =
            EktefelleDto(
                adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
                bostedsadresse = listOf(BostedsadresseDto(null, defaultVegadresse, null, null)),
                foedsel = listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
                navn = listOf(defaultNavnDto),
            )
        val ektefelle = mapper.ektefelleDtoToDomain(pdlEktefelle, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle).isNotNull
        assertThat(ektefelle!!.ikkeTilgangTilEktefelle).isFalse
        assertThat(ektefelle.fornavn).isEqualTo(FORNAVN)
        assertThat(ektefelle.mellomnavn).isEqualTo(MELLOMNAVN)
        assertThat(ektefelle.etternavn).isEqualTo(ETTERNAVN)
        assertThat(ektefelle.fnr).isEqualTo(EKTEFELLEIDENT)
        assertThat(ektefelle.fodselsdato).hasToString("1970-01-01")
        assertThat(ektefelle.folkeregistrertSammen).isTrue
    }

    @Test
    fun ektefelleOgPersonErIkkeFolkeregistrertSammenMedUlikMatrikkelId() {
        val pdlPerson =
            PersonDto(
                bostedsadresse =
                    listOf(
                        BostedsadresseDto(
                            coAdressenavn = null,
                            vegadresse = defaultVegadresse,
                            matrikkeladresse = null,
                            ukjentBosted = null,
                        ),
                    ),
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlEktefelle =
            EktefelleDto(
                adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
                bostedsadresse =
                    listOf(
                        BostedsadresseDto(
                            coAdressenavn = null,
                            vegadresse = defaultVegadresse.copy(matrikkelId = "2matrikkelId"),
                            matrikkeladresse = null,
                            ukjentBosted = null,
                        ),
                    ),
                // kun matrikkelId er ulik
                foedsel = listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
                navn = listOf(defaultNavnDto),
            )
        val ektefelle = mapper.ektefelleDtoToDomain(pdlEktefelle, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle).isNotNull
        assertThat(ektefelle!!.folkeregistrertSammen).isFalse
    }

    @Test
    fun ektefelleOgPersonErFolkeregistrertSammenUtenMatrikkelId() {
        val pdlPerson =
            PersonDto(
                bostedsadresse =
                    listOf(
                        BostedsadresseDto(
                            coAdressenavn = null,
                            vegadresse = defaultVegadresse.copy(matrikkelId = null),
                            matrikkeladresse = null,
                            ukjentBosted = null,
                        ),
                    ),
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlEktefelle =
            EktefelleDto(
                adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
                bostedsadresse =
                    listOf(
                        BostedsadresseDto(
                            coAdressenavn = null,
                            vegadresse = defaultVegadresse.copy(matrikkelId = null),
                            matrikkeladresse = null,
                            ukjentBosted = null,
                        ),
                    ),
                foedsel = listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
                navn = listOf(defaultNavnDto),
            )
        val ektefelle = mapper.ektefelleDtoToDomain(pdlEktefelle, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle).isNotNull
        assertThat(ektefelle!!.folkeregistrertSammen).isTrue
    }

    @Test
    fun ektefelleMedAdressebeskyttelse() {
        val pdlPerson =
            PersonDto(
                bostedsadresse = listOf(BostedsadresseDto(null, defaultVegadresse, null, null)),
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlEktefelle =
            EktefelleDto(
                adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.STRENGT_FORTROLIG)),
                bostedsadresse = listOf(BostedsadresseDto(null, null, null, null)),
                foedsel = listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
                navn = listOf(defaultNavnDto),
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
        val pdlPerson =
            PersonDto(
                bostedsadresse = listOf(BostedsadresseDto(null, defaultVegadresse, null, null)),
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val ektefelle = mapper.ektefelleDtoToDomain(null, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle).isNull()
    }

    @Test
    fun ektefelleOgPersonNullAdresse() {
        val pdlPerson =
            PersonDto(
                bostedsadresse = null, // Ingen bostedsadresse
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlEktefelle =
            EktefelleDto(
                adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
                bostedsadresse = null, // Ingen bostedsadresse
                foedsel = listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
                navn = listOf(defaultNavnDto),
            )
        val ektefelle = mapper.ektefelleDtoToDomain(pdlEktefelle, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle!!.folkeregistrertSammen).isFalse
    }

    @Test
    fun ektefelleOgPersonTomAdresse() {
        val pdlPerson =
            PersonDto(
                bostedsadresse = emptyList(),
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = emptyList(),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlEktefelle =
            EktefelleDto(
                adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
                bostedsadresse = emptyList(),
                foedsel = listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
                navn = listOf(defaultNavnDto),
            )
        val ektefelle = mapper.ektefelleDtoToDomain(pdlEktefelle, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle!!.folkeregistrertSammen).isFalse
    }

    @Test
    fun ektefelleOgPersonMatrikkelAdresse() {
        val pdlPerson =
            PersonDto(
                bostedsadresse =
                    listOf(
                        BostedsadresseDto(
                            coAdressenavn = null,
                            vegadresse = null,
                            matrikkeladresse = MatrikkeladresseDto("matrikkelId", "postnr", "tillegg", "kommunenr", "bruksenhetsnr"),
                            ukjentBosted = null,
                        ),
                    ),
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = emptyList(),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlEktefelle =
            EktefelleDto(
                adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
                bostedsadresse =
                    listOf(
                        BostedsadresseDto(
                            coAdressenavn = null,
                            vegadresse = null,
                            matrikkeladresse = MatrikkeladresseDto("matrikkelId", "postnr", "tillegg", "kommunenr", "bruksenhetsnr"),
                            ukjentBosted = null,
                        ),
                    ),
                foedsel = listOf(FoedselDto(LocalDate.of(1970, 1, 1))),
                navn = listOf(defaultNavnDto),
            )
        val ektefelle = mapper.ektefelleDtoToDomain(pdlEktefelle, EKTEFELLEIDENT, pdlPerson)
        assertThat(ektefelle!!.folkeregistrertSammen).isTrue
    }

    @Test
    fun fulltUtfyltBarn() {
        val pdlPerson =
            PersonDto(
                bostedsadresse = listOf(BostedsadresseDto(null, defaultVegadresse, null, null)),
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlBarn =
            BarnDto(
                adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
                bostedsadresse = listOf(BostedsadresseDto(null, defaultVegadresse, null, null)),
                folkeregisterpersonstatus = listOf(FolkeregisterpersonstatusDto("ikke-doed")),
                foedsel = listOf(FoedselDto(FOEDSELSDATO_BARN)),
                navn = listOf(defaultNavnDto.copy(mellomnavn = null)),
            )
        val barn = mapper.barnDtoToDomain(pdlBarn, BARNIDENT, pdlPerson)
        assertThat(barn).isNotNull
        assertThat(barn!!.fornavn).isEqualTo(FORNAVN)
        assertThat(barn.mellomnavn).isBlank
        assertThat(barn.etternavn).isEqualTo(ETTERNAVN)
        assertThat(barn.fnr).isEqualTo(BARNIDENT)
        assertThat(barn.fodselsdato).isEqualTo(
            LocalDate.of(
                FOEDSELSDATO_BARN.year,
                FOEDSELSDATO_BARN.monthValue,
                FOEDSELSDATO_BARN.dayOfMonth,
            ),
        )
        assertThat(barn.folkeregistrertSammen).isTrue
    }

    @Test
    fun barnMedAdressebeskyttelse() {
        val pdlPerson =
            PersonDto(
                bostedsadresse = listOf(BostedsadresseDto(null, defaultVegadresse, null, null)),
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlBarn =
            BarnDto(
                adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.FORTROLIG)),
                bostedsadresse = listOf(BostedsadresseDto(null, null, null, null)),
                folkeregisterpersonstatus = listOf(FolkeregisterpersonstatusDto("ikke-doed")),
                foedsel = listOf(FoedselDto(FOEDSELSDATO_BARN)),
                navn = listOf(defaultNavnDto.copy(mellomnavn = null)),
            )
        val barn = mapper.barnDtoToDomain(pdlBarn, BARNIDENT, pdlPerson)
        assertThat(barn).isNull()
    }

    @Test
    fun barnDoed() {
        val pdlPerson =
            PersonDto(
                bostedsadresse = listOf(BostedsadresseDto(null, defaultVegadresse, null, null)),
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlBarn =
            BarnDto(
                adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
                bostedsadresse = listOf(BostedsadresseDto(null, defaultVegadresse, null, null)),
                folkeregisterpersonstatus = listOf(FolkeregisterpersonstatusDto(DOED)),
                foedsel = listOf(FoedselDto(FOEDSELSDATO_BARN)),
                navn = listOf(defaultNavnDto.copy(mellomnavn = null)),
            )
        val barn = mapper.barnDtoToDomain(pdlBarn, BARNIDENT, pdlPerson)
        assertThat(barn).isNull()
    }

    @Test
    fun barnMyndig() {
        val pdlPerson =
            PersonDto(
                bostedsadresse = listOf(BostedsadresseDto(null, defaultVegadresse, null, null)),
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlBarn =
            BarnDto(
                adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
                bostedsadresse = listOf(BostedsadresseDto(null, defaultVegadresse, null, null)),
                folkeregisterpersonstatus = listOf(FolkeregisterpersonstatusDto("ikke-doed")),
                foedsel = listOf(FoedselDto(FOEDSELSDATO_BARN_MYNDIG)),
                navn = listOf(defaultNavnDto.copy(mellomnavn = null)),
            )
        val barn = mapper.barnDtoToDomain(pdlBarn, BARNIDENT, pdlPerson)
        assertThat(barn).isNull()
    }

    @Test
    fun barnOgPersonNullAdresse() {
        val pdlPerson =
            PersonDto(
                bostedsadresse = null, // Ingen bostedsadresse
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlBarn =
            BarnDto(
                adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
                bostedsadresse = null,
                folkeregisterpersonstatus = listOf(FolkeregisterpersonstatusDto("ikke-doed")),
                foedsel = listOf(FoedselDto(FOEDSELSDATO_BARN)),
                navn = listOf(defaultNavnDto.copy(mellomnavn = null)),
            )
        val barn = mapper.barnDtoToDomain(pdlBarn, BARNIDENT, pdlPerson)
        assertThat(barn!!.folkeregistrertSammen).isFalse
    }

    @Test
    fun barnOgPersonTomAdresseliste() {
        val pdlPerson =
            PersonDto(
                bostedsadresse = emptyList(),
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlBarn =
            BarnDto(
                adressebeskyttelse = listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
                bostedsadresse = emptyList(),
                folkeregisterpersonstatus = listOf(FolkeregisterpersonstatusDto("ikke-doed")),
                foedsel = listOf(FoedselDto(FOEDSELSDATO_BARN)),
                navn = listOf(defaultNavnDto.copy(mellomnavn = null)),
            )
        val barn = mapper.barnDtoToDomain(pdlBarn, BARNIDENT, pdlPerson)
        assertThat(barn!!.folkeregistrertSammen).isFalse
    }

    @Test
    fun assertUtledingAvMyndighetErKorrekt() {
        val dagenFoerBarnBlirMyndig = LocalDate.now().minusYears(18).plusDays(1)
        val dagenBarnBlirMyndig = LocalDate.now().minusYears(18)
        val dagenEtterBarnBlirMyndig = LocalDate.now().minusYears(18).minusDays(1)
        val pdlPerson =
            PersonDto(
                bostedsadresse = emptyList(),
                oppholdsadresse = null, // Ingen oppholdsadresse
                forelderBarnRelasjon = listOf(defaultForelderBarnRelasjonDto),
                navn = listOf(defaultNavnDto),
                sivilstand = listOf(defaultSivilstandDto),
                statsborgerskap = listOf(StatsborgerskapDto(LAND)),
            )
        val pdlBarnDagenFoerBarnBlirMyndig = defaultBarn.copy(foedsel = listOf(FoedselDto(dagenFoerBarnBlirMyndig)))
        val barnDagenFoerMyndig = mapper.barnDtoToDomain(pdlBarnDagenFoerBarnBlirMyndig, BARNIDENT, pdlPerson)
        assertThat(barnDagenFoerMyndig).isNotNull

        val pdlBarnDagenBarnBlirMyndig = defaultBarn.copy(foedsel = listOf(FoedselDto(dagenBarnBlirMyndig)))
        val barnDagenBarnBlirMyndig = mapper.barnDtoToDomain(pdlBarnDagenBarnBlirMyndig, BARNIDENT, pdlPerson)
        assertThat(barnDagenBarnBlirMyndig).isNull()

        val pdlBarnDagenEtterBarnBlirMyndig = defaultBarn.copy(foedsel = listOf(FoedselDto(dagenEtterBarnBlirMyndig)))
        val barnDagenEtterMyndig = mapper.barnDtoToDomain(pdlBarnDagenEtterBarnBlirMyndig, BARNIDENT, pdlPerson)
        assertThat(barnDagenEtterMyndig).isNull()
    }

    @Test
    fun adressebeskyttelseStrengtFortrolig() {
        val pdlAdressebeskyttelse =
            PersonAdressebeskyttelseDto(
                listOf(AdressebeskyttelseDto(Gradering.STRENGT_FORTROLIG)),
            )
        val gradering = mapper.personAdressebeskyttelseDtoToGradering(pdlAdressebeskyttelse)
        assertThat(gradering).isNotNull
        assertThat(gradering).isEqualTo(Gradering.STRENGT_FORTROLIG)
    }

    @Test
    fun adressebeskyttelseStrengtUgradert() {
        val pdlAdressebeskyttelse =
            PersonAdressebeskyttelseDto(
                listOf(AdressebeskyttelseDto(Gradering.UGRADERT)),
            )
        val gradering = mapper.personAdressebeskyttelseDtoToGradering(pdlAdressebeskyttelse)
        assertThat(gradering).isNotNull
        assertThat(gradering).isEqualTo(Gradering.UGRADERT)
    }

    @Test
    fun adressebeskyttelseNull() {
        val pdlAdressebeskyttelse =
            PersonAdressebeskyttelseDto(
                listOf(AdressebeskyttelseDto(null)),
            )
        val gradering = mapper.personAdressebeskyttelseDtoToGradering(pdlAdressebeskyttelse)
        assertThat(gradering).isNull()
    }
}
