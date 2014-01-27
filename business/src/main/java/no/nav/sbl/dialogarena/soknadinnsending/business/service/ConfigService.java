package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ConfigService {
    @Value("${minehenvendelser.link.url}")
    private String mineHenvendelserUrl;
    @Value("${dialogarena.navnolink.url}")
    private String navNoUrl;
    @Value("${soknad.inngangsporten.url}")
    private String inngangsportenUrl;
    @Value("${soknad.skjemaveileder.url}")
    private String skjemaveilederUrl;
    @Value("${soknad.alderspensjon.url}")
    private String alderspensjonUrl;
    @Value("${soknad.reelarbeidsoker.url}")
    private String reelarbeidsokerUrl;
    @Value("${soknad.dagpengerbrosjyre.url}")
    private String dagpengerBrosjyreUrl;
    @Value("${soknad.brukerprofil.url}")
    private String brukerProfilUrl;
    @Value("${soknad.sluttaarsak.url}")
    private String sluttaarsakUrl;
    @Value("${soknad.lonnskravskjema.url}")
    private String lonnskravSkjemaUrl;
    @Value("${soknad.permitteringsskjema.url}")
    private String permitteringskjemaUrl;
    
    
    
    public Map<String,String> getConfig() {
        Map<String, String> result = new HashMap<String, String>();
        
        result.put("minehenvendelser.link.url", mineHenvendelserUrl);
        result.put("dialogarena.navnolink.url", navNoUrl);
        result.put("soknad.inngangsporten.url", inngangsportenUrl);
        result.put("soknad.skjemaveileder.url", skjemaveilederUrl);
        result.put("soknad.alderspensjon.url", alderspensjonUrl);
        result.put("soknad.reelarbeidsoker.url", reelarbeidsokerUrl);
        result.put("soknad.dagpengerbrosjyre.url", dagpengerBrosjyreUrl);
        result.put("soknad.brukerprofil.url", brukerProfilUrl);
        result.put("soknad.sluttaarsak.url", sluttaarsakUrl);
        result.put("soknad.lonnskravskjema.url", lonnskravSkjemaUrl);
        result.put("soknad.permitteringsskjema.url", permitteringskjemaUrl);
        
        return result;
    }
    
}