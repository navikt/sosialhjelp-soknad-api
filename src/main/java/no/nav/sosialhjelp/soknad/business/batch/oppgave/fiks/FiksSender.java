package no.nav.sosialhjelp.soknad.business.batch.oppgave.fiks;

import no.finn.unleash.Unleash;
import no.ks.fiks.svarut.klient.model.Digitaladresse;
import no.ks.fiks.svarut.klient.model.ForsendelsesId;
import no.ks.fiks.svarut.klient.model.NoarkMetadataFraAvleverendeSaksSystem;
import no.ks.fiks.svarut.klient.model.UtskriftsKonfigurasjon;
import no.ks.svarut.servicesv9.Adresse;
import no.ks.svarut.servicesv9.Brevtype;
import no.ks.svarut.servicesv9.Dokument;
import no.ks.svarut.servicesv9.Forsendelse;
import no.ks.svarut.servicesv9.ForsendelsesServiceV9;
import no.ks.svarut.servicesv9.NoarkMetadataFraAvleverendeSakssystem;
import no.ks.svarut.servicesv9.OrganisasjonDigitalAdresse;
import no.ks.svarut.servicesv9.PostAdresse;
import no.ks.svarut.servicesv9.Printkonfigurasjon;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sosialhjelp.soknad.business.InnsendingService;
import no.nav.sosialhjelp.soknad.business.pdfmedpdfbox.SosialhjelpPdfGenerator;
import no.nav.sosialhjelp.soknad.consumer.fiks.DokumentKrypterer;
import no.nav.sosialhjelp.soknad.consumer.svarut.SvarUtService;
import no.nav.sosialhjelp.soknad.domain.SendtSoknad;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static no.nav.sosialhjelp.soknad.business.util.SenderUtils.createPrefixedBehandlingsIdInNonProd;

@Service
public class FiksSender {

    static final String SOKNAD_TIL_NAV = "Søknad til NAV";
    static final String ETTERSENDELSE_TIL_NAV = "Ettersendelse til NAV";

    private static final String IS_SVARUT_REST_ENABLED = "sosialhjelp.soknad.is-svarut-rest-enabled";

    private static final Logger log = LoggerFactory.getLogger(FiksSender.class);

    private final ForsendelsesServiceV9 forsendelsesService;
    private final InnsendingService innsendingService;
    private final FiksDokumentHelper fiksDokumentHelper;
    private final boolean krypteringEnabled;
    private final SvarUtService svarUtService;
    private final FiksDokumentHelperRest fiksDokumentHelperRest;
    private final Unleash unleash;

    public FiksSender(
            ForsendelsesServiceV9 forsendelsesService,
            DokumentKrypterer dokumentKrypterer,
            InnsendingService innsendingService,
            SosialhjelpPdfGenerator sosialhjelpPdfGenerator,
            @Value("${feature.fiks.kryptering.enabled}") boolean krypteringEnabled,
            SvarUtService svarUtService,
            Unleash unleash
    ) {
        this.forsendelsesService = forsendelsesService;
        this.innsendingService = innsendingService;
        this.krypteringEnabled = krypteringEnabled;
        this.fiksDokumentHelper = new FiksDokumentHelper(krypteringEnabled, dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator);
        this.svarUtService = svarUtService;
        this.fiksDokumentHelperRest = new FiksDokumentHelperRest(krypteringEnabled, dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator);
        this.unleash = unleash;
    }

    private final Printkonfigurasjon fakePrintConfig = new Printkonfigurasjon()
            .withBrevtype(Brevtype.APOST)
            .withFargePrint(true)
            .withTosidig(true);

    private final UtskriftsKonfigurasjon fakeUtskriftsConfig = new UtskriftsKonfigurasjon()
            .withUtskriftMedFarger(true)
            .withTosidig(true);

    public String sendTilFiks(SendtSoknad sendtSoknad) {
        if (unleash.isEnabled(IS_SVARUT_REST_ENABLED, false)) {
            try {
                return sendTilFiksRestservice(sendtSoknad);
            } catch (Exception e) {
                log.warn("Noe feilet ved sending til SvarUt Rest-tjeneste. Fallback til Soap-versjonen.", e);
                return sendTilFiksWebservice(sendtSoknad);
            }
        }
        return sendTilFiksWebservice(sendtSoknad);
    }

    public String sendTilFiksRestservice(SendtSoknad sendtSoknad) {
        var filnavnInputStreamMap = new HashMap<String, InputStream>();
        final var forsendelse = createForsendelse(sendtSoknad, filnavnInputStreamMap);
        return svarUtService.send(forsendelse, filnavnInputStreamMap);
    }

