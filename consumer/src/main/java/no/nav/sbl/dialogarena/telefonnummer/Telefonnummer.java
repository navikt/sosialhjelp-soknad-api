package no.nav.sbl.dialogarena.telefonnummer;


import no.nav.sbl.dialogarena.types.Copyable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.common.TekstUtils.fjernSpesialtegn;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.commons.lang3.StringUtils.trim;

public class Telefonnummer implements Copyable<Telefonnummer>, Serializable {
    private String landkode;
    private String nummer;

    public Telefonnummer() {
    }

    public Telefonnummer(String landkodeTelefonnummer, String telefonnummer) {
        this.landkode = landkodeTelefonnummer;
        this.nummer = telefonnummer;
    }

    public String getLandkode() {
        if (isBlank(landkode)) {
            return HarTelefonLand.LANDKODE_NORGE; //default
        }
        return landkode;
    }

    public String getNummer() {
        return nummer;
    }

    public void setLandkode(String landkode) {
        this.landkode = landkode;
    }

    public void setNummer(String nummer) {
        this.nummer = nummer;
    }


    public static List<String> validerTelefonnummer(String landkode, String nummer) {
        List<String> feilmeldinger = new ArrayList<>();
        String formatertNummer = fjernSpesialtegn(nummer);

        if (HarTelefonLand.LANDKODE_NORGE.equalsIgnoreCase(trim(landkode))) {
            if (!isNumeric(formatertNummer) || formatertNummer.length() != 8) {
                feilmeldinger.add("maa-vaere-8-siffer");
            }
        } else {
            if (!isNumeric(formatertNummer) || formatertNummer.length() > 16) {
                feilmeldinger.add("kun-siffer-og-max-lengde-er-16");
            }
        }

        return feilmeldinger;
    }

    public boolean erUtfylt() {
        return isNotBlank(getLandkode()) && isNotBlank(getNummer());
    }

    @Override
    public Telefonnummer copy() {
        return new Telefonnummer(getLandkode(), nummer);
    }
}
