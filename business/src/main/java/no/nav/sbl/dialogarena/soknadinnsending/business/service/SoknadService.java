package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLVedlegg;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.collections.iter.PreparedIterable;
import no.nav.modig.lang.collections.predicate.InstanceOf;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk;
import no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.Nokkel;
import no.nav.sbl.dialogarena.detect.Detect;
import no.nav.sbl.dialogarena.detect.pdf.PdfDetector;
import no.nav.sbl.dialogarena.pdf.Convert;
import no.nav.sbl.dialogarena.pdf.ConvertToPng;
import no.nav.sbl.dialogarena.pdf.PdfMerger;
import no.nav.sbl.dialogarena.pdf.PdfWatermarker;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknadId;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.OpplastingException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.SoknadAvbruttException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.SoknadAvsluttetException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.UgyldigOpplastingTypeException;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadStruktur;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.oppsett.SoknadVedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.message.NavMessageSource;
import no.nav.sbl.dialogarena.soknadinnsending.business.person.Personalia;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerConnector;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseConnector;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStatus;
import org.apache.commons.collections15.Closure;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.Splitter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static javax.xml.bind.JAXBContext.newInstance;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.LASTET_OPP;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.BRUKERREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.toInnsendingsvalg;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.WebSoknadUtils.getJournalforendeEnhet;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.WebSoknadUtils.getSkjemanummer;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class SoknadService implements SendSoknadService, VedleggService {
    private static final Logger logger = getLogger(SoknadService.class);
    @Inject
    @Named("soknadInnsendingRepository")
    private SoknadRepository repository;

    @Inject
    @Named("vedleggRepository")
    private VedleggRepository vedleggRepository;
    @Inject
    private HenvendelseConnector henvendelseConnector;
    @Inject
    private FillagerConnector fillagerConnector;
    @Inject
    private Kodeverk kodeverk;
    @Inject
    private NavMessageSource navMessageSource;

    private static final String EKSTRA_VEDLEGG_KEY = "ekstraVedlegg";

    private PdfWatermarker watermarker = new PdfWatermarker();
    private List<String> gyldigeSkjemaer = Arrays.asList("NAV 04-01.03");
    private PdfMerger pdfMerger = new PdfMerger();

    private static void sjekkOmPdfErGyldig(PDDocument document) {
        PdfDetector detector = new PdfDetector(document);
        if (detector.pdfIsSigned()) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være signert.", null,
                    "opplasting.feilmelding.pdf.signert");
        } else if (detector.pdfIsEncrypted()) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være krypert.", null,
                    "opplasting.feilmelding.pdf.krypert");
        } else if (detector.pdfIsSavedOrExportedWithApplePreview()) {
            throw new UgyldigOpplastingTypeException(
                    "PDF kan ikke være lagret med Apple Preview.", null,
                    "opplasting.feilmelding.pdf.applepreview");
        }
    }

    @Override
    public WebSoknad hentSoknadMetaData(long soknadId) {
        return repository.hentSoknad(soknadId);
    }

    @Override
    public void settDelsteg(Long soknadId, DelstegStatus delstegStatus) {
        repository.settDelstegstatus(soknadId, delstegStatus);
    }

    @Override
    public WebSoknad hentSoknad(long soknadId) {
        // TODO: Burde se på uthenting av vedlegg med navn
        WebSoknad soknad = repository.hentSoknadMedData(soknadId);
        List<Vedlegg> vedlegg = hentPaakrevdeVedlegg(soknadId);
        soknad.setVedlegg(vedlegg);
        return soknad;
    }

    @Override
    public String hentSoknadEier(Long soknadId) {
        return repository.hentSoknad(soknadId).getAktoerId();
    }

    @Override
    public Faktum lagreSoknadsFelt(Long soknadId, Faktum faktum) {
        faktum.setType(BRUKERREGISTRERT);
        faktum.setSoknadId(soknadId);
        Long faktumId = repository.lagreFaktum(soknadId, faktum);
        repository.settSistLagretTidspunkt(soknadId);

        settDelstegStatus(soknadId, faktum.getKey());

        Faktum resultat = repository.hentFaktum(soknadId, faktumId);
        genererVedleggForFaktum(resultat);
        on(repository.hentBarneFakta(soknadId, faktum.getFaktumId())).forEach(new Closure<Faktum>() {
            @Override
            public void execute(Faktum faktum) {
                genererVedleggForFaktum(faktum);
            }
        });

        return resultat;
    }

    @Override
    public void slettBrukerFaktum(Long soknadId, Long faktumId) {
        final Faktum faktum;
        try {
            faktum = repository.hentFaktum(soknadId, faktumId);
        } catch (IncorrectResultSizeDataAccessException e) {
            logger.info("Skipped delete bechause faktum does not exist.");
            return;
        }
        String faktumKey = faktum.getKey();
        List<Vedlegg> vedleggliste = vedleggRepository.hentVedleggForFaktum(soknadId, faktumId);

        for (Vedlegg vedlegg : vedleggliste) {
            vedleggRepository.slettVedleggOgData(soknadId, vedlegg.getFaktumId(), vedlegg.getSkjemaNummer());
        }
        repository.slettBrukerFaktum(soknadId, faktumId);
        settDelstegStatus(soknadId, faktumKey);
    }

    @Override
    public Long lagreSystemFaktum(Long soknadId, Faktum f, String uniqueProperty) {
        logger.debug("*** Lagrer systemfaktum ***: " + f.getKey());
        f.setType(SYSTEMREGISTRERT);
        List<Faktum> fakta = repository.hentSystemFaktumList(soknadId, f.getKey());

        if (!uniqueProperty.isEmpty()) {
            for (Faktum faktum : fakta) {
                if (faktum.matcherUnikProperty(uniqueProperty, f)) {
                    f.setFaktumId(faktum.getFaktumId());
                    Long lagretFaktumId = repository.lagreFaktum(soknadId, f, true);
                    Faktum hentetFaktum = repository.hentFaktum(soknadId, lagretFaktumId);
                    genererVedleggForFaktum(hentetFaktum);
                    return lagretFaktumId;
                }
            }
        }
        Long lagretFaktumId = repository.lagreFaktum(soknadId, f, true);
        Faktum hentetFaktum = repository.hentFaktum(soknadId, lagretFaktumId);
        genererVedleggForFaktum(hentetFaktum);
        return lagretFaktumId;
    }

    @Override
    public void sendSoknad(long soknadId, byte[] pdf) {
        WebSoknad soknad = hentSoknad(soknadId);
        fillagerConnector.lagreFil(soknad.getBrukerBehandlingId(), soknad.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(pdf));
        List<Vedlegg> vedleggForventnings = soknad.getVedlegg();
        String skjemanummer = getSkjemanummer(soknad);
        String journalforendeEnhet = getJournalforendeEnhet(soknad);
        XMLHovedskjema hovedskjema = new XMLHovedskjema()
                .withInnsendingsvalg(LASTET_OPP.toString())
                .withSkjemanummer(skjemanummer)
                .withFilnavn(skjemanummer)
                .withMimetype("application/pdf")
                .withFilstorrelse("" + pdf.length)
                .withUuid(soknad.getUuid())
                .withJournalforendeEnhet(journalforendeEnhet);
        henvendelseConnector.avsluttSoknad(soknad.getBrukerBehandlingId(),
                hovedskjema,
                Transformers.convertToXmlVedleggListe(vedleggForventnings));
        repository.slettSoknad(soknadId);
    }

    public Long hentSoknadMedBehandlingsId(String behandlingsId) {
        WebSoknad soknad = repository.hentMedBehandlingsId(behandlingsId);
        if (soknad == null) {
            populerFraHenvendelse(behandlingsId);
            soknad = repository.hentMedBehandlingsId(behandlingsId);
        }
        return soknad.getSoknadId();
    }

    private void settDelstegStatus(Long soknadId, String faktumKey) {
        //Setter delstegstatus dersom et faktum blir lagret, med mindre det er epost eller ekstra vedlegg. Bør gjøres mer elegant, litt quickfix
        if (!Personalia.EPOST_KEY.equals(faktumKey) && !EKSTRA_VEDLEGG_KEY.equals(faktumKey)) {
            repository.settDelstegstatus(soknadId, DelstegStatus.UTFYLLING);
        }
    }

    private void populerFraHenvendelse(String behandlingsId) {
        WSHentSoknadResponse wsSoknadsdata = henvendelseConnector.hentSoknad(behandlingsId);
        String soknadStatus = wsSoknadsdata.getStatus();
        if (!soknadStatus.equals(WSStatus.UNDER_ARBEID.value())) {
            if (WSStatus.AVBRUTT_AV_BRUKER.value().equals(soknadStatus)) {
                throw new SoknadAvbruttException("Soknaden er avbrutt", null, "soknad.avbrutt");
            } else if (WSStatus.FERDIG.value().equals(soknadStatus)) {
                throw new SoknadAvsluttetException("Soknaden er avsluttet", null, "soknad.avsluttet");
            }
            throw new RuntimeException();
        }
        XMLMetadataListe vedleggListe = (XMLMetadataListe) wsSoknadsdata.getAny();
        Optional<XMLMetadata> hovedskjema = on(vedleggListe.getMetadata()).filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class)).head();
        if (hovedskjema.isSome()) {
            byte[] bytes = fillagerConnector.hentFil(((XMLHovedskjema) hovedskjema.get()).getUuid());
            WebSoknad soknad = JAXB.unmarshal(new ByteArrayInputStream(bytes), WebSoknad.class);
            repository.populerFraStruktur(soknad);
            List<WSInnhold> innhold = fillagerConnector.hentFiler(soknad.getBrukerBehandlingId());
            populerVedleggMedDataFraHenvendelse(soknad, innhold);
        } else {
            throw new ApplicationException("Kunne ikke hente opp søknad");
        }
    }

    private void populerVedleggMedDataFraHenvendelse(WebSoknad soknad, List<WSInnhold> innhold) {
        for (WSInnhold wsInnhold : innhold) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                wsInnhold.getInnhold().writeTo(baos);
            } catch (IOException e) {
                throw new ApplicationException("Kunne ikke hente opp soknaddata", e);
            }
            Vedlegg vedlegg = soknad.hentVedleggMedUID(wsInnhold.getUuid());
            if (vedlegg != null) {
                vedlegg.setData(baos.toByteArray());
                vedleggRepository.lagreVedleggMedData(soknad.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
            }
        }
    }

    @Override
    public WebSoknad startEttersending(String behandingsId) {
        WSHentSoknadResponse wsSoknadsdata = henvendelseConnector.hentSisteBehandlingIBehandlingskjede(behandingsId);
        return lagEttersendingFraWsSoknad(wsSoknadsdata);
//        henvendelseConnector.startEttersending(wsSoknadsdata);
    }

    @Override
    public Long hentEttersendingForBehandlingskjedeId(String behandlingsId) {
        Optional<WebSoknad> soknad = repository.hentEttersendingMedBehandlingskjedeId(behandlingsId);
        if (soknad.isSome()) {
            return soknad.get().getSoknadId();
        } else {
            return null;
        }
    }


    @Override
    public void sendEttersending(Long soknadId, String behandingsId) {
        WSHentSoknadResponse opprinneligInnsending = henvendelseConnector.hentSisteBehandlingIBehandlingskjede(behandingsId);

        WebSoknad soknad = repository.hentSoknadMedData(soknadId);
        List<Vedlegg> vedleggForventnings = soknad.getVedlegg();

        XMLMetadataListe xmlMetaData = (XMLMetadataListe) opprinneligInnsending.getAny();
        Optional<XMLMetadata> hovedskjema = on(xmlMetaData.getMetadata()).filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class)).head();
        if (!hovedskjema.isSome()) {
            throw new ApplicationException("Kunne ikke hente opp hovedskjema for søknad");
        }
        XMLHovedskjema xmlHovedskjema = (XMLHovedskjema) hovedskjema.get();

        String ettersendingsBehandlingId = henvendelseConnector.startEttersending(opprinneligInnsending);

        henvendelseConnector.avsluttSoknad(ettersendingsBehandlingId,
                xmlHovedskjema,
                Transformers.convertToXmlVedleggListe(vedleggForventnings));

        repository.slettSoknad(soknad.getSoknadId());
    }


    private WebSoknad lagEttersendingFraWsSoknad(WSHentSoknadResponse wsSoknadsdata) {
        WebSoknad soknad = WebSoknad.startEttersending();
        String mainUid = randomUUID().toString();
        XMLMetadataListe xmlVedleggListe = (XMLMetadataListe) wsSoknadsdata.getAny();

        Optional<XMLMetadata> hovedskjema = on(xmlVedleggListe.getMetadata()).filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class)).head();
        if (!hovedskjema.isSome()) {
            throw new ApplicationException("Kunne ikke hente opp hovedskjema for søknad");
        }
        XMLHovedskjema xmlHovedskjema = (XMLHovedskjema) hovedskjema.get();

        soknad.medUuid(mainUid)
                .medAktorId(getSubjectHandler().getUid())
                .medskjemaNummer(xmlHovedskjema.getSkjemanummer())
                .medBehandlingskjedeId(wsSoknadsdata.getBehandlingsId());

        Long soknadId = repository.opprettSoknad(soknad);
        WebSoknadId websoknadId = new WebSoknadId();
        websoknadId.setId(soknadId);
        soknad.setSoknadId(soknadId);

        Faktum soknadInnsendingsDato = new Faktum()
                .medKey("soknadInnsendingsDato")
                .medValue(String.valueOf(wsSoknadsdata.getInnsendtDato().getMillis()))
                .medType(SYSTEMREGISTRERT);
        lagreSystemFaktum(soknadId, soknadInnsendingsDato, "");
        soknad.setFaktaListe(repository.hentAlleBrukerData(soknadId));

        soknad.setVedlegg(hentVedleggOgPersister(soknad, xmlVedleggListe, soknadId));

        return soknad;
    }

    private List<Vedlegg> hentVedleggOgPersister(WebSoknad soknad, XMLMetadataListe xmlVedleggListe, Long soknadId) {
        PreparedIterable<XMLMetadata> vedlegg = on(xmlVedleggListe.getMetadata()).filter(new InstanceOf<XMLMetadata>(XMLVedlegg.class));
        List<Vedlegg> soknadVedlegg = new ArrayList<>();
        for (XMLMetadata xmlMetadata : vedlegg) {
            if (xmlMetadata instanceof XMLHovedskjema) {
                continue;
            }
            XMLVedlegg xmlVedlegg = (XMLVedlegg) xmlMetadata;

            Integer antallSider = xmlVedlegg.getSideantall() != null ? xmlVedlegg.getSideantall() : 0;

            Vedlegg v = new Vedlegg()
                    .medSkjemaNummer(xmlVedlegg.getSkjemanummer())
                    .medAntallSider(antallSider)
                    .medInnsendingsvalg(toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
                    .medOpprinneligInnsendingsvalg(toInnsendingsvalg(xmlVedlegg.getInnsendingsvalg()))
                    .medSoknadId(soknadId)
                    .medNavn(xmlVedlegg.getTilleggsinfo());

            medKodeverk(v);
            vedleggRepository.opprettVedlegg(v, null);
            soknadVedlegg.add(v);
        }
        return soknadVedlegg;
    }

    @Override
    public void avbrytSoknad(Long soknadId) {
        WebSoknad soknad = repository.hentSoknad(soknadId);
        repository.avbryt(soknadId);
        henvendelseConnector.avbrytSoknad(soknad.getBrukerBehandlingId());
    }

    @Override
    public List<Faktum> hentFakta(Long soknadId) {
        return repository.hentAlleBrukerData(soknadId);
    }

    @Override
    public String startSoknad(String navSoknadId) {
        validerSkjemanummer(navSoknadId);

        String mainUid = randomUUID().toString();
        String behandlingsId = henvendelseConnector
                .startSoknad(getSubjectHandler().getUid(), navSoknadId, mainUid);
        WebSoknad soknad = WebSoknad.startSoknad()
                .medBehandlingId(behandlingsId).medskjemaNummer(navSoknadId)
                .medUuid(mainUid)
                .medAktorId(getSubjectHandler().getUid())
                .medOppretteDato(DateTime.now());

        Long soknadId = repository.opprettSoknad(soknad);
        WebSoknadId websoknadId = new WebSoknadId();
        websoknadId.setId(soknadId);

        List<String> bolker = Arrays.asList("reellarbeidssoker", "arbeidsforhold", "egennaering", "verneplikt", "utdanning", "ytelser", "personalia", "barnetillegg", "fritekst");
        Map<String, String> erBolkerValidert = new HashMap<>();
        for (String bolk : bolker) {
            erBolkerValidert.put(bolk, "false");
        }

        Faktum bolkerFaktum = new Faktum().medSoknadId(soknadId).medKey("bolker").medType(BRUKERREGISTRERT);
        bolkerFaktum.setProperties(erBolkerValidert);

        repository.lagreFaktum(soknadId, bolkerFaktum);

        return behandlingsId;
    }

    private void validerSkjemanummer(String navSoknadId) {
        if (!gyldigeSkjemaer.contains(navSoknadId)) {
            throw new ApplicationException("Ikke gyldig skjemanummer " + navSoknadId);
        }
    }

    @Override
    @Transactional
    public List<Long> splitOgLagreVedlegg(Vedlegg vedlegg,
                                          InputStream inputStream) {
        List<Long> resultat = new ArrayList<>();
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            if (Detect.isImage(bytes)) {
                bytes = Convert.scaleImageAndConvertToPdf(bytes, new Dimension(1240, 1754));

                Vedlegg sideVedlegg = new Vedlegg()
                        .medVedleggId(null)
                        .medSoknadId(vedlegg.getSoknadId())
                        .medFaktumId(vedlegg.getFaktumId())
                        .medSkjemaNummer(vedlegg.getSkjemaNummer())
                        .medNavn(vedlegg.getNavn())
                        .medStorrelse((long) bytes.length)
                        .medAntallSider(1)
                        .medFillagerReferanse(UUID.randomUUID().toString())
                        .medData(null)
                        .medOpprettetDato(vedlegg.getOpprettetDato())
                        .medInnsendingsvalg(Vedlegg.Status.UnderBehandling);

                resultat.add(vedleggRepository.opprettVedlegg(sideVedlegg,
                        bytes));

            } else if (Detect.isPdf(bytes)) {
                PDDocument document = PDDocument.load(new ByteArrayInputStream(
                        bytes));
                sjekkOmPdfErGyldig(document);
                List<PDDocument> pages = new Splitter().split(document);
                for (PDDocument page : pages) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    page.save(baos);
                    page.close();

                    Vedlegg sideVedlegg = new Vedlegg()
                            .medVedleggId(null)
                            .medSoknadId(vedlegg.getSoknadId())
                            .medFaktumId(vedlegg.getFaktumId())
                            .medSkjemaNummer(vedlegg.getSkjemaNummer())
                            .medNavn(vedlegg.getNavn())
                            .medStorrelse((long) baos.size())
                            .medAntallSider(1)
                            .medFillagerReferanse(UUID.randomUUID().toString())
                            .medData(null)
                            .medOpprettetDato(vedlegg.getOpprettetDato())
                            .medInnsendingsvalg(Vedlegg.Status.UnderBehandling);

                    resultat.add(vedleggRepository.opprettVedlegg(sideVedlegg,
                            baos.toByteArray()));
                }
                document.close();
            } else {
                throw new UgyldigOpplastingTypeException(
                        "Ugyldig filtype for opplasting", null,
                        "vedlegg.opplasting.feil.filtype");
            }
        } catch (IOException | COSVisitorException e) {
            throw new OpplastingException("Kunne ikke lagre fil", e,
                    "vedlegg.opplasting.feil.generell");
        }
        return resultat;
    }

    @Override
    public List<Vedlegg> hentVedleggUnderBehandling(Long soknadId,
                                                    Long faktumId, String skjemaNummer) {
        return vedleggRepository.hentVedleggUnderBehandling(soknadId, faktumId,
                skjemaNummer);
    }

    @Override
    public Vedlegg hentVedlegg(Long soknadId, Long vedleggId, boolean medInnhold) {
        if (medInnhold) {
            Vedlegg vedlegg = vedleggRepository.hentVedleggMedInnhold(soknadId, vedleggId);
            medKodeverk(vedlegg);
            return vedlegg;
        } else {
            Vedlegg vedlegg = vedleggRepository.hentVedlegg(soknadId, vedleggId);
            medKodeverk(vedlegg);
            return vedlegg;
        }
    }

    @Override
    public void slettVedlegg(Long soknadId, Long vedleggId) {
        vedleggRepository.slettVedlegg(soknadId, vedleggId);
        repository.settDelstegstatus(soknadId, DelstegStatus.SKJEMA_VALIDERT);
    }

    @Override
    public byte[] lagForhandsvisning(Long soknadId, Long vedleggId, int side) {
        return new ConvertToPng(new Dimension(600, 800), side).transform(vedleggRepository.hentVedleggData(soknadId, vedleggId));
    }

    @Override
    public Long genererVedleggFaktum(Long soknadId, Long vedleggId) {
        Vedlegg forventning = vedleggRepository
                .hentVedlegg(soknadId, vedleggId);
        List<Vedlegg> vedleggUnderBehandling = vedleggRepository
                .hentVedleggUnderBehandling(soknadId,
                        forventning.getFaktumId(),
                        forventning.getSkjemaNummer());
        List<byte[]> bytes = new ArrayList<>();
        for (Vedlegg vedlegg : vedleggUnderBehandling) {
            bytes.add(vedleggRepository.hentVedleggData(soknadId, vedlegg.getVedleggId()));
        }
        byte[] doc = pdfMerger.transform(bytes);
        doc = watermarker.forIdent(getSubjectHandler().getUid(), false).transform(doc);

        forventning.leggTilInnhold(doc, vedleggUnderBehandling.size());
        WebSoknad soknad = repository.hentSoknad(soknadId);
        fillagerConnector.lagreFil(soknad.getBrukerBehandlingId(),
                forventning.getFillagerReferanse(), soknad.getAktoerId(),
                new ByteArrayInputStream(doc));
        vedleggRepository.slettVedleggUnderBehandling(soknadId,
                forventning.getFaktumId(), forventning.getSkjemaNummer());
        vedleggRepository.lagreVedleggMedData(soknadId, vedleggId, forventning);
        return vedleggId;
    }

    @Override
    public List<Vedlegg> hentPaakrevdeVedlegg(Long soknadId) {
        List<Vedlegg> paakrevdeVedlegg = vedleggRepository.hentPaakrevdeVedlegg(soknadId);
        leggTilKodeverkFelter(paakrevdeVedlegg);
        return paakrevdeVedlegg;
    }

    @Override
    public SoknadStruktur hentSoknadStruktur(Long soknadId) {
        return hentStruktur(repository.hentSoknadType(soknadId));
    }

    private void genererVedleggForFaktum(Faktum faktum) {
        SoknadStruktur struktur = hentSoknadStruktur(faktum.getSoknadId());
        List<SoknadVedlegg> aktuelleVedlegg = struktur.vedleggFor(faktum.getKey());
        for (SoknadVedlegg soknadVedlegg : aktuelleVedlegg) {
            Vedlegg vedlegg = vedleggRepository.hentVedleggForskjemaNummer(faktum.getSoknadId(), soknadVedlegg.getFlereTillatt() ? faktum.getFaktumId() : null, soknadVedlegg.getSkjemaNummer());
            Faktum parentFaktum = faktum.getParrentFaktum() != null ? repository.hentFaktum(faktum.getSoknadId(), faktum.getParrentFaktum()) : null;
            if (soknadVedlegg.trengerVedlegg(faktum) && erParentAktiv(soknadVedlegg, parentFaktum)) {
                lagrePaakrevdVedlegg(faktum, soknadVedlegg, vedlegg);
            } else if (vedlegg != null && !erVedleggKrevdAvAnnetFaktum(faktum, struktur, soknadVedlegg)) { // sett vedleggsforventning til ikke paakrevd
                vedlegg.setInnsendingsvalg(Vedlegg.Status.IkkeVedlegg);
                vedleggRepository.lagreVedlegg(faktum.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
            }
        }
    }

    private boolean erVedleggKrevdAvAnnetFaktum(Faktum faktum,
                                                SoknadStruktur struktur, SoknadVedlegg soknadVedlegg) {
        return !soknadVedlegg.getFlereTillatt() && annetFaktumHarForventning(faktum.getSoknadId(), soknadVedlegg.getSkjemaNummer(), soknadVedlegg.getOnValue(), struktur);
    }

    private void lagrePaakrevdVedlegg(Faktum faktum, SoknadVedlegg soknadVedlegg, Vedlegg v) {
        Vedlegg vedlegg = v;
        if (vedlegg == null) {
            vedlegg = new Vedlegg(faktum.getSoknadId(), soknadVedlegg.getFlereTillatt() ? faktum.getFaktumId() : null, soknadVedlegg.getSkjemaNummer(), Vedlegg.Status.VedleggKreves);
            vedlegg.setVedleggId(vedleggRepository.opprettVedlegg(vedlegg, null));
        }
        vedlegg.oppdatertInnsendtStatus();

        if (soknadVedlegg.getProperty() != null && faktum.getProperties().containsKey(soknadVedlegg.getProperty())) {
            vedlegg.setNavn(faktum.getProperties().get(soknadVedlegg.getProperty()));
        } else if (soknadVedlegg.harOversetting()) {
            vedlegg.setNavn(navMessageSource.getMessage(soknadVedlegg.getOversetting().replace("${key}", faktum.getKey()), new Object[0], new Locale("nb", "NO")));
        }
        vedleggRepository.lagreVedlegg(faktum.getSoknadId(), vedlegg.getVedleggId(), vedlegg);
    }

    private boolean erParentAktiv(SoknadVedlegg soknadVedlegg, Faktum parent) {
        return parent == null || parent.getValue().equals(soknadVedlegg.getFaktum().getDependOnValue());
    }

    /**
     * Looper alle mulige vedleggsforventinger for gitt skjemanummer,
     * dersom soknadbrukerdata har et innslag som har riktig onValue, returneres true (et annet faktum trigger vedlegget)
     * ellers returneres false
     */
    private boolean annetFaktumHarForventning(Long soknadId, String skjemaNummer, String onValue, SoknadStruktur struktur) {
        List<SoknadVedlegg> vedleggMedGittSkjemanummer = struktur.vedleggForSkjemanr(skjemaNummer);
        for (SoknadVedlegg sv : vedleggMedGittSkjemanummer) {

            String faktumKey = sv.getFaktum().getId();
            if (repository.isVedleggPaakrevd(soknadId, faktumKey, onValue, sv.getFaktum().getDependOnValue())) {
                return true;
            }
        }
        return false;
    }

    public void leggTilKodeverkFelter(List<Vedlegg> vedlegg) {
        for (Vedlegg v : vedlegg) {
            medKodeverk(v);
        }
    }

    private void medKodeverk(Vedlegg vedlegg) {
        try {
            Map<Kodeverk.Nokkel, String> koder = kodeverk.getKoder(vedlegg.getSkjemaNummerFiltrert());
            for (Entry<Nokkel, String> nokkelEntry : koder.entrySet()) {
                if (nokkelEntry.getKey().toString().contains("URL")) {
                    vedlegg.leggTilURL(nokkelEntry.getKey().toString(), koder.get(nokkelEntry.getKey()));
                }
            }
            vedlegg.setTittel(koder.get(Kodeverk.Nokkel.TITTEL));
        } catch (Exception ignore) {
            logger.debug("ignored exception");

        }
    }

    @Override
    public void lagreVedlegg(Long soknadId, Long vedleggId, Vedlegg vedlegg) {
        if(nedgradertEllerForLavtInnsendingsValg(vedlegg)) {
            throw new ApplicationException("Ugyldig innsendingsstatus, opprinnelig innsendingstatus kan aldri nedgraderes");
        }
        vedleggRepository.lagreVedlegg(soknadId, vedleggId, vedlegg);
        repository.settDelstegstatus(soknadId, DelstegStatus.SKJEMA_VALIDERT);
    }

    private boolean nedgradertEllerForLavtInnsendingsValg(Vedlegg vedlegg) {
        Vedlegg.Status nyttInnsendingsvalg = vedlegg.getInnsendingsvalg();
        Vedlegg.Status opprinneligInnsendingsvalg = vedlegg.getOpprinneligInnsendingsvalg();
        if(nyttInnsendingsvalg != null && opprinneligInnsendingsvalg != null){
            if(nyttInnsendingsvalg.getPrioritet() <= 1 || (nyttInnsendingsvalg.getPrioritet() < opprinneligInnsendingsvalg.getPrioritet())) {
                return true;
            }
        }
        return false;
    }

    private SoknadStruktur hentStruktur(String skjema) {
        String type = skjema + ".xml";
        try {
            Unmarshaller unmarshaller = newInstance(SoknadStruktur.class)
                    .createUnmarshaller();
            return (SoknadStruktur) unmarshaller.unmarshal(SoknadStruktur.class
                    .getResourceAsStream(format("/soknader/%s", type)));
        } catch (JAXBException e) {
            throw new RuntimeException("Kunne ikke laste definisjoner. ", e);
        }
    }
}
