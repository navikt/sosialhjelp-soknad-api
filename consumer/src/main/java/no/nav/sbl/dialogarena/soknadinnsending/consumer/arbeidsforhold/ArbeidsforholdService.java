package no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold;

import com.google.common.collect.Lists;
import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto.ArbeidsforholdDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto.OrganisasjonDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto.PeriodeDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.dto.PersonDto;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.organisasjon.OrganisasjonService;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.NorskIdent;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Periode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Regelverker;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.datatype.DatatypeFactory;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils.lagDatatypeFactory;

@Component
public class ArbeidsforholdService {

    private static final Logger log = LoggerFactory.getLogger(ArbeidsforholdService.class);
    private static final String AAREG_API_ENABLED = "aareg_api_enabled";

    @Inject
    private ArbeidsforholdConsumer arbeidsforholdConsumer;

    @Inject
    private OrganisasjonService organisasjonService;

    // Webservice start
    @Inject
    @Named("arbeidEndpoint")
    private ArbeidsforholdV3 arbeidsforholdWebWervice;

    @Inject
    private ArbeidsforholdTransformer arbeidsforholdTransformer;

    private static final Regelverker AA_ORDNINGEN = new Regelverker();

    private DatatypeFactory datatypeFactory = lagDatatypeFactory();

    static {
        AA_ORDNINGEN.setValue("A_ORDNINGEN");
    }
    // Webservice slutt

    private boolean brukAaregRestApi() {
        return Boolean.parseBoolean(System.getProperty(AAREG_API_ENABLED, "false"));
    }

    public List<Arbeidsforhold> hentArbeidsforhold(String fodselsnummer, no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.ArbeidsforholdService.Sokeperiode soekeperiode) {
        return brukAaregRestApi() ? hentArbeidsforholdRest(fodselsnummer, soekeperiode) : hentArbeidsforholdWS(fodselsnummer, soekeperiode);
    }

    private List<Arbeidsforhold> hentArbeidsforholdRest(String fnr, no.nav.sbl.dialogarena.soknadinnsending.consumer.arbeidsforhold.ArbeidsforholdService.Sokeperiode soekeperiode) {
        try {
            List<ArbeidsforholdDto> arbeidsforholdDtos = arbeidsforholdConsumer.finnArbeidsforholdForArbeidstaker(fnr);
            log.info("Hentet {} arbeidsforhold fra aareg", arbeidsforholdDtos.size());

            return arbeidsforholdDtos.stream()
                    .map(this::mapToDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Noe feilet mot aareg.api -> bruker arbeidsforhold_v3 webservice som fallback");
            return hentArbeidsforholdWS(fnr, soekeperiode);
        }
    }

    public Arbeidsforhold mapToDomain(ArbeidsforholdDto dto) {
        Arbeidsforhold result = new Arbeidsforhold();
        result.edagId = dto.getNavArbeidsforholdId();
        result.orgnr = null;
        result.arbeidsgivernavn = "";

        if (dto.getArbeidsgiver() instanceof OrganisasjonDto) {
            result.orgnr = ((OrganisasjonDto) dto.getArbeidsgiver()).getOrganisasjonsnummer();
            result.arbeidsgivernavn = organisasjonService.hentOrgNavn(result.orgnr);
        } else if (dto.getArbeidsgiver() instanceof PersonDto) {
            result.arbeidsgivernavn = "Privatperson";
        }

        PeriodeDto periode = dto.getAnsettelsesperiode().getPeriode();
        result.fom = periode.getFom().format(DateTimeFormatter.ISO_LOCAL_DATE);
        result.tom = periode.getTom() != null ? periode.getTom().format(DateTimeFormatter.ISO_LOCAL_DATE) : null;

        dto.getArbeidsavtaler()
                .forEach(arbeidsavtale -> {
                    result.harFastStilling = true;
                    result.fastStillingsprosent += (long) arbeidsavtale.getStillingsprosent(); // Hvorfor er "fastStillingsprosent" long?
                });
        return result;
    }

    // webservice start
    private List<Arbeidsforhold> hentArbeidsforholdWS(String fodselsnummer, Sokeperiode soekeperiode) {
        try {
            FinnArbeidsforholdPrArbeidstakerRequest finnArbeidsforholdPrArbeidstakerRequest = lagArbeidsforholdRequest(fodselsnummer, lagPeriode(soekeperiode.fom, soekeperiode.tom));

            List<no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold> arbeidsforhold
                    = arbeidsforholdWebWervice.finnArbeidsforholdPrArbeidstaker(finnArbeidsforholdPrArbeidstakerRequest).getArbeidsforhold();
            return Lists.transform(arbeidsforhold, arbeidsforholdTransformer);
        } catch (FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning | FinnArbeidsforholdPrArbeidstakerUgyldigInput e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private FinnArbeidsforholdPrArbeidstakerRequest lagArbeidsforholdRequest(String fodselsnummer, Periode periode) {
        FinnArbeidsforholdPrArbeidstakerRequest request = new FinnArbeidsforholdPrArbeidstakerRequest();
        request.setArbeidsforholdIPeriode(periode);
        request.setRapportertSomRegelverk(AA_ORDNINGEN);
        request.setIdent(lagIdent(fodselsnummer));
        return request;
    }

    private NorskIdent lagIdent(String fodselsnummer) {
        NorskIdent ident = new NorskIdent();
        ident.setIdent(fodselsnummer);
        return ident;
    }


    private Periode lagPeriode(DateTime fom, DateTime tom) {
        Periode periode = new Periode();
        periode.setFom(datatypeFactory.newXMLGregorianCalendar(fom.toGregorianCalendar()));
        periode.setTom(datatypeFactory.newXMLGregorianCalendar(tom.toGregorianCalendar()));
        return periode;
    }

    public static final class Sokeperiode {

        private final DateTime fom;
        private final DateTime tom;

        public Sokeperiode(DateTime fom, DateTime tom) {
            this.fom = fom;
            this.tom = tom;
        }

        public DateTime getFom() {
            return fom;
        }

        public DateTime getTom() {
            return tom;
        }

    }
    // webservice slutt
}
