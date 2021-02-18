package no.nav.sosialhjelp.soknad.business.util;

import java.time.LocalDate;

public final class EttersendelseUtils {

    private EttersendelseUtils() {
    }

    public static boolean soknadSendtForMindreEnn30DagerSiden(LocalDate innsendtDato) {
        return innsendtDato.isAfter(LocalDate.now().minusDays(30));
    }
}
