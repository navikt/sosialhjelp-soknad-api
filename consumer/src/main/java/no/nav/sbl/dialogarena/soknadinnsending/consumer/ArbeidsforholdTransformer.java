package no.nav.sbl.dialogarena.soknadinnsending.consumer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import no.nav.sbl.dialogarena.sendsoknad.domain.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsavtale;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.HistoriskArbeidsgiverMedArbeidsgivernummer;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;

@Service
public class ArbeidsforholdTransformer implements Transformer<no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold, Arbeidsforhold>,
        Function<no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold, Arbeidsforhold> {

    @Inject
    @Named("organisasjonEndpoint")
    private OrganisasjonV4 organisasjonWebService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ArbeidsforholdTransformer.class);
    public static final String KODEVERK_AVLONNING_FAST = "fast";


    @Override
    public Arbeidsforhold transform(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        Arbeidsforhold result = new Arbeidsforhold();
        result.edagId = arbeidsforhold.getArbeidsforholdIDnav();
        result.orgnr = hentOrganisasjonsnummer(arbeidsforhold);
        result.arbeidsgivernavn =  erOrganisasjon(arbeidsforhold) ||
                erHistoriskArbeidsgiver(arbeidsforhold) ? hentOrgNavn(arbeidsforhold, result.orgnr) : "Privatperson";

        Gyldighetsperiode periode = arbeidsforhold.getAnsettelsesPeriode().getPeriode();
        result.fom = toStringDate(periode.getFom());
        result.tom = toStringDate(periode.getTom());

        if (arbeidsforhold.getArbeidsavtale() != null) {
            for (Arbeidsavtale arbeidsavtale : arbeidsforhold.getArbeidsavtale()) {
                if (erFastStilling(arbeidsavtale)) {
                    result.harFastStilling = true;
                    result.fastStillingsprosent += nullSafe(arbeidsavtale.getStillingsprosent());
                } else {
                    result.variabelStillingsprosent = true;
                }
            }
        }
        return result;

    }

    private boolean erOrganisasjon(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        return arbeidsforhold.getArbeidsgiver() instanceof Organisasjon;
    }

    private boolean erHistoriskArbeidsgiver(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        return arbeidsforhold.getArbeidsgiver() instanceof HistoriskArbeidsgiverMedArbeidsgivernummer;
    }

    private String hentOrganisasjonsnummer(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        return erOrganisasjon(arbeidsforhold) ? ((Organisasjon) arbeidsforhold.getArbeidsgiver()).getOrgnummer() : null;
    }

    private String toStringDate(XMLGregorianCalendar fom) {
        return fom != null ? new DateTime(fom.toGregorianCalendar()).toString("yyyy-MM-dd") : null;
    }

    private Long nullSafe(BigDecimal number) {
        return number != null ? number.longValue() : 0;
    }

    private boolean erFastStilling(Arbeidsavtale arbeidsavtale) {
        return true;
    }


    private String hentOrgNavn(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold, String orgnr) {
        if (orgnr != null) {
            HentOrganisasjonRequest hentOrganisasjonRequest = lagOrgRequest(orgnr);
            try {
                //Kan bare være ustrukturert navn.
                return Joiner.on(", ").join(((UstrukturertNavn) organisasjonWebService.hentOrganisasjon(hentOrganisasjonRequest).getOrganisasjon().getNavn()).getNavnelinje());
            } catch (Exception ex) {
                LOGGER.warn("Kunne ikke hente orgnr: " + orgnr, ex);
                return "";
            }
        } else if (erHistoriskArbeidsgiver(arbeidsforhold)) {
            return ((HistoriskArbeidsgiverMedArbeidsgivernummer) arbeidsforhold.getArbeidsgiver()).getNavn();
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
