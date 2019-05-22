package no.nav.sbl.dialogarena.soknadinnsending.business.person;

import no.nav.sbl.dialogarena.sendsoknad.domain.*;
import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.sendsoknad.domain.util.StatsborgerskapType;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.BolkService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

import static no.nav.sbl.dialogarena.sendsoknad.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia.*;
import static no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus.Status.GIFT;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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

    List<Faktum> genererPersonaliaFaktum(Long soknadId, Personalia personalia) {
        String statsborgerskap = personalia.getStatsborgerskap();
        List<Faktum> fakta = new ArrayList<>();
        fakta.add(new Faktum().medSoknadId(soknadId).medKey("personalia")
                .medSystemProperty(FNR_KEY, personalia.getFnr())
                .medSystemProperty(KONTONUMMER_KEY, personalia.getKontonummer())
                .medSystemProperty(ER_UTENLANDSK_BANKKONTO, personalia.getErUtenlandskBankkonto().toString())
                .medSystemProperty(UTENLANDSK_KONTO_BANKNAVN, personalia.getUtenlandskKontoBanknavn())
                .medSystemProperty(UTENLANDSK_KONTO_LAND, personalia.getUtenlandskKontoLand())
                .medSystemProperty(ALDER_KEY, personalia.getAlder())
                .medSystemProperty(NAVN_KEY, personalia.getNavn())
                .medSystemProperty(FORNAVN_KEY, personalia.getFornavn())
                .medSystemProperty(MELLOMNAVN_KEY, personalia.getMellomnavn())
                .medSystemProperty(ETTERNAVN_KEY, personalia.getEtternavn())
                .medSystemProperty(EPOST_KEY, personalia.getEpost())
                .medSystemProperty(STATSBORGERSKAP_KEY, statsborgerskap)
                .medSystemProperty(STATSBORGERSKAPTYPE_KEY, StatsborgerskapType.get(statsborgerskap))
                .medSystemProperty(KJONN_KEY, personalia.getKjonn())
                .medSystemProperty(FOLKEREGISTRERTADRESSE_KEY, folkeregistrertAdresseString(personalia))
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

        String sivilstatus = personalia.getSivilstatus();
        if (isNotEmpty(sivilstatus)) {
            fakta.add(genererSystemregistrertSivilstandFaktum(soknadId, sivilstatus));
        }

        if (personalia.getEktefelle() != null && isNotEmpty(sivilstatus) && GIFT.equals(JsonSivilstatus.Status.fromValue(sivilstatus))) {
            fakta.add(genererSystemregistrertEktefelleFaktum(soknadId, personalia));
            fakta.add(new Faktum().medSoknadId(soknadId)
                    .medKey("familie.sivilstatus.sivilstatusOverskrivesAvBruker")
                    .medValue("false"));
        } else {
            fakta.add(new Faktum().medSoknadId(soknadId)
                    .medKey("familie.sivilstatus.sivilstatusOverskrivesAvBruker")
                    .medValue("true"));
        }
        return fakta;
    }

    private Faktum genererSystemregistrertSivilstandFaktum(Long soknadId, String sivilstatus) {
        Faktum systemregistrertSivilstand = new Faktum().medSoknadId(soknadId).medKey("system.familie.sivilstatus")
                .medType(SYSTEMREGISTRERT)
                .medValue(sivilstatus);
        return systemregistrertSivilstand;
    }

    Faktum genererSystemregistrertEktefelleFaktum(Long soknadId, Personalia personalia) {
        Ektefelle ektefelle = personalia.getEktefelle();
        if (ektefelle == null) {
            return null;
        }
        return new Faktum().medSoknadId(soknadId).medKey("system.familie.sivilstatus.gift.ektefelle")
                .medType(SYSTEMREGISTRERT)
                .medSystemProperty("fornavn", ektefelle.getFornavn())
                .medSystemProperty("mellomnavn", ektefelle.getMellomnavn())
                .medSystemProperty("etternavn", ektefelle.getEtternavn())
                .medSystemProperty("fodselsdato", ektefelle.getFodselsdato() != null ? ektefelle.getFodselsdato().toString() : null)
                .medSystemProperty("fnr", ektefelle.getFnr())
                .medSystemProperty("folkeregistrertsammen", ektefelle.erFolkeregistrertsammen() + "")
                .medSystemProperty("ikketilgangtilektefelle", ektefelle.harIkketilgangtilektefelle() + "");
    }

    private String folkeregistrertAdresseString(Personalia personalia) {
        final Adresse folkeregistrertAdresse = personalia.getFolkeregistrertAdresse();
        if (folkeregistrertAdresse == null) {
            return null;
        }
        return folkeregistrertAdresse.getAdresse();
    }
}
