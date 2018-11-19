package no.nav.sbl.dialogarena.sendsoknad.mockmodul.arbeid;

import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.ServiceUtils;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.*;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArbeidsforholdMock {

    private static Map<String, FinnArbeidsforholdPrArbeidstakerResponse> responses = new HashMap<>();



    public ArbeidsforholdV3 arbeidMock() {
        ArbeidsforholdV3 mock = mock(ArbeidsforholdV3.class);

        try {
            when(mock.finnArbeidsforholdPrArbeidstaker(any(FinnArbeidsforholdPrArbeidstakerRequest.class)))
                    .thenAnswer((invocationOnMock -> getOrCreateCurrentUserResponse()));
        } catch (Exception ignored) {
        }
        return mock;
    }

    private static FinnArbeidsforholdPrArbeidstakerResponse getOrCreateCurrentUserResponse() {
        FinnArbeidsforholdPrArbeidstakerResponse response = responses.get(SubjectHandler.getSubjectHandler().getUid());
        if (response == null) {
            response = createNewResponse();
            responses.put(SubjectHandler.getSubjectHandler().getUid(), response);
        }
        return response;
    }

    private static FinnArbeidsforholdPrArbeidstakerResponse createNewResponse() {
        FinnArbeidsforholdPrArbeidstakerResponse response = new FinnArbeidsforholdPrArbeidstakerResponse();
        Arbeidsforhold arbeidsforhold = new Arbeidsforhold();
        Organisasjon organisasjon = new Organisasjon();
        organisasjon.setOrgnummer("123");
        arbeidsforhold.setArbeidsgiver(organisasjon);
        arbeidsforhold.setAnsettelsesPeriode(hentPeriode());
        arbeidsforhold.getArbeidsavtale().add(lagArbeidsavtale());
        response.getArbeidsforhold().add(arbeidsforhold);
        return response;
    }


    private static AnsettelsesPeriode hentPeriode() {
        AnsettelsesPeriode periode = new AnsettelsesPeriode();
        Gyldighetsperiode gperiode = new Gyldighetsperiode();
        gperiode.setFom(ServiceUtils.stringTilXmldato("2014-01-02"));
        // Denne avgjør om man er ansatt eller ikke for øyeblikket
        gperiode.setTom(ServiceUtils.stringTilXmldato(LocalDate.now().plusMonths(1).format(DateTimeFormatter.ISO_DATE)));
        periode.setPeriode(gperiode);
        return periode;
    }

    private static Arbeidsavtale lagArbeidsavtale() {
        Arbeidsavtale avtale = new Arbeidsavtale();
        avtale.setStillingsprosent(BigDecimal.valueOf(100));
        Avloenningstyper avloenningstyper = new Avloenningstyper();
        avloenningstyper.setKodeRef("fast");
        avtale.setAvloenningstype(avloenningstyper);
        return avtale;
    }

}
