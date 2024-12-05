package no.nav.sosialhjelp.soknad.pdf

import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
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
import no.nav.sosialhjelp.soknad.pdf.Situasjonsendring.leggTilSituasjonsendring
import no.nav.sosialhjelp.soknad.pdf.UtgifterOgGjeld.leggTilUtgifterOgGjeld
import no.nav.sosialhjelp.soknad.pdf.Utils.getJsonNavnTekst
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import org.apache.commons.lang3.LocaleUtils
import org.springframework.stereotype.Component

@Component
class SosialhjelpPdfGenerator(
    private val navMessageSource: NavMessageSource,
    private val textHelpers: TextHelpers,
    private val pdfUtils: PdfUtils,
) {
    fun generate(
        jsonInternalSoknad: JsonInternalSoknad,
        utvidetSoknad: Boolean,
    ): ByteArray =
        try {
            val pdf = PdfGenerator()

            val data = jsonInternalSoknad.soknad.data
            val jsonPersonalia = data.personalia // personalia er required

            // Add header
            val heading = getTekst("applikasjon.sidetittel")
            val jsonPersonIdentifikator = jsonPersonalia.personIdentifikator // required
            val jsonSokernavn = jsonPersonalia.navn // required

            val isKortSoknad = jsonInternalSoknad.soknad.data?.soknadstype == JsonData.Soknadstype.KORT

            val navn = getJsonNavnTekst(jsonSokernavn)

            val fnr = jsonPersonIdentifikator.verdi // required

            val soknadstype = "Type søknad: " + if (jsonInternalSoknad.soknad.data?.soknadstype == JsonData.Soknadstype.KORT) "kort" else "standard"

            leggTilHeading(pdf, heading, soknadstype, navn, fnr)

            leggTilPersonalia(pdf, pdfUtils, textHelpers, data.personalia, jsonInternalSoknad.midlertidigAdresse, utvidetSoknad)
            leggTilBegrunnelse(pdf, pdfUtils, data.begrunnelse, utvidetSoknad, isKortSoknad)
            if (isKortSoknad) {
                leggTilSituasjonsendring(pdf, pdfUtils, utvidetSoknad, data.situasjonendring)
            }
            leggTilFamilie(pdf, pdfUtils, data.familie, utvidetSoknad, isKortSoknad)
            leggTilInntektOgFormue(pdf, pdfUtils, data.okonomi, jsonInternalSoknad.soknad, utvidetSoknad, isKortSoknad)
            if (!isKortSoknad) {
                leggTilArbeidOgUtdanning(pdf, pdfUtils, data.arbeid, data.utdanning, utvidetSoknad)
                leggTilBosituasjon(pdf, pdfUtils, data.bosituasjon, utvidetSoknad)
                leggTilUtgifterOgGjeld(pdf, pdfUtils, data.okonomi, jsonInternalSoknad.soknad, utvidetSoknad)
                leggTilOkonomiskeOpplysningerOgVedlegg(pdf, pdfUtils, data.okonomi, jsonInternalSoknad.vedlegg, utvidetSoknad)
            }
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

    fun generateBrukerkvitteringPdf(): ByteArray =
        try {
            val pdf = PdfGenerator()

            leggTilHeading(pdf, "Brukerkvittering")

            pdf.skrivTekst(
                "Fil ikke i bruk, generert for bakoverkompatibilitet med filformat / File not in use, generated for backward compatibility with fileformat",
            )

            pdf.finish()
        } catch (e: Exception) {
            throw PdfGenereringException("Kunne ikke generere Brukerkvittering.pdf", e)
        }

    fun getTekst(key: String?): String = navMessageSource.getBundleFor("soknadsosialhjelp", LocaleUtils.toLocale("nb_NO")).getProperty(key)

    private fun leggTilHeading(
        pdf: PdfGenerator,
        heading: String,
        vararg undertitler: String,
    ) {
        pdf.addCenteredH1Bold(heading)
        undertitler
            .filter { it.isNotEmpty() }
            .forEach { pdf.addCenteredH4Bold(it) }
        pdf.addDividerLine()
        pdf.addBlankLine()
    }
}
