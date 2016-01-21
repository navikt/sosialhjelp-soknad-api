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

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;

public class ArbeidsforholdTransformer implements Transformer<no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold, Arbeidsforhold>,
        Function<no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold, Arbeidsforhold> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArbeidsforholdTransformer.class);
    public static final String KODEVERK_AVLONNING_FAST = "fast";
    private final OrganisasjonV4 organisasjonV4;

    public ArbeidsforholdTransformer(OrganisasjonV4 organisasjonV4) {
        this.organisasjonV4 = organisasjonV4;
    }

    @Override
    public Arbeidsforhold transform(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        Arbeidsforhold result = new Arbeidsforhold();
        result.edagId = arbeidsforhold.getArbeidsforholdIDnav();
        result.orgnr = getNavn(arbeidsforhold);
        result.arbridsgiverNavn = hentOrgNavn(arbeidsforhold, result.orgnr);

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

    private String getNavn(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        if (arbeidsforhold.getArbeidsgiver() instanceof Organisasjon) {
            return ((Organisasjon) arbeidsforhold.getArbeidsgiver()).getOrgnummer();
        } else {
            return null;
        }
    }

    private String toStringDate(XMLGregorianCalendar fom) {
        return fom != null ? new DateTime(fom.toGregorianCalendar()).toString("yyyy-MM-dd") : null;
    }

    private Long nullSafe(BigDecimal number) {
        return number != null ? number.longValue() : 0;
    }

    private boolean erFastStilling(Arbeidsavtale arbeidsavtale) {
        return arbeidsavtale.getAvloenningstype().getKodeRef().equals(KODEVERK_AVLONNING_FAST);
    }


    private String hentOrgNavn(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold, String orgnr) {
        if (orgnr != null) {
            HentOrganisasjonRequest hentOrganisasjonRequest = lagOrgRequest(orgnr);
            try {
                //Kan bare være ustrukturert navn.
                return Joiner.on(", ").join(((UstrukturertNavn) organisasjonV4.hentOrganisasjon(hentOrganisasjonRequest).getOrganisasjon().getNavn()).getNavnelinje());
            } catch (Exception ex) {
                LOGGER.warn("Kunne ikke hente orgnr: " + orgnr, ex);
                return "";
            }
        } else if (arbeidsforhold.getArbeidsgiver() instanceof HistoriskArbeidsgiverMedArbeidsgivernummer) {
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
