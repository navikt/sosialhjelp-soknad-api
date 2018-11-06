package no.nav.sbl.dialogarena.soknadinnsending.business.batch.oppgave.fiks;

import com.google.common.collect.ImmutableMap;
import no.ks.svarut.servicesv9.*;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fiks.DokumentKrypterer;
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.sosialhjelp.InnsendingService;
import no.nav.sbl.sosialhjelp.SoknadUnderArbeidService;
import no.nav.sbl.sosialhjelp.domain.*;
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

    @Inject
    private ForsendelsesServiceV9 forsendelsesService;

    @Inject
    private FillagerService fillager;

    @Inject
    private DokumentKrypterer dokumentKrypterer;

    @Inject
    private InnsendingService innsendingService;

    @Inject
    private SoknadUnderArbeidService soknadUnderArbeidService;

    private final Printkonfigurasjon fakePrintConfig = new Printkonfigurasjon()
            .withBrevtype(Brevtype.APOST)
            .withFargePrint(true)
            .withTosidig(true);

    public String sendTilFiks(FiksData data) {
        PostAdresse fakeAdresse = new PostAdresse()
                .withNavn(data.mottakerNavn)
                .withPostnr("0000")
                .withPoststed("Ikke send");

        final boolean skalKryptere = skalKryptere();

        final Forsendelse forsendelse = opprettForsendelse(data, fakeAdresse, skalKryptere);

        return forsendelsesService.sendForsendelse(forsendelse);
    }

    public String sendTilFiks(SendtSoknad sendtSoknad) {
        PostAdresse fakeAdresse = new PostAdresse()
                .withNavn(sendtSoknad.getNavEnhetsnavn())
                .withPostnr("0000")
                .withPoststed("Ikke send");

        final boolean skalKryptere = skalKryptere();

        final Forsendelse forsendelse = opprettForsendelse(sendtSoknad, fakeAdresse, skalKryptere);

        return forsendelsesService.sendForsendelse(forsendelse);
    }

    Forsendelse opprettForsendelse(FiksData data, PostAdresse fakeAdresse, boolean skalKryptere) {
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
                    .withKryptert(skalKryptere)
                    .withKrevNiva4Innlogging(skalKryptere)
                    .withSvarPaForsendelse(erNySoknad(data.ettersendelsePa) ? null : data.ettersendelsePa) // For ettersendelser
                    .withDokumenter(data.dokumentInfoer.stream()
                            .map(i -> fiksDokumentFraDokumentInfo(i, skalKryptere))
                            .collect(toList()))
                    .withMetadataFraAvleverendeSystem(
                            new NoarkMetadataFraAvleverendeSakssystem()
                                    .withDokumentetsDato(data.innsendtDato)
                    );
    }

    Forsendelse opprettForsendelse(SendtSoknad sendtSoknad, PostAdresse fakeAdresse, boolean skalKryptere) {
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
                .withKryptert(skalKryptere)
                .withKrevNiva4Innlogging(skalKryptere)
                .withSvarPaForsendelse(sendtSoknad.erEttersendelse() ?
                        innsendingService.finnSendtSoknadForEttersendelse(soknadUnderArbeid).getFiksforsendelseId() : null)
                .withDokumenter(hentDokumenterFraSoknad(soknadUnderArbeid, skalKryptere))
                .withMetadataFraAvleverendeSystem(
                        new NoarkMetadataFraAvleverendeSakssystem()
                                .withDokumentetsDato(sendtSoknad.getBrukerFerdigDato())
                );
    }

    Collection<Dokument> hentDokumenterFraSoknad(SoknadUnderArbeid soknadUnderArbeid, boolean skalKryptere) {
        List<OpplastetVedlegg> opplastedeVedlegg = innsendingService.hentAlleOpplastedeVedleggForSoknad(soknadUnderArbeid);
        //hent vedlegg fra json
        JsonInternalSoknad soknad = soknadUnderArbeidService.hentJsonInternalSoknadFraSoknadUnderArbeid(soknadUnderArbeid);//flytte til service?

        if (soknadUnderArbeid.erEttersendelse()) {
/*            infoer.add(leggTilEttersendelsePdf(metadata.hovedskjema));
            infoer.add(leggTilVedleggJson(metadata.hovedskjema));
            infoer.addAll(leggTilVedlegg(metadata.vedlegg));*/
        } else {
/*            infoer.add(leggTilSoknadJson(metadata.hovedskjema));
            infoer.add(leggTilPdf(metadata.hovedskjema));
            infoer.add(leggTilVedleggJson(metadata.hovedskjema));
            infoer.add(leggTilJuridiskPdf(metadata.hovedskjema));
            infoer.addAll(leggTilVedlegg(metadata.vedlegg));*/
        }

        return null;
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

    public Dokument fiksDokumentFraDokumentInfo(FiksData.DokumentInfo info, boolean skalKryptere) {
        byte[] filData = fillager.hentFil(info.uuid);

        final String filnavn = FILNAVN_MAPPER.containsKey(info.filnavn) ? FILNAVN_MAPPER.get(info.filnavn) : info.filnavn;

        if (skalKryptere) {
            filData = dokumentKrypterer.krypterData(filData);
        }

        ByteDataSource dataSource = new ByteDataSource(filData);
        dataSource.setName(filnavn);
        dataSource.setContentType("application/octet-stream");

        return new Dokument()
                .withFilnavn(filnavn)
                .withMimetype(info.mimetype != null ? info.mimetype : "application/pdf")
                .withEkskluderesFraPrint(info.ekskluderesFraPrint)
                .withData(new DataHandler(dataSource));
    }

    private boolean skalKryptere() {
        return !Boolean.valueOf(System.getProperty(KRYPTERING_DISABLED, "false"));
    }

    private static final Map<String, String> FILNAVN_MAPPER = new ImmutableMap.Builder<String, String>()
            .put("L7", "Brukerkvittering.pdf")
            .build();
}
