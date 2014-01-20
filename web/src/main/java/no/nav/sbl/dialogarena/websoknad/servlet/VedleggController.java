package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.RestFeil;
import no.nav.sbl.dialogarena.soknadinnsending.VedleggOpplasting;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

/**
 * Controller klasse som brukes til å laste opp filer fra frontend.
 */
@Controller()
@ControllerAdvice()
@RequestMapping("/soknad/{soknadId}/faktum/{faktumId}/vedlegg")
public class VedleggController {
    private static final Logger LOG = LoggerFactory.getLogger(VedleggController.class);
    //private static final List<String> LEGAL_CONTENT_TYPES = Arrays.asList("application/pdf", "image/png", "image/jpeg");
    @Inject
    private VedleggService vedleggService;

    @RequestMapping(value = "/{vedleggId}/delete", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public void slettVedlegg(@PathVariable final Long soknadId, @PathVariable final Long vedleggId) {
        vedleggService.slettVedlegg(soknadId, vedleggId);
    }

    @RequestMapping(value = "/{vedleggId}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public Vedlegg hentVedlegg(@PathVariable final Long soknadId, @PathVariable final Long vedleggId) {
        return vedleggService.hentVedlegg(soknadId, vedleggId, false);
    }

    @RequestMapping(value = "/{vedleggId}/data", method = RequestMethod.GET, produces = APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody()
    public byte[] hentVedleggData(@PathVariable final Long soknadId, @PathVariable final Long vedleggId, HttpServletResponse response) {
        Vedlegg vedlegg = vedleggService.hentVedlegg(soknadId, vedleggId, true);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + vedlegg.getId() + ".pdf\"");
        return vedlegg.getData();
    }

    @RequestMapping(value = "/{vedleggId}/thumbnail", method = RequestMethod.GET, produces = IMAGE_PNG_VALUE)
    @ResponseBody()
    public byte[] lagForhandsvisningForVedlegg(@PathVariable final Long soknadId, @PathVariable final Long vedleggId, @RequestParam(value = "side", defaultValue = "0") final int side) {
        return vedleggService.lagForhandsvisning(soknadId, vedleggId, side);
    }

    @ExceptionHandler(UgyldigOpplastingTypeException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public RestFeil handterFeilType(UgyldigOpplastingTypeException ex) {
        LOG.warn("Feilet opplasting med: " + ex, ex);
        return new RestFeil(ex.getId());
    }

    @ExceptionHandler(OpplastingException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public RestFeil handterVedleggException(OpplastingException ex) {
        LOG.warn("Feilet opplasting med: " + ex, ex);
        return new RestFeil(ex.getId());
    }

    @RequestMapping(value = "", params = "gosysId", method = RequestMethod.POST, produces = "text/plain; charset=utf-8")
    @ResponseBody()
    @ResponseStatus(HttpStatus.CREATED)
    public Callable<VedleggOpplasting> lastOppDokumentSoknad(@PathVariable final Long soknadId, @PathVariable final Long faktumId, @RequestParam final String gosysId, @RequestParam("files[]") final List<MultipartFile> files) {
        return new Callable<VedleggOpplasting>() {

            @Override
            public VedleggOpplasting call() throws Exception {
                List<Vedlegg> res = new ArrayList<>();
                for (MultipartFile file : files) {
                    byte[] in = getByteArray(file);
                    Vedlegg vedlegg = new Vedlegg(null, soknadId, faktumId, gosysId, file.getOriginalFilename(), file.getSize(), 1, null, in);
                    List<Long> ids = vedleggService.splitOgLagreVedlegg(vedlegg, new ByteArrayInputStream(in));
                    for (Long id : ids) {
                        res.add(vedleggService.hentVedlegg(soknadId, id, false));
                    }
                }
                return new VedleggOpplasting(res);
            }
        };
    }

    @RequestMapping(value = "", params = "gosysId", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public VedleggOpplasting hentVedleggForFaktum(@PathVariable final Long soknadId, @PathVariable final Long faktumId, @RequestParam String gosysId) {
        List<Vedlegg> vedlegg = vedleggService.hentVedleggForFaktum(soknadId, faktumId, gosysId);
        return new VedleggOpplasting(vedlegg);
    }

    @RequestMapping(value = "/generer", params = "gosysId", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public Vedlegg bekreftFaktumVedlegg(@PathVariable final Long soknadId, @PathVariable final Long faktumId, @RequestParam final String gosysId) {
        Long vedleggId = vedleggService.genererVedleggFaktum(soknadId, faktumId, gosysId);
        return vedleggService.hentVedlegg(soknadId, vedleggId, false);
    }

    private static byte[] getByteArray(MultipartFile file) {
        try {
            return IOUtils.toByteArray(file.getInputStream());
        } catch (IOException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell");
        }
    }

}
