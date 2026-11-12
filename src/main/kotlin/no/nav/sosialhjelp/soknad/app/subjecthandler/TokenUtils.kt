package no.nav.sosialhjelp.soknad.app.subjecthandler

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.app.MiljoUtils

object TokenUtils {
    private val log by logger()
    private var currentSubjectHandler: SubjectHandler = DefaultSubjectHandler

    fun getUserIdFromToken(): String = currentSubjectHandler.getUserIdFromToken()

    fun getToken(): String = currentSubjectHandler.getToken()

    fun setNewSubjectHandler(subjectHandler: SubjectHandler) {
        if (!MiljoUtils.isNonProduction()) {
            log.error("Forsøker å sette en annen SubjectHandlerImpl i prod!")
            throw RuntimeException("Forsøker å sette en annen SubjectHandlerImpl i prod!")
        } else {
            currentSubjectHandler = subjectHandler
        }
    }

    fun resetSubjectHandlerImpl() {
        currentSubjectHandler = DefaultSubjectHandler
    }
}
