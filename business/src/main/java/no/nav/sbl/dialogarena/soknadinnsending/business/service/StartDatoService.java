package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.springframework.stereotype.Component;

import static org.joda.time.DateTime.now;

@Component
public class StartDatoService {

    public Boolean erJanuarEllerFebruar() {
        return now().monthOfYear().get() < 3;
    }
}