    public String sendTilFiksWebservice(SendtSoknad sendtSoknad) {
        PostAdresse fakeAdresse = new PostAdresse()
                .withNavn(sendtSoknad.getNavEnhetsnavn())
                .withPostnr("0000")
                .withPoststed("Ikke send");

        final Forsendelse forsendelse = opprettForsendelse(sendtSoknad, fakeAdresse);
        return forsendelsesService.sendForsendelse(forsendelse);
    }

    public Forsendelse opprettForsendelse(SendtSoknad sendtSoknad, PostAdresse fakeAdresse) {
        final SoknadUnderArbeid soknadUnderArbeid = innsendingService.hentSoknadUnderArbeid(sendtSoknad.getBehandlingsId(), sendtSoknad.getEier());
        final String svarPaForsendelseId = sendtSoknad.erEttersendelse() ?
                innsendingService.finnSendtSoknadForEttersendelse(soknadUnderArbeid).getFiksforsendelseId() : null;

        validerAtEttersendelseSinSoknadHarForsendelseId(sendtSoknad, svarPaForsendelseId);

        return new Forsendelse()
                .withMottaker(new Adresse()
                        .withDigitalAdresse(
                                new OrganisasjonDigitalAdresse().withOrgnr(sendtSoknad.getOrgnummer()))
                        .withPostAdresse(fakeAdresse))
                .withAvgivendeSystem("digisos_avsender")
                .withForsendelseType("nav.digisos")
                .withEksternref(createPrefixedBehandlingsIdInNonProd(sendtSoknad.getBehandlingsId()))
                .withTittel(sendtSoknad.erEttersendelse() ? ETTERSENDELSE_TIL_NAV : SOKNAD_TIL_NAV)
                .withKunDigitalLevering(false)
                .withPrintkonfigurasjon(fakePrintConfig)
                .withKryptert(krypteringEnabled)
                .withKrevNiva4Innlogging(krypteringEnabled)
                .withSvarPaForsendelse(svarPaForsendelseId)
                .withDokumenter(hentDokumenterFraSoknad(soknadUnderArbeid))
                .withMetadataFraAvleverendeSystem(
                        new NoarkMetadataFraAvleverendeSakssystem()
                                .withDokumentetsDato(sendtSoknad.getBrukerFerdigDato())
                );
    }

    private void validerAtEttersendelseSinSoknadHarForsendelseId(SendtSoknad sendtSoknad, String svarPaForsendelseId) {
        if (sendtSoknad.erEttersendelse() && (svarPaForsendelseId == null || svarPaForsendelseId.isEmpty())) {
            throw new IllegalStateException("Ettersendelse med behandlingsId " + sendtSoknad.getBehandlingsId() +
                    " er knyttet til en søknad med behandlingsId " + sendtSoknad.getTilknyttetBehandlingsId() +
                    " som ikke har mottat fiksForsendelseId. Innsending til SvarUt vil feile nå og bli forsøkt på nytt senere.");
        }
    }

