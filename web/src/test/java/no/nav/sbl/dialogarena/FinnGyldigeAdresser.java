package no.nav.sbl.dialogarena;

import no.nav.sbl.dialogarena.config.SoknadinnsendingConfig;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdresseData;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.AdressesokRespons;
import no.nav.sbl.dialogarena.sendsoknad.domain.adresse.AdresseSokConsumer.Sokedata;
import no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;


import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static no.nav.sbl.dialogarena.soknadinnsending.business.db.config.DatabaseTestContext.buildDataSource;


/**
 * Verktøy for å finne TPS-adresser man kan bruke for en liste med GT.
 * 
 * Velg miljø gjennom å oppdatere "tps.adresse.url" i "environment-test.properties".
 * 
 * Se/endre main-metoden etter behov.
 */
public class FinnGyldigeAdresser {

    public static final int PORT = 8181;

    private AdresseSokConsumer adresseSokConsumer;
    
    
    private FinnGyldigeAdresser(AdresseSokConsumer adresseSokConsumer) {
        this.adresseSokConsumer = adresseSokConsumer;
    }
    
    
    private Map<String, List<AdresseData>> finnAdresser(List<String> gts, int adresserPerGt) {
        final Map<String, List<AdresseData>> adresser = new LinkedHashMap<>();
        
        for (String gt : gts) {
            final boolean harBydel = gt.length() > 4;
            final String kommunenummer = gt.substring(0, 4);
            final AdressesokRespons svar = sokKommunenummer(kommunenummer);
            
            for (AdresseData ad : svar.adresseDataList) {
                if (skalIgnoreres(ad)) {
                    continue;
                }

                if (!harBydel || gt.equals(ad.bydel)) {
                    final List<AdresseData> liste = getOrCreate(adresser, gt);
                    liste.add(ad);
                    if (liste.size() >= adresserPerGt) {
                        break;
                    }
                }
            }
        }
        
        return adresser;
    }

    private List<AdresseData> getOrCreate(final Map<String, List<AdresseData>> adresser, String gt) {
        List<AdresseData> liste = adresser.get(gt);
        if (liste == null) {
            liste = new ArrayList<AdresseData>();
            adresser.put(gt, liste);
        }
        return liste;
    }
    
    private boolean skalIgnoreres(AdresseData ad) {
        try {
            return ad.adressenavn == null
                    || ad.adressenavn.length() < 4
                    || ad.postnummer == null
                    || ad.poststed == null
                    || adresseSokConsumer.sokAdresse(toGateadresseSokestreng(ad)).adresseDataList.size() != 1;
        } catch (RuntimeException e) {
            return true;
        }
    }

    private String toGateadresseSokestreng(AdresseData ad) {
        return ad.adressenavn + ", " + ad.postnummer + " " + ad.poststed;
    }
    
    public AdressesokRespons sokKommunenummer(String kommunenummer) {
        return adresseSokConsumer.sokAdresse(new Sokedata().withKommunenummer(kommunenummer));
    }

    
    private static ApplicationContext initializeContext() throws IOException, NamingException {
        SoknadsosialhjelpServer.setFrom("environment-test.properties");
        final DataSource dataSource = buildDataSource("hsqldb.properties");

        final SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
        builder.bind("jdbc/SoknadInnsendingDS", dataSource);
        builder.activate();

        return new AnnotationConfigApplicationContext(SoknadinnsendingConfig.class);
    }
    
    private static void skrivUt(Map<String, List<AdresseData>> adresser) {
        for (Entry<String, List<AdresseData>> entry : adresser.entrySet()) {
            System.out.println(entry.getKey());
            for (AdresseData ad : entry.getValue()) {
                System.out.println("    " + ad.toString());
            }
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        final ApplicationContext context = initializeContext();
        final AdresseSokConsumer adresseSokConsumer = context.getBean(AdresseSokConsumer.class);
        
        final FinnGyldigeAdresser app = new FinnGyldigeAdresser(adresseSokConsumer);
        
        final int adresserPerGt = 3;
        final List<String> gts = Arrays.asList(
                "0219",
                "0701",
                "120101",
                "120102",
                "120103",
                "120104",
                "120105",
                "120106",
                "120107",
                "120108",
                "1247",
                "030102",
                "030103",
                "030105",
                "030110",
                "030111",
                "030114",
                "030115"
        );
        final Map<String, List<AdresseData>> adresser = app.finnAdresser(gts, adresserPerGt);
        skrivUt(adresser);
    }
}
