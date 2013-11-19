package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.VedleggOpplastingResultat;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

/**
 * Controller klasse som brukes til å laste opp filer fra frontend.
 */
@Controller()
@RequestMapping("/soknad/{soknadId}/vedlegg")
public class VedleggController {
    public static final String BASE_URL = "soknad/%d/vedlegg/%d/%s";
    @Inject
    private SoknadService soknadService;

    @RequestMapping(value = "", params = "faktumId", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public List<VedleggOpplastingResultat> lastOppDokumentSoknad(@PathVariable Long soknadId, @RequestParam Long faktumId, @RequestParam("files[]") List<MultipartFile> files) {
        List<VedleggOpplastingResultat> res = new ArrayList<>();
        for (MultipartFile file : files) {
            Vedlegg vedlegg = new Vedlegg();
            vedlegg.setSoknadId(soknadId);
            vedlegg.setNavn(file.getOriginalFilename());
            vedlegg.setFaktum(faktumId);
            vedlegg.setStorrelse(file.getSize());
            try {
                vedlegg.setInputStream(file.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException("Kunne ikke lagre fil", e);
            }
            Long id = soknadService.lagreVedlegg(vedlegg);

            VedleggOpplastingResultat ut = new VedleggOpplastingResultat();
            ut.setName(vedlegg.getNavn());
            ut.setSize(vedlegg.getStorrelse().intValue());
            ut.setThumbnailUrl(format(BASE_URL, soknadId, id, "thumbnail"));
            ut.setDeleteUrl(format(BASE_URL, soknadId, id, "delete"));
            res.add(ut);
        }
        return res;
    }

    @RequestMapping(value = "/{vedlegg}/delete", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    public void slettVedlegg(@PathVariable final Long soknadId, @PathVariable final Long vedleggId) {
        soknadService.slettVedlegg(soknadId, vedleggId);
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


    @RequestMapping(value = "/bekreft?faktumId={faktumId}", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    public Long bekreftFaktumVedlegg(@PathVariable Long soknadId, @RequestParam Long faktumId) {
        return 0L;
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