    List<Dokument> hentDokumenterFraSoknad(SoknadUnderArbeid soknadUnderArbeid) {
        final JsonInternalSoknad internalSoknad = soknadUnderArbeid.getJsonInternalSoknad();
        if (internalSoknad == null) {
            throw new RuntimeException("Kan ikke sende forsendelse til FIKS fordi søknad mangler");
        } else if (!soknadUnderArbeid.erEttersendelse() && internalSoknad.getSoknad() == null) {
            throw new RuntimeException("Kan ikke sende søknad fordi søknaden mangler");
        } else if (soknadUnderArbeid.erEttersendelse() && internalSoknad.getVedlegg() == null) {
            throw new RuntimeException("Kan ikke sende ettersendelse fordi vedlegg mangler");
        }

        // TODO: 2019-11-25 pcn: Denne er her midlertidig for å fange opp søknader som er started før bostøtte ble rullet ut...
        if (!soknadUnderArbeid.erEttersendelse()) {
            if (internalSoknad.getSoknad().getData().getOkonomi().getOpplysninger().getBostotte() == null) {
                internalSoknad.getSoknad().getDriftsinformasjon().setStotteFraHusbankenFeilet(true);
            }
        }

        List<Dokument> fiksDokumenter = new ArrayList<>();
        int antallVedleggForsendelse;
        if (soknadUnderArbeid.erEttersendelse()) {
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForEttersendelsePdf(internalSoknad, soknadUnderArbeid.getEier()));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForVedleggJson(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForBrukerkvitteringPdf());
            List<Dokument> dokumenterForVedlegg = fiksDokumentHelper.lagDokumentListeForVedlegg(soknadUnderArbeid);
            antallVedleggForsendelse = dokumenterForVedlegg.size();
            fiksDokumenter.addAll(dokumenterForVedlegg);
        } else {
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForSoknadJson(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForSaksbehandlerPdf(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForVedleggJson(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForJuridiskPdf(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForBrukerkvitteringPdf());
            List<Dokument> dokumenterForVedlegg = fiksDokumentHelper.lagDokumentListeForVedlegg(soknadUnderArbeid);
            antallVedleggForsendelse = dokumenterForVedlegg.size();
            fiksDokumenter.addAll(dokumenterForVedlegg);
        }

        int antallFiksDokumenter = fiksDokumenter.size();
        log.info("Antall vedlegg: {}. Antall vedlegg lastet opp av bruker: {}", antallFiksDokumenter, antallVedleggForsendelse);

        try {
            List<JsonVedlegg> opplastedeVedleggstyper = internalSoknad.getVedlegg().getVedlegg().stream().filter(jsonVedlegg -> jsonVedlegg.getStatus().equals("LastetOpp"))
                    .collect(Collectors.toList());
            int antallBrukerOpplastedeVedlegg = 0;
            for (JsonVedlegg vedlegg : opplastedeVedleggstyper){
                antallBrukerOpplastedeVedlegg += vedlegg.getFiler().size();
            }
            if (antallVedleggForsendelse != antallBrukerOpplastedeVedlegg) {
                log.warn("Ulikt antall vedlegg i vedlegg.json og forsendelse til Fiks. vedlegg.json: {}, forsendelse til Fiks: {}. Er ettersendelse: {}", antallBrukerOpplastedeVedlegg, antallVedleggForsendelse, soknadUnderArbeid.erEttersendelse());
            }
        } catch (RuntimeException e) {
            log.debug("Ignored exception");
        }

        return fiksDokumenter;
    }

    private no.ks.fiks.svarut.klient.model.Forsendelse createForsendelse(SendtSoknad sendtSoknad, Map<String, InputStream> map) {
        final var soknadUnderArbeid = innsendingService.hentSoknadUnderArbeid(sendtSoknad.getBehandlingsId(), sendtSoknad.getEier());
        final var svarPaForsendelseId = sendtSoknad.erEttersendelse() ?
                new ForsendelsesId().withId(UUID.fromString(innsendingService.finnSendtSoknadForEttersendelse(soknadUnderArbeid).getFiksforsendelseId())) : null;

        final var fakeAdresse = new no.ks.fiks.svarut.klient.model.PostAdresse()
                .withNavn(sendtSoknad.getNavEnhetsnavn())
                .withPostNummer("0000")
                .withPostSted("Ikke send");

        validerAtEttersendelseSinSoknadHarForsendelseId(sendtSoknad, svarPaForsendelseId);

        return new no.ks.fiks.svarut.klient.model.Forsendelse()
                .withMottaker(
                        new no.ks.fiks.svarut.klient.model.Adresse()
                                .withDigitalAdresse(
                                        new Digitaladresse().withOrganisasjonsNummer(sendtSoknad.getOrgnummer())
                                )
                                .withPostAdresse(fakeAdresse)
                )
                .withAvgivendeSystem("digisos_avsender")
                .withForsendelsesType("nav.digisos")
                .withEksternReferanse(createPrefixedBehandlingsIdInNonProd(sendtSoknad.getBehandlingsId()))
                .withTittel(sendtSoknad.erEttersendelse() ? ETTERSENDELSE_TIL_NAV : SOKNAD_TIL_NAV)
                .withKunDigitalLevering(false)
                .withUtskriftsKonfigurasjon(fakeUtskriftsConfig)
                .withKryptert(krypteringEnabled)
                .withKrevNiva4Innlogging(krypteringEnabled)
                .withSvarPaForsendelse(svarPaForsendelseId)
                .withDokumenter(hentDokumenterFraSoknads(soknadUnderArbeid, map))
                .withMetadataFraAvleverendeSystem(
                        new NoarkMetadataFraAvleverendeSaksSystem()
                                .withDokumentetsDato(Date.valueOf(sendtSoknad.getBrukerFerdigDato().toLocalDate()))
                );
    }

    private void validerAtEttersendelseSinSoknadHarForsendelseId(SendtSoknad sendtSoknad, ForsendelsesId svarPaForsendelseId) {
        if (sendtSoknad.erEttersendelse() && (svarPaForsendelseId == null || svarPaForsendelseId.getId() == null || svarPaForsendelseId.getId().toString().isEmpty())) {
            throw new IllegalStateException("Ettersendelse med behandlingsId " + sendtSoknad.getBehandlingsId() +
                    " er knyttet til en søknad med behandlingsId " + sendtSoknad.getTilknyttetBehandlingsId() +
                    " som ikke har mottat fiksForsendelseId. Innsending til SvarUt vil feile nå og bli forsøkt på nytt senere.");
        }
    }

    private List<no.ks.fiks.svarut.klient.model.Dokument> hentDokumenterFraSoknads(SoknadUnderArbeid soknadUnderArbeid, Map<String, InputStream> map) {
        final JsonInternalSoknad internalSoknad = soknadUnderArbeid.getJsonInternalSoknad();
        if (internalSoknad == null) {
            throw new RuntimeException("Kan ikke sende forsendelse til FIKS fordi søknad mangler");
        } else if (!soknadUnderArbeid.erEttersendelse() && internalSoknad.getSoknad() == null) {
            throw new RuntimeException("Kan ikke sende søknad fordi søknaden mangler");
        } else if (soknadUnderArbeid.erEttersendelse() && internalSoknad.getVedlegg() == null) {
            throw new RuntimeException("Kan ikke sende ettersendelse fordi vedlegg mangler");
        }

        List<no.ks.fiks.svarut.klient.model.Dokument> fiksDokumenter = new ArrayList<>();
        int antallVedleggForsendelse;
        if (soknadUnderArbeid.erEttersendelse()) {
            fiksDokumenter.add(fiksDokumentHelperRest.lagDokumentForEttersendelsePdf(internalSoknad, soknadUnderArbeid.getEier(), map));
            fiksDokumenter.add(fiksDokumentHelperRest.lagDokumentForVedleggJson(internalSoknad, map));
            fiksDokumenter.add(fiksDokumentHelperRest.lagDokumentForBrukerkvitteringPdf(map));
            List<no.ks.fiks.svarut.klient.model.Dokument> dokumenterForVedlegg = fiksDokumentHelperRest.lagDokumentListeForVedlegg(soknadUnderArbeid, map);
            antallVedleggForsendelse = dokumenterForVedlegg.size();
            fiksDokumenter.addAll(dokumenterForVedlegg);
        } else {
            fiksDokumenter.add(fiksDokumentHelperRest.lagDokumentForSoknadJson(internalSoknad, map));
            fiksDokumenter.add(fiksDokumentHelperRest.lagDokumentForSaksbehandlerPdf(internalSoknad, map));
            fiksDokumenter.add(fiksDokumentHelperRest.lagDokumentForVedleggJson(internalSoknad, map));
            fiksDokumenter.add(fiksDokumentHelperRest.lagDokumentForJuridiskPdf(internalSoknad, map));
            fiksDokumenter.add(fiksDokumentHelperRest.lagDokumentForBrukerkvitteringPdf(map));
            List<no.ks.fiks.svarut.klient.model.Dokument> dokumenterForVedlegg = fiksDokumentHelperRest.lagDokumentListeForVedlegg(soknadUnderArbeid, map);
            antallVedleggForsendelse = dokumenterForVedlegg.size();
            fiksDokumenter.addAll(dokumenterForVedlegg);
        }

        int antallFiksDokumenter = fiksDokumenter.size();
        log.info("Antall vedlegg: {}. Antall vedlegg lastet opp av bruker: {}", antallFiksDokumenter, antallVedleggForsendelse);

        try {
            List<JsonVedlegg> opplastedeVedleggstyper = internalSoknad.getVedlegg().getVedlegg().stream().filter(jsonVedlegg -> jsonVedlegg.getStatus().equals("LastetOpp"))
                    .collect(Collectors.toList());
            int antallBrukerOpplastedeVedlegg = 0;
            for (JsonVedlegg vedlegg : opplastedeVedleggstyper){
                antallBrukerOpplastedeVedlegg += vedlegg.getFiler().size();
            }
            if (antallVedleggForsendelse != antallBrukerOpplastedeVedlegg) {
                log.warn("Ulikt antall vedlegg i vedlegg.json og forsendelse til Fiks. vedlegg.json: {}, forsendelse til Fiks: {}. Er ettersendelse: {}", antallBrukerOpplastedeVedlegg, antallVedleggForsendelse, soknadUnderArbeid.erEttersendelse());
            }
        } catch (RuntimeException e) {
            log.debug("Ignored exception");
        }

        return fiksDokumenter;
    }

}
