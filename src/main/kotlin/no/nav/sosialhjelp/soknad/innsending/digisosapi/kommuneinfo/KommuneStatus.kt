package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

enum class KommuneStatus {
    HAR_KONFIGURASJON_MED_MANGLER,
    MANGLER_KONFIGURASJON,
    SKAL_SENDE_SOKNADER_VIA_FDA,
    SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD,
    FIKS_NEDETID_OG_TOM_CACHE
}
