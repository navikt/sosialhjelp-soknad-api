package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

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
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.navn.toJson
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.v2.familie.Forsorger
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstand

@Component
class FamilieToJsonMapper(private val familieRepository: FamilieRepository) : DomainToJsonMapper {
    override fun mapToSoknad(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ) {
        familieRepository.findByIdOrNull(soknadId)?.let {
            with(jsonInternalSoknad.soknad.data.familie) {
                sivilstatus = it.sivilstand.toJsonSivilstatus()
                forsorgerplikt = it.forsorger.toJsonForsorgerplikt()
            }
        }
    }
}

private fun Sivilstand.toJsonSivilstatus() = JsonSivilstatus()
    .withKilde(ektefelle?.toJsonKilde())
    .withStatus(sivilstatus?.toJson())
    .withEktefelle(ektefelle?.toJson())
    .withBorSammenMed(ektefelle?.borSammen)
    .withFolkeregistrertMedEktefelle(ektefelle?.folkeregistrertMedEktefelle)

private fun Ektefelle.toJsonKilde() = if(kildeErSystem) JsonKilde.SYSTEM else JsonKilde.BRUKER

private fun Sivilstatus.toJson() = JsonSivilstatus.Status.valueOf(name)

private fun Ektefelle.toJson() = JsonEktefelle().withNavn(navn?.toJson()).withFodselsdato(fodselsdato).withPersonIdentifikator(personId)

private fun Forsorger.toJsonForsorgerplikt() = JsonForsorgerplikt()
    .withHarForsorgerplikt(JsonHarForsorgerplikt().withVerdi(harForsorgerplikt))
    .withBarnebidrag(JsonBarnebidrag().withVerdi(barnebidrag?.toJson()))
    .withAnsvar(ansvar.values.toJson())

private fun Barnebidrag.toJson() = JsonBarnebidrag.Verdi.valueOf(name)

private fun Barn.toJson() =
    JsonAnsvar().withBarn(JsonBarn().withFodselsdato(fodselsdato).withNavn(navn?.toJson()).withPersonIdentifikator(personId))

private fun Iterable<Barn>.toJson() = map(Barn::toJson)
