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

    // Validering av inputdata mislyktes
    UgyldigInput,

    // Placeholder for generelle feil
    GeneralError,

    // Ikke tilgang til ressurs
    Forbidden,

    // Ikke tilgang til ressurs
    NoAccess,

    // Søknad er allerede sendt inn
    SoknadAlleredeSendt,

    // Samtidig oppdatering av søknad
    SoknadUpdateConflict,

    // PDL-kall feilet
    PdlKallFeilet,

    // Søker er under 18 år
    SokerUnder18,

    // For mange soknader sendt siste 24 timer
    AntallSoknaderSendt,

    // Søknaden er i en tilstand som gjør at den aldri vil kunne sendes inn. Retry hjelper ikke.
    BrokenSoknad,

    // Mottak er allerede påbegynt hos FIKS
    MottakPabegynt,
}
