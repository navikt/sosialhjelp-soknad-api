package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Kilde: https://www.nav.no/1073751655.cms
 * Accessdate: 28.01.2014
 */
@Component
public class EosBorgerService {
    List<String> eosLand = Arrays.asList("BEL","BGR", "DNK", "EST", "FIN", "FRA", "GRC", "IRL", "ISL", "ITA", "CYP", "LVA", "LIE", "LTU", "LUX", "MLT", "NLD"
            , "POL", "PRT", "ROU", "SVK", "SVN", "ESP", "GBR", "SWE", "CZE", "DEU", "HUN", "AUT");
    
    public String getStatsborgeskapType(String landkode) {
        if("NOR".equals(landkode)) {
            return "norsk";
        } else if(eosLand.contains(landkode)) {
            return "eos";
        } else {
            return "ikkeEos";
        }
    }
    
    public boolean isEosLandAnnetEnnNorge(String landkode) {
        return eosLand.contains(landkode);
    }
}
