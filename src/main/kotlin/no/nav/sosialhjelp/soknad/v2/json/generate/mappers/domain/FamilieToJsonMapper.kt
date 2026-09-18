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
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSamvarsgrad
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
    ): JsonInternalSoknad = familieRepository.findByIdOrNull(soknadId)?.let { doMapping(it, jsonInternalSoknad) } ?: jsonInternalSoknad

    internal companion object Mapper {
        fun doMapping(
            familie: Familie,
            json: JsonInternalSoknad,
        ): JsonInternalSoknad =
            json.soknad!!.let { soknad ->
                json.copy(
                    soknad =
                        soknad.copy(
                            data =
                                soknad.data.copy(
                                    familie =
                                        no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie(
                                            forsorgerplikt = familie.toJsonForsorgerplikt(),
                                            sivilstatus = familie.sivilstatus?.let { familie.toJsonSivilstatus() },
                                        ),
                                ),
                        ),
                )
            }
    }
}

private fun Familie.toJsonSivilstatus() =
    JsonSivilstatus(
        kilde = ektefelle?.toJsonKilde() ?: JsonKilde.BRUKER,
        status = sivilstatus?.toJson() ?: JsonSivilstatus.Status.UGIFT,
        ektefelle = ektefelle?.toJson() ?: toEmptyEktefelleIfGift(sivilstatus),
        ektefelleHarDiskresjonskode = ektefelle?.takeIf { it.kildeErSystem }?.let { false },
        folkeregistrertMedEktefelle = ektefelle?.takeIf { it.kildeErSystem }?.folkeregistrertMedEktefelle,
        borSammenMed = ektefelle?.takeUnless { it.kildeErSystem }?.borSammen,
    )

private fun toEmptyEktefelleIfGift(sivilstatus: Sivilstatus?): JsonEktefelle? =
    when (sivilstatus) {
        Sivilstatus.GIFT -> JsonEktefelle(toEmptyJsonNavn())
        else -> null
    }

private fun Ektefelle.toJsonKilde() = if (kildeErSystem) JsonKilde.SYSTEM else JsonKilde.BRUKER

private fun Sivilstatus.toJson() = JsonSivilstatus.Status.valueOf(name)

private fun Ektefelle.toJson() =
    JsonEktefelle(navn?.toJson() ?: toEmptyJsonNavn(), fodselsdato, personId)

private fun toEmptyJsonNavn() =
    JsonNavn("", "", "")

private fun Familie.toJsonForsorgerplikt() =
    JsonForsorgerplikt(
        JsonHarForsorgerplikt(JsonKilde.SYSTEM, harForsorgerplikt),
        JsonBarnebidrag(JsonKildeBruker.BRUKER, barnebidrag?.toJson()),
        ansvar.values.toJson(),
    )

private fun Barnebidrag.toJson() = JsonBarnebidrag.Verdi.valueOf(name)

private fun Barn.toJson() =
    JsonAnsvar(
        barn = JsonBarn(JsonKilde.SYSTEM, navn?.toJson() ?: toEmptyJsonNavn(), fodselsdato, personId, false),
        erFolkeregistrertSammen = JsonErFolkeregistrertSammen(JsonKildeSystem.SYSTEM, folkeregistrertSammen ?: false),
        harDeltBosted = deltBosted?.let { JsonHarDeltBosted(JsonKildeBruker.BRUKER, it) },
        samvarsgrad = samvarsgrad?.let { JsonSamvarsgrad(JsonKildeBruker.BRUKER, it) },
    )

// mellomnavn er required i json-modellen
fun Navn.toJson(): JsonNavn = JsonNavn(fornavn ?: "", mellomnavn ?: "", etternavn ?: "")

private fun Iterable<Barn>.toJson() = map(Barn::toJson)
