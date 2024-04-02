package no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo

enum class KommuneStatus {
    MANGLER_KONFIGURASJON,
    SKAL_SOKNADER_VIA_DIGISOS_API,
    SKAL_VISE_MIDLERTIDIG_FEILSIDE_FOR_SOKNAD,
    FIKS_NEDETID_OG_TOM_CACHE
}
