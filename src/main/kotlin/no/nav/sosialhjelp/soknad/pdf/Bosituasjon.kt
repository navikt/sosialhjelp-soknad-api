package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.bosituasjon.JsonBosituasjon

object Bosituasjon {
    fun leggTilBosituasjon(
        pdf: PdfGenerator,
        pdfUtils: PdfUtils,
        bosituasjon: JsonBosituasjon?,
        utvidetSoknad: Boolean
    ) {
        pdf.skrivH4Bold(pdfUtils.getTekst("bosituasjonbolk.tittel"))
        pdf.addBlankLine()

        if (bosituasjon != null) {
            pdf.skrivTekstBold(pdfUtils.getTekst("bosituasjon.sporsmal"))
            val botype = bosituasjon.botype
            if (botype != null) {
                val tekst = pdfUtils.getTekst("bosituasjon." + botype.value())
                if (tekst.isNullOrEmpty()) {
                    pdf.skrivTekst(pdfUtils.getTekst("bosituasjon.annet.botype." + botype.value()))
                } else {
                    pdf.skrivTekst(tekst)
                }
            } else {
                pdfUtils.skrivIkkeUtfylt(pdf)
            }
            pdf.addBlankLine()

            if (utvidetSoknad) {
                val svaralternativer: MutableList<String> = ArrayList(5)
                svaralternativer.add("bosituasjon.eier")
                svaralternativer.add("bosituasjon.kommunal")
                svaralternativer.add("bosituasjon.leier")
                svaralternativer.add("bosituasjon.ingen")
                svaralternativer.add("bosituasjon.annet")
                pdfUtils.skrivSvaralternativer(pdf, svaralternativer)

                pdf.skrivTekstBold(pdfUtils.getTekst("bosituasjon.annet"))
                val andreAlternativer: MutableList<String> = ArrayList()
                andreAlternativer.add("bosituasjon.annet.botype.foreldre")
                andreAlternativer.add("bosituasjon.annet.botype.familie")
                andreAlternativer.add("bosituasjon.annet.botype.venner")
                andreAlternativer.add("bosituasjon.annet.botype.institusjon")
                andreAlternativer.add("bosituasjon.annet.botype.fengsel")
                andreAlternativer.add("bosituasjon.annet.botype.krisesenter")
                pdfUtils.skrivSvaralternativer(pdf, andreAlternativer)
            }

            pdf.skrivTekstBold(pdfUtils.getTekst("bosituasjon.antallpersoner.sporsmal"))
            bosituasjon.antallPersoner
                ?.let { pdf.skrivTekst(it.toString()) }
                ?: pdfUtils.skrivIkkeUtfylt(pdf)
        }
        pdf.addBlankLine()
    }
}
