package no.nav.sbl.dialogarena.common.kodeverk;

import java.util.HashMap;
import java.util.Map;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.SosialhjelpSoknadApiException;

abstract class BaseKodeverk implements Kodeverk {
    protected Map<String, KodeverkElement> dbSkjema = new HashMap();
    protected Map<String, KodeverkElement> dbVedlegg = new HashMap();

    BaseKodeverk() {
    }

    public boolean isEgendefinert(String vedleggsIdOrskjemaId) {
        return "N6".equals(vedleggsIdOrskjemaId);
    }

    public String getTittel(String vedleggsIdOrskjemaId) {
        return this.getKode(vedleggsIdOrskjemaId, Nokkel.TITTEL);
    }

    public String getKode(String vedleggsIdOrskjemaId, Nokkel nokkel) {
        return this.getKoder(vedleggsIdOrskjemaId).get(nokkel);
    }

    public Map<Nokkel, String> getKoder(String vedleggsIdOrSkjemaId) {
        if (this.dbSkjema.containsKey(vedleggsIdOrSkjemaId)) {
            return this.dbSkjema.get(vedleggsIdOrSkjemaId).getKoderMap();
        } else if (this.dbVedlegg.containsKey(vedleggsIdOrSkjemaId)) {
            return this.dbVedlegg.get(vedleggsIdOrSkjemaId).getKoderMap();
        } else {
            throw new SosialhjelpSoknadApiException("\n ---- Fant ikke kodeverk : " + vedleggsIdOrSkjemaId + "---- \n");
        }
    }

    public void setDbSkjema(Map<String, KodeverkElement> dbSkjema) {
        this.dbSkjema = dbSkjema;
    }

    public void setDbVedlegg(Map<String, KodeverkElement> dbVedlegg) {
        this.dbVedlegg = dbVedlegg;
    }
}