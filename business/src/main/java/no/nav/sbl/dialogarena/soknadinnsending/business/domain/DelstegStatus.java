package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import java.util.List;

import static java.util.Arrays.asList;

public enum DelstegStatus {
    OPPRETTET, UTFYLLING, SKJEMA_VALIDERT, VEDLEGG_VALIDERT, SAMTYKKET, ETTERSENDING_OPPRETTET, ETTERSENDING_UTFYLLING;

    private static final List<DelstegStatus> ETTERSENDING = asList(ETTERSENDING_OPPRETTET, ETTERSENDING_UTFYLLING);

    public boolean erEttersending() {
        return ETTERSENDING.contains(this);
    }
}
