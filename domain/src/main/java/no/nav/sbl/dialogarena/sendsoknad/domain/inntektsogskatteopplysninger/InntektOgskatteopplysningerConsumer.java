package no.nav.sbl.dialogarena.sendsoknad.domain.inntektsogskatteopplysninger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface InntektOgskatteopplysningerConsumer {

    InntektOgskatteopplysningerRespons sok(Sokedata sokedata);

    void ping();

    class InntektOgskatteopplysningerRespons {

        public Opplysninger opplysninger = new Opplysninger();
    }

    class Opplysninger {
        //Passende dto
    }

    class Sokedata {
        //Builder med personidentifikator og fom tom, brukes som parametere til rest kallet
        public String identifikator;
        public LocalDate fom;
        public LocalDate tom;

        public Sokedata withIdentifikator(String identifikator) {
            this.identifikator = identifikator;
            return this;
        }

        @Override
        public String toString() {
            return "Sokedata{" +
                    "identifikator='" + identifikator + '\'' +
                    ", fom=" + fom +
                    ", tom=" + tom +
                    '}';
        }
    }
}
