package no.nav.sbl.dialogarena.rest.ressurser.familie;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.rest.ressurser.NavnFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.rest.mappers.PersonMapper.getPersonnummerFromFnr;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/familie/sivilstatus")
@Timed
@Produces(APPLICATION_JSON)
public class SivilstatusRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @GET
    public SivilstatusFrontend hentSivilstatus(@PathParam("behandlingsId") String behandlingsId){
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        final JsonSivilstatus jsonSivilstatus = soknad.getSoknad().getData().getFamilie().getSivilstatus();

        if (jsonSivilstatus == null){
            return null;
        }

        return mapToSivilstatusFrontend(jsonSivilstatus);
    }

    @PUT
    public void updateSivilstatus(@PathParam("behandlingsId") String behandlingsId, SivilstatusFrontend sivilstatusFrontend) throws ParseException {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
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
