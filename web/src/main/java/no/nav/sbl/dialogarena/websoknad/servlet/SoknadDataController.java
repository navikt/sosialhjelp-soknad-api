package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.print.HtmlGenerator;
import no.nav.sbl.dialogarena.print.HtmlToPdf;
import no.nav.sbl.dialogarena.print.PDFFabrikk;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.EttersendingService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SendSoknadService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.XsrfGenerator;
import no.nav.sbl.dialogarena.websoknad.domain.StartSoknad;
import org.apache.commons.collections15.Predicate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static javax.xml.bind.JAXBContext.newInstance;
import static no.nav.modig.lang.collections.IterUtils.on;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

/**
 * Klassen håndterer alle rest kall for å hente grunnlagsdata til applikasjonen.
 */
@Controller
@ControllerAdvice()
@RequestMapping("/soknad")
public class SoknadDataController {

    @Inject
    private SendSoknadService soknadService;
    @Inject
    private EttersendingService ettersendingService;
    @Inject
    private VedleggService vedleggService;
    @Inject
    private HtmlGenerator pdfTemplate;

    private HtmlToPdf pdfGenerator = new PDFFabrikk();

    @RequestMapping(value = "/{soknadId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public WebSoknad hentSoknadData(@PathVariable Long soknadId) {
        return soknadService.hentSoknad(soknadId);
    }

    @RequestMapping(value = "/{soknadId}/struktur", method = RequestMethod.GET, produces = "application/json")
    public String hentSoknadSokXMl(@PathVariable Long soknadId) throws UnsupportedEncodingException {
        ByteArrayOutputStream ous = new ByteArrayOutputStream();
        JAXB.marshal(soknadService.hentSoknad(soknadId), ous);
        return ous.toString("UTF-8");
    }

    @RequestMapping(value = "/behandlingmetadata/{behandlingsId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    public Map<String, String> hentBehandlingMetaData(@PathVariable String behandlingsId) {
        return soknadService.hentInnsendtDatoForOpprinneligSoknad(behandlingsId);
    }

    @RequestMapping(value = "/metadata/{soknadId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public WebSoknad hentSoknadMetaData(@PathVariable Long soknadId) {
        return soknadService.hentSoknad(soknadId);
    }

    @RequestMapping(value = "/behandling/{behandlingsId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public Map<String, String> hentSoknadIdMedBehandligsId(@PathVariable String behandlingsId) {
        Map<String, String> result = new HashMap<>();
        String sanitizedBehandlingsId = behandlingsId.replaceAll("%20", " ");
        WebSoknad soknad = soknadService.hentSoknadMedBehandlingsId(sanitizedBehandlingsId);

        if (soknad == null || !soknad.erUnderArbeid()) {
            soknad = ettersendingService.hentEttersendingForBehandlingskjedeId(sanitizedBehandlingsId);
        }

        result.put("result", soknad.getSoknadId().toString());

        return result;
    }

