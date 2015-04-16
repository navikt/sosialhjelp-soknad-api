package no.nav.sbl.dialogarena.soknadinnsending.business.arbeid;

import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Utenlandsopphold;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import org.apache.commons.collections15.Transformer;
import org.joda.time.Interval;

public class ArbeidsforholdTransformer implements Transformer<no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold, Arbeidsforhold> {
    private final OrganisasjonV4 organisasjonV4;

    public ArbeidsforholdTransformer(OrganisasjonV4 organisasjonV4) {
        this.organisasjonV4 = organisasjonV4;
    }

    @Override
    public Arbeidsforhold transform(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        Arbeidsforhold result = new Arbeidsforhold();
        result.orgnr = ((Organisasjon) arbeidsforhold.getArbeidsgiver()).getOrgnummer();
        result.arbridsgiverNavn = hentOrgNavn(result.orgnr);
        Gyldighetsperiode periode = arbeidsforhold.getAnsettelsesPeriode().getPeriode();
        result.fom = periode.getFom().toGregorianCalendar().getTimeInMillis();
        result.tom = periode.getTom() != null ? periode.getTom().toGregorianCalendar().getTimeInMillis() : null;
        if(arbeidsforhold.getUtenlandsopphold() != null){
            for (Utenlandsopphold utenlandsopphold : arbeidsforhold.getUtenlandsopphold()) {
                Gyldighetsperiode uperiode = utenlandsopphold.getPeriode();
                Arbeidsforhold.Utenlandsopphold opphold = new Arbeidsforhold.Utenlandsopphold();
                opphold.fom = uperiode.getFom().toGregorianCalendar().getTimeInMillis();
                opphold.tom = uperiode.getTom() != null ? uperiode.getTom().toGregorianCalendar().getTimeInMillis() : null;
                opphold.land = utenlandsopphold.getLand().getKodeRef();
                result.utenlandsopphold.add(opphold);
            }
        }

        return result;

    }

    private String hentOrgNavn(String orgnr){
        HentOrganisasjonRequest hentOrganisasjonRequest = lagOrgRequest(orgnr);
        try {
            return ((UstrukturertNavn) organisasjonV4.hentOrganisasjon(hentOrganisasjonRequest).getOrganisasjon().getNavn()).getNavnelinje().get(0);
        } catch (HentOrganisasjonOrganisasjonIkkeFunnet | HentOrganisasjonUgyldigInput ex) {
            throw new RuntimeException(ex);
        }
    }

    private HentOrganisasjonRequest lagOrgRequest(String orgnr) {
        HentOrganisasjonRequest hentOrganisasjonRequest = new HentOrganisasjonRequest();
        hentOrganisasjonRequest.setOrgnummer(orgnr);
        hentOrganisasjonRequest.setInkluderHierarki(false);
        hentOrganisasjonRequest.setInkluderHistorikk(false);
        return hentOrganisasjonRequest;
    }
}
