package no.nav.sosialhjelp.soknad.organisasjon

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOrganisasjon
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
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

    fun mapToJsonOrganisasjon(orgnr: String): JsonOrganisasjon? {
        if (orgnr.matches(Regex("\\d{9}"))) {
            return JsonOrganisasjon()
                .withNavn(hentOrgNavn(orgnr))
                .withOrganisasjonsnummer(orgnr)
        }
        if (orgnr.matches(Regex("\\d{11}"))) {
            log.info("Utbetalingens opplysningspliktigId er et personnummer. Dette blir ikke inkludert i soknad.json")
        } else {
            log.error("Utbetalingens opplysningspliktigId er verken et organisasjonsnummer eller personnummer: $orgnr. Kontakt skatteetaten.")
        }
        return null
    }

    companion object {
        private val log = LoggerFactory.getLogger(OrganisasjonService::class.java)
    }
}
