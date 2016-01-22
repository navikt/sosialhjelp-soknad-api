package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import com.google.common.collect.Lists;
import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.NorskIdent;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Periode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Regelverker;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.List;

@Service
public class ArbeidsforholdService {


    @Inject
    @Named("arbeidEndpoint")
    private ArbeidsforholdV3 arbeidsforholdWebWervice;

    private static final Regelverker AA_ORDNINGEN = new Regelverker();
    static {
        AA_ORDNINGEN.setValue("A_ORDNINGEN");
    }

    private DatatypeFactory datatypeFactory = lagDatatypeFactory();


    public List<Arbeidsforhold> hentArbeidsforhold(String fodselsnummer) {
        try {
            FinnArbeidsforholdPrArbeidstakerRequest finnArbeidsforholdPrArbeidstakerRequest = lagArbeidsforholdRequest(fodselsnummer);

            List<no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold> arbeidsforhold = arbeidsforholdWebWervice.finnArbeidsforholdPrArbeidstaker(finnArbeidsforholdPrArbeidstakerRequest).getArbeidsforhold();
            return Lists.transform(arbeidsforhold, new ArbeidsforholdTransformer());
        } catch (FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning | FinnArbeidsforholdPrArbeidstakerUgyldigInput e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private FinnArbeidsforholdPrArbeidstakerRequest lagArbeidsforholdRequest(String fodselsnummer) {
        FinnArbeidsforholdPrArbeidstakerRequest request = new FinnArbeidsforholdPrArbeidstakerRequest();
        request.setArbeidsforholdIPeriode(lagSporrePeriode());
        request.setRapportertSomRegelverk(AA_ORDNINGEN);
        request.setIdent(lagIdent(fodselsnummer));
        return request;
    }

    private NorskIdent lagIdent(String fodselsnummer) {
        NorskIdent ident = new NorskIdent();
        ident.setIdent(fodselsnummer);
        return ident;
    }

    private Periode lagSporrePeriode() {
        Periode periode = new Periode();
        periode.setFom(datatypeFactory.newXMLGregorianCalendar(new DateTime().minusMonths(10).toGregorianCalendar()));
        periode.setTom(datatypeFactory.newXMLGregorianCalendar(new DateTime().toGregorianCalendar()));
        return periode;
    }


    private DatatypeFactory lagDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

}
