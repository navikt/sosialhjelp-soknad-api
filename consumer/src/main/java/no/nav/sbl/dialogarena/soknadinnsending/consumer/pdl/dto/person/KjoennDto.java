package no.nav.sbl.dialogarena.soknadinnsending.consumer.pdl.dto.person;

public class KjoennDto {

    private final Kjoenn kjoenn;

    public KjoennDto(Kjoenn kjoenn) {
        this.kjoenn = kjoenn;
    }

    public Kjoenn getKjoenn() {
        return kjoenn;
    }

    public enum Kjoenn {
        MANN, KVINNE, UKJENT
    }
}
