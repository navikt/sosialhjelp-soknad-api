package no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Arrays;

import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdresseData;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdressesokRespons;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;

public class AdresseSokConsumerMock {
    private static final Logger logger = getLogger(AdresseSokConsumerMock.class);

    public AdresseSokConsumer adresseRestService() {
        AdresseSokConsumer mock = mock(AdresseSokConsumer.class);

        when(mock.sokAdresse(any(Sokedata.class))).thenAnswer(adressesokResponsMock());
        when(mock.sokAdresse(anyString())).thenAnswer(adressesokResponsMock());

        return mock;
    }

    private Answer<?> adressesokResponsMock() {
        return invocation -> {
            Object param = invocation.getArgumentAt(0, Object.class);

            logger.info("Mocker respons til adressesok for søk: {}", param);

            AdressesokRespons respons = new AdressesokRespons();
            final AdresseData a1 = new AdresseData();
            a1.kommunenummer = "1201";
            a1.kommunenavn = "Bergen";
            a1.adressenavn = "SANNERGATA";
            a1.husnummerFra = "0001";
            a1.husnummerTil = "0010";
            a1.postnummer = "1337";
            a1.poststed = "Leet";
            a1.geografiskTilknytning = "120102";
            a1.gatekode = "02081";
            a1.bydel = "120102";
            
            final AdresseData a2 = new AdresseData();
            a2.kommunenummer = "1201";
            a2.kommunenavn = "Bergen";
            a2.adressenavn = "SANNERGATA";
            a2.husnummerFra = "0011";
            a2.husnummerTil = "9999";
            a2.postnummer = "1337";
            a2.poststed = "Leet";
            a2.geografiskTilknytning = "120107";
            a2.gatekode = "02081";
            a2.bydel = "120107";

            respons.adresseDataList = Arrays.asList(a1, a2);
            
            return respons;
        };
    }
}
