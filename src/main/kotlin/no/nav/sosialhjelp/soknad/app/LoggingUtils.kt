package no.nav.sosialhjelp.soknad.app

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.full.companionObject

object LoggingUtils {
    fun maskerFnr(tekst: String?): String? {
        return tekst?.replace("\\b[0-9]{11}\\b".toRegex(), "[FNR]")
    }

    fun <R : Any> R.logger(): Lazy<Logger> {
        return lazy { LoggerFactory.getLogger(unwrapCompanionClass(this.javaClass).name) }
    }

    // unwrap companion class to enclosing class given a Java Class
    private fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
        return ofClass.enclosingClass
            ?.takeIf { ofClass.enclosingClass.kotlin.companionObject?.java == ofClass }
            ?: ofClass
    }
}
