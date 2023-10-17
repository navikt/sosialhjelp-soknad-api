package no.nav.sosialhjelp.soknad.app.subjecthandler

import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils

object SubjectHandlerUtils {

    private val log by logger()
    private var subjectHandlerService: SubjectHandler = SubjectHandlerImpl(SpringTokenValidationContextHolder())

    fun getUserIdFromToken(): String = subjectHandlerService.getUserIdFromToken()
    fun getConsumerId(): String = subjectHandlerService.getConsumerId()
    fun getToken(): String = subjectHandlerService.getToken()

    fun setNewSubjectHandlerImpl(subjectHandlerImpl: SubjectHandler) {
        if (!MiljoUtils.isNonProduction()) {
            log.error("Forsøker å sette en annen SubjectHandlerImpl i prod!")
            throw RuntimeException("Forsøker å sette en annen SubjectHandlerImpl i prod!")
        } else {
            subjectHandlerService = subjectHandlerImpl
        }
    }

    fun resetSubjectHandlerImpl() {
        subjectHandlerService = SubjectHandlerImpl(SpringTokenValidationContextHolder())
    }
}
