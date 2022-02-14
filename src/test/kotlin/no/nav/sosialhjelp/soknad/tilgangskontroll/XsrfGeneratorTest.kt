package no.nav.sosialhjelp.soknad.tilgangskontroll

import no.nav.sosialhjelp.soknad.common.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
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
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
    }

    @Test
    fun skalGenerereBasertPaaInput() {
        val token = generateXsrfToken("1L")
        val tokenYesterday = generateXsrfToken("1L", ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        sjekkXsrfToken(token, "1L", false)
        sjekkXsrfToken(tokenYesterday, "1L", false)
        sjekkAtMetodeKasterException(token, 2L)
        sjekkAtMetodeKasterException(token, 1L)
    }

    private fun sjekkAtMetodeKasterException(token: String, soknadId: Long) {
        try {
            sjekkXsrfToken(token, soknadId.toString(), false)
            Assertions.fail<Any>("Kastet ikke exception")
        } catch (ex: AuthorizationException) {
        }
    }
}
