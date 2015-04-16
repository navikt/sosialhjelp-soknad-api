package no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.AnsettelsesPeriode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
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
        return null;
    }
}
