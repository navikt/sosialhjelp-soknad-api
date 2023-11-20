package no.nav.sosialhjelp.soknad.personalia.familie

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.app.systemdata.Systemdata
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Barn
import no.nav.sosialhjelp.soknad.personalia.person.domain.Ektefelle
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import org.apache.commons.lang3.StringUtils.isEmpty
import org.springframework.stereotype.Component

@Component
class FamilieSystemdata(
    private val personService: PersonService
) : Systemdata {

    override fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid) {
        val jsonData = soknadUnderArbeid.jsonInternalSoknad?.soknad?.data ?: return
        val personIdentifikator = jsonData.personalia.personIdentifikator.verdi
        val familie = jsonData.familie
        val systemverdiSivilstatus = innhentSystemverdiSivilstatus(personIdentifikator)

        if (systemverdiSivilstatus != null || familie.sivilstatus == null || familie.sivilstatus.kilde == JsonKilde.SYSTEM) {
            familie.sivilstatus = systemverdiSivilstatus
        }

        val forsorgerplikt = familie.forsorgerplikt
        val harForsorgerplikt = forsorgerplikt.harForsorgerplikt
        val systemverdiForsorgerplikt = innhentSystemverdiForsorgerplikt(personIdentifikator)

        if (systemverdiForsorgerplikt.harForsorgerplikt.verdi) {
            forsorgerplikt.harForsorgerplikt = systemverdiForsorgerplikt.harForsorgerplikt

            val ansvarList = forsorgerplikt.ansvar?.toMutableList()
            if (!ansvarList.isNullOrEmpty()) {
                ansvarList.removeIf { it.barn.kilde == JsonKilde.SYSTEM && isNotInList(it, systemverdiForsorgerplikt.ansvar) }
                ansvarList.addAll(
                    systemverdiForsorgerplikt.ansvar.filter { isNotInList(it, forsorgerplikt.ansvar) }
                )
            } else {
                forsorgerplikt.ansvar = systemverdiForsorgerplikt.ansvar
            }
        } else if (harForsorgerplikt == null || harForsorgerplikt.kilde == JsonKilde.SYSTEM || !harForsorgerplikt.verdi) {
            forsorgerplikt.harForsorgerplikt = systemverdiForsorgerplikt.harForsorgerplikt
            forsorgerplikt.barnebidrag = null
            forsorgerplikt.ansvar = ArrayList()
        }
    }

    private fun isNotInList(jsonAnsvar: JsonAnsvar, jsonAnsvarList: List<JsonAnsvar>): Boolean {
        return jsonAnsvarList.none {
            checkNotNull(it.barn) { "JsonAnsvar mangler barn. Ikke mulig å skille fra andre barn" }
            return if (it.barn.personIdentifikator != null) {
                it.barn.personIdentifikator == jsonAnsvar.barn.personIdentifikator
            } else {
                it.barn.navn == jsonAnsvar.barn.navn
            }
        }
    }

    private fun innhentSystemverdiSivilstatus(personIdentifikator: String): JsonSivilstatus? {
        val person = personService.hentPerson(personIdentifikator)
        if (person == null || isEmpty(person.sivilstatus)) {
            return null
        }
        val ektefelle = person.ektefelle
        val status = JsonSivilstatus.Status.fromValue(person.sivilstatus)
        return if (JsonSivilstatus.Status.GIFT != status || ektefelle == null) {
            null
        } else JsonSivilstatus()
            .withKilde(JsonKilde.SYSTEM)
            .withStatus(status)
            .withEktefelle(tilSystemregistrertJsonEktefelle(ektefelle))
            .withEktefelleHarDiskresjonskode(ektefelle.ikkeTilgangTilEktefelle)
            .withFolkeregistrertMedEktefelle(ektefelle.folkeregistrertSammen)
    }

    private fun innhentSystemverdiForsorgerplikt(personIdentifikator: String): JsonForsorgerplikt {
        val jsonForsorgerplikt = JsonForsorgerplikt().withHarForsorgerplikt(JsonHarForsorgerplikt())
        val barn = personService.hentBarnForPerson(personIdentifikator)
        if (barn.isNullOrEmpty()) {
            jsonForsorgerplikt.harForsorgerplikt
                .withKilde(JsonKilde.SYSTEM)
                .withVerdi(false)
            return jsonForsorgerplikt
        }
        jsonForsorgerplikt.harForsorgerplikt
            .withKilde(JsonKilde.SYSTEM)
            .withVerdi(true)
        jsonForsorgerplikt.ansvar = barn.map { mapToJsonAnsvar(it) }
        return jsonForsorgerplikt
    }

    private fun mapToJsonAnsvar(barn: Barn): JsonAnsvar {
        return JsonAnsvar()
            .withBarn(
                JsonBarn()
                    .withKilde(JsonKilde.SYSTEM)
                    .withNavn(
                        JsonNavn()
                            .withFornavn(barn.fornavn)
                            .withMellomnavn(barn.mellomnavn)
                            .withEtternavn(barn.etternavn)
                    )
                    .withFodselsdato(barn.fodselsdato?.toString())
                    .withPersonIdentifikator(barn.fnr)
                    .withHarDiskresjonskode(false)
            )
            .withErFolkeregistrertSammen(
                JsonErFolkeregistrertSammen()
                    .withKilde(JsonKildeSystem.SYSTEM)
                    .withVerdi(barn.folkeregistrertSammen)
            )
    }

    companion object {
        private fun tilSystemregistrertJsonEktefelle(ektefelle: Ektefelle?): JsonEktefelle {
            return if (ektefelle == null || ektefelle.ikkeTilgangTilEktefelle) {
                JsonEktefelle().withNavn(
                    JsonNavn()
                        .withFornavn("")
                        .withMellomnavn("")
                        .withEtternavn("")
                )
            } else JsonEktefelle()
                .withNavn(mapToJsonNavn(ektefelle))
                .withFodselsdato(ektefelle.fodselsdato?.toString())
                .withPersonIdentifikator(ektefelle.fnr)
        }

        private fun mapToJsonNavn(ektefelle: Ektefelle): JsonNavn {
            return JsonNavn()
                .withFornavn(ektefelle.fornavn ?: "")
                .withMellomnavn(ektefelle.mellomnavn ?: "")
                .withEtternavn(ektefelle.etternavn ?: "")
        }
    }
}
