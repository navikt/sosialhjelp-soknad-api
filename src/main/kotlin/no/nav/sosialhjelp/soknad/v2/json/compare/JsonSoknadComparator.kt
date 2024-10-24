package no.nav.sosialhjelp.soknad.v2.json.compare

import no.nav.sbl.soknadsosialhjelp.json.JsonSosialhjelpObjectMapper
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger

// TODO Kan fjernes når all is good
class JsonSoknadComparator(
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
        original.compareBosituasjon(shadow)
        original.compareUtdanning(shadow)
        original.compareSoknadstype(shadow)
        original.comparePostadresse(shadow)
        original.comparePersonalia(shadow)
        JsonOkonomiCollectionComparator(original, shadow).compareCollections()
    }

    private fun JsonInternalSoknad.compareVedlegg(shadowJson: JsonInternalSoknad) {
        val original = this.vedlegg
        val shadow = shadowJson.vedlegg

        if (original == null || shadow == null) {
            logger.warn(
                "Original: \n${original.asJson()} " +
                    "- \n\nShadow: ${shadow.asJson()}",
            )
        } else {
            original.vedlegg.forEach { orgVedlegg ->
                shadow.vedlegg
                    .find {
                        orgVedlegg.type == it.type && orgVedlegg.tilleggsinfo == it.tilleggsinfo &&
                            orgVedlegg.status == it.status
                    }
                    ?.let {
                        if (orgVedlegg.filer.size != it.filer.size) {
                            logger.warn(
                                "Antall filer er ikke likt: " +
                                    "${orgVedlegg.asJson()} - \n\nshadow: ${it.asJson()}",
                            )
                        }
                    }
                    ?: logger.warn(
                        "Fant ikke vedlegg i shadow-json: ${orgVedlegg.asJson()} " +
                            "- \n\nshadow: ${shadow.vedlegg.asJson()}",
                    )
            }

            shadow.vedlegg.forEach { shadowVedlegg ->
                original.vedlegg
                    .find {
                        shadowVedlegg.type == it.type && shadowVedlegg.tilleggsinfo == it.tilleggsinfo &&
                            shadowVedlegg.status == it.status
                    }
                    ?: logger.warn(
                        "Fant ikke vedlegg i original-json: ${shadowVedlegg.asJson()} " +
                            "- \n\norginal: ${original.vedlegg.asJson()}",
                    )
            }
        }
    }

    private fun JsonInternalSoknad.compareArbeidsForhold(shadowJson: JsonInternalSoknad) {
        val original = soknad?.data?.arbeid
        val shadow = shadowJson.soknad?.data?.arbeid

        if (original == null || shadow == null) {
            logger.warn(
                "Original: \n${original.asJson()} - " +
                    "\nShadow: \n${shadow.asJson()}",
            )
        } else {
            if (original.forhold.size != shadow.forhold.size) {
                logger.warn(
                    "Antall arbeidsforhold er ikke likt: " +
                        "\nOrginal \n{${original.forhold.asJson()}} -" +
                        "\nShadow: \n{${shadow.forhold.asJson()}}",
                )
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

        if (original == null || shadow == null) {
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

    private fun JsonInternalSoknad.compareBosituasjon(shadowJson: JsonInternalSoknad) {
        val bosituasjon = soknad?.data?.bosituasjon.asJson()
        val shadow = shadowJson.soknad?.data?.bosituasjon.asJson()

        if (bosituasjon != shadow) {
            logger.warn(
                "Bosituasjon er ikke like: \nOriginal: \n$bosituasjon" +
                    " \nShadow: \n$shadow",
            )
        }
    }

    private fun JsonInternalSoknad.compareUtdanning(shadowJson: JsonInternalSoknad) {
        val utdanning = soknad?.data?.utdanning.asJson()
        val shadow = shadowJson.soknad?.data?.utdanning.asJson()

        if (utdanning != shadow) {
            logger.warn(
                "Utdanning er ikke like: \nOriginal: \n$utdanning" +
                    " \nShadow: \n$shadow",
            )
        }
    }

    private fun JsonInternalSoknad.comparePostadresse(shadowJson: JsonInternalSoknad) {
        val postadresse = soknad?.data?.personalia?.postadresse.asJson()
        val shadow = shadowJson.soknad?.data?.personalia?.postadresse.asJson()

        if (postadresse != shadow) {
            logger.warn(
                "Postadresse er ikke like: \nOriginal: \n$postadresse" +
                    " \nShadow: \n$shadow",
            )
        }
    }

    private fun JsonInternalSoknad.compareSoknadstype(shadowJson: JsonInternalSoknad) {
        val soknadType = soknad?.data?.soknadstype.asJson()
        val shadow = shadowJson.soknad?.data?.soknadstype.asJson()

        if (soknadType != shadow) {
            logger.warn(
                "Soknadstype er ikke like: \nOriginal: \n$soknadType" +
                    " \nShadow: \n$shadow",
            )
        }
    }

    private fun JsonInternalSoknad.comparePersonalia(shadowJson: JsonInternalSoknad) {
        val personalia = soknad?.data?.personalia.asJson()
        val shadow = shadowJson.soknad?.data?.personalia.asJson()

        if (personalia != shadow) {
            logger.warn(
                "Personalia er ikke like: \nOriginal: \n$personalia" +
                    " \nShadow: \n$shadow",
            )
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
            logger.warn(
                "Okonomi er null - \nOriginal: ${original.asJson()}" +
                    " - \nShadow: ${shadow.asJson()}original",
            )
            return
        }

        if (original.opplysninger == null || shadow.opplysninger == null) {
            logger.warn(
                "Opplysninger er null - Original: \n${original.opplysninger.asJson()}" +
                    " - Shadow: \n${original.opplysninger.asJson()}",
            )
        } else {
            original.opplysninger.compareBekreftelser(shadow.opplysninger)
            original.opplysninger.compareUtbetalinger(shadow.opplysninger)
            original.opplysninger.compareUtgifter(shadow.opplysninger)
            original.opplysninger.compareSaker(shadow.opplysninger)
        }

        if (original.oversikt == null || shadow.oversikt == null) {
            logger.warn(
                "Oversikt er null - Original: \n${original.oversikt.asJson()}" +
                    " - Shadow: \n${original.oversikt.asJson()}",
            )
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
            logger.warn(
                "Ikke likt antall bekreftelser i opplysninger. Original: \n${original.asJson()} " +
                    "** Shadow: \n${shadow.asJson()}",
            )
        } else {
            shadow.forEach { bekreftelse ->
                original.find { bekreftelse.type == it.type }
                    ?: logger.warn(
                        "Fant ikke bekreftelse \n${bekreftelse.asJson()} i" +
                            " orginal: \n${original.asJson()}",
                    )
            }
        }
    }

    private fun JsonOkonomiopplysninger.compareUtbetalinger(shadowJson: JsonOkonomiopplysninger) {
        val original = this.utbetaling
        val shadow = shadowJson.utbetaling

        if (original.size != shadow.size) {
            logger.warn(
                "Ikke likt antall utbetalinger i opplysninger. Original: \n${original.asJson()}" +
                    " \n** Shadow: \n${shadow.asJson()}",
            )
        } else {
            shadow.forEach { utbetaling ->
                original.find {
                    utbetaling.type == it.type && utbetaling.tittel == it.tittel &&
                        utbetaling.utbetalingsdato == it.utbetalingsdato && utbetaling.kilde == it.kilde
                }
                    ?.let { compareNumbers(utbetaling, it) }
                    ?: logger.warn(
                        "Fant ikke utbetaling \n${utbetaling.asJson()} i" +
                            " \norginal: \n${original.asJson()}",
                    )
            }
        }
    }

    private fun JsonOkonomiopplysninger.compareUtgifter(shadowJson: JsonOkonomiopplysninger) {
        val original = this.utgift
        val shadow = shadowJson.utgift

        if (original.size != shadow.size) {
            logger.warn(
                "Ikke likt antall utgifter i opplysninger." +
                    " \nOriginal: \n${original.asJson()} **" +
                    " \nShadow: \n${shadow.asJson()}",
            )
        } else {
            shadow.forEach { utgift ->
                original.find {
                    utgift.type == it.type &&
                        utgift.tittelWithoutWhitespaces() == it.tittelWithoutWhitespaces() &&
                        utgift.kilde == it.kilde
                }
                    ?.let { if (!isValid(utgift.belop, it.belop)) null else 1 }
                    ?: logger.warn(
                        "Fant ikke utgift \n${utgift.asJson()} i" +
                            " \norginal: \n${original.asJson()}",
                    )
            }
        }
    }

    private fun JsonOkonomiopplysninger.compareSaker(shadowJson: JsonOkonomiopplysninger) {
        val original = this.bostotte
        val shadow = shadowJson.bostotte

        if (original == null || shadow == null) {
            logger.warn(
                "En Bostotte er null. \nOriginal: \n${original.asJson()}" +
                    " \n** Shadow: \n${shadow.asJson()}",
            )
        } else {
            shadow.saker.forEach { sak ->
                original.saker.find {
                    sak.type == it.type && sak.status == it.status && sak.dato == it.dato &&
                        sak.vedtaksstatus == it.vedtaksstatus
                }
                    ?: logger.warn(
                        "Fant ikke bostottesak \n${sak.asJson()} i" +
                            " \norginal: \n${original.asJson()}",
                    )
            }
        }
    }

    private fun JsonOkonomioversikt.compareFormue(shadowJson: JsonOkonomioversikt) {
        val original = this.formue
        val shadow = shadowJson.formue

        if (original.size != shadow.size) {
            logger.warn(
                "Ikke likt antall formuer i oversikt. \nOriginal: \n${original.asJson()}" +
                    " \n** Shadow: \n${shadow.asJson()}",
            )
        } else {
            shadow.forEach { formue ->
                original.find { formue.type == it.type && formue.tittel == it.tittel && formue.kilde == it.kilde }
                    ?.let { if (!isValid(formue.belop, it.belop)) null else 1 }
                    ?: logger.warn(
                        "Fant ikke formuer \n${formue.asJson()}" +
                            "\n i orginal: \n${original.asJson()}",
                    )
            }
        }
    }

    private fun JsonOkonomioversikt.compareInntekter(shadowJson: JsonOkonomioversikt) {
        val original = this.inntekt
        val shadow = shadowJson.inntekt

        if (original.size != shadow.size) {
            logger.warn(
                "Ikke likt antall inntekter i oversikt. \nOriginal: \n${original.asJson()}" +
                    " \n** Shadow: \n${shadow.asJson()}",
            )
        } else {
            shadow.forEach { inntekt ->
                original.find { inntekt.type == it.type && inntekt.tittel == it.tittel && inntekt.kilde == it.kilde }
                    ?.let { compareNumbers(inntekt, it) }
                    ?: logger.warn(
                        "Fant ikke inntekt \n${inntekt.asJson()} " +
                            "\ni orginal: \n${original.asJson()}",
                    )
            }
        }
    }

    private fun JsonOkonomioversikt.compareUtgifter(shadowJson: JsonOkonomioversikt) {
        val original = this.utgift
        val shadow = shadowJson.utgift

        if (original.size != shadow.size) {
            logger.warn(
                "Ikke likt antall utgifter i oversikt. \nOriginal: \n${original.asJson()}" +
                    "\n ** Shadow: \n${shadow.asJson()}",
            )
        } else {
            shadow.forEach { utgift ->
                original.find { utgift.type == it.type && utgift.tittel == it.tittel && utgift.kilde == it.kilde }
                    ?.let { if (!isValid(utgift.belop, it.belop)) null else 1 }
                    ?: logger.warn(
                        "Fant ikke utgift \n${utgift.asJson()}" +
                            " \ni orginal: \n${original.asJson()}",
                    )
            }
        }
    }
}

private fun JsonOkonomiOpplysningUtgift.tittelWithoutWhitespaces() =
    tittel?.replace("\\s".toRegex(), "") ?: null

private fun Any?.asJson() =
    JsonSosialhjelpObjectMapper.createObjectMapper().writeValueAsString(this)
        ?: "null"

private fun compareNumbers(
    shadow: JsonOkonomiOpplysningUtbetaling,
    original: JsonOkonomiOpplysningUtbetaling,
): Int? {
    if (!isValid(shadow.brutto, original.brutto)) return null
    if (!isValid(shadow.netto, original.netto)) return null
    if (!isValid(shadow.belop, original.belop)) return null
    if (!isValid(shadow.skattetrekk, original.skattetrekk)) return null
    if (!isValid(shadow.andreTrekk, original.andreTrekk)) return null

    return 1
}

private fun compareNumbers(
    shadow: JsonOkonomioversiktInntekt,
    original: JsonOkonomioversiktInntekt,
): Int? {
    if (!isValid(shadow.brutto, original.brutto)) return null
    if (!isValid(shadow.netto, original.netto)) return null

    return 1
}

private fun isValid(
    shadow: Double?,
    original: Double?,
): Boolean {
    if (shadow != original) {
        if (shadow != null && original != null) {
            return false
        } else {
            if (shadow == null && original != 0.0) return false
            if (original == null && shadow != 0.0) return false
        }
    }
    return true
}

private fun isValid(
    shadow: Int?,
    original: Int?,
): Boolean {
    if (shadow != original) {
        if (shadow != null && original != null) {
            return false
        } else {
            if (shadow == null && original != 0) return false
            if (original == null && shadow != 0) return false
        }
    }
    return true
}
