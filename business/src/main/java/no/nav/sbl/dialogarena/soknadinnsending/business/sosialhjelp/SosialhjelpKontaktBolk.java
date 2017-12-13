package no.nav.sbl.dialogarena.soknadinnsending.business.sosialhjelp;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

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
                new Faktum().medSoknadId(soknadId).medKey("kontakt.system.kontonummer").medValue(norskMobiltelefon(personalia)),
                new Faktum().medSoknadId(soknadId).medKey("kontakt.system.telefon").medValue(personalia.getMobiltelefonnummer())
        );
    }

    private String norskMobiltelefon(Personalia personalia) {
        if (personalia.getErUtenlandskBankkonto() != null && personalia.getErUtenlandskBankkonto()) {
            return "";
        } else {
            return personalia.getKontonummer();
        }
    }
}
