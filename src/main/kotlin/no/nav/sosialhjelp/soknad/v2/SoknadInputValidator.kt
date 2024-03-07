package no.nav.sosialhjelp.soknad.v2

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import java.util.*
import kotlin.reflect.KClass

/**
 * Felles valideringslogikk på tvers av klasser
 */
class SoknadInputValidator(private val clazz: KClass<*>) {

    fun validateTextInput(id: UUID?, input: String) {
        // TODO må støtte andre "normale" tegn som !,.% etc.
        if (!input.all { it.isLetterOrDigit() || it.isWhitespace() })
            throw NotValidInputException(id, "$clazz - Ugyldige tegn i tekst")
    }

    fun validateAllInputNotNullOrEmpty(id: UUID, vararg input: Any?) {
        if (input.all { isVariableNullOrEmpty(it) }) {
            throw NotValidInputException(id, "$clazz - Input er tom")
        }
    }

    private fun isVariableNullOrEmpty(variable: Any?): Boolean {
        return variable?.let {
            if (variable is String) variable.isEmpty() || variable.isBlank()
            else false
        }
            ?: true
    }

    fun validateIsNumber(soknadId: UUID, number: String) {
        if (number.toList().any { !it.isDigit() }) {
            throw NotValidInputException(soknadId, "$clazz - Kun siffer er tillat i input")
        }
    }
}

class NotValidInputException(id: UUID?, message: String) : SosialhjelpSoknadApiException(message, null, id?.toString())
