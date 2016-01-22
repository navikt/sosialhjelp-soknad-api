package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.dialogarena.soknadinnsending.consumer.MockUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import static no.nav.sbl.dialogarena.soknadinnsending.consumer.MockUtil.startdatoMockErTillattOgAktivert;

@Component
public class StartDatoUtil {

    public Boolean erJanuarEllerFebruar() {
        Integer maaned;
        if (startdatoMockErTillattOgAktivert()) {
            maaned = MockUtil.valgtMaaned();
        } else {
            maaned = DateTime.now().monthOfYear().get();
        }
        return maaned < 3;
    }
}
