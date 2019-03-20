package no.nav.sbl.dialogarena.sendsoknad.mockmodul.inntektsogskatteopplysninger;

import no.nav.sbl.dialogarena.sendsoknad.domain.inntektsogskatteopplysninger.InntektOgskatteopplysningerConsumer;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InntektOgSkatteopplysningerMock {
    public InntektOgskatteopplysningerConsumer inntektOgSkatteopplysningerRestService() {
        InntektOgskatteopplysningerConsumer mock = mock(InntektOgskatteopplysningerConsumer.class);
        when(mock.sok(any(InntektOgskatteopplysningerConsumer.Sokedata.class))).thenAnswer(adressesokResponsMock());

        return mock;
    }

    private Answer<?> adressesokResponsMock() {
        return invocation -> {
            Object param = invocation.getArgumentAt(0, Object.class);
            InntektOgskatteopplysningerConsumer.InntektOgskatteopplysningerRespons respons = new InntektOgskatteopplysningerConsumer.InntektOgskatteopplysningerRespons();
            return respons;
        };
    }
}
