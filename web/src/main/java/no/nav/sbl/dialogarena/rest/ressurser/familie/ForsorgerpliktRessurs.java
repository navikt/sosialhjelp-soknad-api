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
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.*;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.rest.mappers.PersonMapper.getPersonnummerFromFnr;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/familie/forsorgerplikt")
@Timed
@Produces(APPLICATION_JSON)
public class ForsorgerpliktRessurs {

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
    public ForsorgerpliktFrontend hentForsorgerplikt(@PathParam("behandlingsId") String behandlingsId){
        final String eier = OidcFeatureToggleUtils.getUserId();
        final JsonInternalSoknad soknad = legacyHelper.hentSoknad(behandlingsId, eier, false).getJsonInternalSoknad();
        final JsonForsorgerplikt jsonForsorgerplikt = soknad.getSoknad().getData().getFamilie().getForsorgerplikt();

        return mapToForsorgerpliktFrontend(jsonForsorgerplikt);
    }

    @PUT
    public void updateForsorgerplikt(@PathParam("behandlingsId") String behandlingsId, ForsorgerpliktFrontend forsorgerpliktFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        update(behandlingsId, forsorgerpliktFrontend);
        legacyUpdate(behandlingsId, forsorgerpliktFrontend);
    }

    private void update(String behandlingsId, ForsorgerpliktFrontend forsorgerpliktFrontend) {
        final String eier = OidcFeatureToggleUtils.getUserId();
        final SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).get();
        final JsonForsorgerplikt forsorgerplikt = soknad.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();

        if(forsorgerpliktFrontend.barnebidrag != null) {
            if (forsorgerplikt.getBarnebidrag() == null) {
                forsorgerplikt.setBarnebidrag(new JsonBarnebidrag().withKilde(JsonKildeBruker.BRUKER).withVerdi(forsorgerpliktFrontend.barnebidrag));
            } else {
                forsorgerplikt.getBarnebidrag().setVerdi(forsorgerpliktFrontend.barnebidrag);
            }
        }

        if (forsorgerpliktFrontend.ansvar != null && !forsorgerpliktFrontend.ansvar.isEmpty()){
            for (AnsvarFrontend ansvarFrontend : forsorgerpliktFrontend.ansvar){
                if (ansvarFrontend.harDiskresjonskode != null && ansvarFrontend.harDiskresjonskode){
                    continue;
                }
                for (JsonAnsvar ansvar : forsorgerplikt.getAnsvar()){
                    if (ansvar.getBarn().getHarDiskresjonskode() != null && ansvar.getBarn().getHarDiskresjonskode()){
                        continue;
                    }
                    if (ansvar.getBarn().getPersonIdentifikator().equals(ansvarFrontend.barn.fodselsnummer)){
                        ansvar.setBorSammenMed(ansvarFrontend.borSammenMed == null ? null :
                                new JsonBorSammenMed().withKilde(JsonKildeBruker.BRUKER).withVerdi(ansvarFrontend.borSammenMed));
                        ansvar.setHarDeltBosted(ansvarFrontend.harDeltBosted == null ? null :
                                new JsonHarDeltBosted().withKilde(JsonKildeBruker.BRUKER).withVerdi(ansvarFrontend.harDeltBosted));
                        ansvar.setSamvarsgrad(ansvarFrontend.samvarsgrad == null ? null :
                                new JsonSamvarsgrad().withKilde(JsonKildeBruker.BRUKER).withVerdi(ansvarFrontend.samvarsgrad));
                    }
                }
            }
        }

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void legacyUpdate(String behandlingsId, ForsorgerpliktFrontend forsorgerpliktFrontend) {
        final WebSoknad webSoknad = soknadService.hentSoknad(behandlingsId, true, false);

        if(forsorgerpliktFrontend.barnebidrag != null) {
            final Faktum barnebidrag = faktaService.hentFaktumMedKey(webSoknad.getSoknadId(), "familie.barn.true.barnebidrag");
            barnebidrag.setType(Faktum.FaktumType.BRUKERREGISTRERT);
            barnebidrag.setValue(forsorgerpliktFrontend.barnebidrag.toString());
            faktaService.lagreBrukerFaktum(barnebidrag);
        }

        if (forsorgerpliktFrontend.ansvar != null && !forsorgerpliktFrontend.ansvar.isEmpty()){
            List<Faktum> barnefakta = webSoknad.getFaktaMedKey("system.familie.barn.true.barn");
            for (AnsvarFrontend ansvarFrontend : forsorgerpliktFrontend.ansvar) {
                for (Faktum faktum : barnefakta) {
                    Map<String, String> barn = faktum.getProperties();
                    if (barn.get("fnr").equals(ansvarFrontend.barn.fodselsnummer)) {
                        barn.put("grad", ansvarFrontend.samvarsgrad != null ? ansvarFrontend.samvarsgrad.toString() : null);
                        barn.put("deltbosted", ansvarFrontend.harDeltBosted != null ? ansvarFrontend.harDeltBosted.toString() : null);
                    }
                    faktaService.lagreBrukerFaktum(faktum);
                }
            }
        }
    }

