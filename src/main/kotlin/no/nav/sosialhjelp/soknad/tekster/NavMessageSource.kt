package no.nav.sosialhjelp.soknad.tekster

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import java.util.Locale
import java.util.Properties

open class NavMessageSource : ReloadableResourceBundleMessageSource() {
    private val basenames: MutableMap<String, String> = HashMap()
    private var fellesBasename: String? = null

    fun getBundleFor(
        type: String?,
        locale: Locale,
    ): Properties {
        return if (basenames.containsKey(type)) {
            val properties = Properties()
            try {
                properties.putAll(hentProperties(fellesBasename, locale))
                properties.putAll(hentProperties(basenames[type], locale))
            } catch (ex: Exception) {
                log.error(
                    "Kunne ikke hente bundle for type=[$type], locale=[$locale], basenames=[$basenames], fellesbasenames=[$fellesBasename]",
                )
                throw ex
            }
            properties
        } else {
            getMergedProperties(
                locale,
            ).properties ?: throw SosialhjelpSoknadApiException("Noe feilet ved henting av tekster for locale=$locale")
        }
    }

    private fun hentProperties(
        propertiesFile: String?,
        locale: Locale,
    ): Properties {
        val localFile = calculateFilenameForLocale(propertiesFile, locale)
        val properties = getProperties(localFile).properties
        return if (properties != null) {
            properties
        } else {
            log.warn("Finner ikke tekster for $propertiesFile for språkbundle ${locale.language} for localefile $localFile.")
            val noLocale = Locale.forLanguageTag("nb-NO")
            if (locale == noLocale) {
                throw IllegalStateException("Kunne ikke laste tekster. Avbryter.")
            } else {
                hentProperties(propertiesFile, noLocale)
            }
        }
    }

    private fun calculateFilenameForLocale(
        type: String?,
        locale: Locale,
    ): String {
        return type + "_" + locale.language + if ("" == locale.country) "" else "_" + locale.country
    }

    fun setBasenames(
        fellesBundle: Bundle,
        vararg soknadBundles: Bundle,
    ) {
        fellesBasename = fellesBundle.propertiesFile
        val basenameStrings: MutableList<String?> = ArrayList()
        basenameStrings.add(fellesBasename)
        for (bundle in soknadBundles) {
            basenames[bundle.type] = bundle.propertiesFile
            basenameStrings.add(bundle.propertiesFile)
        }
        setBasenames(*basenameStrings.toTypedArray())
    }

    fun getBasenames(): Map<String, String> {
        return basenames
    }

    class Bundle(var type: String, var propertiesFile: String)

    companion object {
        val log by logger()
    }
}
