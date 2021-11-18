package no.nav.sosialhjelp.soknad.person

import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlHentPersonConsumer
import no.nav.sosialhjelp.soknad.domain.model.NavFodselsnummer
import no.nav.sosialhjelp.soknad.person.domain.Barn
import no.nav.sosialhjelp.soknad.person.domain.Ektefelle
import no.nav.sosialhjelp.soknad.person.domain.MapperHelper
import no.nav.sosialhjelp.soknad.person.domain.PdlDtoMapper
import no.nav.sosialhjelp.soknad.person.domain.Person
import no.nav.sosialhjelp.soknad.person.dto.Gradering
import no.nav.sosialhjelp.soknad.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.person.dto.SivilstandType
import org.slf4j.LoggerFactory.getLogger
import java.time.LocalDate

open class PersonService(
    private val pdlHentPersonConsumer: PdlHentPersonConsumer,
    private val helper: MapperHelper,
    private val mapper: PdlDtoMapper
) {

    open fun hentPerson(ident: String): Person? {
        val personDto = pdlHentPersonConsumer.hentPerson(ident) ?: return null
        val person = mapper.personDtoToDomain(personDto, ident)
        if (person != null) {
            person.ektefelle = hentEktefelle(personDto)
        }
        return person
    }

    open fun hentBarnForPerson(ident: String?): List<Barn>? {
        val personDto = pdlHentPersonConsumer.hentPerson(ident)
        if (personDto == null || personDto.forelderBarnRelasjon == null) {
            return null
        }
        return personDto.forelderBarnRelasjon
            .filter { it.relatertPersonsRolle.equals(BARN, ignoreCase = true) }
            .map {
                if (it.relatertPersonsIdent.isEmpty()) {
                    log.info("ForelderBarnRelasjon.relatertPersonsIdent (barnIdent) er null -> kaller ikke hentPerson for barn")
                    return null
                }
                if (erFDAT(it.relatertPersonsIdent)) {
                    log.info("ForelderBarnRelasjon.relatertPersonsIdent (barnIdent) er FDAT -> kaller ikke hentPerson for barn")
                    return null
                }
                loggHvisIdentIkkeErFnr(it.relatertPersonsIdent)
                val pdlBarn = pdlHentPersonConsumer.hentBarn(it.relatertPersonsIdent)
                mapper.barnDtoToDomain(pdlBarn, it.relatertPersonsIdent, personDto)
            }
            .filterNotNull()
    }

    private fun hentEktefelle(personDto: PersonDto?): Ektefelle? {
        if (personDto != null && personDto.sivilstand != null && !personDto.sivilstand.isEmpty()) {
            val sivilstand = helper.utledGjeldendeSivilstand(personDto.sivilstand)
            if (sivilstand != null && (SivilstandType.GIFT === sivilstand.type || SivilstandType.REGISTRERT_PARTNER === sivilstand.type)) {
                val ektefelleIdent = sivilstand.relatertVedSivilstand
                if (ektefelleIdent == null || ektefelleIdent.isEmpty()) {
                    log.info("Sivilstand.relatertVedSivilstand (ektefelleIdent) er null -> kaller ikke hentPerson for ektefelle")
                    return null
                }
                if (erFDAT(ektefelleIdent)) {
                    log.info("Sivilstand.relatertVedSivilstand (ektefelleIdent) er FDAT -> kaller ikke hentPerson for ektefelle")
                    return Ektefelle("", "", "", finnFodselsdatoFraFnr(ektefelleIdent), ektefelleIdent, false, false)
                }
                loggHvisIdentIkkeErFnr(ektefelleIdent)
                val ektefelleDto = pdlHentPersonConsumer.hentEktefelle(ektefelleIdent)
                return mapper.ektefelleDtoToDomain(ektefelleDto, ektefelleIdent, personDto)
            }
        }
        return null
    }

    open fun hentAdressebeskyttelse(ident: String?): Gradering? {
        val personAdressebeskyttelseDto = pdlHentPersonConsumer.hentAdressebeskyttelse(ident)
        return mapper.personAdressebeskyttelseDtoToGradering(personAdressebeskyttelseDto)
    }

    private fun erFDAT(ident: String): Boolean {
        return ident.length == 11 && ident.substring(6).equals("00000", ignoreCase = true)
    }

    private fun finnFodselsdatoFraFnr(ident: String): LocalDate? {
        val fnr = NavFodselsnummer(ident)
        return LocalDate.parse(fnr.birthYear + "-" + fnr.month + "-" + fnr.dayInMonth)
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
