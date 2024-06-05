package no.nav.sosialhjelp.soknad.vedlegg.exceptions

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException

open class DokumentUploadError(message: String, cause: Throwable? = null, id: String? = null) :
    SosialhjelpSoknadApiException(message, cause, id)

class DokumentUploadPossibleVirus(message: String) : DokumentUploadError(message)

class DokumentUploadUnsupportedMediaType(message: String) : DokumentUploadError(message)

class DokumentUploadFileEncrypted : DokumentUploadError("PDF kan ikke være kryptert")

class DokumentUploadDuplicateFilename : DokumentUploadError("fil med samme navn eksisterer allerede i søknad")
