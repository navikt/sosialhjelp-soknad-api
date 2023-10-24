package no.nav.sosialhjelp.soknad.fullfort.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeid
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.arbeid.JsonKommentarTilArbeidsforhold
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.fullfort.SoknadToJsonMapper
import no.nav.sosialhjelp.soknad.domene.arbeid.Arbeid
import no.nav.sosialhjelp.soknad.domene.soknad.ArbeidRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

@Component
class ArbeidMapper (
    private val arbeidRepository: ArbeidRepository
): SoknadToJsonMapper {
    override fun mapToSoknadJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        val arbeid = arbeidRepository.findByIdOrNull(soknadId)
        arbeid?.let { jsonInternalSoknad.soknad.data.withArbeid(it.toJsonObject()) }
    }
}

fun Arbeid.toJsonObject(): JsonArbeid = JsonArbeid()
    .withKommentarTilArbeidsforhold(JsonKommentarTilArbeidsforhold().withVerdi(kommentarArbeid))
    .withForhold(
        arbeidsforhold
            .map {
                JsonArbeidsforhold()
                    .withKilde(JsonKilde.SYSTEM)
                    .withArbeidsgivernavn(it.arbeidsgivernavn)
                    .withFom(it.fraOgMed)
                    .withTom(it.tilOgMed)
                    .withStillingsprosent(it.stillingsprosent)
                    .withStillingstype(JsonArbeidsforhold.Stillingstype.valueOf(it.stillingstype.name))
            }
    )
