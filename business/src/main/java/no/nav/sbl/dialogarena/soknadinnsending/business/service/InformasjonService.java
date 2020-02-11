package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InformasjonService implements Miljovariabler {

    @Value("${saksoversikt.link.url}")
    private String saksoversiktUrl;
    @Value("${soknad.alderspensjon.url}")
    private String alderspensjonUrl;
    @Value("${dittnav.link.url}")
    private String dittnavUrl;
    @Value("${soknad.ettersending.antalldager}")
    private String antallDager;
    @Value("${dialogarena.cms.url}")
    private String appresUrl;
    @Value("${soknadinnsending.soknad.path}")
    private String soknadinnsendingSoknadPath;
    @Value("${soknadtilleggsstonader.path}")
    private String soknadtilleggsstonaderPath;
    @Value("${dineutbetalinger.link.url}")
    private String dineUtbetalingerLink;

    private String sporsmalsvarUrl = "";

    public Map<String,String> hentMiljovariabler() {
        Map<String, String> result = new HashMap<>();

        result.put("saksoversikt.link.url", saksoversiktUrl);
        result.put("dittnav.link.url", dittnavUrl);
        result.put("soknad.alderspensjon.url", alderspensjonUrl);
        result.put("dialogarena.cms.url", appresUrl);
        result.put("soknadinnsending.soknad.path", soknadinnsendingSoknadPath);
        result.put("modia.url", sporsmalsvarUrl);
        result.put("soknadtilleggsstonader.url", soknadtilleggsstonaderPath);
        result.put("dineutbetalinger.link.url", dineUtbetalingerLink);
        result.put("soknad.ettersending.antalldager", antallDager);

        result.putAll(getTestSpesifikkConfig());

        return result;
    }

    private Map<String, String> getTestSpesifikkConfig() {
        Map<String, String> testEnvVars = new HashMap<>();

        for(String envVar : System.getProperties().stringPropertyNames()) {
            if(envVar.startsWith("testconfig.")) {
                testEnvVars.put(envVar, System.getProperty(envVar));
            }
        }
        return testEnvVars;
    }


}