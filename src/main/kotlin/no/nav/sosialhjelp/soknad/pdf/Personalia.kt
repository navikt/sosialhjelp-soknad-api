package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonPostboksAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia
import no.nav.sosialhjelp.soknad.pdf.PdfGenerator.Companion.INNRYKK_2
import no.nav.sosialhjelp.soknad.pdf.PdfGenerator.Companion.INNRYKK_4

fun leggTilPersonalia(
    pdf: PdfGenerator,
    pdfUtils: PdfUtils,
    textHelpers: TextHelpers,
    jsonPersonalia: JsonPersonalia,
    midlertidigAdresse: JsonAdresse?,
    utvidetSoknad: Boolean,
) {
    pdf.skrivH4Bold(pdfUtils.getTekst("kontakt.tittel"))
    pdf.addBlankLine()

    if (utvidetSoknad) {
        pdf.skrivH4Bold(pdfUtils.getTekst("kontakt.system.personalia.sporsmal"))
        pdfUtils.skrivInfotekst(pdf, "kontakt.system.personalia.infotekst.tekst")
    }

    // Statsborgerskap
    pdf.skrivTekstBold(pdfUtils.getTekst("kontakt.system.personalia.statsborgerskap"))
    pdf.skrivTekst(textHelpers.fulltNavnForLand(jsonPersonalia.statsborgerskap?.verdi))
    pdf.addBlankLine()

    // Adresse
    if (utvidetSoknad) {
        pdf.skrivH4Bold(pdfUtils.getTekst("soknadsmottaker.sporsmal"))
        pdfUtils.skrivHjelpetest(pdf, "soknadsmottaker.hjelpetekst.tekst")
    } else {
        pdf.skrivTekstBold(pdfUtils.getTekst("kontakt.system.adresse"))
    }

    jsonPersonalia.folkeregistrertAdresse?.let {
        pdf.skrivTekst(pdfUtils.getTekst("kontakt.system.oppholdsadresse.folkeregistrertAdresse"))
        val folkeregistrertAdresseTekst = when (it.type) {
            JsonAdresse.Type.GATEADRESSE -> jsonGateAdresseToString(it as JsonGateAdresse)
            JsonAdresse.Type.MATRIKKELADRESSE -> jsonMatrikkelAdresseToString(pdfUtils, it as JsonMatrikkelAdresse)
            JsonAdresse.Type.POSTBOKS -> jsonPostboksAdresseToString(pdfUtils, it as JsonPostboksAdresse)
            JsonAdresse.Type.USTRUKTURERT -> jsonUstrukturertAdresseToString(it as JsonUstrukturertAdresse)
        }
        pdf.skrivTekst(folkeregistrertAdresseTekst)
        pdf.addBlankLine()
    }

    jsonPersonalia.oppholdsadresse?.let {
        pdf.skrivTekst(pdfUtils.getTekst("soknadsmottaker.infotekst.tekst"))

        val oppholdsAdresseTekst = when (it.type) {
            JsonAdresse.Type.GATEADRESSE -> jsonGateAdresseToString(it as JsonGateAdresse)
            JsonAdresse.Type.MATRIKKELADRESSE -> jsonMatrikkelAdresseToString(pdfUtils, it as JsonMatrikkelAdresse)
            JsonAdresse.Type.POSTBOKS -> jsonPostboksAdresseToString(pdfUtils, it as JsonPostboksAdresse)
            JsonAdresse.Type.USTRUKTURERT -> jsonUstrukturertAdresseToString(it as JsonUstrukturertAdresse)
        }
        pdf.skrivTekst(oppholdsAdresseTekst)
        pdf.addBlankLine()
    }

    if (utvidetSoknad) {
        pdf.skrivTekst("Valgt adresse:")
        if (jsonPersonalia.oppholdsadresse != null) {
            val adresseValg = jsonPersonalia.oppholdsadresse.adresseValg
            if (adresseValg == JsonAdresseValg.SOKNAD) {
                pdf.skrivTekstMedInnrykk(pdfUtils.getTekst("kontakt.system.oppholdsadresse.valg.soknad"), INNRYKK_2)
            } else {
                pdf.skrivTekstMedInnrykk(pdfUtils.getTekst("kontakt.system.oppholdsadresse." + adresseValg.value() + "Adresse"), INNRYKK_2)
            }
            pdf.addBlankLine()

            pdf.skrivTekstBold("Svaralternativer:")
            jsonPersonalia.folkeregistrertAdresse?.let {
                pdf.skrivTekstMedInnrykk(pdfUtils.getTekst("kontakt.system.oppholdsadresse.folkeregistrertAdresse"), INNRYKK_2)
            }

            midlertidigAdresse?.let {
                pdf.skrivTekstMedInnrykk(pdfUtils.getTekst("kontakt.system.oppholdsadresse.midlertidigAdresse"), INNRYKK_2)
                if (adresseValg == JsonAdresseValg.MIDLERTIDIG) {
                    leggTilUtvidetInfoAdresse(pdf, pdfUtils, jsonPersonalia.oppholdsadresse)
                } else {
                    leggTilUtvidetInfoAdresse(pdf, pdfUtils, it)
                }
            }

            pdf.skrivTekstMedInnrykk(pdfUtils.getTekst("kontakt.system.oppholdsadresse.valg.soknad"), INNRYKK_2)
            pdf.addBlankLine()
        }
    }

    // Telefonnummer
    val jsonTelefonnummer = jsonPersonalia.telefonnummer
    if (jsonTelefonnummer != null) {
        if (jsonTelefonnummer.kilde == JsonKilde.SYSTEM) {
            pdf.skrivTekstBold(pdfUtils.getTekst("kontakt.system.telefoninfo.sporsmal"))
            if (utvidetSoknad) {
                pdfUtils.skrivInfotekst(pdf, "kontakt.system.telefoninfo.infotekst.tekst")
            }
            pdf.skrivTekst(pdfUtils.getTekst("kontakt.system.telefon.label"))
            pdf.skrivTekst(jsonTelefonnummer.verdi)
            pdf.addBlankLine()
            if (utvidetSoknad) {
                pdfUtils.skrivKnappTilgjengelig(pdf, "kontakt.system.telefon.endreknapp.label")
            }
        } else {
            pdf.skrivTekstBold(pdfUtils.getTekst("kontakt.telefon.sporsmal"))
            pdf.skrivTekst(pdfUtils.getTekst("kontakt.telefon.label"))
            if (jsonTelefonnummer.verdi == null || jsonTelefonnummer.verdi.isEmpty()) {
                pdfUtils.skrivIkkeUtfylt(pdf)
            } else {
                pdf.skrivTekst(jsonTelefonnummer.verdi)
            }
            pdf.addBlankLine()
            if (utvidetSoknad) {
                pdfUtils.skrivKnappTilgjengelig(pdf, "systeminfo.avbrytendringknapp.label")
                pdfUtils.skrivInfotekst(pdf, "kontakt.telefon.infotekst.tekst")
            }
        }
    }

    // Kontonummer
    jsonPersonalia.kontonummer?.let {
        pdf.skrivTekstBold(pdfUtils.getTekst("kontakt.kontonummer.sporsmal"))
        if (utvidetSoknad && it.kilde == JsonKilde.SYSTEM) {
            pdfUtils.skrivInfotekst(pdf, "kontakt.system.personalia.infotekst.tekst")
        }
        if (it.kilde == JsonKilde.SYSTEM) {
            pdf.skrivTekst(pdfUtils.getTekst("kontakt.system.kontonummer.label"))
        } else {
            pdf.skrivTekst(pdfUtils.getTekst("kontakt.kontonummer.label"))
        }
        if (it.harIkkeKonto != null && it.harIkkeKonto) {
            pdf.skrivTekst(pdfUtils.getTekst("kontakt.kontonummer.harikke.true"))
        } else {
            if (it.verdi == null || it.verdi.isEmpty()) {
                pdfUtils.skrivIkkeUtfylt(pdf)
            } else {
                pdf.skrivTekst(it.verdi)
            }
        }
        pdf.addBlankLine()

        if (utvidetSoknad) {
            if (it.kilde == JsonKilde.SYSTEM) {
                pdfUtils.skrivKnappTilgjengelig(pdf, "kontakt.system.kontonummer.endreknapp.label")
            } else {
                val svaralternativer: MutableList<String> = ArrayList(1)
                svaralternativer.add("kontakt.kontonummer.harikke")
                pdfUtils.skrivSvaralternativer(pdf, svaralternativer)
                pdfUtils.skrivKnappTilgjengelig(pdf, "systeminfo.avbrytendringknapp.label")
                pdfUtils.skrivInfotekst(pdf, "kontakt.kontonummer.infotekst.tekst")
            }
        }
    }
    pdf.addBlankLine()
}

