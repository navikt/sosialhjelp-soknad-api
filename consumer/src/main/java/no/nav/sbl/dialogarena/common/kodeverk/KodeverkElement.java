package no.nav.sbl.dialogarena.common.kodeverk;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.Nokkel;

class KodeverkElement {
    private final Map<Nokkel, String> koder = new HashMap();

    KodeverkElement(Map<Nokkel, String> kodeverkMap) {
        this.koder.put(Nokkel.SKJEMANUMMER, kodeverkMap.get(Nokkel.SKJEMANUMMER));
        this.koder.put(Nokkel.BESKRIVELSE, kodeverkMap.get(Nokkel.BESKRIVELSE));
        this.koder.put(Nokkel.VEDLEGGSID, kodeverkMap.get(Nokkel.VEDLEGGSID));
        this.koder.put(Nokkel.GOSYS_ID, kodeverkMap.get(Nokkel.GOSYS_ID));
        this.koder.put(Nokkel.TEMA, kodeverkMap.get(Nokkel.TEMA));
        this.koder.put(Nokkel.TITTEL, kodeverkMap.get(Nokkel.TITTEL));
        this.koder.put(Nokkel.TITTEL_EN, kodeverkMap.get(Nokkel.TITTEL_EN));
        this.koder.put(Nokkel.URL, kodeverkMap.get(Nokkel.URL));
        this.koder.put(Nokkel.URLENGLISH, kodeverkMap.get(Nokkel.URLENGLISH));
        this.koder.put(Nokkel.URLNEWNORWEGIAN, kodeverkMap.get(Nokkel.URLNEWNORWEGIAN));
        this.koder.put(Nokkel.URLPOLISH, kodeverkMap.get(Nokkel.URLPOLISH));
        this.koder.put(Nokkel.URLFRENCH, kodeverkMap.get(Nokkel.URLFRENCH));
        this.koder.put(Nokkel.URLSPANISH, kodeverkMap.get(Nokkel.URLSPANISH));
        this.koder.put(Nokkel.URLGERMAN, kodeverkMap.get(Nokkel.URLGERMAN));
        this.koder.put(Nokkel.URLSAMISK, kodeverkMap.get(Nokkel.URLSAMISK));
    }

    Map<Nokkel, String> getKoderMap() {
        return Collections.unmodifiableMap(this.koder);
    }
}
