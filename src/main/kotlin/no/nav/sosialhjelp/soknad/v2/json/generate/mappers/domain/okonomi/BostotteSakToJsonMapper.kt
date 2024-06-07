package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteSak
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeSystem
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sosialhjelp.soknad.v2.okonomi.BostotteSak
import no.nav.sosialhjelp.soknad.v2.okonomi.Vedtaksstatus
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType

class BostotteSakToJsonMapper(
    private val saker: List<BostotteSak>,
    jsonOkonomi: JsonOkonomi,
) : OkonomiDelegateMapper {
    private val opplysninger = jsonOkonomi.opplysninger ?: jsonOkonomi.withOpplysninger(JsonOkonomiopplysninger()).opplysninger

    override fun doMapping() {
        val jsonBostotte = opplysninger.bostotte ?: opplysninger.withBostotte(JsonBostotte()).bostotte
        jsonBostotte.saker.addAll(saker.map { it.toJsonBostotteSak() })
    }
}

private fun BostotteSak.toJsonBostotteSak() =
    JsonBostotteSak()
        .withKilde(JsonKildeSystem.SYSTEM)
        .withType(InntektType.UTBETALING_HUSBANKEN.name)
        .withDato(dato.toString())
        .withStatus(status.name)
        .withBeskrivelse(beskrivelse)
        .withVedtaksstatus(vedtaksstatus?.toJsonVedtaksstatus())

private fun Vedtaksstatus.toJsonVedtaksstatus(): JsonBostotteSak.Vedtaksstatus {
    return JsonBostotteSak.Vedtaksstatus.entries.find { it.name == this.name } ?: error("Finner ikke JsonVedtaksstatus")
}
