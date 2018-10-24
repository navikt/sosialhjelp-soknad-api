package no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse;

import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdresseData;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdressesokRespons;
import org.slf4j.Logger;

import java.util.ArrayList;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

public class AdresseSokConsumerMock {
    private static final Logger logger = getLogger(AdresseSokConsumerMock.class);

    public AdresseSokConsumer adresseRestService() {
        AdresseSokConsumer mock = mock(AdresseSokConsumer.class);

        when(mock.sokAdresse(anyString())).thenAnswer(invocation -> {
            String param = invocation.getArgumentAt(0, String.class);

            logger.info("Mocker respons til adressesok for søk: {}", param);

            AdressesokRespons respons = new AdressesokRespons();
            AdresseData a = new AdresseData();
            a.kommunenummer = "1841";
            a.kommunenavn = "Fauske";
            a.adressenavn = "SANNERGATA";
            a.husnummerFra = "0001";
            a.husnummerTil = "9999";
            a.postnummer = null;
            a.poststed = null;
            a.geografiskTilknytning = "0216";
            a.gatekode = "02081";
            a.bydel = null;

            respons.adresseDataList = new ArrayList<>();
            respons.adresseDataList.add(a);
            return respons;
        });

        return mock;
    }
}
