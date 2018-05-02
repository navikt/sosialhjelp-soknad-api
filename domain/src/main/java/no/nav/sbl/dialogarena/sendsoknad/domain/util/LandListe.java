package no.nav.sbl.dialogarena.sendsoknad.domain.util;


import java.util.*;

import static java.util.Arrays.asList;

/**
 * Kilde: https://www.nav.no/1073751655.cms (Arkivert innhold. Søk opp nøkkelen 1073751655 i Enonic Admin
 * Sveits er lagt til i tillegg til EØS-landene
 * Accessdate: 28.01.2014
 *
 * I tilegg har Kroatia (HRV) blitt lagt til etter at enonic-innholdet ble arkivert.
 *
 */
public class LandListe {

    public static final List<String> EOS_LAND = asList("BEL", "BGR", "DNK", "CZE", "EST", "FIN", "FRA", "GRC", "IRL", "ISL", "ITA", "HRV", "CYP", "LVA", "LIE", "LTU", "LUX", "MLT", "NLD"
            , "POL", "PRT", "ROU", "SVK", "SVN", "ESP", "GBR", "CHE", "SWE", "DEU", "HUN", "AUT");
    public static final String EOS = "eos";

}
