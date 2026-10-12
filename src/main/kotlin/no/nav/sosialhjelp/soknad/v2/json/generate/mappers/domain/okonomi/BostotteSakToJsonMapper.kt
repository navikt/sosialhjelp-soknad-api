package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sosialhjelp.soknad.v2.json.OpplysningTypeMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.Vedtaksstatus

class BostotteSakToJsonMapper(
    private val saker: List<BostotteSak>,
) : OkonomiElementsToJsonMapper {
    override fun doMapping(jsonOkonomi: JsonOkonomi): JsonOkonomi {
        // At denne settes til tross for ingen saker, indikerer at bruker har fått spørsmål om bostotte
        return jsonOkonomi.copy(
            opplysninger = jsonOkonomi.opplysninger.copy(bostotte = JsonBostotte(saker.map { it.toJsonBostotteSak() })),
        )
    }
}

private fun BostotteSak.toJsonBostotteSak() =
    JsonBostotteSak(
        JsonKildeSystem.SYSTEM,
        InntektType.UTBETALING_HUSBANKEN.toJsonInntektType(),
        dato.toString(),
        status.name,
        beskrivelse,
        vedtaksstatus?.toJsonVedtaksstatus(),
    )

internal fun Vedtaksstatus.toJsonVedtaksstatus(): JsonBostotteSak.Vedtaksstatus {
    return JsonBostotteSak.Vedtaksstatus.entries.find { it.name == this.name } ?: error("Finner ikke JsonVedtaksstatus")
}

private fun InntektType.toJsonInntektType(): String {
    return OpplysningTypeMapper.getJsonVerdier(this).navn?.verdi ?: error("Finner ikke InntektType")
}
