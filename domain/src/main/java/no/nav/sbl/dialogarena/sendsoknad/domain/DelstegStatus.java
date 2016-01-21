package no.nav.sbl.dialogarena.sendsoknad.domain;

import java.util.Arrays;
import java.util.List;

public enum DelstegStatus {
    OPPRETTET, UTFYLLING, SKJEMA_VALIDERT, VEDLEGG_VALIDERT, SAMTYKKET, ETTERSENDING_OPPRETTET, ETTERSENDING_UTFYLLING;

    private static final List<DelstegStatus> ETTERSENDING = Arrays.asList(ETTERSENDING_OPPRETTET, ETTERSENDING_UTFYLLING);
    public boolean erEttersending() {
        return ETTERSENDING.contains(this);
    }
}
