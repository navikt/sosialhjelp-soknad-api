package no.nav.sosialhjelp.soknad.tilgangskontroll

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.sosialhjelp.soknad.common.MiljoUtils
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
        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun skalGenerereBasertPaaInput() {
        val xsrfTokenFraFnr = generateXsrfToken("1L")
        val xsrfTokenYesterdayFraFnr = generateXsrfToken("1L", ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        sjekkXsrfToken(xsrfTokenFraFnr, "1L", false)
        sjekkXsrfToken(xsrfTokenYesterdayFraFnr, "1L", false)
        sjekkAtMetodeKasterException(xsrfTokenFraFnr, 2L)
        sjekkAtMetodeKasterException(xsrfTokenFraFnr, 1L)

        val xsrfTokenFraToken = generateXsrfToken("1L", id = SubjectHandlerUtils.getToken())
        val xsrfTokenYesterdayFraToken = generateXsrfToken("1L", ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")), SubjectHandlerUtils.getToken())
        sjekkXsrfToken(xsrfTokenFraToken, "1L", false)
        sjekkXsrfToken(xsrfTokenYesterdayFraToken, "1L", false)
        sjekkAtMetodeKasterException(xsrfTokenFraToken, 2L)
        sjekkAtMetodeKasterException(xsrfTokenFraToken, 1L)
    }

    private fun sjekkAtMetodeKasterException(token: String, soknadId: Long) {
        try {
            sjekkXsrfToken(token, soknadId.toString(), false)
            Assertions.fail<Any>("Kastet ikke exception")
        } catch (ex: AuthorizationException) {
        }
    }
}
