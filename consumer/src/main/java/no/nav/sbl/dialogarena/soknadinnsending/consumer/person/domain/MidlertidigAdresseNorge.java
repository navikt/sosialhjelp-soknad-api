package no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain;

public class MidlertidigAdresseNorge {
    private StrukturertAdresse strukturertAdresse;

    public StrukturertAdresse getStrukturertAdresse() {
        return strukturertAdresse;
    }

    public MidlertidigAdresseNorge withStrukturertAdresse(StrukturertAdresse strukturertAdresse) {
        this.strukturertAdresse = strukturertAdresse;
        return this;
    }

}
