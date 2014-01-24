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
    
    public Map<String,String> getConfig() {
        Map<String, String> result = new HashMap<String, String>();
        
        result.put("minehenvendelser.link.url", mineHenvendelserUrl);
        result.put("dialogarena.navnolink.url", navNoUrl);
        result.put("soknad.inngangsporten.url", inngangsportenUrl);
        result.put("soknad.skjemaveileder.url", skjemaveilederUrl);
        result.put("soknad.alderspensjon.url", alderspensjonUrl);
        return result;
    }
    
}