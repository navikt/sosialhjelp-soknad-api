package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.sbl.dialogarena.soknadinnsending.business.util.MockUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import static no.nav.sbl.dialogarena.soknadinnsending.business.util.MockUtil.startdatoMockErTillattOgAktivert;

@Component
public class StartDatoService {

    public boolean erJanuarEllerFebruar() {
        Integer maaned;
        if (startdatoMockErTillattOgAktivert()) {
            maaned = MockUtil.valgtMaaned();
        } else {
            maaned = DateTime.now().monthOfYear().get();
        }
        return maaned < 3;
    }
}
