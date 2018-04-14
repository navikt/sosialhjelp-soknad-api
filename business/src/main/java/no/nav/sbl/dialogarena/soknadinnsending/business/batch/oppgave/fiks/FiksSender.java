package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import no.ks.svarut.servicesv9.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import org.apache.cxf.attachment.ByteDataSource;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.inject.Inject;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Service
public class FiksSender {

    public static String KRYPTERING_DISABLED = "feature.fiks.kryptering.disabled";

    @Inject
    private ForsendelsesServiceV9 forsendelsesService;

    @Inject
    private FillagerService fillager;

    @Inject
    private DokumentKrypterer dokumentKrypterer;

    private final Printkonfigurasjon fakePrintConfig = new Printkonfigurasjon()
            .withBrevtype(Brevtype.APOST)
            .withFargePrint(true)
            .withTosidig(true);

    public String sendTilFiks(FiksData data) {
        PostAdresse fakeAdresse = new PostAdresse()
                .withNavn(data.mottakerNavn)
                .withPostnr("0000")
                .withPoststed("Ikke send");

        boolean skalKryptere = skalKryptere();

        Forsendelse forsendelse = new Forsendelse()
                .withMottaker(new Adresse()
                        .withDigitalAdresse(
                                new OrganisasjonDigitalAdresse().withOrgnr(data.mottakerOrgNr))
                        .withPostAdresse(fakeAdresse))
                .withAvgivendeSystem("digisos_avsender")
                .withForsendelseType("nav.digisos")
                .withEksternref(data.behandlingsId)
                .withTittel("Søknad til NAV")
                .withKunDigitalLevering(false)
                .withPrintkonfigurasjon(fakePrintConfig)
                .withKryptert(skalKryptere)
                .withKrevNiva4Innlogging(skalKryptere)
                .withSvarPaForsendelse(isEmpty(data.ettersendelsePa) ? null : data.ettersendelsePa) // For ettersendelser
                .withDokumenter(data.dokumentInfoer.stream()
                        .map(i -> fiksDokumentFraDokumentInfo(i, skalKryptere))
                        .collect(toList()))
                .withMetadataFraAvleverendeSystem(
                        new NoarkMetadataFraAvleverendeSakssystem()
                                .withDokumentetsDato(data.innsendtDato)
                );

        return forsendelsesService.sendForsendelse(forsendelse);
    }

    public Dokument fiksDokumentFraDokumentInfo(FiksData.DokumentInfo info, boolean skalKryptere) {
        byte[] filData = fillager.hentFil(info.uuid);

        if (skalKryptere) {
            filData = dokumentKrypterer.krypterData(filData);
        }

        ByteDataSource dataSource = new ByteDataSource(filData);
        dataSource.setName(info.filnavn);
        dataSource.setContentType("application/octet-stream");

        return new Dokument()
                .withFilnavn(info.filnavn)
                .withMimetype(info.mimetype != null ? info.mimetype : "application/pdf")
                .withEkskluderesFraPrint(info.ekskluderesFraPrint)
                .withData(new DataHandler(dataSource));
    }

    private boolean skalKryptere() {
        return !Boolean.valueOf(System.getProperty(KRYPTERING_DISABLED, "false"));
    }
}
