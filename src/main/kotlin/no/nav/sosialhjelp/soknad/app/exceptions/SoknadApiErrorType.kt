package no.nav.sosialhjelp.soknad.app.exceptions

enum class SoknadApiErrorType {
    // Tjenesten er midlertidig utilgjengelig hos kommunen
    InnsendingMidlertidigUtilgjengelig,

    // Tjenesten er ikke aktivert hos kommunen
    InnsendingIkkeAktivert,

    // FIXME: Hva er forskjell mellom denne, og InnsendingMidlertidigUtilgjengelig?
    // Tjenesten er midlertidig ikke tilgjengelig
    InnsendingUtilgjengelig,

    // Søknaden har planlagt nedetid nå
    PlanlagtNedetid,

    // Rendering av søknad til PDF mislyktes
    PdfGenereringFeilet,

    // Ingen søknad med denne behandlingsId funnet
    NotFound,

    // En fil med samme filnavn eksisterer allerede i søknad
    DokumentUploadDuplicateFilename,

    // En feil oppstod ved filkonvertering
    DokumentKonverteringFeilet,

    // Validering av inputdata mislyktes
    UgyldigInput,

    // Placeholder for generelle feil
    GeneralError,

    // Ikke tilgang til ressurs
    Forbidden,

    // Søknad er allerede sendt inn
    SoknadAlleredeSendt,

    // Feil ved opplasting av dokument
    DokumentUploadError,

    // Kunne ikke lagre fil fordi total filstørrelse er for stor
    DokumentUploadTooLarge,

    // Dokument ikke-støttet mediatype
    DokumentUploadUnsupportedMediaType,

    DokumentUploadFileEncrypted,

    DokumentUploadPossibleVirus,

    // Samtidig oppdatering av søknad
    SoknadUpdateConflict,

    // PDL-kall feilet
    PdlKallFeilet,
}
