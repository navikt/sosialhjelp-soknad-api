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
    private String navNoLink;
    
    public Map<String,String> getConfig() {
        Map<String, String> result = new HashMap<String, String>();
        
        result.put("minehenvendelser.link.url", mineHenvendelserUrl);
        result.put("dialogarena.navnolink.url", navNoLink);
        
        return result;
    }
    
}