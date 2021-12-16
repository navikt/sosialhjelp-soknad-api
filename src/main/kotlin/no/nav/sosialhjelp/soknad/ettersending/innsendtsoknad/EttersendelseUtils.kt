package no.nav.sosialhjelp.soknad.ettersending.innsendtsoknad

import java.time.LocalDate

object EttersendelseUtils {
    fun soknadSendtForMindreEnn30DagerSiden(innsendtDato: LocalDate): Boolean {
        return innsendtDato.isAfter(LocalDate.now().minusDays(30))
    }
}
