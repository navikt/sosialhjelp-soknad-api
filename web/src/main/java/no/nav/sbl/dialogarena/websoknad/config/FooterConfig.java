package no.nav.sbl.dialogarena.websoknad.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static no.nav.sbl.dialogarena.webkomponent.footer.FooterPanel.DIALOGARENA_FOOTER_BASEURL;

@Configuration
public class FooterConfig {

    @Value("${dialogarena.navnolink.url}")
    private String navnolink;

    @Bean
    public Map<String, String> footerLinks() {
        Map<String, String> footerLinks = new HashMap<>();
        footerLinks.put(DIALOGARENA_FOOTER_BASEURL, navnolink);
        return footerLinks;
    }
}