package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.ks.svarut.servicesv9.*;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.pdf.PDFService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class FiksSender {

    static final String SOKNAD_TIL_NAV = "Søknad til NAV";
    static final String ETTERSENDELSE_TIL_NAV = "Ettersendelse til NAV";
    public static String KRYPTERING_DISABLED = "feature.fiks.kryptering.disabled";
    private boolean SKAL_KRYPTERE = !Boolean.valueOf(System.getProperty(KRYPTERING_DISABLED, "false"));

    private ForsendelsesServiceV9 forsendelsesService;
    private InnsendingService innsendingService;
    private FiksDokumentHelper fiksDokumentHelper;

    @Inject
    public FiksSender(ForsendelsesServiceV9 forsendelsesService, DokumentKrypterer dokumentKrypterer,
                      InnsendingService innsendingService, PDFService pdfService) {
        this.forsendelsesService = forsendelsesService;
        this.innsendingService = innsendingService;
        this.fiksDokumentHelper = new FiksDokumentHelper(SKAL_KRYPTERE, dokumentKrypterer, innsendingService, pdfService);
    }

    private final Printkonfigurasjon fakePrintConfig = new Printkonfigurasjon()
            .withBrevtype(Brevtype.APOST)
            .withFargePrint(true)
            .withTosidig(true);

    public String sendTilFiks(SendtSoknad sendtSoknad) {
        PostAdresse fakeAdresse = new PostAdresse()
                .withNavn(sendtSoknad.getNavEnhetsnavn())
                .withPostnr("0000")
                .withPoststed("Ikke send");

        Forsendelse forsendelse = opprettForsendelse(sendtSoknad, fakeAdresse);
        return forsendelsesService.sendForsendelse(forsendelse);
    }

    Forsendelse opprettForsendelse(SendtSoknad sendtSoknad, PostAdresse fakeAdresse) {
        SoknadUnderArbeid soknadUnderArbeid = innsendingService.hentSoknadUnderArbeid(sendtSoknad.getBehandlingsId(), sendtSoknad.getEier());
        return new Forsendelse()
                .withMottaker(new Adresse()
                        .withDigitalAdresse(
                                new OrganisasjonDigitalAdresse().withOrgnr(sendtSoknad.getOrgnummer()))
                        .withPostAdresse(fakeAdresse))
                .withAvgivendeSystem("digisos_avsender")
                .withForsendelseType("nav.digisos")
                .withEksternref(environmentNameIfTest() + sendtSoknad.getBehandlingsId())
                .withTittel(sendtSoknad.erEttersendelse() ? ETTERSENDELSE_TIL_NAV : SOKNAD_TIL_NAV)
                .withKunDigitalLevering(false)
                .withPrintkonfigurasjon(fakePrintConfig)
                .withKryptert(SKAL_KRYPTERE)
                .withKrevNiva4Innlogging(SKAL_KRYPTERE)
                .withSvarPaForsendelse(sendtSoknad.erEttersendelse() ?
                        innsendingService.finnSendtSoknadForEttersendelse(soknadUnderArbeid).getFiksforsendelseId() : null)
                .withDokumenter(hentDokumenterFraSoknad(soknadUnderArbeid))
                .withMetadataFraAvleverendeSystem(
                        new NoarkMetadataFraAvleverendeSakssystem()
                                .withDokumentetsDato(sendtSoknad.getBrukerFerdigDato().toLocalDateTime())
                );
    }

    List<Dokument> hentDokumenterFraSoknad(SoknadUnderArbeid soknadUnderArbeid) {
        JsonInternalSoknad internalSoknad = soknadUnderArbeid.getJsonInternalSoknad();
        if (internalSoknad == null) {
            throw new RuntimeException("Kan ikke sende forsendelse til FIKS fordi søknad mangler");
        } else if (!soknadUnderArbeid.erEttersendelse() && internalSoknad.getSoknad() == null) {
            throw new RuntimeException("Kan ikke sende søknad fordi søknaden mangler");
        } else if (soknadUnderArbeid.erEttersendelse() && internalSoknad.getVedlegg() == null) {
            throw new RuntimeException("Kan ikke sende ettersendelse fordi vedlegg mangler");
        }

        List<Dokument> fiksDokumenter = new ArrayList<>();
        if (soknadUnderArbeid.erEttersendelse()) {
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForEttersendelsePdf(internalSoknad, soknadUnderArbeid.getEier()));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForVedleggJson(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForBrukerkvitteringPdf(internalSoknad, true, soknadUnderArbeid.getEier()));
            fiksDokumenter.addAll(fiksDokumentHelper.lagDokumentListeForVedlegg(soknadUnderArbeid));
        } else {
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForSoknadJson(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForSaksbehandlerPdf(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForVedleggJson(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForJuridiskPdf(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForBrukerkvitteringPdf(internalSoknad, false, soknadUnderArbeid.getEier()));
            fiksDokumenter.addAll(fiksDokumentHelper.lagDokumentListeForVedlegg(soknadUnderArbeid));
        }

        return fiksDokumenter;
    }

    private String environmentNameIfTest() {
        String environment = System.getProperty("environment.name");
        if (environment == null || "p".equals(environment)) {
            return "";
        }
        return environment + "-";
    }
}
