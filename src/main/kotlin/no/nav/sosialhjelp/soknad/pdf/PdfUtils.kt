package no.nav.sosialhjelp.soknad.pdf

import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import org.apache.commons.lang3.LocaleUtils
import org.springframework.stereotype.Component

@Component
class PdfUtils(private val navMessageSource: NavMessageSource) {

    fun getTekst(key: String?): String? {
        return navMessageSource.getBundleFor("soknadsosialhjelp", LocaleUtils.toLocale("nb_NO")).getProperty(key)
    }

    fun skrivInfotekst(pdf: PdfGenerator, vararg keys: String?) {
        pdf.skrivTekstBold(getTekst("infotekst.oppsummering.tittel"))
        for (key in keys) {
            if (!key.isNullOrEmpty()) {
                pdf.skrivTekst(getTekst(key))
            }
        }
        pdf.addBlankLine()
    }

    fun skrivHjelpetest(pdf: PdfGenerator, key: String?) {
        pdf.skrivTekstBold(getTekst("hjelpetekst.oppsummering.tittel"))
        pdf.skrivTekst(getTekst(key))
        pdf.addBlankLine()
    }

    fun skrivKnappTilgjengelig(pdf: PdfGenerator, key: String) {
        pdf.skrivTekstBold("Knapp tilgjengelig:")
        pdf.skrivTekst(getTekst(key))
        pdf.addBlankLine()
    }

    fun skrivIkkeUtfylt(pdf: PdfGenerator) {
        pdf.skrivTekst(getTekst("oppsummering.ikkeutfylt"))
    }

    fun skrivIkkeUtfyltMedGuard(pdf: PdfGenerator, key: String) {
        pdf.skrivTekst(getTekst(key) + ": " + getTekst("oppsummering.ikkeutfylt"))
    }

    fun skrivTekstMedGuard(pdf: PdfGenerator, tekst: String?, key: String) {
        if (tekst != null) {
            pdf.skrivTekst(getTekst(key) + ": " + tekst)
        }
    }

    fun skrivTekstMedGuardOgIkkeUtfylt(pdf: PdfGenerator, tekst: String?, key: String) {
        if (tekst != null) {
            pdf.skrivTekst(getTekst(key) + ": " + tekst)
        } else {
            skrivIkkeUtfyltMedGuard(pdf, key)
        }
    }

    fun skrivTekstMedGuardOgIkkeUtfylt(pdf: PdfGenerator, verdi: Int?, key: String) {
        if (verdi != null) {
            pdf.skrivTekst(getTekst(key) + ": " + verdi)
        } else {
            pdf.skrivTekst(getTekst(key) + ": " + getTekst("oppsummering.ikkeutfylt"))
        }
    }

    fun skrivTekstMedGuardOgIkkeUtfylt(pdf: PdfGenerator, verdi: Double?, key: String) {
        if (verdi != null) {
            pdf.skrivTekst(getTekst(key) + ": " + String.format("%.2f", verdi))
        } else {
            pdf.skrivTekst(getTekst(key) + ": " + getTekst("oppsummering.ikkeutfylt"))
        }
    }

    fun addLinks(pdf: PdfGenerator, uris: Map<String, String>) {
        pdf.skrivTekst("Lenker på siden: ")
        for ((name, uri) in uris) {
            pdf.skrivTekst("$name: $uri")
        }
        pdf.addBlankLine()
    }

    fun skrivSvaralternativer(pdf: PdfGenerator, keys: List<String>) {
        pdf.skrivTekstBold("Svaralternativer:")
        for (key in keys) {
            pdf.skrivTekstMedInnrykk(getTekst(key), PdfGenerator.INNRYKK_2)
        }
    }

    fun skrivUtBarnebidragAlternativer(pdf: PdfGenerator, utvidetSoknad: Boolean) {
        if (utvidetSoknad) {
            val svaralternativer: MutableList<String> = ArrayList(4)
            svaralternativer.add("familie.barn.true.barnebidrag.betaler")
            svaralternativer.add("familie.barn.true.barnebidrag.mottar")
            svaralternativer.add("familie.barn.true.barnebidrag.begge")
            svaralternativer.add("familie.barn.true.barnebidrag.ingen")
            skrivSvaralternativer(pdf, svaralternativer)
        }
    }
}
