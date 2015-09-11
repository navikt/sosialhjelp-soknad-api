package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadVedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Component
public class InformasjonService {

    @Inject
    private Kodeverk kodeverk;

    @Inject
    private SoknadService soknadService;

    @Value("${saksoversikt.link.url}")
    private String saksoversiktUrl;
    @Value("${dialogarena.navnolink.url}")
    private String navNoUrl;
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
    @Value("${dittnav.link.url}")
    private String dittnavUrl;
    @Value("${soknad.ettersending.antalldager}")
    private String antallDager;
    @Value("${dialogarena.cms.url}")
    private String appresUrl;
    @Value("${soknadinnsending.soknad.path}")
    private String soknadinnsendingSoknadPath;

    public Map<String,String> hentMiljovariabler() {
        Map<String, String> result = new HashMap<>();
        
        result.put("saksoversikt.link.url", saksoversiktUrl);
        result.put("dittnav.link.url", dittnavUrl);
        result.put("dialogarena.navnolink.url", navNoUrl);
        result.put("soknad.skjemaveileder.url", skjemaveilederUrl);
        result.put("soknad.alderspensjon.url", alderspensjonUrl);
        result.put("soknad.reelarbeidsoker.url", reelarbeidsokerUrl);
        result.put("soknad.dagpengerbrosjyre.url", dagpengerBrosjyreUrl);
        result.put("soknad.brukerprofil.url", brukerProfilUrl);
        result.put("dialogarena.cms.url", appresUrl);
        result.put("soknadinnsending.soknad.path", soknadinnsendingSoknadPath);

        result.put("soknad.ettersending.antalldager", antallDager);

        return result;
    }

    public Map<String,String> hentVedleggsskjemaForBehandlingsId(String behandlingsId) {
        WebSoknad soknad = soknadService.hentSoknad(behandlingsId, true, false);
        return hentVedleggsskjema(soknad.getskjemaNummer());
    }


    public Map<String,String> hentVedleggsskjema(String type) {
        Map<String, String> result = new HashMap<>();

        SoknadStruktur struktur = soknadService.hentSoknadStruktur(type);

        for (SoknadVedlegg soknadVedlegg : struktur.getVedlegg()) {
            settInnUrlForSkjema(soknadVedlegg.getSkjemaNummer(), result);
        }

        for (String skjemanummer : struktur.getVedleggReferanser()) {
            settInnUrlForSkjema(skjemanummer, result);
        }

        return result;
    }

    private void settInnUrlForSkjema(String skjemanummer, Map<String, String> resultMap) {
        String url = kodeverk.getKode(skjemanummer, Kodeverk.Nokkel.URL);

        if (!url.isEmpty()) {
            resultMap.put(skjemanummer.toLowerCase().replaceAll("\\s+", "") + ".url", url);
        }
    }


}