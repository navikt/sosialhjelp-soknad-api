package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.*;
import org.springframework.stereotype.*;

import javax.inject.*;
import java.util.*;

import static no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia.*;

@Service
public class PersonaliaBolk implements BolkService {

    private static final String BOLKNAVN = "Personalia";

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
        String statsborgerskap = personalia.getStatsborgerskap();
        return Arrays.asList(new Faktum().medSoknadId(soknadId).medKey("personalia")
                .medSystemProperty(FNR_KEY, personalia.getFnr())
                .medSystemProperty(KONTONUMMER_KEY, personalia.getKontonummer())
                .medSystemProperty(ER_UTENLANDSK_BANKKONTO, personalia.getErUtenlandskBankkonto().toString())
                .medSystemProperty(UTENLANDSK_KONTO_BANKNAVN, personalia.getUtenlandskKontoBanknavn())
                .medSystemProperty(UTENLANDSK_KONTO_LAND, personalia.getUtenlandskKontoLand())
                .medSystemProperty(ALDER_KEY, personalia.getAlder())
                .medSystemProperty(NAVN_KEY, personalia.getNavn())
                .medSystemProperty(EPOST_KEY, personalia.getEpost())
                .medSystemProperty(STATSBORGERSKAP_KEY, statsborgerskap)
                .medSystemProperty(STATSBORGERSKAPTYPE_KEY, StatsborgerskapType.get(statsborgerskap))
                .medSystemProperty(KJONN_KEY, personalia.getKjonn())
                .medSystemProperty(GJELDENDEADRESSE_KEY, personalia.getGjeldendeAdresse().getAdresse())
                .medSystemProperty(DISKRESJONSKODE, personalia.getDiskresjonskode())
                .medSystemProperty(GJELDENDEADRESSE_TYPE_KEY, personalia.getGjeldendeAdresse().getAdressetype())
                .medSystemProperty(GJELDENDEADRESSE_GYLDIGFRA_KEY, personalia.getGjeldendeAdresse().getGyldigFra())
                .medSystemProperty(GJELDENDEADRESSE_GYLDIGTIL_KEY, personalia.getGjeldendeAdresse().getGyldigTil())
                .medSystemProperty(GJELDENDEADRESSE_LANDKODE, personalia.getGjeldendeAdresse().getLandkode())
                .medSystemProperty(SEKUNDARADRESSE_KEY, personalia.getSekundarAdresse().getAdresse())
                .medSystemProperty(SEKUNDARADRESSE_TYPE_KEY, personalia.getSekundarAdresse().getAdressetype())
                .medSystemProperty(SEKUNDARADRESSE_GYLDIGFRA_KEY, personalia.getSekundarAdresse().getGyldigFra())
                .medSystemProperty(SEKUNDARADRESSE_GYLDIGTIL_KEY, personalia.getSekundarAdresse().getGyldigTil()));
    }
}