    private ForsorgerpliktFrontend mapToForsorgerpliktFrontend(JsonForsorgerplikt jsonForsorgerplikt) {
        final List<AnsvarFrontend> ansvar = jsonForsorgerplikt.getAnsvar() == null ? null :
                jsonForsorgerplikt.getAnsvar().stream().map(this::mapToAnsvarFrontend)
                        .collect(Collectors.toList());
        return new ForsorgerpliktFrontend()
                .withHarForsorgerplikt(jsonForsorgerplikt.getHarForsorgerplikt() == null ? null :
                        jsonForsorgerplikt.getHarForsorgerplikt().getVerdi())
                .withBarnebidrag(jsonForsorgerplikt.getBarnebidrag() == null ? null :
                        jsonForsorgerplikt.getBarnebidrag().getVerdi())
                .withAnsvar(ansvar);
    }

    private AnsvarFrontend mapToAnsvarFrontend(JsonAnsvar jsonAnsvar) {
        if (jsonAnsvar == null){
            return null;
        }

        return new AnsvarFrontend().withBarn(mapToBarnFrontend(jsonAnsvar.getBarn()))
                .withHarDiskresjonskode(jsonAnsvar.getBarn() != null ? jsonAnsvar.getBarn().getHarDiskresjonskode() : null)
                .withErFolkeregistrertSammen(jsonAnsvar.getErFolkeregistrertSammen() == null ? null :
                        jsonAnsvar.getErFolkeregistrertSammen().getVerdi())
                .withBorSammenMed(jsonAnsvar.getBorSammenMed() == null ? null : jsonAnsvar.getBorSammenMed().getVerdi())
                .withHarDeltBosted(jsonAnsvar.getHarDeltBosted() == null ? null : jsonAnsvar.getHarDeltBosted().getVerdi())
                .withSamvarsgrad(jsonAnsvar.getSamvarsgrad() == null ? null : jsonAnsvar.getSamvarsgrad().getVerdi());
    }

    private BarnFrontend mapToBarnFrontend(JsonBarn barn) {
        if (barn == null){
            return null;
        }

        return new BarnFrontend()
                .withNavn(new NavnFrontend(barn.getNavn().getFornavn(), barn.getNavn().getMellomnavn(), barn.getNavn().getEtternavn()))
                .withFodselsdato(barn.getFodselsdato())
                .withPersonnummer(getPersonnummerFromFnr(barn.getPersonIdentifikator()))
                .withFodselsnummer(barn.getPersonIdentifikator());
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class ForsorgerpliktFrontend {
        public Boolean harForsorgerplikt;
        public JsonBarnebidrag.Verdi barnebidrag;
        public List<AnsvarFrontend> ansvar;

        public ForsorgerpliktFrontend withHarForsorgerplikt(Boolean harForsorgerplikt) {
            this.harForsorgerplikt = harForsorgerplikt;
            return this;
        }

        public ForsorgerpliktFrontend withBarnebidrag(JsonBarnebidrag.Verdi barnebidrag) {
            this.barnebidrag = barnebidrag;
            return this;
        }

        public ForsorgerpliktFrontend withAnsvar(List<AnsvarFrontend> ansvar) {
            this.ansvar = ansvar;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class AnsvarFrontend {
        public BarnFrontend barn;
        public Boolean harDiskresjonskode;
        public Boolean borSammenMed;
        public Boolean erFolkeregistrertSammen;
        public Boolean harDeltBosted;
        public Integer samvarsgrad;

        public AnsvarFrontend withBarn(BarnFrontend barn) {
            this.barn = barn;
            return this;
        }

        public AnsvarFrontend withHarDiskresjonskode(Boolean harDiskresjonskode) {
            this.harDiskresjonskode = harDiskresjonskode;
            return this;
        }

        public AnsvarFrontend withBorSammenMed(Boolean borSammenMed) {
            this.borSammenMed = borSammenMed;
            return this;
        }

        public AnsvarFrontend withErFolkeregistrertSammen(Boolean erFolkeregistrertSammen) {
            this.erFolkeregistrertSammen = erFolkeregistrertSammen;
            return this;
        }

        public AnsvarFrontend withHarDeltBosted(Boolean harDeltBosted) {
            this.harDeltBosted = harDeltBosted;
            return this;
        }

        public AnsvarFrontend withSamvarsgrad(Integer samvarsgrad) {
            this.samvarsgrad = samvarsgrad;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class BarnFrontend {
        public NavnFrontend navn;
        public String fodselsdato;
        public String personnummer;
        public String fodselsnummer;

        public BarnFrontend withNavn(NavnFrontend navn) {
            this.navn = navn;
            return this;
        }

        public BarnFrontend withFodselsdato(String fodselsdato) {
            this.fodselsdato = fodselsdato;
            return this;
        }

        public BarnFrontend withPersonnummer(String personnummer) {
            this.personnummer = personnummer;
            return this;
        }

        public BarnFrontend withFodselsnummer(String fodselsnummer) {
            this.fodselsnummer = fodselsnummer;
            return this;
        }
    }
}
