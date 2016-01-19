package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.dto.Land;
import no.nav.sbl.dialogarena.soknadinnsending.business.util.StatsborgerskapType;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;

import static no.nav.sbl.dialogarena.kodeverk.Kodeverk.EksponertKodeverk.LANDKODE;

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
    public static final List<String> EOS_LAND = Arrays.asList("BEL","BGR", "DNK", "CZE", "EST", "FIN", "FRA", "GRC", "IRL", "ISL", "ITA", "HRV" , "CYP", "LVA", "LIE", "LTU", "LUX", "MLT", "NLD"
            , "POL", "PRT", "ROU", "SVK", "SVN", "ESP", "GBR", "CHE", "SWE", "DEU", "HUN", "AUT");

    public static final String EOS = "eos";

    @Inject
    private Kodeverk kodeverk;

    public List<Land> hentLand(String filter) {
        if (EOS.equals(filter)) {
            return hentEosLand();
        }
        return hentAlleLand();
    }

    public Map<String, String> hentStatsborgerskapstype(String landkode) {
        Map<String, String> result = new HashMap<>();
        result.put("result", String.valueOf(StatsborgerskapType.get(landkode)));
        return result;
    }

    private List<Land> hentAlleLand() {
        List<Land> landliste = new ArrayList<>();
        List<String> landKoder = kodeverk.hentAlleKodenavnFraKodeverk(LANDKODE);

        for (String landkode : landKoder) {
            Land land = new Land();
            land.setText(kodeverk.getLand(landkode));
            land.setValue(landkode);
            landliste.add(land);
        }
        if (!landKoder.contains("NOR")) {
            Land norge = new Land("Norge", "NOR");

            List<Land> alleAndreLand = landliste;
            landliste = new ArrayList<>();
            landliste.add(norge);
            landliste.addAll(alleAndreLand);
        }

        return landliste;
    }

    private List<Land> hentEosLand() {
        List<String> eosLand = EOS_LAND;
        List<Land> landliste = new ArrayList<>();

        for (String landkode : eosLand) {
            Land land = new Land();
            String landnavn = kodeverk.getLand(landkode);
            landnavn = landnavn != null ? landnavn : landkode;
            land.setText(landnavn);
            land.setValue(landkode);
            landliste.add(land);
        }
        return landliste;
    }
}
