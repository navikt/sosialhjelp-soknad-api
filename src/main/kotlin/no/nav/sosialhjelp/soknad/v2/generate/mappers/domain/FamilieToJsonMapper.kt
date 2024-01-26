package no.nav.sosialhjelp.soknad.v2.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonHarForsorgerplikt
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.v2.familie.Barn
import no.nav.sosialhjelp.soknad.v2.familie.Barnebidrag
import no.nav.sosialhjelp.soknad.v2.familie.Ektefelle
import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.navn.toJson
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Component
class FamilieToJsonMapper(private val familieRepository: FamilieRepository) : DomainToJsonMapper {
    override fun mapToSoknad(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        val familie = familieRepository.findById(soknadId).getOrNull() ?: error("Fant ikke familie på soknadId $soknadId")
        with(jsonInternalSoknad.soknad.data.familie) {
            sivilstatus = JsonSivilstatus()
                .withStatus(familie.sivilstatus?.toJson())
                .withEktefelle(familie.ektefelle?.toJson())
                .withBorSammenMed(familie.ektefelle?.borSammen)
                .withFolkeregistrertMedEktefelle(familie.ektefelle?.folkeregistrertMedEktefelle)
            forsorgerplikt = JsonForsorgerplikt()
                .withHarForsorgerplikt(JsonHarForsorgerplikt().withVerdi(familie.harForsorgerplikt))
                .withBarnebidrag(JsonBarnebidrag().withVerdi(familie.barnebidrag?.toJson()))
                .withAnsvar(familie.ansvar.values.toJson())
        }
    }
}

private fun Sivilstatus.toJson() = JsonSivilstatus.Status.valueOf(name)

private fun Ektefelle.toJson() = JsonEktefelle().withNavn(navn?.toJson()).withFodselsdato(fodselsdato).withPersonIdentifikator(personId)

private fun Barnebidrag.toJson() = JsonBarnebidrag.Verdi.valueOf(name)

private fun Barn.toJson() = JsonAnsvar().withBarn(JsonBarn().withFodselsdato(fodselsdato).withNavn(navn?.toJson()).withPersonIdentifikator(personId))

private fun Iterable<Barn>.toJson() = map(Barn::toJson)
