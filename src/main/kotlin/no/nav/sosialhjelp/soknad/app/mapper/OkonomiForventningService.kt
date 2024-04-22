package no.nav.sosialhjelp.soknad.app.mapper

import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktFormue
import no.nav.sosialhjelp.soknad.app.mapper.TitleKeyMapper.soknadTypeToTitleKey
import no.nav.sosialhjelp.soknad.tekster.TextService
import org.springframework.stereotype.Service

@Service
class OkonomiForventningService(
    private val textService: TextService,
) {
    fun setOversiktFormue(
        behandlingsId: String,
        formuer: MutableList<JsonOkonomioversiktFormue>,
        type: String,
        isChecked: Boolean,
    ) {
        // Databasekode goes here

        when (isChecked) {
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
        behandlingsId: String,
        utbetalinger: MutableList<JsonOkonomiOpplysningUtbetaling>,
        type: String,
        isChecked: Boolean,
        titleKey: String? = soknadTypeToTitleKey[type],
    ) {
        // Databasekode goes here

        when (isChecked) {
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
}
