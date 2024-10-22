package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sosialhjelp.soknad.v2.json.OpplysningTypeMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.Vedtaksstatus
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType

class BostotteSakToJsonMapper(
    private val saker: List<BostotteSak>,
    jsonOkonomi: JsonOkonomi,
) : OkonomiElementsToJsonMapper {
    private val opplysninger = jsonOkonomi.opplysninger

    override fun doMapping() {
        // At denne settes til tross for ingen saker, indikerer at bruker har fått spørsmål om bostotte
        val jsonBostotte = opplysninger.bostotte ?: opplysninger.withBostotte(JsonBostotte()).bostotte
        jsonBostotte.saker.addAll(saker.map { it.toJsonBostotteSak() })
    }
}

private fun BostotteSak.toJsonBostotteSak() =
    JsonBostotteSak()
        .withKilde(JsonKildeSystem.SYSTEM)
        // Alltid denne typen - legges ikke ved som en del av modellen
        .withType(InntektType.UTBETALING_HUSBANKEN.toJsonInntektType())
        .withDato(dato.toString())
        .withStatus(status.name)
        .withBeskrivelse(beskrivelse)
        .withVedtaksstatus(vedtaksstatus?.toJsonVedtaksstatus())

internal fun Vedtaksstatus.toJsonVedtaksstatus(): JsonBostotteSak.Vedtaksstatus {
    return JsonBostotteSak.Vedtaksstatus.entries.find { it.name == this.name } ?: error("Finner ikke JsonVedtaksstatus")
}

private fun InntektType.toJsonInntektType(): String {
    return OpplysningTypeMapper.getJsonVerdier(this).navn?.verdi ?: error("Finner ikke InntektType")
}
