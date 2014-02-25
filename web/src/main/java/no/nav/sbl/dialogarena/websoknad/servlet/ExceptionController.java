package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.core.exception.ModigException;
import no.nav.modig.core.exception.SystemException;
import no.nav.sbl.dialogarena.soknadinnsending.RestFeil;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.UgyldigOpplastingTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Håndterer alle exceptions som blir kastet fra rest klasser
 */
@ControllerAdvice
@Controller
public class ExceptionController {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

    @ExceptionHandler(UgyldigOpplastingTypeException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResponseEntity<RestFeil> handterFeilType(UgyldigOpplastingTypeException ex) {
        logger.warn("Feilet opplasting med: " + ex, ex);
        return getResult(ex.getId(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(OpplastingException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseEntity<RestFeil> handterVedleggException(OpplastingException ex) {
        logger.warn("Feilet opplasting med: " + ex, ex);
        return getResult(ex.getId(), HttpStatus.NOT_ACCEPTABLE);
    }

    @ResponseBody
    @ExceptionHandler({ApplicationException.class, SystemException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RestFeil> handlerApplicationException(ModigException ex) {
        logger.warn("Rest kall feilet med: " + ex, ex);
        return getResult(ex.getId(), HttpStatus.BAD_REQUEST);
    }
    @ResponseBody
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RestFeil> handlerException(Exception ex) {
        logger.warn("Rest kall feilet med: " + ex, ex);
        return getResult("generell", HttpStatus.BAD_REQUEST);
    }


    private ResponseEntity<RestFeil> getResult(String id, HttpStatus badRequest) {
        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        return new ResponseEntity<>(new RestFeil(id), header, badRequest);
    }
}
