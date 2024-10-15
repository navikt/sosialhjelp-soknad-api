package no.nav.sosialhjelp.soknad.v2.json.compare.prodsafe

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger

class OkonomiComparator(
    private val original: JsonOkonomi,
    private val shadow: JsonOkonomi,
) : ProductionComparator {
    override fun compare() {
        compareBekreftelser()
        compareBeskrivelseAvAnnet()
        compareInntekter()
        compareUtbetalinger()
        compareOversiktUtgifter()
        compareOpplysningUtgifter()
        compareFormuer()
    }

    private fun compareFormuer() {
        if (original.oversikt.formue.size == shadow.oversikt.formue.size) return

        val originalTypes = original.oversikt.formue.map { it.type }
        val shadowTypes = shadow.oversikt.formue.map { it.type }

        logger.warn(
            "NyModell: Formue typer er ikke like: \n\n" +
                "Original: $originalTypes\n\n" +
                "Shadow: $shadowTypes",
        )
    }

    private fun compareOversiktUtgifter() {
        if (original.oversikt.utgift.size == shadow.oversikt.utgift.size) return

        val originalTypes = original.oversikt.utgift.map { it.type }
        val shadowTypes = shadow.oversikt.utgift.map { it.type }

        logger.warn(
            "NyModell: OversiktUtgifter typer er ikke like: \n\n" +
                "Original: $originalTypes\n\n" +
                "Shadow: $shadowTypes",
        )
    }

    private fun compareOpplysningUtgifter() {
        if (original.opplysninger.utgift.size == shadow.opplysninger.utgift.size) return

        val originalTypes = original.opplysninger.utgift.map { it.type }
        val shadowTypes = shadow.opplysninger.utgift.map { it.type }

        logger.warn(
            "NyModell: OpplysningUtgifter typer er ikke like: \n\n" +
                "Original: $originalTypes\n\n" +
                "Shadow: $shadowTypes",
        )
    }

    private fun compareUtbetalinger() {
        if (original.opplysninger.utbetaling.size == shadow.opplysninger.utbetaling.size) return

        val originalTypes = original.opplysninger.utbetaling.map { it.type }
        val shadowTypes = shadow.opplysninger.utbetaling.map { it.type }

        logger.warn(
            "NyModell: Utbetalinger typer er ikke like: \n\n" +
                "Original: $originalTypes\n\n" +
                "Shadow: $shadowTypes",
        )
    }

    private fun compareInntekter() {
        if (original.oversikt.inntekt.size == shadow.oversikt.inntekt.size) return

        val originalTypes = original.oversikt.inntekt.map { it.type }
        val shadowTypes = shadow.oversikt.inntekt.map { it.type }

        logger.warn(
            "NyModell: Inntekter typer er ikke like: \n\n" +
                "Original: $originalTypes\n\n" +
                "Shadow: $shadowTypes",
        )
    }

    private fun compareBeskrivelseAvAnnet() {
        BeskrivelseAvAnnetComparator(
            original = original.opplysninger.beskrivelseAvAnnet,
            shadow = shadow.opplysninger.beskrivelseAvAnnet,
        ).compare()
    }

    private fun compareBekreftelser() {
        BekreftelseComparator(
            originals = original.opplysninger.bekreftelse,
            shadows = shadow.opplysninger.bekreftelse,
        ).compare()
    }

    companion object {
        private val logger by logger()
    }
}
