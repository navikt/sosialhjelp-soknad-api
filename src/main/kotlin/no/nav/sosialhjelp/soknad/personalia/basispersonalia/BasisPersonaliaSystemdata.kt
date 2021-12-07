package no.nav.sosialhjelp.soknad.personalia.basispersonalia

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonNordiskBorger
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonIdentifikator
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonSokernavn
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap
import no.nav.sosialhjelp.soknad.business.service.soknadservice.Systemdata
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person

class BasisPersonaliaSystemdata(
    private val personService: PersonService
) : Systemdata {

    override fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid) {
        val personalia = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
        val personIdentifikator = personalia.personIdentifikator.verdi
        val systemPersonalia = innhentSystemBasisPersonalia(personIdentifikator) ?: return

        personalia.navn = systemPersonalia.navn
        personalia.statsborgerskap = systemPersonalia.statsborgerskap
        personalia.nordiskBorger = systemPersonalia.nordiskBorger
    }

    fun innhentSystemBasisPersonalia(personIdentifikator: String?): JsonPersonalia? {
        val person = personService.hentPerson(personIdentifikator!!) ?: return null
        return mapToJsonPersonalia(person)
    }

    private fun mapToJsonPersonalia(person: Person): JsonPersonalia {
        return JsonPersonalia()
            .withPersonIdentifikator(mapToJsonPersonIdentifikator(person))
            .withNavn(mapToJsonSokernavn(person))
            .withStatsborgerskap(mapToJsonStatsborgerskap(person))
            .withNordiskBorger(mapToJsonNordiskBorger(person))
    }

    private fun mapToJsonPersonIdentifikator(person: Person): JsonPersonIdentifikator {
        return JsonPersonIdentifikator()
            .withKilde(JsonPersonIdentifikator.Kilde.SYSTEM)
            .withVerdi(person.fnr)
    }

    private fun mapToJsonSokernavn(person: Person): JsonSokernavn {
        return JsonSokernavn()
            .withKilde(JsonSokernavn.Kilde.SYSTEM)
            .withFornavn(person.fornavn ?: "")
            .withMellomnavn(person.mellomnavn ?: "")
            .withEtternavn(person.etternavn ?: "")
    }

    private fun mapToJsonStatsborgerskap(person: Person): JsonStatsborgerskap? {
        val statsborgerskap = prioritertStatsborgerskap(person)
        return if (statsborgerskap == null || statsborgerskap == "???" || statsborgerskap == PDL_UKJENT_STATSBORGERSKAP || statsborgerskap == PDL_STATSLOS) {
            null
        } else JsonStatsborgerskap()
            .withKilde(JsonKilde.SYSTEM)
            .withVerdi(statsborgerskap)
    }

    private fun mapToJsonNordiskBorger(person: Person): JsonNordiskBorger? {
        val nordiskBorger = erNordiskBorger(prioritertStatsborgerskap(person))
            ?: return null
        return JsonNordiskBorger()
            .withKilde(JsonKilde.SYSTEM)
            .withVerdi(nordiskBorger)
    }

    private fun prioritertStatsborgerskap(person: Person): String? {
        val list = person.statsborgerskap
        if (list!!.isEmpty()) {
            return null
        }
        if (list.contains(NOR)) {
            return NOR
        }
        if (list.contains(SWE)) {
            return SWE
        }
        if (list.contains(FRO)) {
            return FRO
        }
        if (list.contains(ISL)) {
            return ISL
        }
        if (list.contains(DNK)) {
            return DNK
        }
        return if (list.contains(FIN)) {
            FIN
        } else list[0]
    }

    companion object {
        private const val NOR = "NOR"
        private const val SWE = "SWE"
        private const val FRO = "FRO"
        private const val ISL = "ISL"
        private const val DNK = "DNK"
        private const val FIN = "FIN"

        const val PDL_UKJENT_STATSBORGERSKAP = "XUK"
        const val PDL_STATSLOS = "XXX"

        fun erNordiskBorger(statsborgerskap: String?): Boolean? {
            return if (statsborgerskap == null || statsborgerskap == "???" || statsborgerskap == PDL_UKJENT_STATSBORGERSKAP || statsborgerskap == PDL_STATSLOS) {
                null
            } else when (statsborgerskap) {
                NOR, SWE, FRO, ISL, DNK, FIN -> true
                else -> false
            }
        }
    }
}