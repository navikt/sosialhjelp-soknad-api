package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SosialhjelpArbeidsforholdBolk extends ArbeidsforholdBolk {


    @Override
    public String tilbyrBolk() {
        return "SosialhjelpArbeidsforhold";
    }

    ArbeidsforholdService.Sokeperiode getSoekeperiode() {
        return new ArbeidsforholdService.Sokeperiode(new DateTime().minusMonths(3), new DateTime());
    }

    @Override
    protected void afterGenererArbeidsforhold(List<Faktum> arbeidsforholdFakta, Long soknadId) {
        // Ignore.
    }
}
