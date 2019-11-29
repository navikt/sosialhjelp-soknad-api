package no.nav.sbl.dialogarena.soknadinnsending.consumer.person.domain;

public class Bostedsadresse {

    private StrukturertAdresse strukturertAdresse;

    public StrukturertAdresse getStrukturertAdresse() {
        return strukturertAdresse;
    }

    public Bostedsadresse withStrukturertAdresse(StrukturertAdresse strukturertAdresse) {
        this.strukturertAdresse = strukturertAdresse;
        return this;
    }

}
