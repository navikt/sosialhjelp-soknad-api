package no.nav.sosialhjelp.soknad.v2.json.compare

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
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
            if (original.vedlegg.size != shadow.vedlegg.size) {
                logger.warn("Antall vedlegg er ikke likt: ${original.vedlegg} - ${shadow.vedlegg}")
            }

            shadow.vedlegg.forEach { vedlegg ->
                original.vedlegg.find {
                    vedlegg.type == it.type && vedlegg.tilleggsinfo == it.tilleggsinfo && vedlegg.status == it.status &&
                        vedlegg.filer.size == it.filer.size
                } ?: logger.warn("Fant ikke vedlegg i original-json: $vedlegg - orginal: ${original.vedlegg}")
            }
        }
    }

    private fun JsonInternalSoknad.compareArbeidsForhold(shadowJson: JsonInternalSoknad) {
        val original = soknad?.data?.arbeid
        val shadow = shadowJson.soknad?.data?.arbeid

        if (original == shadow) {
            logger.info("Compare Arbeid: Original og shadow er like: $original")
        } else if (original == null || shadow == null) {
            logger.warn("Original: $original - Shadow: $shadow")
        } else {
            if (original.forhold.size != shadow.forhold.size) {
                logger.warn("Antall arbeidsforhold er ikke likt: Orginal {${original.forhold}} - {${shadow.forhold}}")
            }

            shadow.forhold.forEach { forhold ->
                original.forhold.find {
                    forhold.arbeidsgivernavn == it.arbeidsgivernavn && forhold.fom == it.fom && forhold.tom == it.tom
                }
                    ?: logger.warn("Fant ikke arbeidsforhold i original-json: $forhold - orginal: ${original.forhold}")
            }
        }
    }

    private fun JsonInternalSoknad.compareAnsvar(shadowJson: JsonInternalSoknad) {
        val original = soknad?.data?.familie?.forsorgerplikt
        val shadow = shadowJson.soknad?.data?.familie?.forsorgerplikt

        if (original == shadow) {
            logger.info("Compare Ansvar: Original og shadow er like: $original")
        } else if (original == null || shadow == null) {
            logger.warn("Original: $original - Shadow: $shadow")
        } else {
            if (original.ansvar.size != shadow.ansvar.size) {
                logger.warn("Antall arbeidsforhold er ikke likt: Orginal {${original.ansvar}} - {${shadow.ansvar}}")
            }

            shadow.ansvar.forEach { forhold ->
                original.ansvar.find {
                    forhold.barn.fodselsdato == it.barn.fodselsdato
                }
                    ?: logger.warn("Fant ikke ansvar i original-json: $forhold - orginal: ${original.ansvar}")
            }
        }
    }
}

private class JsonOkonomiCollectionComparator(originalJson: JsonInternalSoknad, shadowJson: JsonInternalSoknad) {
    private val original = originalJson.soknad?.data?.okonomi
    private val shadow = shadowJson.soknad?.data?.okonomi

    companion object {
        private val logger by logger()
    }

    fun compareCollections() {
        if (original == null || shadow == null) {
            logger.warn("Okonomi er null - Original: $original - Shadow: $original")
            return
        }

        if (original.opplysninger == null || shadow.opplysninger == null) {
            logger.warn("Opplysninger er null - Original: ${original.opplysninger} - Shadow: ${original.opplysninger}")
        } else {
            original.opplysninger.compareBekreftelser(shadow.opplysninger)
            original.opplysninger.compareUtbetalinger(shadow.opplysninger)
            original.opplysninger.compareUtgifter(shadow.opplysninger)
            original.opplysninger.compareSaker(shadow.opplysninger)
        }

        if (original.oversikt == null || shadow.oversikt == null) {
            logger.warn("Oversikt er null - Original: ${original.oversikt} - Shadow: ${original.oversikt}")
        } else {
            original.oversikt.compareUtgifter(shadow.oversikt)
            original.oversikt.compareInntekter(shadow.oversikt)
            original.oversikt.compareFormue(shadow.oversikt)
        }
    }

