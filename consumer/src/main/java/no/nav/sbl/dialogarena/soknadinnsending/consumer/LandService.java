package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import no.nav.sbl.dialogarena.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.sendsoknad.domain.dto.Land;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.StatsborgerskapType;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.LandListe.EOS;
import static no.nav.sbl.dialogarena.sendsoknad.domain.util.LandListe.EOS_LAND;

@Component
public class LandService {

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
        result.put("result", StatsborgerskapType.get(landkode));
        return result;
    }

    private List<Land> hentAlleLand() {
        List<Land> landliste = new ArrayList<>();
        List<String> landKoder = kodeverk.hentAlleKodenavnFraKodeverk(Kodeverk.EksponertKodeverk.LANDKODE);

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
        List<Land> landliste = new ArrayList<>();

        for (String landkode : EOS_LAND) {
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
