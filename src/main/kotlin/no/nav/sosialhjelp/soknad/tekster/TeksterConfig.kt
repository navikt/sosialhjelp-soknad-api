package no.nav.sosialhjelp.soknad.tekster

import no.nav.sosialhjelp.soknad.tekster.NavMessageSource.Bundle
import org.slf4j.LoggerFactory.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@EnableScheduling
@Configuration
class TeksterConfig(
    @Value("\${scheduler.disable}") private val schedulerDisabled: Boolean
) {

    @Bean
    fun navMessageSource(): NavMessageSource {
        val messageSource = NavMessageSource()
        val bundle = getBundle(SOKNADSOSIALHJELP)
        val fellesBundle = getBundle("sendsoknad")
        messageSource.setBasenames(fellesBundle, bundle)
        messageSource.setDefaultEncoding("UTF-8")

        // Sjekk for nye filer en gang hvert 15. sekund.
        messageSource.setCacheSeconds(15)
        return messageSource
    }

    @Scheduled(fixedRate = FEM_MINUTTER)
    private fun slettCache() {
        if (schedulerDisabled) {
            log.info("Scheduler is disabled")
            return
        }
        navMessageSource().clearCache()
    }

    private fun getBundle(bundleName: String): Bundle {
        return Bundle(bundleName, "classpath:/$bundleName")
    }

    companion object {
        private const val FEM_MINUTTER = 1000 * 60 * 5L
        private const val SOKNADSOSIALHJELP = "soknadsosialhjelp"

        private val log = getLogger(TeksterConfig::class.java)
    }
}
