package no.nav.sbl.dialogarena.rest.ressurser.familie;

import no.nav.metrics.aspects.Timed;
import no.nav.sbl.dialogarena.rest.ressurser.NavnFrontend;
import no.nav.sbl.dialogarena.sendsoknad.domain.oidc.OidcFeatureToggleUtils;
import no.nav.sbl.dialogarena.sikkerhet.Tilgangskontroll;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.TextService;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKildeBruker;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.*;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.opplysning.JsonOkonomiOpplysningUtgift;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktInntekt;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.oversikt.JsonOkonomioversiktUtgift;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.soknadunderbehandling.SoknadUnderArbeidRepository;
import no.nav.security.oidc.api.ProtectedWithClaims;
import org.springframework.stereotype.Controller;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.sbl.dialogarena.rest.mappers.PersonMapper.getPersonnummerFromFnr;
import static no.nav.sbl.dialogarena.rest.mappers.PersonMapper.mapToJsonNavn;
import static no.nav.sbl.dialogarena.soknadinnsending.business.mappers.OkonomiMapper.*;

@Controller
@ProtectedWithClaims(issuer = "selvbetjening", claimMap = { "acr=Level4" })
@Path("/soknader/{behandlingsId}/familie/forsorgerplikt")
@Timed
@Produces(APPLICATION_JSON)
public class ForsorgerpliktRessurs {

    @Inject
    private Tilgangskontroll tilgangskontroll;

    @Inject
    private TextService textService;

    @Inject
    private SoknadUnderArbeidRepository soknadUnderArbeidRepository;

    @GET
    public ForsorgerpliktFrontend hentForsorgerplikt(@PathParam("behandlingsId") String behandlingsId){
        String eier = OidcFeatureToggleUtils.getUserId();
        JsonInternalSoknad soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier).getJsonInternalSoknad();
        JsonForsorgerplikt jsonForsorgerplikt = soknad.getSoknad().getData().getFamilie().getForsorgerplikt();

