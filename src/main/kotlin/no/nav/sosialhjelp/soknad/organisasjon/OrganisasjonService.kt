package no.nav.sosialhjelp.soknad.organisasjon

import org.slf4j.LoggerFactory

open class OrganisasjonService(
    private val organisasjonClient: OrganisasjonClient
) {

    open fun hentOrgNavn(orgnr: String?): String {
        return if (orgnr != null) {
            try {
                val noekkelinfo = organisasjonClient.hentOrganisasjonNoekkelinfo(orgnr)
                if (noekkelinfo == null) {
                    log.warn("Kunne ikke hente orgnr fra Ereg: $orgnr")
                    return orgnr
                }
                mutableListOf(
                    noekkelinfo.navn.navnelinje1,
                    noekkelinfo.navn.navnelinje2,
                    noekkelinfo.navn.navnelinje3,
                    noekkelinfo.navn.navnelinje4,
                    noekkelinfo.navn.navnelinje5
                )
                    .filterNotNull()
                    .filter { it.isNotEmpty() }
                    .joinToString(separator = ", ") { it }
            } catch (e: Exception) {
                log.warn("Kunne ikke hente orgnr fra Ereg: $orgnr", e)
                orgnr
            }
        } else {
            ""
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OrganisasjonService::class.java)
    }
}
