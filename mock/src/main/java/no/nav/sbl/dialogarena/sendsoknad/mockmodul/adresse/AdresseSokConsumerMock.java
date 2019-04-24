package no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdressesokRespons;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import static java.util.stream.Collectors.toList;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokMockHelper.adresseDataList;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokMockHelper.bergen;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokMockHelper.bergenLowercase;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokMockHelper.kristiansand;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokMockHelper.kristiansandLowercase;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokMockHelper.oslo;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokMockHelper.osloLowercase;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokMockHelper.svalbard;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokMockHelper.svalbardLowercase;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokMockHelper.tromso;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokMockHelper.tromsoLowercase;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokMockHelper.trondheim;
import static no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse.AdresseSokMockHelper.trondheimLowercase;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;


public class AdresseSokConsumerMock {
    private static final Logger logger = getLogger(AdresseSokConsumerMock.class);

    public AdresseSokConsumer adresseRestService() {
        AdresseSokConsumer mock = mock(AdresseSokConsumer.class);

        when(mock.sokAdresse(any(Sokedata.class))).thenAnswer(adressesokResponsMock());

        when(mock.sokAdresse(or(refEq(oslo), refEq(osloLowercase)))).thenAnswer(mockAnswer("Oslo"));
        when(mock.sokAdresse(or(refEq(bergen), refEq(bergenLowercase)))).thenAnswer(mockAnswer("Bergen"));
        when(mock.sokAdresse(or(refEq(trondheim), refEq(trondheimLowercase)))).thenAnswer(mockAnswer("Trondheim"));
        when(mock.sokAdresse(or(refEq(tromso), refEq(tromsoLowercase)))).thenAnswer(mockAnswer("Tromsø"));
        when(mock.sokAdresse(or(refEq(kristiansand), refEq(kristiansandLowercase)))).thenAnswer(mockAnswer("Kristiansand"));
        when(mock.sokAdresse(or(refEq(svalbard), refEq(svalbardLowercase)))).thenAnswer(mockAnswer("Svalbard"));

        when(mock.sokAdresse(anyString())).thenAnswer(adressesokResponsMock());

        return mock;
    }

    private Answer<?> adressesokResponsMock() {
        return invocation -> {
            Object param = invocation.getArgumentAt(0, Object.class);

            logger.info("Mocker respons til adressesok for søk: {}", param);

            AdressesokRespons respons = new AdressesokRespons();

            respons.adresseDataList = adresseDataList;

            return respons;
        };
    }

    private Answer<?> mockAnswer(String adresse) {
        return invocation -> {
            Object param = invocation.getArgumentAt(0, Object.class);

            logger.info("Mocker respons til adressesok for søk: {}", param);

            AdressesokRespons respons = new AdressesokRespons();

            respons.adresseDataList = adresseDataList.stream().filter(adresseData -> adresse.equals(adresseData.kommunenavn)).collect(toList());

            return respons;
        };
    }


}
