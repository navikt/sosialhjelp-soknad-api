package no.nav.sosialhjelp.soknad.app

import no.nav.sosialhjelp.soknad.app.LoggingUtils.maskerFnr
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class LoggingUtilsTest {

    @Test
    fun skalFjerne_alleFnr_fraFeilmelding() {
        val str = "12121212121 feilmelding som har flere fnr 12345678911 og 11111111111"
        val res = maskerFnr(str)
        assertThat(res).isEqualTo("[FNR] feilmelding som har flere fnr [FNR] og [FNR]")
    }

    @Test
    fun skalFjerne_fnr_fraUrl() {
        val str = "/ekstern/skatt/datasamarbeid/api/innrapportert/inntektsmottaker/11111111111/oppgave/inntekt?fnr=12121212121&a=b"
        val res = maskerFnr(str)
        assertThat(res).isEqualTo("/ekstern/skatt/datasamarbeid/api/innrapportert/inntektsmottaker/[FNR]/oppgave/inntekt?fnr=[FNR]&a=b")
    }

    @Test
    fun skalIkkeFjerne_12siffretTall_fraFeilmelding() {
        val str = "feilmelding som har for langt fnr 111112222233"
        val res = maskerFnr(str)
        assertThat(res).isEqualTo(str)
    }

    @Test
    fun skalIkkeFjerne_10siffretTall_fraFeilmelding() {
        val str = "feilmelding som har for kort fnr 1111122222"
        val res = maskerFnr(str)
        assertThat(res).isEqualTo(str)
    }

    @Test
    fun skalFjerne_11siffretTallWrappetMedHermetegn_fraFeilmelding() {
        val str = "feilmelding som har fnr \"12345612345\""
        val res = maskerFnr(str)
        assertThat(res).isEqualTo("feilmelding som har fnr \"[FNR]\"")
    }

    @Test
    fun skalIkkeFeile_medNull_iFeilmelding() {
        val res = maskerFnr(null)
        assertThat(res).isNull()
    }

    @Test
    fun skalFjerneFnrFraRedisFeilmelding() {
        val str = "cache key=hent-person-12345612345"
        val res = maskerFnr(str)
        assertThat(res).isEqualTo("cache key=hent-person-[FNR]")
    }
}
