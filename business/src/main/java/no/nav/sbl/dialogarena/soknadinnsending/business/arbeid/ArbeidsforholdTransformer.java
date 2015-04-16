package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.sbl.dialogarena.soknadinnsending.business.domain.dto.Land;

import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon;
import org.apache.commons.collections15.Transformer;
import org.joda.time.Interval;

public class ArbeidsforholdTransformer implements Transformer<no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold, Arbeidsforhold > {
    @Override
    public Arbeidsforhold transform(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        Arbeidsforhold result = new Arbeidsforhold();
        result.orgnr = ((Organisasjon)arbeidsforhold.getArbeidsgiver()).getOrgnummer();
        result.arbridsgiverNavn = ((Organisasjon)arbeidsforhold.getArbeidsgiver()).getNavn();
        Gyldighetsperiode periode = arbeidsforhold.getAnsettelsesPeriode().getPeriode();
        result.periode = new Interval(periode.getFom().toGregorianCalendar().getTimeInMillis(), periode.getTom() != null ? periode.getTom().toGregorianCalendar().getTimeInMillis(): System.currentTimeMillis());
        return result;

    }
}