        return mapToForsorgerpliktFrontend(jsonForsorgerplikt);
    }

    @PUT
    public void updateForsorgerplikt(@PathParam("behandlingsId") String behandlingsId, ForsorgerpliktFrontend forsorgerpliktFrontend) {
        tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId);
        String eier = OidcFeatureToggleUtils.getUserId();
        SoknadUnderArbeid soknad = soknadUnderArbeidRepository.hentSoknad(behandlingsId, eier);
        JsonForsorgerplikt forsorgerplikt = soknad.getJsonInternalSoknad().getSoknad().getData().getFamilie().getForsorgerplikt();

        updateBarnebidrag(forsorgerpliktFrontend, soknad, forsorgerplikt);
        updateAnsvarAndHarForsorgerplikt(forsorgerpliktFrontend, soknad, forsorgerplikt);

        soknadUnderArbeidRepository.oppdaterSoknadsdata(soknad, eier);
    }

    private void updateBarnebidrag(ForsorgerpliktFrontend forsorgerpliktFrontend, SoknadUnderArbeid soknad, JsonForsorgerplikt forsorgerplikt) {
        String barnebidragType = "barnebidrag";
        JsonOkonomioversikt oversikt = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi().getOversikt();
        List<JsonOkonomioversiktInntekt> inntekter = oversikt.getInntekt();
        List<JsonOkonomioversiktUtgift> utgifter = oversikt.getUtgift();

        if(forsorgerpliktFrontend.barnebidrag != null) {
            if (forsorgerplikt.getBarnebidrag() == null) {
                forsorgerplikt.setBarnebidrag(new JsonBarnebidrag().withKilde(JsonKildeBruker.BRUKER).withVerdi(forsorgerpliktFrontend.barnebidrag));
            } else {
                forsorgerplikt.getBarnebidrag().setVerdi(forsorgerpliktFrontend.barnebidrag);
            }
            String tittel_mottar = textService.getJsonOkonomiTittel("opplysninger.familiesituasjon.barnebidrag.mottar");
            String tittel_betaler = textService.getJsonOkonomiTittel("opplysninger.familiesituasjon.barnebidrag.betaler");
            switch (forsorgerpliktFrontend.barnebidrag){
                case BEGGE:
                    addInntektIfNotPresentInOversikt(inntekter, barnebidragType, tittel_mottar);
                    addUtgiftIfNotPresentInOversikt(utgifter, barnebidragType, tittel_betaler);
                    break;
                case BETALER:
                    removeInntektIfPresentInOversikt(inntekter, barnebidragType);
                    addUtgiftIfNotPresentInOversikt(utgifter, barnebidragType, tittel_betaler);
                    break;
                case MOTTAR:
                    addInntektIfNotPresentInOversikt(inntekter, barnebidragType, tittel_mottar);
                    removeUtgiftIfPresentInOversikt(utgifter, barnebidragType);
                    break;
                case INGEN:
                    removeInntektIfPresentInOversikt(inntekter, barnebidragType);
                    removeUtgiftIfPresentInOversikt(utgifter, barnebidragType);
                    break;
            }
        } else {
            forsorgerplikt.setBarnebidrag(null);
            removeInntektIfPresentInOversikt(inntekter, barnebidragType);
            removeUtgiftIfPresentInOversikt(utgifter, barnebidragType);
        }
    }

    private void updateAnsvarAndHarForsorgerplikt(ForsorgerpliktFrontend forsorgerpliktFrontend, SoknadUnderArbeid soknad, JsonForsorgerplikt forsorgerplikt) {
        List<JsonAnsvar> systemAnsvar = forsorgerplikt.getAnsvar() == null? new ArrayList<>() : forsorgerplikt.getAnsvar().stream()
                .filter(jsonAnsvar -> jsonAnsvar.getBarn().getKilde().equals(JsonKilde.SYSTEM)).collect(Collectors.toList());
        if (forsorgerpliktFrontend.ansvar != null){
            for (AnsvarFrontend ansvarFrontend : forsorgerpliktFrontend.ansvar){
                for (JsonAnsvar ansvar : systemAnsvar){
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

        List<JsonAnsvar> brukerregistrertAnsvar = new ArrayList<>();
        if (forsorgerpliktFrontend.brukerregistrertAnsvar != null && !forsorgerpliktFrontend.brukerregistrertAnsvar.isEmpty()){
            if (forsorgerplikt.getHarForsorgerplikt() == null || forsorgerplikt.getHarForsorgerplikt().getVerdi().equals(false)) {
                forsorgerplikt.setHarForsorgerplikt(new JsonHarForsorgerplikt().withKilde(JsonKilde.BRUKER).withVerdi(true));
            }
            for (AnsvarFrontend ansvarFrontend : forsorgerpliktFrontend.brukerregistrertAnsvar){
                JsonAnsvar jsonAnsvar = new JsonAnsvar()
                        .withBorSammenMed(ansvarFrontend.borSammenMed == null ? null :
                                new JsonBorSammenMed().withKilde(JsonKildeBruker.BRUKER).withVerdi(ansvarFrontend.borSammenMed))
                        .withSamvarsgrad(ansvarFrontend.samvarsgrad == null ? null :
                                new JsonSamvarsgrad().withKilde(JsonKildeBruker.BRUKER).withVerdi(ansvarFrontend.samvarsgrad))
                        .withBarn(new JsonBarn().withKilde(JsonKilde.BRUKER)
                                .withNavn(mapToJsonNavn(ansvarFrontend.barn.navn))
                                .withFodselsdato(ansvarFrontend.barn.fodselsdato));
                brukerregistrertAnsvar.add(jsonAnsvar);
            }
        } else {
            if (forsorgerplikt.getHarForsorgerplikt() != null && forsorgerplikt.getHarForsorgerplikt().getKilde().equals(JsonKilde.BRUKER)) {
                forsorgerplikt.setHarForsorgerplikt(new JsonHarForsorgerplikt().withKilde(JsonKilde.SYSTEM).withVerdi(false));
                removeBarneutgifterFromSoknad(soknad);
            }
        }

        systemAnsvar.addAll(brukerregistrertAnsvar);
        forsorgerplikt.setAnsvar(systemAnsvar.isEmpty()? null : systemAnsvar);
    }

    private void removeBarneutgifterFromSoknad(SoknadUnderArbeid soknad) {
        JsonOkonomi okonomi = soknad.getJsonInternalSoknad().getSoknad().getData().getOkonomi();
        List<JsonOkonomiOpplysningUtgift> opplysningerBarneutgifter = okonomi.getOpplysninger().getUtgift();
        List<JsonOkonomioversiktUtgift> oversiktBarneutgifter = okonomi.getOversikt().getUtgift();

        okonomi.getOpplysninger().getBekreftelse().removeIf(bekreftelse -> bekreftelse.getType().equals("barneutgifter"));
        removeUtgiftIfPresentInOversikt(oversiktBarneutgifter, "barnehage");
        removeUtgiftIfPresentInOversikt(oversiktBarneutgifter, "sfo");
        removeUtgiftIfPresentInOpplysninger(opplysningerBarneutgifter, "barnFritidsaktiviteter");
        removeUtgiftIfPresentInOpplysninger(opplysningerBarneutgifter, "barnTannregulering");
        removeUtgiftIfPresentInOpplysninger(opplysningerBarneutgifter, "annenBarneutgift");
    }

    private ForsorgerpliktFrontend mapToForsorgerpliktFrontend(JsonForsorgerplikt jsonForsorgerplikt) {
        List<AnsvarFrontend> ansvar = jsonForsorgerplikt.getAnsvar() == null ? null :
                jsonForsorgerplikt.getAnsvar().stream().filter(jsonAnsvar -> jsonAnsvar.getBarn().getKilde().equals(JsonKilde.SYSTEM)).map(this::mapToAnsvarFrontend)
                        .collect(Collectors.toList());
        List<AnsvarFrontend> brukerregistrertAnsvar = jsonForsorgerplikt.getAnsvar() == null ? null :
                jsonForsorgerplikt.getAnsvar().stream().filter(jsonAnsvar -> jsonAnsvar.getBarn().getKilde().equals(JsonKilde.BRUKER)).map(this::mapToAnsvarFrontend)
                        .collect(Collectors.toList());
        return new ForsorgerpliktFrontend()
                .withHarForsorgerplikt(jsonForsorgerplikt.getHarForsorgerplikt() == null ? null :
                        jsonForsorgerplikt.getHarForsorgerplikt().getVerdi())
                .withBarnebidrag(jsonForsorgerplikt.getBarnebidrag() == null ? null :
                        jsonForsorgerplikt.getBarnebidrag().getVerdi())
                .withAnsvar(ansvar)
                .withBrukerregistrertAnsvar(brukerregistrertAnsvar);
    }

    private AnsvarFrontend mapToAnsvarFrontend(JsonAnsvar jsonAnsvar) {
        if (jsonAnsvar == null){
            return null;
        }

        return new AnsvarFrontend().withBarn(mapToBarnFrontend(jsonAnsvar.getBarn()))
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
        public List<AnsvarFrontend> brukerregistrertAnsvar;

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

        public ForsorgerpliktFrontend withBrukerregistrertAnsvar(List<AnsvarFrontend> brukerregistrertAnsvar) {
            this.brukerregistrertAnsvar = brukerregistrertAnsvar;
            return this;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class AnsvarFrontend {
        public BarnFrontend barn;
        public Boolean borSammenMed;
        public Boolean erFolkeregistrertSammen;
        public Boolean harDeltBosted;
        public Integer samvarsgrad;

        public AnsvarFrontend withBarn(BarnFrontend barn) {
            this.barn = barn;
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
