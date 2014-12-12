package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Kilde: https://www.nav.no/1073751655.cms (Arkivert innhold. Søk opp nøkkelen 1073751655 i Enonic Admin
 * Sveits er lagt til i tillegg til EØS-landene
 * Accessdate: 28.01.2014
 *
 * I tilegg har Kroatia (HRV) blitt lagt til etter at enonic-innholdet ble arkivert.
 *
 */
@Component
public class LandService {
    private static final List<String> EOS_LAND = Arrays.asList("BEL","BGR", "DNK", "EST", "FIN", "FRA", "GRC", "IRL", "ISL", "ITA", "CYP", "LVA", "LIE", "LTU", "LUX", "MLT", "NLD"
            , "POL", "PRT", "ROU", "SVK", "SVN", "ESP", "GBR", "SWE", "CZE", "DEU", "HUN", "AUT", "CHE", "HRV");

    public static final String EOS = "eos";

    public String getStatsborgeskapType(String landkode) {
        if("NOR".equals(landkode)) {
            return "norsk";
        } else if(EOS_LAND.contains(landkode)) {
            return "eos";
        } else {
            return "ikkeEos";
        }
    }

    public List<String> getEosLand() {
        return EOS_LAND;
    }
}
