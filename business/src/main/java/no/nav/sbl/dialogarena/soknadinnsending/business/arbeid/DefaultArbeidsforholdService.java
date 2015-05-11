package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import com.google.common.collect.Lists;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.NorskIdent;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Periode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Regelverker;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.List;
import java.util.Objects;

@Service
public class DefaultArbeidsforholdService implements ArbeidsforholdService, BolkService {
    @Inject
    @Named("arbeidEndpoint")
    private ArbeidsforholdV3 arbeidsforholdWebWervice;
    @Inject
    @Named("organisasjonEndpoint")
    private OrganisasjonV4 organisasjonWebService;
    @Inject
    private FaktaService faktaService;
    private ArbeidsforholdTransformer transformer;
    private DatatypeFactory datatypeFactory = lagDatatypeFactory();
    private static final Regelverker AA_ORDNINGEN = new Regelverker();
    static {
        AA_ORDNINGEN.setValue("A_ORDNINGEN");
    }

    private DatatypeFactory lagDatatypeFactory(){
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    @PostConstruct
    public void createTransformer() throws DatatypeConfigurationException {
        transformer = new ArbeidsforholdTransformer(organisasjonWebService);
    }

    @Override
    public List<Arbeidsforhold> hentArbeidsforhold(String fodselsnummer) {
        try {
            return Lists.transform(arbeidsforholdWebWervice.finnArbeidsforholdPrArbeidstaker(lagArbeidsforholdRequest(fodselsnummer)).getArbeidsforhold(), transformer);
        } catch (FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning | FinnArbeidsforholdPrArbeidstakerUgyldigInput e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void lagreArbeidsforhold(String fodselsnummer, Long soknadId) {
        List<Arbeidsforhold> forhold = hentArbeidsforhold(fodselsnummer);
        for (Arbeidsforhold arbeidsforhold : forhold) {
            Faktum arbridsforholdFaktum = new Faktum()
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
                    .medSystemProperty("edagref", "" + arbeidsforhold.edagId);
            arbridsforholdFaktum.setFaktumId(faktaService.lagreSystemFaktum(soknadId, arbridsforholdFaktum, "edagref"));
        }
        if(!forhold.isEmpty()){
            Faktum yrkesaktiv = faktaService.hentFaktumMedKey(soknadId, "arbeidsforhold.yrkesaktiv");
            if(yrkesaktiv == null){
                yrkesaktiv = new Faktum()
                        .medSoknadId(soknadId)
                        .medKey("arbeidsforhold.yrkesaktiv");
                faktaService.lagreSystemFaktum(soknadId, yrkesaktiv.medValue("false"), "");
            } else if("true".equals(yrkesaktiv.getValue())){
                faktaService.lagreSystemFaktum(soknadId, yrkesaktiv.medValue("false"), "");
            }
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

    private String trueFalse(boolean test) {
        return test ? "true" : "false";
    }

    private String finnStillingsType(Arbeidsforhold arbeidsforhold) {
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
    public void lagreBolk(String fodselsnummer, Long soknadId) {
        lagreArbeidsforhold(fodselsnummer, soknadId);
    }
}