    @RequestMapping(value = "/options/{soknadId}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public SoknadStruktur hentSoknadStruktur(@PathVariable Long soknadId) {
        String type = soknadService.hentSoknad(soknadId).getskjemaNummer() + ".xml";
        try {
            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class)
                    .createUnmarshaller();
            return (SoknadStruktur) unmarshaller.unmarshal(SoknadStruktur.class
                    .getResourceAsStream(format("/soknader/%s", type)));
        } catch (JAXBException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }
    }

    @RequestMapping(value = "/delsteg/{soknadId}/{delsteg}", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @SjekkTilgangTilSoknad
    public void settDelstegStatus(@PathVariable Long soknadId, @PathVariable String delsteg) {
        if (delsteg == null) {
            throw new ApplicationException("Ugyldig delsteg sendt inn til REST-controller.");
        } else {
            DelstegStatus delstegstatus;
            if (delsteg.equalsIgnoreCase("utfylling")) {
                delstegstatus = DelstegStatus.UTFYLLING;

            } else if (delsteg.equalsIgnoreCase("vedlegg")) {
                delstegstatus = DelstegStatus.SKJEMA_VALIDERT;

            } else if (delsteg.equalsIgnoreCase("oppsummering")) {
                delstegstatus = DelstegStatus.VEDLEGG_VALIDERT;
            } else {
                throw new ApplicationException("Ugyldig delsteg sendt inn til REST-controller.");
            }
            soknadService.settDelsteg(soknadId, delstegstatus);
        }
    }

    @RequestMapping(value = "{soknadId}/{faktumId}/forventning", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public List<Vedlegg> hentPaakrevdeVedleggForFaktum(
            @PathVariable final Long soknadId, @PathVariable final Long faktumId) {
        return on(vedleggService.hentPaakrevdeVedlegg(soknadId)).filter(new Predicate<Vedlegg>() {
            @Override
            public boolean evaluate(Vedlegg vedleggForventning) {
                return vedleggForventning.getFaktumId().equals(faktumId);
            }
        }).collect();
    }

    @RequestMapping(value = "/send/{soknadId}", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public void sendSoknad(@PathVariable Long soknadId) {
        WebSoknad soknad = soknadService.hentSoknad(soknadId);

        byte[] kvittering = genererPdf(soknad, "/skjema/kvittering");;
        if (soknad.erEttersending()) {
            soknadService.sendSoknad(soknadId, kvittering);
        } else {
            byte[] soknadPdf;
            if(soknad.erGjenopptak()) {
                soknadPdf = genererPdf(soknad, "/skjema/gjenopptak");
            } else {
                soknadPdf = genererPdf(soknad, "/skjema/dagpenger");
            }
            vedleggService.lagreKvitteringSomVedlegg(soknadId, kvittering);
            soknadService.sendSoknad(soknadId, soknadPdf);
        }
    }

    private byte[] genererPdf(WebSoknad soknad, String hbsSkjemaPath) {
        String pdfMarkup;
        try {
            vedleggService.leggTilKodeverkFelter(soknad.getVedlegg());
            pdfMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, hbsSkjemaPath);
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup av søknad", e);
        }

        return pdfGenerator.lagPdfFil(pdfMarkup);
    }

    @RequestMapping(value = "/lagre/{soknadId}", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public void lagreSoknad(@PathVariable Long soknadId,
                            @RequestBody WebSoknad webSoknad) {
        for (Faktum faktum : webSoknad.getFaktaListe()) {
            soknadService.lagreSoknadsFelt(soknadId, faktum);
        }
    }

    @RequestMapping(value = "/{soknadId}/hentpdf", method = RequestMethod.GET, produces = APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public byte[] hentPdf(@PathVariable final Long soknadId) {
        WebSoknad soknad = soknadService.hentSoknad(soknadId);
        String oppsummeringMarkup;
        try {
            vedleggService.leggTilKodeverkFelter(soknad.getVedlegg());
            oppsummeringMarkup = pdfTemplate.fyllHtmlMalMedInnhold(soknad, "/skjema/dagpenger");
        } catch (IOException e) {
            throw new ApplicationException("Kunne ikke lage markup av søknad", e);
        }
        return pdfGenerator.lagPdfFil(oppsummeringMarkup);
    }

    @RequestMapping(value = "/opprett/ettersending/{behandlingskjedeId}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody()
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> opprettSoknadEttersending(@PathVariable String behandlingskjedeId) {
        Map<String, String> result = new HashMap<>();
        WebSoknad soknad = ettersendingService.hentEttersendingForBehandlingskjedeId(behandlingskjedeId);
        Long soknadId;
        if (soknad == null) {
            soknadId = ettersendingService.startEttersending(behandlingskjedeId);
        } else {
            soknadId = soknad.getSoknadId();
        }

        result.put("soknadId", soknadId.toString());
        return result;
    }

    @RequestMapping(value = "/opprett", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody()
    public Map<String, String> opprettSoknad(@RequestBody StartSoknad soknadType, HttpServletResponse response) {
        Map<String, String> result = new HashMap<>();

        String behandlingId = soknadService.startSoknad(soknadType.getSoknadType());
        result.put("brukerbehandlingId", behandlingId);
        Cookie xsrfCookie = new Cookie("XSRF-TOKEN", XsrfGenerator.generateXsrfToken(behandlingId));
        xsrfCookie.setPath("/");
        response.addCookie(xsrfCookie);
        return result;
    }

    @RequestMapping(value = "/delete/{soknadId}", method = RequestMethod.POST)
    @ResponseBody()
    @SjekkTilgangTilSoknad
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void slettSoknad(@PathVariable Long soknadId) {
        soknadService.avbrytSoknad(soknadId);
    }

    @RequestMapping(value = "/oppsummering/{soknadId}", method = RequestMethod.GET, produces = "text/html")
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public String hentOppsummering(@PathVariable Long soknadId) throws IOException {
        WebSoknad soknad = soknadService.hentSoknad(soknadId);
        vedleggService.leggTilKodeverkFelter(soknad.getVedlegg());
        if(soknad.erGjenopptak()) {
            return  pdfTemplate.fyllHtmlMalMedInnhold(soknad, "/skjema/gjenopptak");
        } else {
            return pdfTemplate.fyllHtmlMalMedInnhold(soknad, "/skjema/dagpenger");
        }
    }
}
