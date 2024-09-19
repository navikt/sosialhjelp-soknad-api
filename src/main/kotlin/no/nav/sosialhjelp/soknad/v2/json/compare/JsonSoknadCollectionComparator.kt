package no.nav.sosialhjelp.soknad.v2.json.compare

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotte
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger

class JsonSoknadCollectionComparator(
    private val original: JsonInternalSoknad,
    private val shadow: JsonInternalSoknad,
) {
    companion object {
        private val logger by logger()
    }

    fun compareCollections() {
        original.compareVedlegg(shadow)
        original.compareArbeidsForhold(shadow)
        original.compareAnsvar(shadow)
        JsonOkonomiCollectionComparator(original, shadow).compareCollections()
    }

    private fun JsonInternalSoknad.compareVedlegg(shadowJson: JsonInternalSoknad) {
        val original = this.vedlegg
        val shadow = shadowJson.vedlegg

        if (original == shadow) {
            logger.info("Compare vedlegg: Original og shadow er like: $original")
        } else if (original == null || shadow == null) {
            logger.warn("Original: $original - Shadow: $shadow")
        } else {
            // TODO Vedlegg har forskjellige typer

            original.vedlegg
        }
    }

    private fun JsonInternalSoknad.compareArbeidsForhold(shadow: JsonInternalSoknad) {
    }

    private fun JsonInternalSoknad.compareAnsvar(shadow: JsonInternalSoknad) {
    }
}

private class JsonOkonomiCollectionComparator(originalJson: JsonInternalSoknad, shadowJson: JsonInternalSoknad) {
    private val original = originalJson.soknad?.data?.okonomi
    private val shadow = shadowJson.soknad?.data?.okonomi

    companion object {
        private val logger by logger()
    }

    fun compareCollections() {
    }

    private fun JsonOkonomiopplysninger.compareBekreftelser(shadow: JsonOkonomiopplysninger) {
    }

    private fun JsonOkonomiopplysninger.compareUtbetalinger(shadow: JsonOkonomiopplysninger) {
    }

    private fun JsonOkonomiopplysninger.compareUtgifter(shadow: JsonOkonomiopplysninger) {
    }

    private fun JsonBostotte.compareSaker(shadow: JsonBostotte) {
    }

    private fun JsonOkonomioversikt.compareFormue(shadow: JsonOkonomioversikt) {
    }

    private fun JsonOkonomioversikt.compareInntekter(shadow: JsonOkonomioversikt) {
    }

    private fun JsonOkonomioversikt.compareUtgifter(shadow: JsonOkonomioversikt) {
    }
}
