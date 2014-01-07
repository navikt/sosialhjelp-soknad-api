package no.nav.sbl.dialogarena.websoknad.servlet;

import no.nav.sbl.dialogarena.soknadinnsending.RestFeil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Håndterer alle exceptions som blir kastet fra rest klasser
 */
@ControllerAdvice
@Service
public class ExceptionController {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionController.class);

    @ResponseBody
    @ExceptionHandler(Throwable.class)
    @RequestMapping(produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RestFeil> handlerException(Exception ex) {
        LOG.warn("Rest kall feilet med: " + ex, ex);
        HttpHeaders header = new HttpHeaders();
        header.setContentType(APPLICATION_JSON);
        return new ResponseEntity<>(new RestFeil("generell"), header, HttpStatus.BAD_REQUEST);
    }
}
