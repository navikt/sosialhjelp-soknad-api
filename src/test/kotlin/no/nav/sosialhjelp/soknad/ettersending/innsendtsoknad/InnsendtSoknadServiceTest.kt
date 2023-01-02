package no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class InnsendtSoknadServiceTest {

    @Test
    fun soknadsalderIMinutter_returnsMinutes() {
        val tidspunktSendt =
            LocalDateTime.now().minusDays(1).plusHours(2).minusMinutes(3) // (24-2)h * 60 m/h - 3 = 22*60-3 =
        val response = InnsendtSoknadService.soknadsalderIMinutter(tidspunktSendt)
        assertThat(response).isEqualTo(1323) // (24-2)h * 60 m/h + 3 = 22*60+3
    }

    @Test
    fun soknadsalderIMinutter_whenDateTimeIsNull_returnsMinusOne() {
        val response = InnsendtSoknadService.soknadsalderIMinutter(null)
        assertThat(response).isEqualTo(-1)
    }
}
