package no.nav.sosialhjelp.soknad.personalia.person

import no.nav.sosialhjelp.soknad.app.config.SoknadApiCacheConfig
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getToken
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils.getUserIdFromToken
import no.nav.sosialhjelp.soknad.personalia.person.domain.Barn
import no.nav.sosialhjelp.soknad.personalia.person.domain.Ektefelle
import no.nav.sosialhjelp.soknad.personalia.person.domain.MapperHelper
import no.nav.sosialhjelp.soknad.personalia.person.domain.PdlDtoMapper
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandType
import no.nav.sosialhjelp.soknad.v2.register.currentUserContext
import org.slf4j.LoggerFactory.getLogger
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class PersonService(
    private val hentPersonClient: HentPersonClient,
    private val mapper: PdlDtoMapper,
) {
    suspend fun hentPerson(
        hentEktefelle: Boolean = true,
    ): Person? {
        val personDto =
            hentPersonClient.hentPerson(currentUserContext().userId, currentUserContext().userToken)
                ?: return null
        val person = mapper.personDtoToDomain(personDto, currentUserContext().userId)
        if (person != null && hentEktefelle) {
            person.ektefelle = hentEktefelle(personDto)
        }
        return person
    }

    @Cacheable(AdressebeskyttelseCacheConfig.CACHE_NAME, unless = "#result == true")
    fun hasAdressebeskyttelse(ident: String): Boolean = hasGradering(ident)

    @CacheEvict(AdressebeskyttelseCacheConfig.CACHE_NAME, key = "#ident")
    fun onSendSoknadHasAdressebeskyttelse(ident: String): Boolean = hasGradering(ident)

    private fun hasGradering(ident: String): Boolean =
        hentPersonClient
            .hentAdressebeskyttelse(ident, getToken())
            .let { dto -> mapper.personAdressebeskyttelseDtoToGradering(dto) }
            .isGradert()

    fun hentBarnForPerson(): List<Barn>? {
        // TODO Ikke nødvendig å hente person igjen -> refaktor
        val personDto = hentPersonClient.hentPerson(getUserIdFromToken(), getToken())
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

    private fun hentEktefelle(personDto: PersonDto?): Ektefelle? {
        if (personDto?.sivilstand != null && personDto.sivilstand.isNotEmpty()) {
            val sivilstand = MapperHelper.utledGjeldendeSivilstand(personDto.sivilstand)
            if (sivilstand != null && (SivilstandType.GIFT === sivilstand.type || SivilstandType.REGISTRERT_PARTNER === sivilstand.type)) {
                val ektefelleIdent = sivilstand.relatertVedSivilstand
                if (ektefelleIdent.isNullOrEmpty()) {
                    log.info("Sivilstand.relatertVedSivilstand (ektefelleIdent) er null -> kaller ikke hentPerson for ektefelle")
                    return null
                }
                if (erFDAT(ektefelleIdent)) {
                    log.info("Sivilstand.relatertVedSivilstand (ektefelleIdent) er FDAT -> kaller ikke hentPerson for ektefelle")
                    return null
                }
                loggHvisIdentIkkeErFnr(ektefelleIdent)
                val ektefelleDto = hentPersonClient.hentEktefelle(ektefelleIdent)
                return mapper.ektefelleDtoToDomain(ektefelleDto, ektefelleIdent, personDto)
            }
        }
        return null
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

private fun Gradering?.isGradert() = this?.let { Gradering.isGradert(it) } ?: false

@Configuration
class AdressebeskyttelseCacheConfig : SoknadApiCacheConfig(CACHE_NAME, EN_HALVTIME) {
    companion object {
        const val CACHE_NAME = "adressebeskyttelse"
        private val EN_HALVTIME = Duration.ofMinutes(30)
    }
}
