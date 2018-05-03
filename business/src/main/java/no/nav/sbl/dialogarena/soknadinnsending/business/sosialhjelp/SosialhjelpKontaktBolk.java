package no.nav.sbl.dialogarena.soknadinnsending.business.sosialhjelp;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse.Gateadresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse.MatrikkelAdresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Adresse.StrukturertAdresse;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;

@Service
public class SosialhjelpKontaktBolk implements BolkService {

    private static final String BOLKNAVN = "SosialhjelpKontakt";

    @Inject
    private PersonaliaFletter personaliaFletter;

    public Personalia hentPersonalia(String fodselsnummer) {
        return personaliaFletter.mapTilPersonalia(fodselsnummer);
    }

    @Override
    public String tilbyrBolk() {
        return BOLKNAVN;
    }

    @Override
    public List<Faktum> genererSystemFakta(String fodselsnummer, Long soknadId) {
        return genererPersonaliaFaktum(soknadId, personaliaFletter.mapTilPersonalia(fodselsnummer));
    }
    
    private List<Faktum> genererPersonaliaFaktum(Long soknadId, Personalia personalia) {
        return Arrays.asList(
                new Faktum().medSoknadId(soknadId).medKey("kontakt.system.kontonummer").medValue(norskKontonummer(personalia)),
                new Faktum().medSoknadId(soknadId).medKey("kontakt.system.telefon").medValue(norskTelefonnummer(personalia.getMobiltelefonnummer())),
                new Faktum().medSoknadId(soknadId).medKey("kontakt.system.personalia.statsborgerskap").medValue(personalia.getStatsborgerskap()),
                genererAdresseFaktum(soknadId, personalia),
                genererFolkeregistrertAdresseFaktum(soknadId, personalia)
        );
    }
 
    static String norskTelefonnummer(String mobiltelefonnummer) {
        if (mobiltelefonnummer == null) {
            return null;
        }
        if (mobiltelefonnummer.length() == 8) {
            return "+47" + mobiltelefonnummer;
        }
        if (mobiltelefonnummer.startsWith("+47") && mobiltelefonnummer.length() == 11) {
            return mobiltelefonnummer;
        }
        return null;
    }

    private String norskKontonummer(Personalia personalia) {
        if (personalia.getErUtenlandskBankkonto() != null && personalia.getErUtenlandskBankkonto()) {
            return "";
        } else {
            return personalia.getKontonummer();
        }
    }

    private Faktum genererFolkeregistrertAdresseFaktum(Long soknadId, Personalia personalia) {
        final Faktum adresseFaktum = new Faktum().medSoknadId(soknadId).medKey("kontakt.system.folkeregistrert.adresse");
        final Adresse folkeregistrertAdresse = personalia.getFolkeregistrertAdresse();
        
        if (folkeregistrertAdresse == null || isUtenlandskAdresse(folkeregistrertAdresse)) {
            return adresseFaktum;
        }
        
        final StrukturertAdresse strukturertAdresse = folkeregistrertAdresse.getStrukturertAdresse();
        
        if (strukturertAdresse == null) {
            // Skal aldri kunne skje med folkeregistrert adresse ref. PersonV1-definisjon.
            return adresseFaktum;
        } else {
            populerStrukturertAdresse(adresseFaktum, strukturertAdresse);
        }
        
        return adresseFaktum;
        
    }
    
    private Faktum genererAdresseFaktum(Long soknadId, Personalia personalia) {
        final Faktum adresseFaktum = new Faktum().medSoknadId(soknadId).medKey("kontakt.system.adresse");
        final Adresse gjeldendeAdresse = personalia.getGjeldendeAdresse();
        
        if (gjeldendeAdresse == null || isUtenlandskAdresse(gjeldendeAdresse)) {
            /*
             * Landkode hardkodes til NOR flere steder i denne filen. Hvis man
             * skal tillate utenlandske adresser må disse også oppdateres.
             */
            return adresseFaktum;
        }
        
        final StrukturertAdresse strukturertAdresse = gjeldendeAdresse.getStrukturertAdresse();
        
        if (strukturertAdresse == null) {
            populerUstrukturertAdresse(adresseFaktum, gjeldendeAdresse);
        } else {
            populerStrukturertAdresse(adresseFaktum, strukturertAdresse);
        }
        
        return adresseFaktum;
        
    }

    private boolean isUtenlandskAdresse(final Adresse gjeldendeAdresse) {
        return gjeldendeAdresse.getLandkode() != null && !gjeldendeAdresse.getLandkode().equals("NOR");
    }

    private void populerUstrukturertAdresse(Faktum adresseFaktum, Adresse adresse) {
        adresseFaktum
                .medSystemProperty("adresse", adresse.getAdresse())
                .medSystemProperty("type", "ustrukturert")
                .medSystemProperty("landkode", "NOR");
    }

    private void populerStrukturertAdresse(final Faktum adresseFaktum, final StrukturertAdresse adresse) {
        adresseFaktum
                .medSystemProperty("type", adresse.type)
                .medSystemProperty("landkode", "NOR")
                .medSystemProperty("kommunenummer", adresse.kommunenummer)
                .medSystemProperty("bolignummer", adresse.bolignummer)
                .medSystemProperty("postnummer", adresse.postnummer)
                .medSystemProperty("poststed", adresse.poststed)
                ;
        
        if (adresse instanceof Gateadresse) {
            populerGateadresse(adresseFaktum, adresse);
        } else if (adresse instanceof MatrikkelAdresse) {
            populerMatrikkeladresse(adresseFaktum, adresse);
        } else {
            throw new RuntimeException("Ukjent adressetype: " + adresse.getClass().getName());
        }
    }

    private void populerGateadresse(final Faktum adresseFaktum, final StrukturertAdresse adresse) {
        final Gateadresse gateadresse = (Gateadresse) adresse;
        adresseFaktum.medSystemProperty("gatenavn", gateadresse.gatenavn);
        adresseFaktum.medSystemProperty("husnummer", gateadresse.husnummer);
        adresseFaktum.medSystemProperty("husbokstav", gateadresse.husbokstav);
        
        /* 
         * Kombinert gatenavn og husnummer etter ønske fra interaksjonsdesigner.
         * Vises kun til bruker der adresse kan overstyres. Brukes ikke i oppsummering.
         */
        adresseFaktum.medSystemProperty("adresse", (gateadresse.gatenavn + " " + gateadresse.husnummer + gateadresse.husbokstav).trim()); 
    }
    
    private void populerMatrikkeladresse(final Faktum adresseFaktum, final StrukturertAdresse adresse) {
        final MatrikkelAdresse matrikkeladresse = (MatrikkelAdresse) adresse;
        adresseFaktum
                .medSystemProperty("eiendomsnavn", matrikkeladresse.eiendomsnavn)
                .medSystemProperty("gaardsnummer", matrikkeladresse.gaardsnummer)
                .medSystemProperty("bruksnummer", matrikkeladresse.bruksnummer)
                .medSystemProperty("festenummer", matrikkeladresse.festenummer)
                .medSystemProperty("seksjonsnummer", matrikkeladresse.seksjonsnummer)
                .medSystemProperty("undernummer", matrikkeladresse.undernummer)
                ;
    }
}
