package no.nav.sbl.dialogarena.dokumentinnsending.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class GAConfig {

    public static final String GA_TRACKINGKEY = "ga.trackingkey";
    public static final String GA_FORTSETT_SENERE_UTM_SOURCE = "ga.utm.fortsettSenere.source";
    public static final String GA_FORTSETT_SENERE_UTM_MEDIUM = "ga.utm.fortsettSenere.medium";
    public static final String GA_FORTSETT_SENERE_UTM_CAMPAIGN = "ga.utm.fortsettSenere.campaign";

    //@Value("${" + GA_TRACKINGKEY + "}")
    private String gaTrackingKey = "UA-9127381-1";

    //@Value("${" + GA_FORTSETT_SENERE_UTM_SOURCE + "}")
    private String fortsettSenereUtmSource = "web";
    //@Value("${" + GA_FORTSETT_SENERE_UTM_MEDIUM + "}")
    private String fortsettSenereUtmMedium = "email";
    //@Value("${" + GA_FORTSETT_SENERE_UTM_CAMPAIGN + "}")
    private String fortsettSenereUtmCampaign = "1";

    @Bean
    public Map<String, String> gaConfig() {
        Map<String, String> footerLinks = new HashMap<>();
        footerLinks.put(GA_TRACKINGKEY, gaTrackingKey);
        footerLinks.put(GA_FORTSETT_SENERE_UTM_SOURCE, fortsettSenereUtmSource);
        footerLinks.put(GA_FORTSETT_SENERE_UTM_MEDIUM, fortsettSenereUtmMedium);
        footerLinks.put(GA_FORTSETT_SENERE_UTM_CAMPAIGN, fortsettSenereUtmCampaign);
        return footerLinks;
    }

    @Bean
    public String fortsettSenereUtmKey() {
        return String.format("utm_source=%s&utm_medium=%s&utm_campaign=%s", fortsettSenereUtmSource, fortsettSenereUtmMedium, fortsettSenereUtmCampaign);
    }

    @Bean
    public String gaScript() {
        return "var _gaq = _gaq || [];  " +
                "_gaq.push(['_setAccount', 'UA-9127381-1']);  " +
                "_gaq.push(['_trackPageview']);  " +
                "_gaq.push(['_gat._anonymizeIp']);" +
                "_gaq.push(['_setDomainName', 'nav.no']);" +
                "_gaq.push(['_trackPageLoadTime']);" +
                "(function() {    " +
                "var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;    " +
                "ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';   " +
                "var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);  " +
                "})();";

    }
}
