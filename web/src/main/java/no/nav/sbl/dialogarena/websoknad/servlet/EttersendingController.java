
package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@ControllerAdvice()
@RequestMapping("/ettersending")
public class EttersendingController {
    @Inject
    private SendSoknadService soknadService;

    @RequestMapping(method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseBody()
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,String> startEttersending(@RequestBody Map<String, String> requestMap, HttpServletResponse response) {
        Map<String, String> result = new HashMap<>();

        String behandlingskjedeId = requestMap.get("behandlingskjedeId");
        WebSoknad soknad = soknadService.hentEttersendingForBehandlingskjedeId(behandlingskjedeId);
        Long soknadId;
        if (soknad == null) {
            soknadId = soknadService.startEttersending(behandlingskjedeId);
        } else {
            soknadId = soknad.getSoknadId();
        }

        result.put("soknadId", soknadId.toString());
        return result;
    }

    @RequestMapping(value = "/{behandlingskjedeId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public WebSoknad hentEttersending(@PathVariable String behandlingskjedeId) {
        return soknadService.hentEttersendingMedData(behandlingskjedeId);
    }

    @RequestMapping(value = "/send", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    public void sendEttersending(@RequestBody Map<String, String> requestMap) {
        Long soknadId = Long.valueOf(requestMap.get("soknadId"));
        String behandlingskjedeId = requestMap.get("behandlingskjedeId");
        soknadService.sendEttersending(soknadId, behandlingskjedeId);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void slettEttersending(@RequestBody Map<String, String> requestMap) {
        Long soknadId = Long.valueOf(requestMap.get("soknadId"));
        soknadService.avbrytSoknad(soknadId);
    }
}
