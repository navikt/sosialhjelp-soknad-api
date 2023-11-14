package no.nav.sosialhjelp.soknad.personalia.person

import no.nav.sosialhjelp.soknad.personalia.person.domain.Barn
import no.nav.sosialhjelp.soknad.personalia.person.domain.Ektefelle
import no.nav.sosialhjelp.soknad.personalia.person.domain.MapperHelper
import no.nav.sosialhjelp.soknad.personalia.person.domain.PdlDtoMapper
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandType
import org.slf4j.LoggerFactory.getLogger
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class PersonService(
    private val hentPersonClient: HentPersonClient,
    private val adressebeskyttelseService: AdressebeskyttelseService,
    private val helper: MapperHelper,
    private val mapper: PdlDtoMapper
) {

    @Cacheable(value = ["PDL-hentPerson"], key = "#ident")
    fun hentPerson(ident: String): Person? {
        val personDto = hentPersonClient.hentPerson(ident) ?: return null
        val person = mapper.personDtoToDomain(personDto, ident)

        if (!personDto.sivilstand.isNullOrEmpty()) person.ektefelle = hentEktefelle(personDto)
        println(person)
        return person
    }

    @Cacheable(value = ["PDL-hentBarnForPerson"], key = "#ident")
    fun hentBarnForPerson(ident: String): List<Barn>? {
        val personDto = hentPersonClient.hentPerson(ident)
        if (personDto?.forelderBarnRelasjon == null) {
            return null
        }
        return personDto.forelderBarnRelasjon
            .filter { it.relatertPersonsRolle.equals(BARN, ignoreCase = true) }
            .map {
                if (it.relatertPersonsIdent.isNullOrEmpty()) {
                    log.info("ForelderBarnRelasjon.relatertPersonsIdent (barnIdent) er null -> kaller ikke hentPerson for barn")
                    return null
                }
                if (erFDAT(it.relatertPersonsIdent)) {
                    log.info("ForelderBarnRelasjon.relatertPersonsIdent (barnIdent) er FDAT -> kaller ikke hentPerson for barn")
                    return null
                }
                loggHvisIdentIkkeErFnr(it.relatertPersonsIdent)
                val pdlBarn = hentPersonClient.hentBarn(it.relatertPersonsIdent)
                mapper.barnDtoToDomain(pdlBarn, it.relatertPersonsIdent, personDto)
            }
            .filterNotNull()
    }

    private fun hentEktefelle(personDto: PersonDto): Ektefelle? {
        val sivilstand = helper.utledGjeldendeSivilstand(personDto.sivilstand) ?: return null
        val ektefelleIdent = sivilstand.relatertVedSivilstand ?: return null

        if (!listOf(SivilstandType.GIFT, SivilstandType.REGISTRERT_PARTNER).contains(sivilstand.type)) return null

        return when {
            adressebeskyttelseService.harAdressebeskyttelse(ektefelleIdent) -> Ektefelle(ikkeTilgangTilEktefelle = true)

            erFDAT(ektefelleIdent) -> {
                // FDAT brukes ikke lenger i PDL
                log.warn("Sivilstand.relatertVedSivilstand (ektefelleIdent) er FDAT -> kaller ikke hentPerson for ektefelle")
                null
            }

            else -> {
                loggHvisIdentIkkeErFnr(ektefelleIdent)
                val ektefelleDto = hentPersonClient.hentEktefelle(ektefelleIdent)
                mapper.ektefelleDtoToDomain(ektefelleDto, ektefelleIdent, personDto)
            }
        }
    }

    private fun erFDAT(ident: String): Boolean {
        return ident.length == 11 && ident.substring(6).equals("00000", ignoreCase = true)
    }

    private fun loggHvisIdentIkkeErFnr(ektefelleIdent: String) {
        if (ektefelleIdent.length == 11 && ektefelleIdent.substring(0, 2).toInt() > 31) {
            log.info("Ident er DNR")
        }
        if (ektefelleIdent.length == 11 && ektefelleIdent.substring(2, 4).toInt() >= 21 && ektefelleIdent.substring(2, 4).toInt() <= 32) {
            log.info("Ident er NPID")
        }
        if (ektefelleIdent.length > 11) {
            log.info("Ident er aktørid")
        }
        if (ektefelleIdent.length < 11) {
            log.info("Ident er ukjent (mindre enn 11 tegn)")
        }
    }

    companion object {
        private val log = getLogger(PersonService::class.java)

        private const val BARN = "BARN"
    }
}
