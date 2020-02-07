package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold;

import com.google.common.base.Function;
import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonService;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.*;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;

@Service
public class ArbeidsforholdTransformer implements Transformer<no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold, Arbeidsforhold>,
        Function<no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold, Arbeidsforhold> {

    @Inject
    private OrganisasjonService organisasjonService;

    @Override
    public Arbeidsforhold transform(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        Arbeidsforhold result = new Arbeidsforhold();
        result.edagId = arbeidsforhold.getArbeidsforholdIDnav();
        result.orgnr = null;
        result.arbeidsgivernavn = "";

        if (arbeidsforhold.getArbeidsgiver() instanceof Organisasjon) {
            result.orgnr = ((Organisasjon) arbeidsforhold.getArbeidsgiver()).getOrgnummer();
            result.arbeidsgivernavn = organisasjonService.hentOrgNavn(result.orgnr);
        } else if (arbeidsforhold.getArbeidsgiver() instanceof HistoriskArbeidsgiverMedArbeidsgivernummer) {
            result.arbeidsgivernavn = ((HistoriskArbeidsgiverMedArbeidsgivernummer) arbeidsforhold.getArbeidsgiver()).getNavn();
        } else if (arbeidsforhold.getArbeidsgiver() instanceof Person) {
            result.arbeidsgivernavn = "Privatperson";
        }

        Gyldighetsperiode periode = arbeidsforhold.getAnsettelsesPeriode().getPeriode();
        result.fom = toStringDate(periode.getFom());
        result.tom = toStringDate(periode.getTom());

        if (arbeidsforhold.getArbeidsavtale() != null) {
            for (Arbeidsavtale arbeidsavtale : arbeidsforhold.getArbeidsavtale()) {
                result.harFastStilling = true;
                result.fastStillingsprosent += nullSafe(arbeidsavtale.getStillingsprosent());
            }
        }
        return result;

    }

    private String toStringDate(XMLGregorianCalendar fom) {
        return fom != null ? new DateTime(fom.toGregorianCalendar()).toString("yyyy-MM-dd") : null;
    }

    private Long nullSafe(BigDecimal number) {
        return number != null ? number.longValue() : 0;
    }

    @Override
    public Arbeidsforhold apply(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        return transform(arbeidsforhold);
    }
}
