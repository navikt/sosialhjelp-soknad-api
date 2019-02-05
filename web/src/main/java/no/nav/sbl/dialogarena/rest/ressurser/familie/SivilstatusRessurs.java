package no.nav.sbl.dialogarena.rest.ressurser.familie;

import no.nav.metrics.aspects.Timed;
import no.nav.modig.core.context.SubjectHandler;
import no.nav.sbl.dialogarena.rest.ressurser.LegacyHelper;
import no.nav.sbl.dialogarena.rest.ressurser.NavnFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.Faktum;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.SoknadService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonFamilie;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@Path("/soknader/{behandlingsId}/familie/sivilstatus")
@Timed
@Produces(APPLICATION_JSON)
public class SivilstatusRessurs {

    @Inject
    private LegacyHelper legacyHelper;

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadService soknadService;

    @Inject
    private FaktaService faktaService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @GET
    public SivilstatusFrontend hentSivilstatus(@PathParam("behandlingsId") String behandlingsId){
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final JsonSivilstatus jsonSivilstatus = soknad.getSoknad().getData().getFamilie().getSivilstatus();

        if (jsonSivilstatus == null){
            return null;
        }

        return mapToSivilstatusFrontend(jsonSivilstatus);
    }

    @PUT
    public void updateSivilstatus(@PathParam("behandlingsId") String behandlingsId, SivilstatusFrontend sivilstatusFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, sivilstatusFrontend);
        legacyUpdate(behandlingsId, sivilstatusFrontend);
    }

    private void update(String behandlingsId, SivilstatusFrontend sivilstatusFrontend) {
        final String eier = SubjectHandler.getSubjectHandler().getUid();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonFamilie familie = soknad.getJsonInternalSoknad().getSoknad().getData().getFamilie();

        if (familie.getSivilstatus() == null){
            soknad.getJsonInternalSoknad().getSoknad().getData().getFamilie().setSivilstatus(new JsonSivilstatus());
        }

        final JsonSivilstatus sivilstatus = familie.getSivilstatus();
        sivilstatus.setKilde(JsonKilde.BRUKER);
        sivilstatus.setStatus(sivilstatusFrontend.sivilstatus);
        sivilstatus.setEktefelle(mapToJsonEktefelle(sivilstatusFrontend.ektefelle));
        sivilstatus.setBorSammenMed(sivilstatusFrontend.borSammenMed);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, SivilstatusFrontend sivilstatusFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum sivilstatus = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "familie.sivilstatus");
        sivilstatus.setType(Faktum.FaktumType.BRUKERREGISTRERT);
        sivilstatus.setValue(sivilstatusFrontend.sivilstatus.toString());
        faktaService.lagreBrukerFaktum(sivilstatus);

        final Faktum ektefelle = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "familie.sivilstatus.gift.ektefelle");
        ektefelle.setType(Faktum.FaktumType.BRUKERREGISTRERT);

        final Map<String, String> ektefelleProperties = getFaktumProperties(ektefelle);
        EktefelleFrontend ektefelleFrontend = sivilstatusFrontend.ektefelle;
        if (ektefelleFrontend != null){
            ektefelleProperties.put("fornavn", ektefelleFrontend.navn.fornavn);
            ektefelleProperties.put("mellomnavn", ektefelleFrontend.navn.mellomnavn);
            ektefelleProperties.put("etternavn", ektefelleFrontend.navn.etternavn);
            ektefelleProperties.put("fodselsdato", ektefelleFrontend.fodselsdato);
            ektefelleProperties.put("fnr", ektefelleFrontend.personIdentifikator);
        }
        ektefelleProperties.put("borsammen", sivilstatusFrontend.borSammenMed != null ? sivilstatusFrontend.borSammenMed.toString() : null);
        if (!ektefelleProperties.isEmpty()){
            ektefelle.setProperties(ektefelleProperties);
        }
        faktaService.lagreBrukerFaktum(ektefelle);
    }

    private static Map<String, String> getFaktumProperties(Faktum faktum) {
        if (faktum == null) {
            return new HashMap<>();
        }
        return faktum.getProperties();
    }

    private EktefelleFrontend addEktefelleFrontend(JsonEktefelle jsonEktefelle) {
        final JsonNavn navn = jsonEktefelle.getNavn();
        return new EktefelleFrontend()
                .withNavn(new NavnFrontend(navn.getFornavn(), navn.getMellomnavn(), navn.getEtternavn()))
                .withFodselsdato(jsonEktefelle.getFodselsdato())
                .withPersonIdentifikator(jsonEktefelle.getPersonIdentifikator());
    }

    private JsonEktefelle mapToJsonEktefelle(EktefelleFrontend ektefelle) {
        if(ektefelle == null){
            return null;
        }
        return new JsonEktefelle().withNavn(mapToJsonNavn(ektefelle.navn))
                .withFodselsdato(ektefelle.fodselsdato)
                .withPersonIdentifikator(ektefelle.personIdentifikator);
    }

    private SivilstatusFrontend mapToSivilstatusFrontend(JsonSivilstatus jsonSivilstatus) {
        return new SivilstatusFrontend()
                .withKildeErSystem(mapToSystemBoolean(jsonSivilstatus.getKilde()))
                .withSivilstatus(jsonSivilstatus.getStatus())
                .withEktefelle(jsonSivilstatus.getEktefelle() == null ? null : addEktefelleFrontend(jsonSivilstatus.getEktefelle()))
                .withEktefelleHarDiskresjonskode(jsonSivilstatus.getEktefelleHarDiskresjonskode())
                .withFolkeregistrertMedEktefelle(jsonSivilstatus.getFolkeregistrertMedEktefelle())
                .withBorSammenMed(jsonSivilstatus.getBorSammenMed());
    }

    private JsonNavn mapToJsonNavn(NavnFrontend navn) {
        return new JsonNavn()
                .withFornavn(navn.fornavn != null ? navn.fornavn : "")
                .withMellomnavn(navn.mellomnavn != null ? navn.mellomnavn : "")
                .withEtternavn(navn.etternavn != null ? navn.etternavn : "");
    }

    private Boolean mapToSystemBoolean(JsonKilde kilde) {
        switch (kilde){
            case SYSTEM:
                return true;
            case BRUKER:
                return false;
            default:
                return null;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class SivilstatusFrontend {
        public Boolean kildeErSystem;
        public JsonSivilstatus.Status sivilstatus;
        public EktefelleFrontend ektefelle;
        public Boolean ektefelleHarDiskresjonskode;
        public Boolean folkeregistrertMedEktefelle;
        public Boolean borSammenMed;

        public SivilstatusFrontend withKildeErSystem(Boolean kildeErSystem) {
            this.kildeErSystem = kildeErSystem;
            return this;
        }

        public SivilstatusFrontend withSivilstatus(JsonSivilstatus.Status sivilstatus) {
            this.sivilstatus = sivilstatus;
            return this;
        }

        public SivilstatusFrontend withEktefelle(EktefelleFrontend ektefelle) {
            this.ektefelle = ektefelle;
            return this;
        }

        public SivilstatusFrontend withEktefelleHarDiskresjonskode(Boolean ektefelleHarDiskresjonskode) {
            this.ektefelleHarDiskresjonskode = ektefelleHarDiskresjonskode;
            return this;
        }

        public SivilstatusFrontend withFolkeregistrertMedEktefelle(Boolean folkeregistrertMedEktefelle) {
            this.folkeregistrertMedEktefelle = folkeregistrertMedEktefelle;
            return this;
        }

        public SivilstatusFrontend withBorSammenMed(Boolean borSammenMed) {
            this.borSammenMed = borSammenMed;
            return this;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class EktefelleFrontend {
        public NavnFrontend navn;
        public String fodselsdato;
        public String personIdentifikator;

        public EktefelleFrontend withNavn(NavnFrontend navn) {
            this.navn = navn;
            return this;
        }

        public EktefelleFrontend withFodselsdato(String fodselsdato) {
            this.fodselsdato = fodselsdato;
            return this;
        }

        public EktefelleFrontend withPersonIdentifikator(String personIdentifikator) {
            this.personIdentifikator = personIdentifikator;
            return this;
        }
    }
}