    private fun JsonOkonomiopplysninger.compareBekreftelser(shadowJson: JsonOkonomiopplysninger) {
        val original = this.bekreftelse
        val shadow = shadowJson.bekreftelse

        if (original.size != shadow.size) {
            logger.warn("Ikke likt antall utbetalinger i opplysninger. Original: $original ** Shadow: $shadow")
        } else {
            shadow.forEach { bekreftelse ->
                original.find { bekreftelse.type == it.type }
                    ?: logger.warn("Fant ikke bekreftelse $bekreftelse i orginal: $original")
            }
        }
    }

    private fun JsonOkonomiopplysninger.compareUtbetalinger(shadowJson: JsonOkonomiopplysninger) {
        val original = this.utbetaling
        val shadow = shadowJson.utbetaling

        if (original.size != shadow.size) {
            logger.warn("Ikke likt antall utbetalinger i opplysninger. Original: $original ** Shadow: $shadow")
        } else {
            shadow.forEach { utbetaling ->
                original.find {
                    utbetaling.type == it.type && utbetaling.brutto == it.brutto &&
                        utbetaling.utbetalingsdato == it.utbetalingsdato && utbetaling.belop == it.belop &&
                        utbetaling.netto == it.netto && utbetaling.tittel == it.tittel
                }
                    ?: logger.warn("Fant ikke utbetaling $utbetaling i orginal: $original")
            }
        }
    }

    private fun JsonOkonomiopplysninger.compareUtgifter(shadowJson: JsonOkonomiopplysninger) {
        val original = this.utgift
        val shadow = shadowJson.utgift

        if (original.size != shadow.size) {
            logger.warn("Ikke likt antall utgifter i opplysninger. Original: $original ** Shadow: $shadow")
        } else {
            shadow.forEach { utgift ->
                original.find { utgift.type == it.type && utgift.tittel == it.tittel && utgift.belop == it.belop }
                    ?: logger.warn("Fant ikke utgift $utgift i orginal: $original")
            }
        }
    }

    private fun JsonOkonomiopplysninger.compareSaker(shadowJson: JsonOkonomiopplysninger) {
        val original = this.bostotte
        val shadow = shadowJson.bostotte

        if (original == null || shadow == null) {
            logger.warn("En Bostotte er null. Original: $original ** Shadow: $shadow")
        } else {
            shadow.saker.forEach { sak ->

                original.saker.find {
                    sak.type == it.type && sak.status == it.status && sak.dato == it.dato &&
                        sak.vedtaksstatus == it.vedtaksstatus
                }
                    ?: logger.warn("Fant ikke bostottesak $sak i orginal: $original")
            }
        }
    }

    private fun JsonOkonomioversikt.compareFormue(shadowJson: JsonOkonomioversikt) {
        val original = this.formue
        val shadow = shadowJson.formue

        if (original.size != shadow.size) {
            logger.warn("Ikke likt antall formuer i oversikt. Original: $original ** Shadow: $shadow")
        } else {
            shadow.forEach { formue ->
                original.find { formue.type == it.type && formue.tittel == it.tittel && formue.belop == it.belop }
                    ?: logger.warn("Fant ikke formuer $formue i orginal: $original")
            }
        }
    }

    private fun JsonOkonomioversikt.compareInntekter(shadowJson: JsonOkonomioversikt) {
        val original = this.inntekt
        val shadow = shadowJson.inntekt

        if (original.size != shadow.size) {
            logger.warn("Ikke likt antall inntekter i oversikt. Original: $original ** Shadow: $shadow")
        } else {
            shadow.forEach { utbetaling ->
                original.find { utbetaling.type == it.type }
                    ?: logger.warn("Fant ikke inntekt $utbetaling i orginal: $original")
            }
        }
    }

    private fun JsonOkonomioversikt.compareUtgifter(shadowJson: JsonOkonomioversikt) {
        val original = this.utgift
        val shadow = shadowJson.utgift

        if (original.size != shadow.size) {
            logger.warn("Ikke likt antall utgifter i oversikt. Original: $original ** Shadow: $shadow")
        } else {
            shadow.forEach { utbetaling ->
                original.find { utbetaling.type == it.type }
                    ?: logger.warn("Fant ikke utgift $utbetaling i orginal: $original")
            }
        }
    }
}
