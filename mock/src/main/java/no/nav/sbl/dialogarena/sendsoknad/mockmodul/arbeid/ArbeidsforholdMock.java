package no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.AnsettelsesPeriode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Virksomhet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArbeidsforholdMock {
    public ArbeidsforholdV3 arbeidMock() {
        ArbeidsforholdV3 amock = mock(ArbeidsforholdV3.class);

        try {
            when(amock.finnArbeidsforholdPrArbeidstaker(any(FinnArbeidsforholdPrArbeidstakerRequest.class))).then(new Answer<FinnArbeidsforholdPrArbeidstakerResponse>() {
                @Override
                public FinnArbeidsforholdPrArbeidstakerResponse answer(InvocationOnMock invocationOnMock) throws Throwable {
                    FinnArbeidsforholdPrArbeidstakerResponse response = new FinnArbeidsforholdPrArbeidstakerResponse();
                    Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
                    arbeidsforhold.setAnsettelsesPeriode(hentPeriode());
                    response.getArbeidsforhold().add(arbeidsforhold);
                    return response;
                }
            });
        } catch (Exception ignored) {
        }
        return amock;
    }

    private AnsettelsesPeriode hentPeriode() {
        AnsettelsesPeriode periode = new AnsettelsesPeriode();
        Gyldighetsperiode gperiode = new Gyldighetsperiode();
        gperiode.setFom(XMLGregorianCalendarImpl.createDate(2014, 1, 1, 0));
        gperiode.setTom(XMLGregorianCalendarImpl.createDate(2015, 1, 1, 0));
        periode.setPeriode(gperiode);
        return periode;
    }


    public OrganisasjonV4 organisasjonMock() {
        OrganisasjonV4 mock = mock(OrganisasjonV4.class);
        try {
            HentOrganisasjonResponse response = new HentOrganisasjonResponse();
            Organisasjon organisasjon = new Virksomhet();
            UstrukturertNavn value = new UstrukturertNavn();
            value.getNavnelinje().add("Mock navn arbeidsgiver");
            organisasjon.setNavn(value);
            response.setOrganisasjon(organisasjon);
            when(mock.hentOrganisasjon(any(HentOrganisasjonRequest.class))).thenReturn(response);
        } catch (HentOrganisasjonOrganisasjonIkkeFunnet | HentOrganisasjonUgyldigInput hentOrganisasjonOrganisasjonIkkeFunnet) {
            hentOrganisasjonOrganisasjonIkkeFunnet.printStackTrace();
        }
        return mock;
    }
}