private fun leggTilUtvidetInfoAdresse(pdf: PdfGenerator, pdfUtils: PdfUtils, jsonAdresse: JsonAdresse) {
    when (jsonAdresse.type) {
        JsonAdresse.Type.GATEADRESSE -> {
            val gateAdresse = jsonAdresse as JsonGateAdresse
            pdf.skrivTekstMedInnrykk("${gateAdresse.gatenavn} ${gateAdresse.husnummer}${gateAdresse.husbokstav}", INNRYKK_4)
            pdf.skrivTekstMedInnrykk("${gateAdresse.postnummer} ${gateAdresse.poststed}", INNRYKK_4)
        }
        JsonAdresse.Type.MATRIKKELADRESSE -> {
            val matrikkelAdresse = jsonAdresse as JsonMatrikkelAdresse
            pdf.skrivTekstMedInnrykk("${pdfUtils.getTekst("kontakt.system.adresse.bruksnummer.label")}: ${matrikkelAdresse.bruksnummer}. ${pdfUtils.getTekst("kontakt.system.adresse.gaardsnummer.label")}: ${matrikkelAdresse.gaardsnummer}. ${pdfUtils.getTekst("kontakt.system.adresse.kommunenummer.label")}:${matrikkelAdresse.kommunenummer}.", INNRYKK_2)
        }
        JsonAdresse.Type.POSTBOKS -> {
            val postboksAdresse = jsonAdresse as JsonPostboksAdresse
            pdf.skrivTekstMedInnrykk("${pdfUtils.getTekst("kontakt.system.adresse.postboks.label")}: ${postboksAdresse.postboks}, ${postboksAdresse.postnummer} ${postboksAdresse.poststed}", INNRYKK_2)
        }
        JsonAdresse.Type.USTRUKTURERT -> {
            val ustrukturertAdresse = jsonAdresse as JsonUstrukturertAdresse
            pdf.skrivTekstMedInnrykk(java.lang.String.join(" ", ustrukturertAdresse.adresse), INNRYKK_2)
        }
    }
}

