package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ArbeidsforholdBolk implements BolkService {

    @Inject
    private FaktaService faktaService;

    @Inject
    private ArbeidsforholdService arbeidsforholdService;

    private static final Logger LOG = LoggerFactory.getLogger(ArbeidsforholdBolk.class);


    public List<Faktum> genererArbeidsforhold(String fodselsnummer, final Long soknadId) {
        List<Faktum> arbeidsforholdFakta =  new ArrayList<>();
        arbeidsforholdFakta.addAll(
                arbeidsforholdService.hentArbeidsforhold(fodselsnummer).stream()
                        .map(arbeidsforhold -> transformerTilFaktum(arbeidsforhold,soknadId))
                        .collect(Collectors.toList())
        );
        if (!arbeidsforholdFakta.isEmpty()) {
            Faktum yrkesaktiv = faktaService.hentFaktumMedKey(soknadId, "arbeidsforhold.yrkesaktiv");

            for (Faktum faktum : arbeidsforholdFakta) {
                faktum.setParrentFaktum(yrkesaktiv.getFaktumId());
            }

            if(maSetteYrkesaktiv(yrkesaktiv)){
                arbeidsforholdFakta.add(yrkesaktiv.medValue("false"));
            }
        }
        return arbeidsforholdFakta;
    }

    private boolean maSetteYrkesaktiv(Faktum yrkesaktiv) {
        return yrkesaktiv.getValue() == null || "true".equals(yrkesaktiv.getValue());
    }

    private Faktum transformerTilFaktum(final Arbeidsforhold arbeidsforhold, final Long soknadId) {
        return new Faktum()
                        .medSoknadId(soknadId)
                        .medKey("arbeidsforhold")
                        .medSystemProperty("orgnr", Objects.toString(arbeidsforhold.orgnr, ""))
                        .medSystemProperty("arbeidsgivernavn", arbeidsforhold.arbridsgiverNavn)
                        .medSystemProperty("ansatt", trueFalse(arbeidsforhold.tom == null))
                        .medSystemProperty("fom", Objects.toString(arbeidsforhold.fom, ""))
                        .medSystemProperty("tom", Objects.toString(arbeidsforhold.tom, ""))
                        .medSystemProperty("land", "NO")
                        .medSystemProperty("stillingstype", finnStillingsType(arbeidsforhold))
                        .medSystemProperty("stillingsprosent", Objects.toString(arbeidsforhold.fastStillingsprosent, ""))
                        .medSystemProperty("kilde", "EDAG")
                        .medSystemProperty("edagref", "" + arbeidsforhold.edagId)
                        .medUnikProperty("edagref");
    }





    private static String trueFalse(boolean test) {
        return test ? "true" : "false";
    }

    private static String finnStillingsType(Arbeidsforhold arbeidsforhold) {
        if (arbeidsforhold.harFastStilling && arbeidsforhold.variabelStillingsprosent) {
            return "fastOgVariabel";
        } else if (arbeidsforhold.harFastStilling) {
            return "fast";
        } else {
            return "variabel";
        }
    }

    @Override
    public String tilbyrBolk() {
        return "Arbeidsforhold";
    }

    @Override
    @Cacheable("arbeidsforholdCache")
    public List<Faktum> genererSystemFakta(String fodselsnummer, Long soknadId) {
        try {
            return genererArbeidsforhold(fodselsnummer, soknadId);
        } catch (Exception e) {
            LOG.warn("Kunne ikke hente arbeidsforhold: " + e, e);
            return Arrays.asList();
        }
    }
}
