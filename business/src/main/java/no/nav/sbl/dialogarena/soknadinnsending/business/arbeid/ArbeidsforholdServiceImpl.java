package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.NorskIdent;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Periode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Regelverker;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

@Service
public class ArbeidsforholdServiceImpl implements ArbeidsforholdService {
    @Inject
    @Named("arbeidEndpoint")
    private ArbeidsforholdV3 arbeidsforholdV2;
    @Inject
    @Named("organisasjonEndpoint")
    private OrganisasjonV4 organisasjonV4;
    private ArbeidsforholdTransformer transformer;

    @PostConstruct
    public void createTransformer(){
        transformer = new ArbeidsforholdTransformer(organisasjonV4);
    }

    @Override
    public List<Arbeidsforhold> hentArbeidsforhold(String fodselsnummer) {
        FinnArbeidsforholdPrArbeidstakerRequest request = new FinnArbeidsforholdPrArbeidstakerRequest();
        Periode periode = new Periode();
        DatatypeFactory datatypeFactory = null;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
        periode.setFom(datatypeFactory.newXMLGregorianCalendar(2015, 1, 1, 0, 0, 0, 0, DatatypeConstants.FIELD_UNDEFINED));
        periode.setTom(datatypeFactory.newXMLGregorianCalendar(2015, 4, 16, 0, 0, 0, 0, DatatypeConstants.FIELD_UNDEFINED));
        request.setArbeidsforholdIPeriode(periode);
        NorskIdent ident = new NorskIdent();
        ident.setIdent(fodselsnummer);
        request.setIdent(ident);
        List<Arbeidsforhold> result = new ArrayList<>();
        try {
            for (no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold : arbeidsforholdV2.finnArbeidsforholdPrArbeidstaker(request).getArbeidsforhold()) {
                result.add(transformer.transform(arbeidsforhold));
            }
        } catch (FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning | FinnArbeidsforholdPrArbeidstakerUgyldigInput e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return result;
    }
}