private fun jsonGateAdresseToString(jsonGateAdresse: JsonGateAdresse): String {
    val adresse = StringBuilder()
    jsonGateAdresse.gatenavn?.let { adresse.append(it).append(" ") }
    jsonGateAdresse.husnummer?.let { adresse.append(it) }
    jsonGateAdresse.husbokstav?.let { adresse.append(it) }
    adresse.append(", ")
    jsonGateAdresse.postnummer?.let { adresse.append(it).append(" ") }
    jsonGateAdresse.poststed?.let { adresse.append(it) }
    return adresse.toString()
}

private fun jsonMatrikkelAdresseToString(pdfUtils: PdfUtils, matrikkelAdresse: JsonMatrikkelAdresse): String {
    return buildString {
        append(pdfUtils.getTekst("kontakt.system.adresse.bruksnummer.label"))
        append(": ")
        append(matrikkelAdresse.bruksnummer)
        append(". ")
        append(pdfUtils.getTekst("kontakt.system.adresse.gaardsnummer.label"))
        append(": ")
        append(matrikkelAdresse.gaardsnummer)
        append(". ")
        append(pdfUtils.getTekst("kontakt.system.adresse.kommunenummer.label"))
        append(":")
        append(matrikkelAdresse.kommunenummer)
        append(".")
    }
}

private fun jsonPostboksAdresseToString(pdfUtils: PdfUtils, postboksAdresse: JsonPostboksAdresse): String {
    return buildString {
        append(pdfUtils.getTekst("kontakt.system.adresse.postboks.label"))
        append(": ")
        append(postboksAdresse.postboks)
        append(", ")
        append(postboksAdresse.postnummer)
        append(" ")
        append(postboksAdresse.poststed)
    }
}

private fun jsonUstrukturertAdresseToString(ustrukturertAdresse: JsonUstrukturertAdresse): String {
    return ustrukturertAdresse.adresse.joinToString(separator = " ", prefix = " ") { it }
}
