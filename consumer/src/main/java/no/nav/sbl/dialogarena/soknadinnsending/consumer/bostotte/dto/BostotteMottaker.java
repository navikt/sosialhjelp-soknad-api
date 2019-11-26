package no.nav.sbl.dialogarena.soknadinnsending.consumer.bostotte.dto;


import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtbetaling;

public enum BostotteMottaker {
    KOMMUNE(JsonOkonomiOpplysningUtbetaling.Mottaker.KOMMUNE.value()), HUSSTAND(JsonOkonomiOpplysningUtbetaling.Mottaker.HUSSTAND.value());

    private String value;

    BostotteMottaker(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
