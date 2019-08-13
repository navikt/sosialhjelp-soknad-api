package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.*;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class ArbeidsforholdTransformer implements Transformer<no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold, Arbeidsforhold>,
        Function<no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold, Arbeidsforhold> {

    @Inject
    @Named("organisasjonEndpoint")
    private OrganisasjonV4 organisasjonWebService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ArbeidsforholdTransformer.class);

    @Override
    public Arbeidsforhold transform(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        Arbeidsforhold result = new Arbeidsforhold();
        result.edagId = arbeidsforhold.getArbeidsforholdIDnav();
        result.orgnr = null;
        result.arbeidsgivernavn = "";

        if(arbeidsforhold.getArbeidsgiver() instanceof Organisasjon) {
            result.orgnr = ((Organisasjon) arbeidsforhold.getArbeidsgiver()).getOrgnummer();
            result.arbeidsgivernavn = hentOrgNavn(result.orgnr);
        }
        else if (arbeidsforhold.getArbeidsgiver() instanceof HistoriskArbeidsgiverMedArbeidsgivernummer) {
            result.arbeidsgivernavn = ((HistoriskArbeidsgiverMedArbeidsgivernummer) arbeidsforhold.getArbeidsgiver()).getNavn();
        }
        else if(arbeidsforhold.getArbeidsgiver() instanceof Person) {
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


    public String hentOrgNavn(String orgnr) {
        if (orgnr != null) {
            HentOrganisasjonRequest hentOrganisasjonRequest = lagOrgRequest(orgnr);
            try {
                //Kan bare være ustrukturert navn.
                no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon organisasjon = organisasjonWebService.hentOrganisasjon(hentOrganisasjonRequest).getOrganisasjon();
                if (organisasjon == null) {
                    LOGGER.warn("Kunne ikke hente orgnr: " + orgnr);
                    return orgnr;
                }
                List<String> orgNavn = ((UstrukturertNavn) organisasjon.getNavn()).getNavnelinje();
                orgNavn.removeAll(Arrays.asList("", null));
                return Joiner.on(", ").join(orgNavn);
            } catch (Exception ex) {
                LOGGER.warn("Kunne ikke hente orgnr: " + orgnr, ex);
                return orgnr;
            }
        } else {
            return "";
        }
    }

    private HentOrganisasjonRequest lagOrgRequest(String orgnr) {
        HentOrganisasjonRequest hentOrganisasjonRequest = new HentOrganisasjonRequest();
        hentOrganisasjonRequest.setOrgnummer(orgnr);
        hentOrganisasjonRequest.setInkluderHierarki(false);
        hentOrganisasjonRequest.setInkluderHistorikk(false);
        return hentOrganisasjonRequest;
    }

    @Override
    public Arbeidsforhold apply(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        return transform(arbeidsforhold);
    }
}
