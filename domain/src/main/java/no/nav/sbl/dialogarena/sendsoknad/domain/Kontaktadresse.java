package no.nav.sbl.dialogarena.sendsoknad.domain;

public class Kontaktadresse {

    private final String coAdressenavn;
    private final Vegadresse vegadresse;

    public Kontaktadresse(
            String coAdressenavn,
            Vegadresse vegadresse
    ) {
        this.coAdressenavn = coAdressenavn;
        this.vegadresse = vegadresse;
    }

    public String getCoAdressenavn() {
        return coAdressenavn;
    }

    public Vegadresse getVegadresse() {
        return vegadresse;
    }
}
