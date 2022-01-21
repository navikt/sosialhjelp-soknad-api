package no.nav.sosialhjelp.soknad.personalia.person.domain

import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.personalia.person.dto.AdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.BostedsadresseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.FoedselDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.FolkeregisterpersonstatusDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import no.nav.sosialhjelp.soknad.personalia.person.dto.KontaktadresseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.MatrikkeladresseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.NavnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.OppholdsadresseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandType
import no.nav.sosialhjelp.soknad.personalia.person.dto.StatsborgerskapDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.VegadresseDto
import java.time.LocalDate
import java.time.Period

open class PdlDtoMapper(
    private val kodeverkService: KodeverkService,
    private val helper: MapperHelper
) {

    companion object {
        const val NOR = "NOR"
        const val DOED = "DOED"

        private val MAP_PDLSIVILSTAND_TIL_JSONSIVILSTATUS: Map<SivilstandType, String> =
            mapOf(
                SivilstandType.UOPPGITT to "",
                SivilstandType.UGIFT to "ugift",
                SivilstandType.GIFT to "gift",
                SivilstandType.ENKE_ELLER_ENKEMANN to "enke",
                SivilstandType.SKILT to "skilt",
                SivilstandType.SEPARERT to "separert",
                SivilstandType.REGISTRERT_PARTNER to "gift",
                SivilstandType.SEPARERT_PARTNER to "separert",
                SivilstandType.SKILT_PARTNER to "skilt",
                SivilstandType.GJENLEVENDE_PARTNER to "enke"
            )
    }

    open fun personDtoToDomain(personDto: PersonDto?, ident: String): Person? {
        return if (personDto == null) {
            null
        } else Person(
            findFornavn(personDto.navn),
            findMellomnavn(personDto.navn),
            findEtternavn(personDto.navn),
            ident,
            findSivilstatus(personDto.sivilstand),
            findStatsborgerskap(personDto.statsborgerskap),
            null,
            mapToBostedsadresse(personDto.bostedsadresse),
            mapToOppholdssadresse(personDto.oppholdsadresse, personDto.bostedsadresse),
            mapToKontaktadresse(personDto.kontaktadresse, personDto.bostedsadresse)
        )
    }

    open fun barnDtoToDomain(barnDto: BarnDto?, barnIdent: String, personDto: PersonDto): Barn? {
        if (barnDto == null || hasAdressebeskyttelse(barnDto.adressebeskyttelse) || isMyndig(barnDto.foedsel) || isDoed(barnDto.folkeregisterpersonstatus)) {
            return null
        }
        return Barn(
            findFornavn(barnDto.navn),
            findMellomnavn(barnDto.navn),
            findEtternavn(barnDto.navn),
            barnIdent,
            findFodselsdato(barnDto.foedsel),
            isFolkeregistrertSammen(personDto.bostedsadresse, barnDto.bostedsadresse)
        )
    }

    open fun ektefelleDtoToDomain(ektefelleDto: EktefelleDto?, ektefelleIdent: String, personDto: PersonDto): Ektefelle? {
        if (ektefelleDto == null) {
            return null
        }
        return if (hasAdressebeskyttelse(ektefelleDto.adressebeskyttelse)) {
            Ektefelle(true)
        } else Ektefelle(
            findFornavn(ektefelleDto.navn),
            findMellomnavn(ektefelleDto.navn),
            findEtternavn(ektefelleDto.navn),
            findFodselsdato(ektefelleDto.foedsel),
            ektefelleIdent,
            isFolkeregistrertSammen(personDto.bostedsadresse, ektefelleDto.bostedsadresse),
            false
        )
    }

    open fun personAdressebeskyttelseDtoToGradering(personAdressebeskyttelseDto: PersonAdressebeskyttelseDto?): Gradering? {
        return if (personAdressebeskyttelseDto?.adressebeskyttelse == null) {
            null
        } else personAdressebeskyttelseDto.adressebeskyttelse.firstOrNull()?.gradering
    }

    private fun findFornavn(navn: List<NavnDto>?): String {
        return helper.utledGjeldendeNavn(navn)?.fornavn?.uppercase() ?: ""
    }

    private fun findMellomnavn(navn: List<NavnDto>?): String {
        return helper.utledGjeldendeNavn(navn)?.mellomnavn?.uppercase() ?: ""
    }

    private fun findEtternavn(navn: List<NavnDto>?): String {
        return helper.utledGjeldendeNavn(navn)?.etternavn?.uppercase() ?: ""
    }

    private fun findFodselsdato(foedsel: List<FoedselDto>?): LocalDate? {
        return foedsel
            ?.firstOrNull()
            ?.let { LocalDate.of(it.foedselsdato.year, it.foedselsdato.monthValue, it.foedselsdato.dayOfMonth) }
    }

    private fun isMyndig(foedsel: List<FoedselDto>?): Boolean {
        return findAlder(foedsel) >= 18
    }

    private fun findAlder(foedsel: List<FoedselDto>?): Int {
        val foedselsdato = findFodselsdato(foedsel) ?: return 0
        return Period.between(foedselsdato, LocalDate.now()).years
    }

    private fun isDoed(folkeregisterpersonstatus: List<FolkeregisterpersonstatusDto>?): Boolean {
        return folkeregisterpersonstatus?.firstOrNull()?.let { DOED.equals(it.status, ignoreCase = true) } ?: false
    }

    private fun findSivilstatus(sivilstand: List<SivilstandDto>?): String? {
        val sivilstandDto = helper.utledGjeldendeSivilstand(sivilstand)
        return if (sivilstandDto != null) MAP_PDLSIVILSTAND_TIL_JSONSIVILSTATUS[sivilstandDto.type] else ""
    }

    private fun findStatsborgerskap(statsborgerskap: List<StatsborgerskapDto>?): List<String> {
        val list = statsborgerskap?.map { it.land } ?: emptyList()
        return list.ifEmpty { listOf(NOR) }
    }

    private fun hasAdressebeskyttelse(adressebeskyttelse: List<AdressebeskyttelseDto>?): Boolean {
        return adressebeskyttelse != null &&
            adressebeskyttelse.isNotEmpty() &&
            !adressebeskyttelse.all { Gradering.UGRADERT == it.gradering }
    }

    private fun isFolkeregistrertSammen(
        personBostedsadresse: List<BostedsadresseDto>?,
        barnEllerEktefelleBostedsadresse: List<BostedsadresseDto>?
    ): Boolean {
        val bostedsadressePerson = findBostedsadresse(personBostedsadresse)
        val bostedsadresseBarnEllerEktefelle = findBostedsadresse(barnEllerEktefelleBostedsadresse)
        if (bostedsadressePerson == null && bostedsadresseBarnEllerEktefelle == null) {
            return false
        }
        // Hvis person og barnEllerEktefelle har bostedsadresse med lik matrikkelId - betyr det at de er registrert som bosatt på samme adresse.
        val matrikkelIdPerson = getMatrikkelId(bostedsadressePerson)
        val matrikkelIdBarnEllerEktefelle = getMatrikkelId(bostedsadresseBarnEllerEktefelle)
        if (matrikkelIdPerson != null && matrikkelIdBarnEllerEktefelle != null) {
            return matrikkelIdPerson == matrikkelIdBarnEllerEktefelle
        }

        // Hvis ikke vegadresse til person eller barnEllerEktefelle har matrikkelId, sammenlign resterende vegadresse-felter
        return if (bostedsadressePerson?.vegadresse != null && bostedsadresseBarnEllerEktefelle?.vegadresse != null) {
            isEqualVegadresser(bostedsadressePerson.vegadresse, bostedsadresseBarnEllerEktefelle.vegadresse)
        } else false
    }

    private fun findBostedsadresse(bostedsadresse: List<BostedsadresseDto>?): BostedsadresseDto? {
        return if (bostedsadresse == null || bostedsadresse.isEmpty()) {
            null
        } else bostedsadresse
            .firstOrNull { it.ukjentBosted == null && (it.vegadresse != null || it.matrikkeladresse != null) }
    }

    private fun getMatrikkelId(bostedsadresseDto: BostedsadresseDto?): String? {
        return when {
            hasVegadresseMatrikkelId(bostedsadresseDto) -> bostedsadresseDto?.vegadresse?.matrikkelId
            hasMatrikkeladresseMatrikkelId(bostedsadresseDto) -> bostedsadresseDto?.matrikkeladresse?.matrikkelId
            else -> null
        }
    }

    private fun hasVegadresseMatrikkelId(bostedsadresseDto: BostedsadresseDto?): Boolean {
        return bostedsadresseDto?.vegadresse?.matrikkelId != null
    }

    private fun hasMatrikkeladresseMatrikkelId(bostedsadresseDto: BostedsadresseDto?): Boolean {
        return bostedsadresseDto?.matrikkeladresse?.matrikkelId != null
    }

    private fun isEqualVegadresser(adr1: VegadresseDto, adr2: VegadresseDto): Boolean {
        return (
            adr1.adressenavn == adr2.adressenavn &&
                adr1.husnummer == adr2.husnummer &&
                adr1.husbokstav == adr2.husbokstav &&
                adr1.tilleggsnavn == adr2.tilleggsnavn &&
                adr1.postnummer == adr2.postnummer &&
                adr1.kommunenummer == adr2.kommunenummer &&
                adr1.bruksenhetsnummer == adr2.bruksenhetsnummer &&
                adr1.bydelsnummer == adr2.bydelsnummer
            )
    }

    private fun isEqualVegadresserWithoutKommunenummer(adr1: VegadresseDto, adr2: VegadresseDto): Boolean {
        return (
            adr1.adressenavn == adr2.adressenavn &&
                adr1.husnummer == adr2.husnummer &&
                adr1.husbokstav == adr2.husbokstav &&
                adr1.tilleggsnavn == adr2.tilleggsnavn &&
                adr1.postnummer == adr2.postnummer &&
                adr1.bruksenhetsnummer == adr2.bruksenhetsnummer
            )
    }

    private fun mapToBostedsadresse(dtos: List<BostedsadresseDto>?): Bostedsadresse? {
        return findBostedsadresse(dtos)
            ?.let {
                Bostedsadresse(
                    it.coAdressenavn,
                    it.vegadresse?.let { adr -> mapToVegadresse(adr) },
                    it.matrikkeladresse?.let { adr -> mapToMatrikkeladresse(adr) }
                )
            } ?: return null
    }

    private fun mapToOppholdssadresse(
        dtos: List<OppholdsadresseDto>?,
        bostedsadresseDtos: List<BostedsadresseDto>?
    ): Oppholdsadresse? {
        if (dtos == null || dtos.isEmpty()) {
            return null
        }
        // vi er kun interessert i norske oppholdsadresser med en faktisk adresse.
        //  Fra pdl-doc:
        //  Man kan ha en oppholdsadresse med Freg som master og en med PDL som master.
        //  Flertallet av oppholdsadressene fra Freg vil være norske, og flertallet av oppholdsadresser registrert av NAV vil være utenlandske.
        //  Fra folkeregisteret kan man også få oppholdsadresse uten en faktisk adresse, men med informasjon i oppholdAnnetSted.
        return dtos
            .filter { it.vegadresse != null }
            .filter { filterVegadresseNotEqualToBostedsadresse(bostedsadresseDtos, it.vegadresse!!) }
            .firstOrNull()
            ?.let { Oppholdsadresse(it.coAdressenavn, mapToVegadresse(it.vegadresse!!)) }
    }

    private fun mapToKontaktadresse(
        dtos: List<KontaktadresseDto>?,
        bostedsadresseDtos: List<BostedsadresseDto>?
    ): Kontaktadresse? {
        return if (dtos == null || dtos.isEmpty()) {
            null
        } else
            dtos
                .filter { it.vegadresse != null }
                .filter { filterVegadresseNotEqualToBostedsadresse(bostedsadresseDtos, it.vegadresse!!) }
                .firstOrNull()
                ?.let { Kontaktadresse(it.coAdressenavn, mapToVegadresse(it.vegadresse!!)) }
    }

    private fun filterVegadresseNotEqualToBostedsadresse(
        bostedsadresseDtos: List<BostedsadresseDto>?,
        dtoVegadresse: VegadresseDto
    ): Boolean {
        val bostedsadresseDto: BostedsadresseDto? = findBostedsadresse(bostedsadresseDtos)
        if (bostedsadresseDto?.vegadresse != null) {
            if (dtoVegadresse.kommunenummer != null) {
                return !isEqualVegadresser(dtoVegadresse, bostedsadresseDto.vegadresse)
            }
            return !isEqualVegadresserWithoutKommunenummer(dtoVegadresse, bostedsadresseDto.vegadresse)
        }
        return false
    }

    private fun mapToVegadresse(dto: VegadresseDto): Vegadresse {
        return Vegadresse(
            dto.adressenavn?.uppercase(),
            dto.husnummer,
            dto.husbokstav,
            dto.tilleggsnavn,
            dto.postnummer,
            getPoststed(dto.postnummer),
            dto.kommunenummer,
            dto.bruksenhetsnummer,
            dto.bydelsnummer
        )
    }

    private fun getPoststed(postnummer: String): String? {
        val poststed = kodeverkService.getPoststed(postnummer)
        return poststed?.uppercase()
    }

    private fun mapToMatrikkeladresse(dto: MatrikkeladresseDto): Matrikkeladresse {
        return Matrikkeladresse(
            dto.matrikkelId,
            dto.postnummer,
            getPoststed(dto.postnummer),
            dto.tilleggsnavn,
            dto.kommunenummer,
            dto.bruksenhetsnummer
        )
    }
}
