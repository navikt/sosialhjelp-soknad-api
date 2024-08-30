package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.v2.familie.Barn
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle
import no.nav.sosialhjelp.soknad.v2.familie.Familie
import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class FamilieToJsonMapper(
    private val familieRepository: FamilieRepository,
) : DomainToJsonMapper {
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ) {
        familieRepository.findByIdOrNull(soknadId)?.let {
            doMapping(it, jsonInternalSoknad)
        }
    }

    internal companion object Mapper {
        fun doMapping(
            familie: Familie,
            json: JsonInternalSoknad,
        ) {
            json.initializeObjects()

            with(json.soknad.data.familie) {
                if (familie.ektefelle != null && familie.sivilstatus != null) {
                    sivilstatus = familie.toJsonSivilstatus()
                }

                forsorgerplikt = familie.toJsonForsorgerplikt()
            }
        }
    }
}

private fun JsonInternalSoknad.initializeObjects() {
    soknad.data.familie ?: soknad.data.withFamilie(JsonFamilie())
    // required i json-modellen uavhengig av om vi har data
    soknad.data.familie.forsorgerplikt
        ?: soknad.data.familie.withForsorgerplikt(JsonForsorgerplikt())
}

private fun Familie.toJsonSivilstatus() =
    JsonSivilstatus()
        .withKilde(ektefelle?.toJsonKilde() ?: JsonKilde.SYSTEM)
        // required i json-modellen
        .withStatus(sivilstatus?.toJson() ?: JsonSivilstatus.Status.UGIFT)
        .withEktefelle(ektefelle?.toJson())
        .withBorSammenMed(ektefelle?.borSammen)
        .withFolkeregistrertMedEktefelle(ektefelle?.folkeregistrertMedEktefelle)

private fun Ektefelle.toJsonKilde() = if (kildeErSystem) JsonKilde.SYSTEM else JsonKilde.BRUKER

private fun Sivilstatus.toJson() = JsonSivilstatus.Status.valueOf(name)

private fun Ektefelle.toJson() =
    JsonEktefelle()
        .withNavn(navn?.toJson())
        .withFodselsdato(fodselsdato)
        .withPersonIdentifikator(personId)

private fun Familie.toJsonForsorgerplikt() =
    JsonForsorgerplikt()
        .withHarForsorgerplikt(
            JsonHarForsorgerplikt()
                .withKilde(JsonKilde.SYSTEM)
                .withVerdi(harForsorgerplikt),
        ).withBarnebidrag(
            JsonBarnebidrag()
                .withKilde(JsonKildeBruker.BRUKER)
                .withVerdi(barnebidrag?.toJson()),
        ).withAnsvar(ansvar.values.toJson())

private fun Barnebidrag.toJson() = JsonBarnebidrag.Verdi.valueOf(name)

private fun Barn.toJson() =
    JsonAnsvar()
        .withBarn(
            JsonBarn()
                .withKilde(JsonKilde.SYSTEM)
                .withFodselsdato(fodselsdato)
                .withNavn(navn?.toJson())
                .withPersonIdentifikator(personId)
                .withHarDiskresjonskode(false),
        ).withErFolkeregistrertSammen(
            JsonErFolkeregistrertSammen()
                .withKilde(JsonKildeSystem.SYSTEM)
                .withVerdi(folkeregistrertSammen),
        ).withHarDeltBosted(
            JsonHarDeltBosted()
                .withKilde(JsonKildeBruker.BRUKER)
                .withVerdi(deltBosted ?: false),
        )

// mellomnavn er required i json-modellen
fun Navn.toJson() = JsonNavn().withFornavn(fornavn).withMellomnavn(mellomnavn ?: "").withEtternavn(etternavn)

private fun Iterable<Barn>.toJson() = map(Barn::toJson)
