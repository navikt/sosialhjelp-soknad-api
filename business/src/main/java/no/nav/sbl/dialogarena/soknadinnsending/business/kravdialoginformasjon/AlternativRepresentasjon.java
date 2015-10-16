package no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon;

import static java.util.UUID.randomUUID;

public class AlternativRepresentasjon {
    private String uuid = randomUUID().toString();

    public String getFilnavn() {
        return "Tilleggstonader.xml";
    }

    public String getMimetype() {
        return "application/xml";
    }

    public String getUuid() {
        return uuid;
    }

    public byte[] getContent() {
        String xml = "<soknad><content>Innhold soknad tillegsstonad</content></soknad>";
        return xml.getBytes();
    }
}
