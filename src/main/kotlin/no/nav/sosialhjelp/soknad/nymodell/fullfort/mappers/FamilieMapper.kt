package no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBorSammenMed
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonErFolkeregistrertSammen
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarDeltBosted
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSamvarsgrad
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers.generell.toJsonKilde
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Barn
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Ektefelle
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Forsorger
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.Sivilstand
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.repository.ForsorgerRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.repository.SivilstandRepository
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.type.Barnebidrag
import no.nav.sosialhjelp.soknad.nymodell.domene.familie.type.Sivilstatus
import org.springframework.stereotype.Component
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Component
class FamilieMapper(
    private val sivilstandRepository: SivilstandRepository,
    private val forsorgerRepository: ForsorgerRepository
): DomainToJsonMapper {
    override fun mapDomainToJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        val jsonSivilstatus = sivilstandRepository.findById(soknadId).getOrNull()?.toJsonSivilstatus()
        val jsonForsorgerplikt = forsorgerRepository.findById(soknadId).getOrNull()?.toJsonForsorgerplikt()

        jsonInternalSoknad.soknad.data
            .withFamilie(
                JsonFamilie()
                    .withSivilstatus(jsonSivilstatus)
                    .withForsorgerplikt(jsonForsorgerplikt)
            )
    }
}

fun Forsorger.toJsonForsorgerplikt(): JsonForsorgerplikt {
    return JsonForsorgerplikt()
        .withHarForsorgerplikt(JsonHarForsorgerplikt().withKilde(JsonKilde.SYSTEM).withVerdi(harForsorgerplikt))
        .withBarnebidrag(barnebidrag?.toJsonBarnebidrag())
        .withAnsvar(barn.map { it.toJsonAnsvar() })
}

fun Barn.toJsonAnsvar(): JsonAnsvar {
    return JsonAnsvar()
        .withBorSammenMed(JsonBorSammenMed().withVerdi(borSammen))
        .withErFolkeregistrertSammen(JsonErFolkeregistrertSammen().withVerdi(folkeregistrertSammen))
        .withHarDeltBosted(JsonHarDeltBosted().withVerdi(deltBosted))
        .withSamvarsgrad(JsonSamvarsgrad().withVerdi(samvarsgrad))
        .withBarn(toJsonBarn())
}

fun Barn.toJsonBarn(): JsonBarn {
    return JsonBarn()
        .withKilde(JsonKilde.SYSTEM)
        .withPersonIdentifikator(personId)
        .withFodselsdato(fodselsdato.toString())
        .withHarDiskresjonskode(false)
        .withNavn(
            JsonNavn()
                .withFornavn(fornavn)
                .withMellomnavn(mellomnavn)
                .withEtternavn(etternavn)
        )
}

fun Sivilstand.toJsonSivilstatus(): JsonSivilstatus {
    val jsonSivilstatus = JsonSivilstatus()
        .withKilde(kilde.toJsonKilde())
        .withStatus(sivilstatus?.toJsonSivilstatus_Status())

    ektefelle?.toJsonSivilstatus(jsonSivilstatus)

    return jsonSivilstatus
}

fun Barnebidrag.toJsonBarnebidrag(): JsonBarnebidrag {
    return JsonBarnebidrag()
        .withVerdi(JsonBarnebidrag.Verdi.valueOf(this.name))
}

fun Ektefelle.toJsonSivilstatus(jsonSivilstatus: JsonSivilstatus): JsonSivilstatus {
    return jsonSivilstatus
        .withEktefelleHarDiskresjonskode(harDiskresjonskode)
        .withFolkeregistrertMedEktefelle(folkeregistrertMed)
        .withBorSammenMed(borSammen)
        .withEktefelle(toJsonEktefelle())
}

fun Ektefelle.toJsonEktefelle(): JsonEktefelle {
    return JsonEktefelle()
        .withFodselsdato(fodselsdato)
        .withPersonIdentifikator(personId)
        .withNavn(
            JsonNavn()
                .withFornavn(fornavn)
                .withMellomnavn(mellomnavn)
                .withEtternavn(etternavn)
        )
}

fun Sivilstatus.toJsonSivilstatus_Status(): JsonSivilstatus.Status {
    return JsonSivilstatus.Status.valueOf(this.name)
}