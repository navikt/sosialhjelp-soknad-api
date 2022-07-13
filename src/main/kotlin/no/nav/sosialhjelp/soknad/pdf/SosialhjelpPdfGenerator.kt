package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sosialhjelp.soknad.pdf.ArbeidOgUtdanning.leggTilArbeidOgUtdanning
import no.nav.sosialhjelp.soknad.pdf.Begrunnelse.leggTilBegrunnelse
import no.nav.sosialhjelp.soknad.pdf.Bosituasjon.leggTilBosituasjon
import no.nav.sosialhjelp.soknad.pdf.Familie.leggTilFamilie
import no.nav.sosialhjelp.soknad.pdf.InformasjonFraForside.leggTilInformasjonFraForsiden
import no.nav.sosialhjelp.soknad.pdf.InntektOgFormue.leggTilInntektOgFormue
import no.nav.sosialhjelp.soknad.pdf.JuridiskInformasjon.leggTilJuridiskInformasjon
import no.nav.sosialhjelp.soknad.pdf.Metainformasjon.leggTilMetainformasjon
import no.nav.sosialhjelp.soknad.pdf.OkonomiskeOpplysningerOgVedlegg.leggTilOkonomiskeOpplysningerOgVedlegg
import no.nav.sosialhjelp.soknad.pdf.Personalia.leggTilPersonalia
import no.nav.sosialhjelp.soknad.pdf.UtgifterOgGjeld.leggTilUtgifterOgGjeld
import no.nav.sosialhjelp.soknad.pdf.Utils.DATO_OG_TID_FORMAT
import no.nav.sosialhjelp.soknad.pdf.Utils.getJsonNavnTekst
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import org.apache.commons.lang3.LocaleUtils
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class SosialhjelpPdfGenerator(
    private val navMessageSource: NavMessageSource,
    private val textHelpers: TextHelpers,
    private val pdfUtils: PdfUtils
) {
    fun generate(jsonInternalSoknad: JsonInternalSoknad, utvidetSoknad: Boolean): ByteArray {
        return try {
            val pdf = PdfGenerator()

            val data = jsonInternalSoknad.soknad.data
            val jsonPersonalia = data.personalia // personalia er required

            // Add header
            val heading = getTekst("applikasjon.sidetittel")
            val jsonPersonIdentifikator = jsonPersonalia.personIdentifikator // required
            val jsonSokernavn = jsonPersonalia.navn // required

            val navn = getJsonNavnTekst(jsonSokernavn)

            val fnr = jsonPersonIdentifikator.verdi // required

            leggTilHeading(pdf, heading, navn, fnr)

            leggTilPersonalia(pdf, pdfUtils, textHelpers, data.personalia, jsonInternalSoknad.midlertidigAdresse, utvidetSoknad)
            leggTilBegrunnelse(pdf, pdfUtils, data.begrunnelse, utvidetSoknad)
            leggTilArbeidOgUtdanning(pdf, pdfUtils, data.arbeid, data.utdanning, utvidetSoknad)
            leggTilFamilie(pdf, pdfUtils, data.familie, utvidetSoknad)
            leggTilBosituasjon(pdf, pdfUtils, data.bosituasjon, utvidetSoknad)
            leggTilInntektOgFormue(pdf, pdfUtils, data.okonomi, jsonInternalSoknad.soknad, utvidetSoknad)
            leggTilUtgifterOgGjeld(pdf, pdfUtils, data.okonomi, jsonInternalSoknad.soknad, utvidetSoknad)
            leggTilOkonomiskeOpplysningerOgVedlegg(pdf, pdfUtils, data.okonomi, jsonInternalSoknad.vedlegg, utvidetSoknad)
            leggTilInformasjonFraForsiden(pdf, pdfUtils, data.personalia, utvidetSoknad)
            leggTilJuridiskInformasjon(pdf, jsonInternalSoknad.soknad, utvidetSoknad)
            leggTilMetainformasjon(pdf, jsonInternalSoknad.soknad)

            pdf.finish()
        } catch (e: Exception) {
            if (utvidetSoknad) {
                throw PdfGenereringException("Kunne ikke generere Soknad-juridisk.pdf", e)
            }
            throw PdfGenereringException("Kunne ikke generere Soknad.pdf", e)
        }
    }

    fun generateEttersendelsePdf(jsonInternalSoknad: JsonInternalSoknad, eier: String): ByteArray {
        return try {
            val pdf = PdfGenerator()

            val tittel = getTekst("ettersending.kvittering.tittel")
            val undertittel = getTekst("skjema.tittel")
            leggTilHeading(pdf, tittel, undertittel, eier)

            val formatter = DateTimeFormatter.ofPattern(DATO_OG_TID_FORMAT)
            val formattedTime = LocalDateTime.now().format(formatter)

            pdf.skrivTekstBold("Følgende vedlegg er sendt $formattedTime:")
            pdf.addBlankLine()

            jsonInternalSoknad.vedlegg?.vedlegg?.forEach { jsonVedlegg ->
                if (jsonVedlegg.status != null && jsonVedlegg.status == "LastetOpp") {
                    pdf.skrivTekst(getTekst("vedlegg.${jsonVedlegg.type}.${jsonVedlegg.tilleggsinfo}.tittel"))
                    pdf.skrivTekst("Filer:")
                    jsonVedlegg.filer.forEach { jsonFiler ->
                        pdf.skrivTekst("Filnavn: " + jsonFiler.filnavn)
                    }
                }
            }
            pdf.finish()
        } catch (e: Exception) {
            throw PdfGenereringException("Kunne ikke generere ettersendelse.pdf", e)
        }
    }

    fun generateBrukerkvitteringPdf(): ByteArray {
        return try {
            val pdf = PdfGenerator()

            leggTilHeading(pdf, "Brukerkvittering")

            pdf.skrivTekst("Fil ikke i bruk, generert for bakoverkompatibilitet med filformat / File not in use, generated for backward compatibility with fileformat")

            pdf.finish()
        } catch (e: Exception) {
            throw PdfGenereringException("Kunne ikke generere Brukerkvittering.pdf", e)
        }
    }

    fun getTekst(key: String?): String {
        return navMessageSource.getBundleFor("soknadsosialhjelp", LocaleUtils.toLocale("nb_NO")).getProperty(key)
    }

    private fun leggTilHeading(pdf: PdfGenerator, heading: String, vararg undertitler: String) {
        pdf.addCenteredH1Bold(heading)
        undertitler
            .filter { it.isNotEmpty() }
            .forEach { pdf.addCenteredH4Bold(it) }
        pdf.addDividerLine()
        pdf.addBlankLine()
    }
}
