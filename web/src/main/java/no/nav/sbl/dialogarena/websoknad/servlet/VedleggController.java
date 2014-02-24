package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.RestFeil;
import no.nav.sbl.dialogarena.soknadinnsending.VedleggOpplasting;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.SjekkTilgangTilSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.sikkerhet.XsrfGenerator;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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

import static no.nav.modig.lang.collections.IterUtils.on;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

/**
 * Controller klasse som brukes til å laste opp filer fra frontend.
 */
@Controller()
@ControllerAdvice()
@RequestMapping("/soknad/{soknadId}/vedlegg")
public class VedleggController {
    private static final Logger LOG = LoggerFactory.getLogger(VedleggController.class);
    @Inject
    private VedleggService vedleggService;

    private static byte[] getByteArray(MultipartFile file) {
        try {
            return IOUtils.toByteArray(file.getInputStream());
        } catch (IOException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e, "vedlegg.opplasting.feil.generell");
        }
    }

    @RequestMapping(method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public List<Vedlegg> hentPaakrevdeVedlegg(
            @PathVariable final Long soknadId) {
        return vedleggService.hentPaakrevdeVedlegg(soknadId);
    }

    @RequestMapping(value = "/{vedleggId}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public Vedlegg hentVedlegg(@PathVariable final Long soknadId, @PathVariable final Long vedleggId) {
        return vedleggService.hentVedlegg(soknadId, vedleggId, false);
    }

    @RequestMapping(value = "/{faktumId}/hentannetvedlegg", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public Vedlegg hentAnnetVedlegg(@PathVariable final Long soknadId, @PathVariable final Long faktumId) {
        return on(vedleggService.hentPaakrevdeVedlegg(soknadId)).filter(new Predicate<Vedlegg>() {
            @Override
            public boolean evaluate(Vedlegg vedleggForventning) {
                return vedleggForventning.getFaktumId().equals(faktumId);
            }
        }).collect().get(0);
    }

    @RequestMapping(value = "/{vedleggId}", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @SjekkTilgangTilSoknad
    public void lagreVedlegg(@PathVariable final Long soknadId, @PathVariable final Long vedleggId, @RequestBody Vedlegg vedlegg) {
        vedleggService.lagreVedlegg(soknadId, vedleggId, vedlegg);
    }

    @RequestMapping(value = "/{vedleggId}/delete", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @SjekkTilgangTilSoknad
    public void slettVedlegg(@PathVariable final Long soknadId, @PathVariable final Long vedleggId) {
        vedleggService.slettVedlegg(soknadId, vedleggId);
    }

    @RequestMapping(value = "/{vedleggId}/data", method = RequestMethod.GET, produces = APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public byte[] hentVedleggData(@PathVariable final Long soknadId, @PathVariable final Long vedleggId, HttpServletResponse response) {
        Vedlegg vedlegg = vedleggService.hentVedlegg(soknadId, vedleggId, true);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + vedlegg.getVedleggId() + ".pdf\"");
        return vedlegg.getData();
    }

    @RequestMapping(value = "/{vedleggId}/thumbnail", method = RequestMethod.GET, produces = IMAGE_PNG_VALUE)
    @ResponseBody()
    @SjekkTilgangTilSoknad
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

    @RequestMapping(value = "/{vedleggId}/opplasting", method = RequestMethod.POST, produces = "text/plain; charset=utf-8")
    @ResponseBody()
    @ResponseStatus(HttpStatus.CREATED)
    @SjekkTilgangTilSoknad(sjekkXsrf = false)
    public VedleggOpplasting lastOppDokumentSoknad(@PathVariable final Long soknadId, @PathVariable final Long vedleggId, @RequestParam("X-XSRF-TOKEN") final String xsrfToken, @RequestParam("files[]") final List<MultipartFile> files) {
        XsrfGenerator.sjekkXsrfToken(xsrfToken, soknadId);
        Vedlegg forventning = vedleggService.hentVedlegg(soknadId, vedleggId, false);

        List<Vedlegg> res = new ArrayList<>();
        for (MultipartFile file : files) {
            byte[] in = getByteArray(file);
            Vedlegg vedlegg = new Vedlegg()
                    .medVedleggId(null)
                    .medSoknadId(soknadId)
                    .medFaktumId(forventning.getFaktumId())
                    .medSkjemaNummer(forventning.getskjemaNummer())
                    .medNavn(forventning.getNavn())
                    .medStorrelse(file.getSize())
                    .medAntallSider(1)
                    .medFillagerReferanse(null)
                    .medData(in)
                    .medOpprettetDato(forventning.getOpprettetDato())
                    .medInnsendingsvalg(Vedlegg.Status.UnderBehandling);

            List<Long> ids = vedleggService.splitOgLagreVedlegg(vedlegg, new ByteArrayInputStream(in));
            for (Long id : ids) {
                res.add(vedleggService.hentVedlegg(soknadId, id, false));
            }
        }
        return new VedleggOpplasting(res);
    }

    @RequestMapping(value = "/{vedleggId}/underBehandling", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @SjekkTilgangTilSoknad
    public List<Vedlegg> hentVedleggUnderBehandling(@PathVariable final Long soknadId, @PathVariable final Long vedleggId) {
        Vedlegg forventning = vedleggService.hentVedlegg(soknadId, vedleggId, false);
        return vedleggService.hentVedleggUnderBehandling(soknadId, forventning.getFaktumId(), forventning.getskjemaNummer());
    }

    @RequestMapping(value = "/{vedleggId}/generer", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ResponseBody()
    @SjekkTilgangTilSoknad
    public Vedlegg bekreftFaktumVedlegg(@PathVariable final Long soknadId, @PathVariable final Long vedleggId) {
        vedleggService.genererVedleggFaktum(soknadId, vedleggId);
        return vedleggService.hentVedlegg(soknadId, vedleggId, false);
    }

}
