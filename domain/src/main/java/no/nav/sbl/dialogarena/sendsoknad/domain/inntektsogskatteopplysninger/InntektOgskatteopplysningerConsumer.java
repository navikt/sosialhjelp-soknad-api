package no.nav.sbl.dialogarena.sendsoknad.domain.inntektsogskatteopplysninger;

import no.nav.sbl.dialogarena.sendsoknad.domain.utbetaling.Utbetaling;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface InntektOgskatteopplysningerConsumer {

    Optional<List<Utbetaling>> sok(Sokedata sokedata);

    void ping();

    class InntektOgskatteopplysningerRespons {
        public List<OppgaveInntektsmottaker> oppgaveInntektsmottaker = new ArrayList<>();
    }


    class OppgaveInntektsmottaker {
        public List<Inntekt> inntekt = new ArrayList<>();
    }

    class Inntekt {
        public String skatteOgAvgiftsregel;
        public String fordel;
        public String utloeserArbeidsgiveravgift;
        public String inngaarIGrunnlagForTrekk;
        public Long beloep;
        public Loennsinntekt loennsinntekt;
        public YtelseFraOffentlige ytelseFraOffentlige;
        public PensjonEllerTrygd pensjonEllerTrygd;
        public Naeringsinntekt naeringsinntekt;
    }

    class Loennsinntekt {
        Tilleggsinformasjon tilleggsinformasjon;
    }

    class YtelseFraOffentlige {
        Tilleggsinformasjon tilleggsinformasjon;
    }

    class PensjonEllerTrygd {
        Tilleggsinformasjon tilleggsinformasjon;
    }

    class Naeringsinntekt {
        Tilleggsinformasjon tilleggsinformasjon;
    }

    class Tilleggsinformasjon {
        DagmammaIEgenBolig dagmammaIEgenBolig;
        LottOgPartInnenFiske lottOgPart;
        AldersUfoereEtterlatteAvtalefestetOgKrigspensjon pensjon;

    }

    class DagmammaIEgenBolig {

    }

    class LottOgPartInnenFiske {

    }

    class AldersUfoereEtterlatteAvtalefestetOgKrigspensjon {

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

        public Sokedata withFom(LocalDate fom) {
            this.fom = fom;
            return this;
        }

        public Sokedata withTom(LocalDate tom) {
            this.tom = tom;
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
