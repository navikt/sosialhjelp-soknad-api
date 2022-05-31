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
        val xsrfToken = generateXsrfToken("1L")
        val xsrfTokenYesterday = generateXsrfToken("1L", ZonedDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        sjekkXsrfToken(xsrfToken, "1L", false)
        sjekkXsrfToken(xsrfTokenYesterday, "1L", false)
        sjekkAtMetodeKasterException(xsrfToken, 2L)
        sjekkAtMetodeKasterException(xsrfToken, 1L)
    }

    private fun sjekkAtMetodeKasterException(token: String, soknadId: Long) {
        try {
            sjekkXsrfToken(token, soknadId.toString(), false)
            Assertions.fail<Any>("Kastet ikke exception")
        } catch (ex: AuthorizationException) {
        }
    }
}
