package no.nav.sbl.dialogarena.soknadinnsending.business.service.systemdata;

import no.nav.sbl.dialogarena.sendsoknad.domain.personalia.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.Systemdata;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.personalia.PersonaliaFletter;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonNordiskBorger;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonPersonalia;
import no.nav.sbl.soknadsosialhjelp.soknad.personalia.JsonStatsborgerskap;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class BasisPersonaliaSystemdata implements Systemdata {

    @Inject
    private PersonaliaFletter personaliaFletter;


    @Override
    public void updateSystemdataIn(SoknadUnderArbeid soknadUnderArbeid) {
        final JsonPersonalia personalia = soknadUnderArbeid.getJsonInternalSoknad().getSoknad().getData().getPersonalia();
        final String personIdentifikator = personalia.getPersonIdentifikator().getVerdi();
        final BasisPersonalia basisPersonalia = innhentSystemBasisPersonalia(personIdentifikator);

        personalia.getPersonIdentifikator().setVerdi(basisPersonalia.personIdentifikator);
        setNavnPaaPersonalia(personalia, basisPersonalia);
        setStatsborgerskapPaaPersonalia(personalia, basisPersonalia);
        setNordiskBorgerPaaPersonalia(personalia, basisPersonalia);
    }

    public BasisPersonalia innhentSystemBasisPersonalia(final String personIdentifikator) {
        final Personalia personalia = personaliaFletter.mapTilPersonalia(personIdentifikator);
        return new BasisPersonalia()
                .withPersonIdentifikator(personalia.getFnr())
                .withFornavn(personalia.getFornavn())
                .withMellomnavn(personalia.getMellomnavn())
                .withEtternavn(personalia.getEtternavn())
                .withStatsborgerskap(personalia.getStatsborgerskap())
                .withNordiskBorger(erNordiskBorger(personalia.getStatsborgerskap()));
    }

    private void setNordiskBorgerPaaPersonalia(JsonPersonalia personalia, BasisPersonalia basisPersonalia) {
        if (basisPersonalia.nordiskBorger == null){
            return;
        }
        if (personalia.getNordiskBorger() == null){
            personalia.setNordiskBorger(new JsonNordiskBorger()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(basisPersonalia.nordiskBorger));
        } else {
            personalia.getNordiskBorger().setKilde(JsonKilde.SYSTEM);
            personalia.getNordiskBorger().setVerdi(basisPersonalia.nordiskBorger);
        }
    }

    private void setStatsborgerskapPaaPersonalia(JsonPersonalia personalia, BasisPersonalia basisPersonalia) {
        if (basisPersonalia.statsborgerskap == null){
            return;
        }
        if (personalia.getStatsborgerskap() == null){
            personalia.setStatsborgerskap(new JsonStatsborgerskap()
                    .withKilde(JsonKilde.SYSTEM)
                    .withVerdi(basisPersonalia.statsborgerskap));
        } else {
            personalia.getStatsborgerskap().setKilde(JsonKilde.SYSTEM);
            personalia.getStatsborgerskap().setVerdi(basisPersonalia.statsborgerskap);
        }
    }

    private void setNavnPaaPersonalia(JsonPersonalia personalia, BasisPersonalia basisPersonalia) {
        personalia.getNavn().setFornavn(basisPersonalia.fornavn != null ? basisPersonalia.fornavn : "");
        personalia.getNavn().setMellomnavn(basisPersonalia.mellomnavn != null ? basisPersonalia.mellomnavn : "");
        personalia.getNavn().setEtternavn(basisPersonalia.etternavn != null ? basisPersonalia.etternavn : "");
    }

    private static boolean erNordiskBorger(String statsborgerskap) {
        switch (statsborgerskap) {
            case "NOR":
            case "SWE":
            case "FRO":
            case "ISL":
            case "DNK":
            case "FIN":
                return true;
            default:
                return false;
        }
    }

    public static final class BasisPersonalia {
        public String personIdentifikator;
        public String fornavn;
        public String mellomnavn;
        public String etternavn;
        public String statsborgerskap;
        public Boolean nordiskBorger;

        public BasisPersonalia withPersonIdentifikator(String fodselsnummer){
            this.personIdentifikator = fodselsnummer;
            return this;
        }
        public BasisPersonalia withFornavn(String fornavn){
            this.fornavn = fornavn;
            return this;
        }

        public BasisPersonalia withMellomnavn(String mellomnavn){
            this.mellomnavn = mellomnavn;
            return this;
        }

        public BasisPersonalia withEtternavn(String etternavn){
            this.etternavn = etternavn;
            return this;
        }

        public BasisPersonalia withStatsborgerskap(String statsborgerskap){
            this.statsborgerskap = statsborgerskap;
            return this;
        }

        public BasisPersonalia withNordiskBorger(Boolean nordiskBorger){
            this.nordiskBorger = nordiskBorger;
            return this;
        }
    }


}