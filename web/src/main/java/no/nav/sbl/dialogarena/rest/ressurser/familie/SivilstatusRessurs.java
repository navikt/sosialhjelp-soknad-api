package no.nav.sbl.dialogarena.rest.ressurser.familie;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
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
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.rest.mappers.PersonMapper.getPersonnummerFromFnr;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
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
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, false).getJsonInternalSoknad();
        final JsonSivilstatus jsonSivilstatus = soknad.getSoknad().getData().getFamilie().getSivilstatus();

        if (jsonSivilstatus == null){
            return null;
        }

        return mapToSivilstatusFrontend(jsonSivilstatus);
    }

    @PUT
    public void updateSivilstatus(@PathParam("behandlingsId") String behandlingsId, SivilstatusFrontend sivilstatusFrontend) throws ParseException {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, sivilstatusFrontend);
        legacyUpdate(behandlingsId, sivilstatusFrontend);
    }

    private void update(String behandlingsId, SivilstatusFrontend sivilstatusFrontend) throws ParseException {
        final String eier = OidcFeatureToggleUtils.getUserId();
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

    private void legacyUpdate(String behandlingsId, SivilstatusFrontend sivilstatusFrontend) throws ParseException {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, false, false);

        final Faktum sivilstatus = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "familie.sivilstatus");
        sivilstatus.setType(Faktum.FaktumType.BRUKERREGISTRERT);
        sivilstatus.setValue(sivilstatusFrontend.sivilstatus.toString());
        faktaService.lagreBrukerFaktum(sivilstatus);

        final EktefelleFrontend ektefelleFrontend = sivilstatusFrontend.ektefelle;
        if (ektefelleFrontend != null) {
            final Faktum ektefelle = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "familie.sivilstatus.gift.ektefelle");
            ektefelle.setType(Faktum.FaktumType.BRUKERREGISTRERT);

            final Map<String, String> ektefelleProperties = getFaktumProperties(ektefelle);
            if (ektefelleFrontend.navn != null){
                ektefelleProperties.put("fornavn", ektefelleFrontend.navn.fornavn != null ? ektefelleFrontend.navn.fornavn : "");
                ektefelleProperties.put("mellomnavn", ektefelleFrontend.navn.mellomnavn != null ? ektefelleFrontend.navn.mellomnavn : "");
                ektefelleProperties.put("etternavn", ektefelleFrontend.navn.etternavn != null ? ektefelleFrontend.navn.etternavn : "");
            }
            if (ektefelleFrontend.fodselsdato != null){
                ektefelleProperties.put("fnr", format_ddmmyyyy(ektefelleFrontend.fodselsdato));
                ektefelleProperties.put("fodselsdato", ektefelleFrontend.fodselsdato);
            }
            if (ektefelleFrontend.personnummer != null){
                ektefelleProperties.put("pnr", ektefelleFrontend.personnummer);
            }
            if (sivilstatusFrontend.borSammenMed != null){
                ektefelleProperties.put("borsammen", sivilstatusFrontend.borSammenMed.toString());
            }
            if (!ektefelleProperties.isEmpty()) {
                ektefelle.setProperties(ektefelleProperties);
            }
            faktaService.lagreBrukerFaktum(ektefelle);
        }
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
                .withPersonnummer(getPersonnummerFromFnr(jsonEktefelle.getPersonIdentifikator()));
    }

    private JsonEktefelle mapToJsonEktefelle(EktefelleFrontend ektefelle) throws ParseException {
        if(ektefelle == null){
            return null;
        }
        return new JsonEktefelle().withNavn(mapToJsonNavn(ektefelle.navn))
                .withFodselsdato(ektefelle.fodselsdato)
                .withPersonIdentifikator(getFnr(ektefelle.fodselsdato, ektefelle.personnummer));
    }

    private String format_ddmmyyyy(String fodselsdato) throws ParseException {
        if (fodselsdato == null){
            return null;
        }
        final DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd");
        final DateFormat targetFormat = new SimpleDateFormat("ddMMyyyy");
        final Date date = originalFormat.parse(fodselsdato);
        return targetFormat.format(date);
    }

    private String getFnr(String fodselsdato, String personnummer) throws ParseException {
        if (fodselsdato == null || personnummer == null){
            return null;
        }
        final DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd");
        final DateFormat targetFormat = new SimpleDateFormat("ddMMyy");
        final Date date = originalFormat.parse(fodselsdato);
        return targetFormat.format(date) + personnummer;
    }

    private SivilstatusFrontend mapToSivilstatusFrontend(JsonSivilstatus jsonSivilstatus) {
        return new SivilstatusFrontend()
                .withKildeErSystem(mapToSystemBoolean(jsonSivilstatus.getKilde()))
                .withSivilstatus(jsonSivilstatus.getStatus())
                .withEktefelle(jsonSivilstatus.getEktefelle() == null ? null :
                        addEktefelleFrontend(jsonSivilstatus.getEktefelle()))
                .withHarDiskresjonskode(jsonSivilstatus.getEktefelleHarDiskresjonskode())
                .withBorSammenMed(jsonSivilstatus.getBorSammenMed())
                .withErFolkeregistrertSammen(jsonSivilstatus.getFolkeregistrertMedEktefelle());
    }

    private JsonNavn mapToJsonNavn(NavnFrontend navn) {
        if (navn == null){
            return null;
        }
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
        public Boolean harDiskresjonskode;
        public Boolean borSammenMed;
        public Boolean erFolkeregistrertSammen;

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

        public SivilstatusFrontend withHarDiskresjonskode(Boolean harDiskresjonskode) {
            this.harDiskresjonskode = harDiskresjonskode;
            return this;
        }

        public SivilstatusFrontend withBorSammenMed(Boolean borSammenMed) {
            this.borSammenMed = borSammenMed;
            return this;
        }

        public SivilstatusFrontend withErFolkeregistrertSammen(Boolean erFolkeregistrertSammen) {
            this.erFolkeregistrertSammen = erFolkeregistrertSammen;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class EktefelleFrontend {
        public NavnFrontend navn;
        public String fodselsdato;
        public String personnummer;

        public EktefelleFrontend withNavn(NavnFrontend navn) {
            this.navn = navn;
            return this;
        }

        public EktefelleFrontend withFodselsdato(String fodselsdato) {
            this.fodselsdato = fodselsdato;
            return this;
        }

        public EktefelleFrontend withPersonnummer(String personnummer) {
            this.personnummer = personnummer;
            return this;
        }
    }
}
