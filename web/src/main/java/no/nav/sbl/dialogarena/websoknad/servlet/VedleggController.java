package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.VedleggOpplasting;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.SoknadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

/**
 * Controller klasse som brukes til å laste opp filer fra frontend.
 */
@Controller()
@RequestMapping("/soknad/{soknadId}/faktum/{faktumId}/vedlegg")
public class VedleggController {
    public static final String BASE_URL = "soknad/%d/faktum/%d/vedlegg/%d/%s";
    @Inject
    private SoknadService soknadService;

    @RequestMapping(value = "", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public VedleggOpplasting lastOppDokumentSoknad(@PathVariable Long soknadId, @PathVariable Long faktumId, @RequestParam("files[]") List<MultipartFile> files) {
        List<Vedlegg> res = new ArrayList<>();
        for (MultipartFile file : files) {
            Vedlegg vedlegg = new Vedlegg(null, soknadId, faktumId, file.getOriginalFilename(), file.getSize(), null);

            try {
                vedlegg.setInputStream(file.getInputStream());
            } catch (IOException e) {
                throw new ApplicationException("Kunne ikke lagre fil", e);
            }
            vedlegg.setId(soknadService.lagreVedlegg(vedlegg, vedlegg.getInputStream()));
            res.add(vedlegg);
        }
        return new VedleggOpplasting(res);
    }

    @RequestMapping(value = "/{vedleggId}/delete", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public void slettVedlegg(@PathVariable final Long soknadId, @PathVariable final Long vedleggId) {
        try {
            soknadService.slettVedlegg(soknadId, vedleggId);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    @RequestMapping(value = "/{vedleggId}", method = RequestMethod.GET, produces = APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody()
    public byte[] hentVedlegg(@PathVariable final Long soknadId, @PathVariable final Long vedleggId, HttpServletResponse response) {
        Vedlegg vedlegg = soknadService.hentVedleggMedInnhold(soknadId, vedleggId);
        response.setHeader("Content-Disposition", "attachment; filename=\"" +vedlegg.getId() + ".pdf\"");
        return vedlegg.getData();
    }
    @RequestMapping(value = "", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public VedleggOpplasting hentVedleggForFaktum(@PathVariable final Long soknadId, @PathVariable final Long vedleggId) {
        List<Vedlegg> vedlegg = soknadService.hentVedleggForFaktum(soknadId, vedleggId);
        return new VedleggOpplasting(vedlegg);
    }

    @RequestMapping(value = "/{vedleggId}/thumbnail", method = RequestMethod.GET, produces = IMAGE_PNG_VALUE)
    @ResponseBody()
    public Callable<byte[]> lagForhandsvisningForVedlegg(@PathVariable final Long soknadId, @PathVariable final Long vedleggId) {
        return new Callable<byte[]>() {
            @Override
            public byte[] call() throws Exception {
                return soknadService.lagForhandsvisning(soknadId, vedleggId);
            }
        };
    }


    @RequestMapping(value = "/generer", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public Callable<Void> bekreftFaktumVedlegg(@PathVariable final Long soknadId, @PathVariable final Long faktumId) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                soknadService.genererVedleggFaktum(soknadId, faktumId);
                return null;
            }
        };
    }

    private static Logger logger = LoggerFactory.getLogger(VedleggController.class);

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handlerException(Exception ex) {
        logger.warn("Feilet opplasting: " + ex, ex);

        return "Feilet med: " + ex + "  " + Arrays.toString(ex.getStackTrace());
    }

}
