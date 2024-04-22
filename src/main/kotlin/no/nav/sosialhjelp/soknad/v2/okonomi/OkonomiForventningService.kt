package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.tekster.TextService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OkonomiForventningService(
    private val textService: TextService,
    private val okonomiForventningRepository: OkonomiForventningRepository,
) {
    fun setOversiktFormue(
        soknadId: String,
        formuer: MutableList<JsonOkonomioversiktFormue>,
        type: String,
        isExpected: Boolean,
    ) {
        tryUpdateRepositoryOrLogError(soknadId, type, isExpected)

        when (isExpected) {
            false -> formuer.removeIf { it.type == type }

            true -> {
                if (formuer.any { it.type == type }) return

                formuer.add(
                    JsonOkonomioversiktFormue()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(type)
                        .withTittel(textService.getJsonOkonomiTittel(soknadTypeToTitleKey[type]))
                        .withOverstyrtAvBruker(false),
                )
            }
        }
    }

    fun setOppysningUtbetalinger(
        soknadId: String,
        utbetalinger: MutableList<JsonOkonomiOpplysningUtbetaling>,
        type: String,
        isExpected: Boolean,
        titleKey: String? = soknadTypeToTitleKey[type],
    ) {
        tryUpdateRepositoryOrLogError(soknadId, type, isExpected)

        when (isExpected) {
            false -> utbetalinger.removeIf { it.type == type }

            true -> {
                if (utbetalinger.any { it.type == type }) return

                utbetalinger.add(
                    JsonOkonomiOpplysningUtbetaling()
                        .withKilde(JsonKilde.BRUKER)
                        .withType(type)
                        .withTittel(textService.getJsonOkonomiTittel(titleKey))
                        .withOverstyrtAvBruker(false),
                )
            }
        }
    }

    private fun tryUpdateRepositoryOrLogError(
        soknadId: String,
        type: String,
        isExpected: Boolean,
    ) = kotlin.runCatching {
        okonomiForventningRepository.setExpectationForOpplysningType(UUID.fromString(soknadId), type, isExpected)
    }.onFailure {
        log.error("kunne ikke sette forventning for soknadId=$soknadId, type=$type, isExpected=$isExpected", it)
    }

    companion object {
        private val log by logger()
    }
}
