package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto;

import no.nav.sbl.soknadsosialhjelp.soknad.bostotte.JsonBostotteUtbetaling;

public enum BostotteMottaker {
    KOMMUNE(JsonBostotteUtbetaling.Mottaker.KOMMUNE.value()), HUSSTAND(JsonBostotteUtbetaling.Mottaker.HUSSTAND.value());

    private String value;

    BostotteMottaker(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
