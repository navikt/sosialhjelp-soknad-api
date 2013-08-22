package no.nav.sbl.dialogarena.dokumentinnsending.kodeverk;


import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;

import javax.inject.Inject;
import java.util.Map;

import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.Nokkel;

public class KodeverkIntegrasjon implements KodeverkClient {

    @Inject
    private Kodeverk kodeverk;

    @Override
    public KodeverkSkjema hentKodeverkSkjemaForSkjemanummer(String skjemanummer) {
        Map<Nokkel, String> koder = kodeverk.getKoder(skjemanummer);
        KodeverkSkjema kodeverkSkjema = byggOppKodeverkSkjema(koder);
        kodeverkSkjema.setSkjemanummer(skjemanummer);

        return kodeverkSkjema;
    }

    @Override
    public KodeverkSkjema hentKodeverkSkjemaForVedleggsid(String vedleggsid) {
        Map<Nokkel, String> koder = kodeverk.getKoder(vedleggsid);

        return byggOppKodeverkSkjema(koder);
    }

    @Override
    public boolean isEgendefinert(String skjemaId) {
        return kodeverk.isEgendefinert(skjemaId);
    }

    private KodeverkSkjema byggOppKodeverkSkjema(Map<Nokkel, String> koder) {
        KodeverkSkjema kodeverkSkjema = new KodeverkSkjema();
        kodeverkSkjema.setBeskrivelse(koder.get(Nokkel.BESKRIVELSE));
        kodeverkSkjema.setUrl(koder.get(Nokkel.URL));
        kodeverkSkjema.setUrlengelsk(koder.get(Nokkel.URLENGLISH));
        kodeverkSkjema.setVedleggsid(koder.get(Nokkel.VEDLEGGSID));
        kodeverkSkjema.setTittel(koder.get(Nokkel.TITTEL));
        kodeverkSkjema.setGosysId(koder.get(Nokkel.GOSYS_ID));
        kodeverkSkjema.setTema(koder.get(Nokkel.TEMA));
        kodeverkSkjema.setUrlfransk(koder.get(Nokkel.URLFRENCH));
        kodeverkSkjema.setUrlnynorsk(koder.get(Nokkel.URLNEWNORWEGIAN));
        kodeverkSkjema.setUrlspansk(koder.get(Nokkel.URLSPANISH));
        kodeverkSkjema.setUrlpolsk(koder.get(Nokkel.URLPOLISH));
        kodeverkSkjema.setUrltysk(koder.get(Nokkel.URLGERMAN));
        return kodeverkSkjema;
    }
}