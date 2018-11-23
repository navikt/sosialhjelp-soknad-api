package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import com.google.common.collect.ImmutableMap;
import no.ks.svarut.servicesv9.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.domain.SendtSoknad;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;
import no.nav.sbl.sosialhjelp.pdf.PDFService;
import org.apache.cxf.attachment.ByteDataSource;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.inject.Inject;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
public class FiksSender {

    static final String SOKNAD_TIL_NAV = "Søknad til NAV";
    static final String ETTERSENDELSE_TIL_NAV = "Ettersendelse til NAV";
    public static String KRYPTERING_DISABLED = "feature.fiks.kryptering.disabled";
    private boolean SKAL_KRYPTERE = !Boolean.valueOf(System.getProperty(KRYPTERING_DISABLED, "false"));

    private ForsendelsesServiceV9 forsendelsesService;
    private FillagerService fillager;
    private DokumentKrypterer dokumentKrypterer;
    private InnsendingService innsendingService;
    private PDFService pdfService;
    private FiksDokumentHelper fiksDokumentHelper;

    @Inject
    public FiksSender(ForsendelsesServiceV9 forsendelsesService, FillagerService fillager, DokumentKrypterer dokumentKrypterer,
                      InnsendingService innsendingService, PDFService pdfService) {
        this.forsendelsesService = forsendelsesService;
        this.fillager = fillager;
        this.dokumentKrypterer = dokumentKrypterer;
        this.innsendingService = innsendingService;
        this.pdfService = pdfService;
        this.fiksDokumentHelper = new FiksDokumentHelper(SKAL_KRYPTERE, dokumentKrypterer, innsendingService, pdfService);
    }

    private final Printkonfigurasjon fakePrintConfig = new Printkonfigurasjon()
            .withBrevtype(Brevtype.APOST)
            .withFargePrint(true)
            .withTosidig(true);

    public String sendTilFiks(FiksData data) {
        PostAdresse fakeAdresse = new PostAdresse()
                .withNavn(data.mottakerNavn)
                .withPostnr("0000")
                .withPoststed("Ikke send");

        final Forsendelse forsendelse = opprettForsendelse(data, fakeAdresse);

        return forsendelsesService.sendForsendelse(forsendelse);
    }

    public String sendTilFiks(SendtSoknad sendtSoknad) {
        PostAdresse fakeAdresse = new PostAdresse()
                .withNavn(sendtSoknad.getNavEnhetsnavn())
                .withPostnr("0000")
                .withPoststed("Ikke send");

        final Forsendelse forsendelse = opprettForsendelse(sendtSoknad, fakeAdresse);
        return forsendelsesService.sendForsendelse(forsendelse);
    }

    Forsendelse opprettForsendelse(FiksData data, PostAdresse fakeAdresse) {
        return new Forsendelse()
                    .withMottaker(new Adresse()
                            .withDigitalAdresse(
                                    new OrganisasjonDigitalAdresse().withOrgnr(data.mottakerOrgNr))
                            .withPostAdresse(fakeAdresse))
                    .withAvgivendeSystem("digisos_avsender")
                    .withForsendelseType("nav.digisos")
                    .withEksternref(environmentNameIfTest() + data.behandlingsId)
                    .withTittel(erNySoknad(data.ettersendelsePa) ? SOKNAD_TIL_NAV : ETTERSENDELSE_TIL_NAV)
                    .withKunDigitalLevering(false)
                    .withPrintkonfigurasjon(fakePrintConfig)
                .withKryptert(SKAL_KRYPTERE)
                .withKrevNiva4Innlogging(SKAL_KRYPTERE)
                    .withSvarPaForsendelse(erNySoknad(data.ettersendelsePa) ? null : data.ettersendelsePa) // For ettersendelser
                    .withDokumenter(data.dokumentInfoer.stream()
                            .map(i -> fiksDokumentFraDokumentInfo(i))
                            .collect(toList()))
                    .withMetadataFraAvleverendeSystem(
                            new NoarkMetadataFraAvleverendeSakssystem()
                                    .withDokumentetsDato(data.innsendtDato)
                    );
    }

    Forsendelse opprettForsendelse(SendtSoknad sendtSoknad, PostAdresse fakeAdresse) {
        final SoknadUnderArbeid soknadUnderArbeid = innsendingService.hentSoknadUnderArbeid(sendtSoknad.getBehandlingsId(), sendtSoknad.getEier());
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
                                .withDokumentetsDato(sendtSoknad.getBrukerFerdigDato())
                );
    }

    List<Dokument> hentDokumenterFraSoknad(SoknadUnderArbeid soknadUnderArbeid) {
        final JsonInternalSoknad internalSoknad = innsendingService.hentJsonInternalSoknadFraSoknadUnderArbeid(soknadUnderArbeid);
        if (internalSoknad == null) {
            throw new RuntimeException("Kan ikke sende forsendelse til FIKS fordi søknad mangler");
        } else if (!soknadUnderArbeid.erEttersendelse() && internalSoknad.getSoknad() == null) {
            throw new RuntimeException("Kan ikke sende søknad fordi søknaden mangler");
        } else if (soknadUnderArbeid.erEttersendelse() && internalSoknad.getVedlegg() == null) {
            throw new RuntimeException("Kan ikke sende ettersendelse fordi vedlegg mangler");
        }

        List<Dokument> fiksDokumenter = new ArrayList<>();
        if (soknadUnderArbeid.erEttersendelse()) {
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForEttersendelsePdf(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForVedleggJson(internalSoknad));
            fiksDokumenter.addAll(fiksDokumentHelper.lagDokumentListeForVedlegg(soknadUnderArbeid));
        } else {
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForSoknadJson(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForSaksbehandlerPdf(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForVedleggJson(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForJuridiskPdf(internalSoknad));
            fiksDokumenter.add(fiksDokumentHelper.lagDokumentForBrukerkvitteringPdf(internalSoknad, false));
            fiksDokumenter.addAll(fiksDokumentHelper.lagDokumentListeForVedlegg(soknadUnderArbeid));
        }

        return fiksDokumenter;
    }

    private boolean erNySoknad(String ettersendelsePa) {
        return isEmpty(ettersendelsePa);
    }

    private String environmentNameIfTest() {
        final String environment = System.getProperty("environment.name");
        if (environment == null || "p".equals(environment)) {
            return "";
        }
        return environment + "-";
    }

    public Dokument fiksDokumentFraDokumentInfo(FiksData.DokumentInfo info) {
        byte[] filData = fillager.hentFil(info.uuid);

        final String filnavn = FILNAVN_MAPPER.containsKey(info.filnavn) ? FILNAVN_MAPPER.get(info.filnavn) : info.filnavn;

        ByteDataSource dataSource = fiksDokumentHelper.krypterOgOpprettByteDatasource(filnavn, filData);
        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(info.mimetype != null ? info.mimetype : "application/pdf")
                .withEkskluderesFraPrint(info.ekskluderesFraPrint)
                .withData(new DataHandler(dataSource));
    }

    private static final Map<String, String> FILNAVN_MAPPER = new ImmutableMap.Builder<String, String>()
            .put("L7", "Brukerkvittering.pdf")
            .build();
}
