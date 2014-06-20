package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import java.util.Arrays;
import java.util.List;

public enum DelstegStatus {
    OPPRETTET, UTFYLLING, SKJEMA_VALIDERT, VEDLEGG_VALIDERT, SAMTYKKET, ETTERSENDING_OPPRETTET, ETTERSENDING_UTFYLLING;

    /**
     * Metode for å kunne skille ettersendingstatus fra en soknadstatus
     * @param status
     * @return
     */
    public static boolean isEttersendingStatus(DelstegStatus status) {
        List<DelstegStatus> ettersendingStatuses = Arrays.asList(ETTERSENDING_UTFYLLING, ETTERSENDING_OPPRETTET);
        return ettersendingStatuses.contains(status);
    }
}
