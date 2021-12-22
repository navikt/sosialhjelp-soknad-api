package no.nav.sosialhjelp.soknad.tilgangskontroll

import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator.generateXsrfToken
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator.sjekkXsrfToken
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal class XsrfGeneratorTest {

    @BeforeEach
    fun setUp() {
        System.setProperty("environment.name", "test")
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        System.clearProperty("environment.name")
    }

    @Test
    fun skalGenerereBasertPaaInput() {
        val token = generateXsrfToken("1L")
        val tokenYesterday = generateXsrfToken("1L", ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        sjekkXsrfToken(token, "1L")
        sjekkXsrfToken(tokenYesterday, "1L")
        sjekkAtMetodeKasterException(token, 2L)
//        (SubjectHandler.getSubjectHandlerService() as StaticSubjectHandlerService).setFakeToken("Token2")
//        (SubjectHandler.getSubjectHandlerService() as StaticSubjectHandlerService).setUser("12345")
        sjekkAtMetodeKasterException(token, 1L)
    }

    private fun sjekkAtMetodeKasterException(token: String, soknadId: Long) {
        try {
            sjekkXsrfToken(token, "soknadId")
            Assertions.fail<Any>("Kastet ikke exception")
        } catch (ex: AuthorizationException) {
        }
    }
}
