package no.nav.sosialhjelp.soknad.app.exceptions

import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "responseType")
@Schema(
    discriminatorProperty = "responseType",
    discriminatorMapping = [
        DiscriminatorMapping(value = "SoknadApiError", schema = SoknadApiError::class),
    ],
)
data class SoknadApiError(
    val error: SoknadApiErrorType,
    @Hidden
    val e: SosialhjelpSoknadApiException? = null,
    val responseType: String = "SoknadApiError",
) {
    val id: String?
        get() = e?.id ?: LEGACY_ID[error]

    val message: String?
        get() = e?.message ?: LEGACY_MESSAGE[error]

    companion object {
        // disse kan fases ut
        val LEGACY_ID =
            mapOf<SoknadApiErrorType, String?>(
                SoknadApiErrorType.InnsendingMidlertidigUtilgjengelig to "innsending_midlertidig_utilgjengelig",
                SoknadApiErrorType.InnsendingIkkeAktivert to "innsending_ikke_aktivert",
                SoknadApiErrorType.InnsendingUtilgjengelig to "innsending_ikke_tilgjengelig",
                SoknadApiErrorType.PlanlagtNedetid to "nedetid",
                SoknadApiErrorType.PdfGenereringFeilet to "pdf_generering",
                SoknadApiErrorType.NotFound to "soknad_not_found",
                SoknadApiErrorType.DokumentKonverteringFeilet to "filkonvertering_error",
                SoknadApiErrorType.DokumentUploadDuplicateFilename to "duplikat_fil",
                SoknadApiErrorType.DokumentUploadTooLarge to "vedlegg.opplasting.feil.forStor",
                SoknadApiErrorType.SoknadUpdateConflict to "web_application_error",
            )

        // disse kan fases ut
        val LEGACY_MESSAGE =
            mapOf<SoknadApiErrorType, String?>(
                SoknadApiErrorType.InnsendingMidlertidigUtilgjengelig to "Tjenesten er midlertidig utilgjengelig hos kommunen",
                SoknadApiErrorType.InnsendingIkkeAktivert to "Tjenesten er ikke aktivert hos kommunen",
                SoknadApiErrorType.InnsendingUtilgjengelig to "Tjenesten er midlertidig ikke tilgjengelig",
                SoknadApiErrorType.PlanlagtNedetid to "Søknaden har planlagt nedetid nå",
                SoknadApiErrorType.PdfGenereringFeilet to "Rendering av søknad til PDF mislyktes",
                SoknadApiErrorType.NotFound to "Ingen søknad med denne behandlingsId funnet",
                SoknadApiErrorType.DokumentUploadDuplicateFilename to "Fil er allerede lastet opp",
                SoknadApiErrorType.SoknadAlleredeSendt to "Søknaden er allerede sendt inn",
                SoknadApiErrorType.Forbidden to "Ikke tilgang til ressurs",
                SoknadApiErrorType.DokumentUploadError to "Feil ved opplasting av dokument",
                SoknadApiErrorType.DokumentUploadTooLarge to "Kunne ikke lagre fil fordi total filstørrelse er for stor",
                SoknadApiErrorType.SoknadUpdateConflict to "Samtidig oppdatering av søknad",
            )
    }
}
