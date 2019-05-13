package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.ArbeidsforholdService;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.LocalDate.now;

@Service
public class SosialhjelpArbeidsforholdBolk extends ArbeidsforholdBolk {

    @Inject
    public SosialhjelpArbeidsforholdBolk(FaktaService faktaService, ArbeidsforholdService arbeidsforholdService) {
        super(faktaService, arbeidsforholdService);
    }

    @Override
    public String tilbyrBolk() {
        return "SosialhjelpArbeidsforhold";
    }

    ArbeidsforholdService.Sokeperiode getSoekeperiode() {
        return new ArbeidsforholdService.Sokeperiode(new DateTime().minusMonths(3), new DateTime());
    }

    @Override
    protected void afterGenererArbeidsforhold(List<Faktum> arbeidsforholdFakta, Long soknadId) {
        boolean harArbeid = !arbeidsforholdFakta.isEmpty();
        boolean skalBeOmSluttoppgjor = arbeidsforholdFakta.stream()
                .anyMatch(arbeid -> arbeid.getProperties().get("tom") != null
                    && !arbeid.getProperties().get("tom").trim().equals("")
                    && isWithinOneMonthAheadInTime(arbeid.getProperties().get("tom")));
        boolean harAvsluttetArbeidsforhold = arbeidsforholdFakta.stream()
                .anyMatch(arbeid -> "false".equals(arbeid.getProperties().get("ansatt")));
        boolean harGjeldendeArbeidsforhold = arbeidsforholdFakta.stream()
                .anyMatch(arbeid -> "true".equals(arbeid.getProperties().get("ansatt")));
        boolean skalBeOmLonnslipp = harGjeldendeArbeidsforhold || arbeidsforholdFakta.stream()
                .anyMatch(arbeid -> arbeid.getProperties().get("tom") != null
                        && !arbeid.getProperties().get("tom").trim().equals("")
                        && !isWithinOneMonthAheadInTime(arbeid.getProperties().get("tom")));;

        Faktum dinSituasjonJobb = new Faktum().medSoknadId(soknadId).medKey("dinsituasjon.registrertjobb")
                .medSystemProperty("harhentetfraaareg", "true")
                .medSystemProperty("hararbeidsforhold", Boolean.toString(harArbeid))
                .medSystemProperty("skalbeomsluttoppgjor", Boolean.toString(skalBeOmSluttoppgjor))
                .medSystemProperty("skalbeomlonnslipp", Boolean.toString(skalBeOmLonnslipp))
                .medSystemProperty("hargjeldendearbeidsforhold", Boolean.toString(harGjeldendeArbeidsforhold))
                .medSystemProperty("haravsluttetarbeidsforhold", Boolean.toString(harAvsluttetArbeidsforhold));
        arbeidsforholdFakta.add(dinSituasjonJobb);
    }

    private static boolean isWithinOneMonthAheadInTime(String datoSomTekst) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(datoSomTekst, formatter);
        return date.isBefore(now().plusMonths(1).plusDays(1));
    }
}
