package no.nav.sbl.dialogarena.sendsoknad.mockmodul.adresse;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdresseData;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdressesokRespons;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.SubjectHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AdresseSokConsumerMock {

    private static Map<String, AdressesokRespons> responses = new HashMap<>();

    public AdresseSokConsumer adresseRestService() {
        AdresseSokConsumer mock = mock(AdresseSokConsumer.class);

        when(mock.sokAdresse(any(Sokedata.class))).thenAnswer(
                invocation -> {
                    Sokedata sokedata = (Sokedata) invocation.getArguments()[0];

                    if (sokedata.adresse == null) {
                        return new AdressesokRespons();
                    }

                    if ("sæ".equalsIgnoreCase(sokedata.adresse)) {
                        return getAdresseWithTwoLettersRespons();
                    }

                    if ("test".equalsIgnoreCase(sokedata.adresse)) {
                        return getTestRespons();
                    }
                    if ("teste".equalsIgnoreCase(sokedata.adresse)) {
                        return getTest2Respons();
                    }

                    if (sokedata.adresse.contains("Dobbel")) {
                        String adresse = sokedata.adresse.replace("gata", "");
                        return getDobbelRespons(adresse, sokedata.postnummer);
                    }

                    if (sokedata.adresse.contains("gata")) {
                        String adresse = sokedata.adresse.replace("gata", "");
                        return getGataRespons(adresse, sokedata.postnummer);
                    }

                    return getOrCreateCurrentUserResponse();

                });

        return mock;
    }

    private static AdressesokRespons getOrCreateCurrentUserResponse() {
        AdressesokRespons response = responses.get(SubjectHandler.getUserId());
        if (response == null){
            response = getDefaultRespons();
            responses.put(SubjectHandler.getUserId(), response);
        }

        return response;
    }

    private static AdressesokRespons getDefaultRespons(){
        AdressesokRespons response = new AdressesokRespons();

        final AdresseData a1 = new AdresseData();
        a1.kommunenummer = "0301";
        a1.kommunenavn = "Oslo";
        a1.adressenavn = "SANNERGATA";
        a1.husnummerFra = "0001";
        a1.husnummerTil = "0010";
        a1.postnummer = "1337";
        a1.poststed = "Leet";
        a1.geografiskTilknytning = "030102";
        a1.gatekode = "02081";
        a1.bydel = "030102";

        response.adresseDataList = Collections.singletonList(a1);

        return response;
    }

    private static AdressesokRespons getGataRespons(String kommunenavn, String kommunenummer){
        AdressesokRespons response = new AdressesokRespons();
        response.adresseDataList.add(createAdresse(kommunenavn, kommunenummer));
        return response;
    }

    private static AdressesokRespons getDobbelRespons(String kommunenavn, String postnummer){
        AdressesokRespons response = new AdressesokRespons();
        response.adresseDataList.add(createAdresse(kommunenavn, postnummer, postnummer));
        response.adresseDataList.add(createAdresse(kommunenavn, Long.toString(Long.parseLong(postnummer) +1), postnummer));
        return response;
    }

    private static AdressesokRespons getAdresseWithTwoLettersRespons(){
        AdressesokRespons response = new AdressesokRespons();
        response.adresseDataList.add(createAdresseWithTwoLetters());
        return response;
    }

    private static AdressesokRespons getTestRespons(){
        AdressesokRespons response = new AdressesokRespons();
        response.adresseDataList.add(createAdresse("Fredrikstad", "0106"));
        response.adresseDataList.add(createAdresse("Horten", "0701"));
        response.adresseDataList.add(createAdresse("Halden", "0101"));
        response.adresseDataList.add(createAdresse("Askøy", "1247"));
        response.adresseDataList.add(createAdresse("Sarpsborg", "0105"));
        response.adresseDataList.add(createAdresse("Bærum", "0219"));
        response.adresseDataList.add(createAdresse("Hvaler", "0111"));
        response.adresseDataList.add(createAdresse("Moss", "5001"));
        response.adresseDataList.add(createAdresse("Rygge", "0136"));
        response.adresseDataList.add(createAdresse("Hamar", "0403"));
        return response;
    }

    private static AdressesokRespons getTest2Respons(){
        AdressesokRespons response = new AdressesokRespons();
        response.adresseDataList.add(createAdresse("Moss", "5001"));
        response.adresseDataList.add(createAdresse("Rygge", "0136"));
        response.adresseDataList.add(createAdresse("Hamar", "0403"));
        response.adresseDataList.add(createAdresse("DobbelKommune", "2222"));
        return response;
    }

    private static AdresseData createAdresse(String kommunenavn, String kommunenummer){
        return createAdresse(kommunenavn, kommunenummer, kommunenummer);
    }


    private static AdresseData createAdresse(String kommunenavn, String kommunenummer, String postnummer){
        final AdresseData a1 = new AdresseData();
        a1.kommunenummer = kommunenummer;
        a1.kommunenavn = kommunenavn;
        a1.adressenavn = kommunenavn + "gata";
        a1.husnummerFra = "0001";
        a1.husnummerTil = "0010";
        a1.postnummer = postnummer;
        a1.poststed = kommunenavn;
        a1.geografiskTilknytning = kommunenummer;
        a1.gatekode = kommunenummer;
        a1.bydel = kommunenummer;

        return a1;
    }

    private static AdresseData createAdresseWithTwoLetters(){
        final AdresseData a1 = new AdresseData();
        a1.kommunenummer = "4614";
        a1.kommunenavn = "Stord";
        a1.adressenavn = "Sæ";
        a1.husnummerFra = "0002";
        a1.husnummerTil = "0010";
        a1.postnummer = "5417";
        a1.poststed = "Stord";
        a1.geografiskTilknytning = "4614";
        a1.gatekode = "1111";

        return a1;
    }


    public static void setAdresser(String jsonAdressesokRespons){

        try {
            ObjectMapper mapper = new ObjectMapper();
            AdressesokRespons response = mapper.readValue(jsonAdressesokRespons, AdressesokRespons.class);
            if (responses.get(SubjectHandler.getUserId()) == null){
                responses.put(SubjectHandler.getUserId(), response);
            } else {
                responses.replace(SubjectHandler.getUserId(), response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resetAdresser(){
        AdressesokRespons defaultRespons = new AdressesokRespons();
        responses.replace(SubjectHandler.getUserId(), defaultRespons);
    }
}
