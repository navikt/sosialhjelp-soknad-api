package no.nav.sbl.dialogarena.kodeverk;

public interface Adressekodeverk {

    String LANDKODE_NORGE = "NOR";

    String getLand(String landkode);

    String getPoststed(String postnummer);

}
