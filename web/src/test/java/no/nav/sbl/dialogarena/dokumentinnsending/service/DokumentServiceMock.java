package no.nav.sbl.dialogarena.dokumentinnsending.service;

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.equalTo;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.dokumentinnsending.fixture.utils.TestUtils.DOKUMENTFORVENTNING_ID;
import static no.nav.sbl.dialogarena.dokumentinnsending.fixture.utils.TestUtils.DOKUMENT_ID;
import static no.nav.sbl.dialogarena.dokumentinnsending.fixture.utils.TestUtils.FORVENTNINGENS_DOKUMENT;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBehandlingsstatus.FERDIG;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBehandlingsstatus.UNDER_ARBEID;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingType.DOKUMENT_BEHANDLING;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingType.DOKUMENT_ETTERSENDING;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg.IKKE_VALGT;
import static no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg.INNSENDT;
import static org.apache.commons.collections15.PredicateUtils.notNullPredicate;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import javax.activation.DataHandler;
import javax.inject.Inject;
import javax.mail.util.ByteArrayDataSource;

import no.nav.modig.core.context.ModigSecurityConstants;
import no.nav.modig.core.context.SubjectHandlerUtils;
import no.nav.modig.core.context.ThreadLocalSubjectHandler;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.core.exception.SystemException;
import no.nav.modig.lang.collections.iter.Elem;
import no.nav.modig.lang.collections.iter.PreparedIterable;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.data.Brukerbehandling;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.data.PersonProfil;
import no.nav.sbl.dialogarena.dokumentinnsending.fixture.utils.TestUtils;
import no.nav.sbl.dialogarena.dokumentinnsending.kodeverk.KodeverkClient;
import no.nav.sbl.dialogarena.dokumentinnsending.kodeverk.KodeverkSkjemaBuilder;
import no.nav.sbl.dialogarena.dokumentinnsending.security.SecurityHandler;
import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandling;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingOppsummering;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSBrukerBehandlingType;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokument;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventning;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventningOppsummering;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventningOppsummeringer;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentForventninger;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSDokumentInnhold;
import no.nav.tjeneste.domene.brukerdialog.henvendelse.v1.informasjon.WSInnsendingsValg;
import no.nav.tjeneste.domene.brukerdialog.henvendelsesbehandling.v1.HenvendelsesBehandlingPortType;
import no.nav.tjeneste.domene.brukerdialog.oppdaterehenvendelsesbehandling.v1.OppdatereHenvendelsesBehandlingPortType;

import org.apache.commons.collections15.MultiMap;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.multimap.MultiHashMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

public class DokumentServiceMock implements OppdatereHenvendelsesBehandlingPortType, HenvendelsesBehandlingPortType, KodeverkClient,
        HenvendelseIntegrationStub {

    static class OpprettBehandling {
        List<WSDokumentForventning> dokumentForventninger;
        boolean erEttersending;
        String avsender;
        String brukerBehandlingId;
    }

    static class OpprettDokumentForventning {
        String navn;
        String link;
        WSInnsendingsValg valg;
        String fritekst;
        boolean hovedSkjema;
        String annetKodeverksId;
        boolean erEttersending;
    }

    static class BrukerBehandling {
        public final List<WSDokumentForventning> forventninger = new ArrayList<>();
        public final WSBrukerBehandling wsBrukerBehandling;

        public BrukerBehandling(List<WSDokumentForventning> forventninger, WSBrukerBehandling wsBrukerBehandling) {
            this.forventninger.addAll(forventninger);
            this.wsBrukerBehandling = wsBrukerBehandling;
        }
    }

    private final Map<String, KodeverkSkjema> kodeverksSkjemaer = new LinkedHashMap<>();

    private final MultiMap<String, BrukerBehandling> behandlinger = new MultiHashMap<>();
    private final Map<String, String> behandlingBruker = new HashMap<>();

    private final Map<Long, WSDokument> dokumenter = new LinkedHashMap<>();

    private final Map<String, Boolean> samtykket = new LinkedHashMap<>();
    private final Map<String, Boolean> innsendt = new LinkedHashMap<>();
    private final Map<String, String> journalFoerendeEnhet = new LinkedHashMap<>();

    private AtomicLong nesteId = new AtomicLong(1);

    private List<WSDokumentForventning> alleLagredeDokumentForventninger() {
        List<WSDokumentForventning> dokumentForventninger = new ArrayList<>();
        for (BrukerBehandling behandling : behandlinger.values()) {
            dokumentForventninger.addAll(behandling.forventninger);
        }
        return dokumentForventninger;
    }

    private List<WSDokument> alleLagredeDokumenter() {
        return new ArrayList<>(dokumenter.values());
    }

    @Override
    public void slettDokument(long id) {
        dokumenter.remove(id);
        for (WSDokumentForventning forventning : alleLagredeDokumentForventninger()) {
            if (forventning.getDokumentId() != null && forventning.getDokumentId().equals(id)) {
                forventning.setDokumentId(null);
                forventning.setInnsendingsValg(IKKE_VALGT);
            }
        }
    }

    @Override
    public WSDokument hentDokument(long id) {
        return on(alleLagredeDokumenter()).filter(where(DOKUMENT_ID, equalTo(id))).head().getOrElse(null);
    }

    @Override
    public long opprettDokument(WSDokumentInnhold dokument, long forventningId) {
        WSDokument dok = (WSDokument) dokument;
        dok.setId(new Random().nextLong());
        dok.setOpplastetDato(new DateTime());
        dokumenter.put(dok.getId(), dok);

        WSDokumentForventning forventning = on(alleLagredeDokumentForventninger()).filter(where(DOKUMENTFORVENTNING_ID, equalTo(forventningId))).head().get();
        forventning.setDokumentId(dok.getId());
        forventning.setInnsendingsValg(WSInnsendingsValg.LASTET_OPP);
        return dok.getId();
    }

    @Override
    public void slettDokumentForventning(long forventningId) {
        for (BrukerBehandling behandling : behandlinger.values()) {
            Optional<WSDokumentForventning> forventning = on(behandling.forventninger).filter(where(DOKUMENTFORVENTNING_ID, equalTo(forventningId)))
                    .head();
            if (forventning.isSome()) {
                behandling.forventninger.remove(forventning.get());
                break;
            }
        }
    }

    @Override
    public void oppdaterDokumentForventning(long id, WSInnsendingsValg valg) {
        Optional<WSDokumentForventning> forventning = on(alleLagredeDokumentForventninger()).filter(where(DOKUMENTFORVENTNING_ID, equalTo(id)))
                .head();
        if (forventning.isSome()) {
            forventning.get().setInnsendingsValg(valg);
        }
    }

    @Override
    public void avbrytHenvendelse(String behandlingsId) {
        Optional<BrukerBehandling> behandling = on(behandlinger.values()).filter(where(BRUKERBEHANDLING_ID, equalTo(behandlingsId))).head();
        if (behandling.isSome()) {
            BrukerBehandling brukerBehandling = behandling.get();
            for (WSDokumentForventning forventning : brukerBehandling.forventninger) {
                if (forventning.getDokumentId() != null) {
                    slettDokument(forventning.getDokumentId());
                }
            }
            brukerBehandling.wsBrukerBehandling.setSistEndret(new DateTime());
            brukerBehandling.wsBrukerBehandling.setStatus(FERDIG);
        }
    }

    @Override
    public void oppdaterDokumentForventningBeskrivelse(long id, String beskrivelse) {
        Optional<WSDokumentForventning> forventning = on(alleLagredeDokumentForventninger()).filter(where(DOKUMENTFORVENTNING_ID, equalTo(id)))
                .head();
        if (forventning.isSome()) {
            forventning.get().setFriTekst(beskrivelse);
        }
    }

    @Override
    public List<WSDokumentForventning> hentDokumentForventningListe(String behandlingsId) {
        Optional<BrukerBehandling> behandling = on(behandlinger.values()).filter(where(BRUKERBEHANDLING_ID, equalTo(behandlingsId))).head();
        if (!behandling.isSome()) {
            throw new ApplicationException("Fant ikke brukerbehandling med ID " + behandlingsId, new Exception("Fant ikke behandling med gitt ID"));
        } else if (!behandling.get().wsBrukerBehandling.getStatus().equals(UNDER_ARBEID)) {
            throw new ApplicationException("Behandlingen med ID " + behandlingsId + " er avsluttet.", new Exception(
                    "Kan ikke hente forventninger til en avsluttet behandling"));
        }
        return behandling.get().forventninger;
    }

    @Override
    public long opprettDokumentForventning(WSDokumentForventning forventning, String behandlingsId) {
        Optional<BrukerBehandling> behandling = on(behandlinger.values()).filter(where(BRUKERBEHANDLING_ID, equalTo(behandlingsId))).head();
        long id = 0L;
        if (behandling.isSome()) {
            id = nesteId.incrementAndGet();
            forventning.setId(id);
            BrukerBehandling brukerBehandling = behandling.get();
            brukerBehandling.forventninger.add(forventning);
            brukerBehandling.wsBrukerBehandling.getDokumentForventninger().withDokumentForventning(forventning);
        }
        return id;
    }

    @Override
    public KodeverkSkjema hentKodeverkSkjemaForSkjemanummer(String skjemanummer) {
        return optional(kodeverksSkjemaer.get(skjemanummer)).getOrThrow(
                new SystemException("KodeverkId " + skjemanummer + " finnes ikke!", new Throwable()));
    }

    @Override
    public KodeverkSkjema hentKodeverkSkjemaForVedleggsid(String vedleggsid) {
        return optional(kodeverksSkjemaer.get(vedleggsid)).getOrThrow(
                new SystemException("KodeverkId " + vedleggsid + " finnes ikke!", new Throwable()));
    }

    @Override
    public boolean isEgendefinert(String skjemaId) {
        return false;
    }

    @Override
    public WSBrukerBehandling hentBrukerBehandling(String behandlingsId) {
        return on(behandlinger.values())
                .filter(where(BRUKERBEHANDLING_ID, equalTo(behandlingsId)))
                .map(TIL_WS_BRUKERBEHANDLING)
                .head()
                .getOrThrow(new ApplicationException("Fant ikke brukerbehandling med ID " + behandlingsId));
    }

    @Override
    public List<WSBrukerBehandlingOppsummering> hentBrukerBehandlingListe(String avsender) {
        return on(behandlinger.get(avsender)).map(TIL_WS_BRUKERBEHANDLING).map(TIL_WS_BRUKERBEHANDLINGOPPSUMMERING).collect();
    }

    @Override
    public WSDokumentForventning hentDokumentForventning(long dokumentForventingsId) {
        return on(alleLagredeDokumentForventninger()).filter(where(TestUtils.DOKUMENTFORVENTNING_ID, equalTo(dokumentForventingsId))).head().get();
    }

    @Override
    public String opprettDokumentBehandling(List<WSDokumentForventning> dokumentForventninger, WSBrukerBehandlingType type) {
        OpprettBehandling behandling = new OpprettBehandling();
        for (WSDokumentForventning forventning : dokumentForventninger) {
            forventning.setInnsendingsValg(IKKE_VALGT);
            forventning.setId(new Random().nextLong());
        }
        behandling.dokumentForventninger = dokumentForventninger;
        behandling.avsender = String.valueOf(nesteId.incrementAndGet());
        String idStreng = String.format("%09d", nesteId.incrementAndGet());
        behandling.brukerBehandlingId = "DA01-" + idStreng.substring(0, 3) + "-" + idStreng.substring(3, 6) + "-" + idStreng.substring(6, 9);
        behandling.erEttersending = type.equals(WSBrukerBehandlingType.DOKUMENT_ETTERSENDING);
        createBrukerBehandling(behandling);
        return behandling.brukerBehandlingId;
    }

    public String opprettDokumentBehandling(List<WSDokumentForventning> dokumentForventninger, WSBrukerBehandlingType type, String ident) {
        OpprettBehandling behandling = new OpprettBehandling();
        for (WSDokumentForventning forventning : dokumentForventninger) {
            if (forventning.getInnsendingsValg() == null) {
                forventning.setInnsendingsValg(WSInnsendingsValg.IKKE_VALGT);
            }
            forventning.setId(new Random().nextLong());
        }
        behandling.dokumentForventninger = dokumentForventninger;
        behandling.avsender = ident;
        behandling.brukerBehandlingId = "DA01-" + ident.substring(0, 3) + "-" + ident.substring(3, 6) + "-" + ident.substring(6, 9);
        behandling.erEttersending = type.equals(WSBrukerBehandlingType.DOKUMENT_ETTERSENDING);
        createBrukerBehandling(behandling);
        return behandling.brukerBehandlingId;
    }

    @Override
    public void reset() {
        kodeverksSkjemaer.clear();
        behandlinger.clear();
    }

    @Override
    public PreparedIterable<WSDokument> getOpplastedeDokumenterFor(String behandlingsId) {
        Optional<BrukerBehandling> behandling = on(behandlinger.values()).filter(where(BRUKERBEHANDLING_ID, equalTo(behandlingsId))).head();
        if (behandling.isSome()) {
            BrukerBehandling brukerBehandling = behandling.get();
            List<Long> dokumentIder = on(brukerBehandling.forventninger).map(FORVENTNINGENS_DOKUMENT).filter(notNullPredicate()).collect();
            return on(dokumenter.values()).filter(dokumentIdBlant(dokumentIder));
        }
        return on(Collections.<WSDokument>emptyList());
    }

    private Predicate<WSDokument> dokumentIdBlant(final List<Long> dokumentIder) {
        return new Predicate<WSDokument>() {
            @Override
            public boolean evaluate(WSDokument object) {
                return dokumentIder.contains(object.getId());
            }
        };
    }

    @Override
    public void stub(KodeverkSkjema skjema) {

        kodeverksSkjemaer.put(skjema.getSkjemanummer(), skjema);
        kodeverksSkjemaer.put(skjema.getVedleggsid(), skjema);
    }

    // CHECKSTYLE:OFF
    @Override
    public void stub(Brukerbehandling rad) {
        List<WSDokumentForventning> dokumentForventninger = new ArrayList<>();

        OpprettDokumentForventning forventning = new OpprettDokumentForventning();
        forventning.navn = rad.soknad;
        forventning.link = rad.soknadlink;
        forventning.valg = rad.soknadStatus;
        forventning.hovedSkjema = true;
        forventning.erEttersending = rad.erEttersending;

        dokumentForventninger.add(createDokumentForventning(forventning));

        List<WSInnsendingsValg> statuserNavVedlegg = on(rad.statuserNavVedlegg).map(new StringToEnum()).collect();

        for (Elem<String> vedlegg : on(rad.navVedlegg).indexed()) {
            forventning = new OpprettDokumentForventning();
            forventning.navn = vedlegg.value;
            forventning.link = vedlegg.index < rad.navVedlegglinker.size() ? rad.navVedlegglinker.get(vedlegg.index) : "http://nav.no/defaultlink";
            forventning.valg = vedlegg.index < statuserNavVedlegg.size() ? statuserNavVedlegg.get(vedlegg.index) : IKKE_VALGT;
            forventning.hovedSkjema = false;
            dokumentForventninger.add(createDokumentForventning(forventning));
        }

        List<WSInnsendingsValg> statuserEksterneVedlegg = on(rad.statuserEksterneVedlegg).map(new StringToEnum()).collect();

        for (Elem<String> vedlegg : on(rad.eksterneVedlegg).indexed()) {
            forventning = new OpprettDokumentForventning();
            forventning.navn = vedlegg.value;
            forventning.valg = vedlegg.index < statuserEksterneVedlegg.size() ? statuserEksterneVedlegg.get(vedlegg.index)
                    : IKKE_VALGT;
            forventning.hovedSkjema = false;
            dokumentForventninger.add(createDokumentForventning(forventning));
        }

        List<WSInnsendingsValg> statuserAnnetVedlegg = on(rad.statuserAnnetVedlegg).map(new StringToEnum()).collect();

        // Annet vedlegg har spesiell oppførsel mtp at de er prefikset med
        // "Annet".
        // Bør skrives om slik at kodeverk har forhold til typen istedenfor
        // prefiks.
        String kodeverkId = randomAlphanumeric(10);
        kodeverksSkjemaer.put(kodeverkId, new KodeverkSkjemaBuilder().kodeverkId(kodeverkId).navn("Annet").build());

        for (Elem<String> vedlegg : on(rad.annetVedlegg).indexed()) {
            forventning = new OpprettDokumentForventning();
            forventning.navn = vedlegg.value;
            forventning.valg = vedlegg.index < statuserAnnetVedlegg.size() ? statuserAnnetVedlegg.get(vedlegg.index) : IKKE_VALGT;
            forventning.hovedSkjema = false;
            forventning.fritekst = StringUtils.remove(vedlegg.value, "Annet: ");
            forventning.annetKodeverksId = kodeverkId;
            dokumentForventninger.add(createDokumentForventning(forventning));
        }

        OpprettBehandling behandling = new OpprettBehandling();
        behandling.dokumentForventninger = dokumentForventninger;
        behandling.erEttersending = rad.erEttersending;
        if (isNotBlank(rad.idnummer)) {
            behandling.avsender = rad.idnummer;
        } else {
            behandling.avsender = String.valueOf(nesteId.incrementAndGet());
        }

        if (isNotBlank(rad.brukerbehandlingId)) {
            behandling.brukerBehandlingId = rad.brukerbehandlingId;
        } else {
            behandling.brukerBehandlingId = String.valueOf(nesteId.incrementAndGet());
        }

        createBrukerBehandling(behandling);
        stubBrukerprofil(behandling, rad);
    }

    public void settSecurityContextFor(String brukerBehandlingId) {
        System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
        System.setProperty(ModigSecurityConstants.SYSTEMUSER_USERNAME, "MD05");
        SubjectHandlerUtils.setEksternBruker(behandlingBruker.get(brukerBehandlingId), 4, "doskjfløaskdjfø");
        SecurityHandler.setSecurityContext(behandlingBruker.get(brukerBehandlingId));
    }

    @Inject
    private PersonServiceMock personServiceMock;

    private void stubBrukerprofil(OpprettBehandling behandling, Brukerbehandling rad) {
        PersonProfil profil = new PersonProfil();
        profil.ident = behandling.avsender;
        profil.epost = rad.epost;
        personServiceMock.stub(profil);
    }

    // CHECKSTYLE:ON

    private void createBrukerBehandling(OpprettBehandling behandling) {
        WSBrukerBehandling brukerBehandling = new WSBrukerBehandling()
                .withBrukerBehandlingType(behandling.erEttersending ? DOKUMENT_ETTERSENDING : DOKUMENT_BEHANDLING)
                .withBehandlingsId(behandling.brukerBehandlingId)
                .withBrukerBehandlingType(DOKUMENT_BEHANDLING)
                .withDokumentForventninger(new WSDokumentForventninger().withDokumentForventning(behandling.dokumentForventninger))
                .withStatus(UNDER_ARBEID);
        behandlinger.put(behandling.avsender, new BrukerBehandling(behandling.dokumentForventninger, brukerBehandling));
        behandlingBruker.put(behandling.brukerBehandlingId, behandling.avsender);
    }

    // Checkstyle:OFF
    private WSDokumentForventning createDokumentForventning(OpprettDokumentForventning forventning) {
        WSDokumentForventning dokumentForventning = new WSDokumentForventning();

        String kodeverkId = forventning.annetKodeverksId;
        if (isBlank(kodeverkId)) {
            kodeverkId = RandomStringUtils.randomAlphanumeric(10);
            KodeverkSkjemaBuilder kodeverkSkjemaBuilder = new KodeverkSkjemaBuilder().kodeverkId(kodeverkId).navn(forventning.navn);
            if (isNotBlank(forventning.link)) {
                kodeverkSkjemaBuilder.link(forventning.link);
            }
            kodeverksSkjemaer.put(kodeverkId, kodeverkSkjemaBuilder.build());
        }

        dokumentForventning.setKodeverkId(kodeverkId);
        dokumentForventning.setHovedskjema(forventning.hovedSkjema);
        dokumentForventning.setId(nesteId.incrementAndGet());
        dokumentForventning.setInnsendingsValg(optional(forventning.valg).getOrElse(IKKE_VALGT));
        if (forventning.hovedSkjema && forventning.erEttersending) {
            dokumentForventning.setInnsendingsValg(INNSENDT);
        }

        if (isNotBlank(forventning.fritekst)) {
            dokumentForventning.setFriTekst(forventning.fritekst);
        }

        if (dokumentForventning.getInnsendingsValg() == WSInnsendingsValg.LASTET_OPP) {
            WSDokument dokument = createDokument(forventning.navn);
            dokumenter.put(dokument.getId(), dokument);
            dokumentForventning.setDokumentId(dokument.getId());
        }
        return dokumentForventning;
    }

    // CHECKSTYLE:ON

    @Override
    public WSDokument createDokument(String navn) {
        WSDokument dokument = new WSDokument();
        dokument.setId(nesteId.incrementAndGet());
        dokument.setFilnavn(navn);
        dokument.setOpplastetDato(new DateTime());

        InputStream resourceAsStream = DokumentServiceMock.class.getResourceAsStream("/vedlegg/NAV 12-34 Kontantstotte.pdf");
        byte[] vedlegg;
        try {
            vedlegg = IOUtils.toByteArray(resourceAsStream);
        } catch (IOException e) {
            throw new ApplicationException(e.getMessage(), e);
        }
        dokument.setInnhold(new DataHandler(new ByteArrayDataSource(vedlegg, "application/octet-stream")));

        return dokument;
    }

    @Override
    public boolean ping() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void identifiserAktor(String behandlingsId, String aktorId) {

    }

    @Override
    public boolean sendHenvendelse(String behandlingsId, String journalForendeEnhet) {
        innsendt.put(behandlingsId, true);
        journalFoerendeEnhet.put(behandlingsId, journalForendeEnhet);
        Optional<BrukerBehandling> behandling = on(behandlinger.values()).filter(where(BRUKERBEHANDLING_ID, equalTo(behandlingsId))).head();
        if (behandling.isSome()) {
            BrukerBehandling brukerBehandling = behandling.get();
            brukerBehandling.wsBrukerBehandling.setSistEndret(new DateTime());
            brukerBehandling.wsBrukerBehandling.setStatus(FERDIG);
            brukerBehandling.wsBrukerBehandling.setInnsendtDato(DateTime.now());
        }
        return true;
    }


    @Override
    public boolean opprettElektroniskSamtykke(String behandlingsId) {
        samtykket.put(behandlingsId, true);
        return true;
    }

    public boolean harSamtykket(String behandlingsId) {
        return samtykket.containsKey(behandlingsId) && samtykket.get(behandlingsId);
    }

    public boolean harInnsendt(String behandlingsId) {
        return innsendt.containsKey(behandlingsId) && innsendt.get(behandlingsId);
    }

    public boolean skalSendesTilNavInternasjonalEnhet(String enhet, String behandlingsId) {
        return journalFoerendeEnhet.get(behandlingsId).equals(enhet);
    }

    private static final class StringToEnum implements Transformer<String, WSInnsendingsValg> {
        @Override
        public WSInnsendingsValg transform(String text) {
            return Enum.valueOf(WSInnsendingsValg.class, text.toUpperCase().replace(' ', '_'));
        }
    }

    private static final Transformer<BrukerBehandling, String> BRUKERBEHANDLING_ID = new Transformer<BrukerBehandling, String>() {
        @Override
        public String transform(BrukerBehandling behandling) {
            return behandling.wsBrukerBehandling.getBehandlingsId();
        }
    };

    private static final Transformer<WSBrukerBehandling, WSBrukerBehandlingOppsummering> TIL_WS_BRUKERBEHANDLINGOPPSUMMERING = new Transformer<WSBrukerBehandling, WSBrukerBehandlingOppsummering>() {
        @Override
        public WSBrukerBehandlingOppsummering transform(WSBrukerBehandling behandling) {
            WSDokumentForventningOppsummeringer wsDokumentForventningOppsummeringer = new WSDokumentForventningOppsummeringer().withDokumentForventningOppsummering(on(behandling.getDokumentForventninger().getDokumentForventning()).map(TIL_WS_DOKUMENTFORVETNINGOPPSUMMERING).collect());
            return new WSBrukerBehandlingOppsummering()
                    .withDokumentbehandlingType(behandling.getDokumentbehandlingType())
                    .withBrukerBehandlingType(behandling.getBrukerBehandlingType())
                    .withBehandlingsId(behandling.getBehandlingsId())
                    .withHovedskjemaId(behandling.getHovedskjemaId())
                    .withInnsendtDato(behandling.getInnsendtDato())
                    .withSistEndret(behandling.getSistEndret())
                    .withStatus(behandling.getStatus())
                    .withDokumentForventningOppsummeringer(wsDokumentForventningOppsummeringer);
        }
    };

    private static final Transformer<WSDokumentForventning, WSDokumentForventningOppsummering> TIL_WS_DOKUMENTFORVETNINGOPPSUMMERING = new Transformer<WSDokumentForventning, WSDokumentForventningOppsummering>() {
        @Override
        public WSDokumentForventningOppsummering transform(WSDokumentForventning forventning) {
            return new WSDokumentForventningOppsummering()
                    .withFriTekst(forventning.getFriTekst())
                    .withInnsendingsValg(forventning.getInnsendingsValg())
                    .withKodeverkId(forventning.getKodeverkId())
                    .withHovedskjema(forventning.isHovedskjema());
        }
    };

    private static final Transformer<BrukerBehandling, WSBrukerBehandling> TIL_WS_BRUKERBEHANDLING = new Transformer<BrukerBehandling, WSBrukerBehandling>() {
        @Override
        public WSBrukerBehandling transform(BrukerBehandling behandling) {
            return behandling.wsBrukerBehandling;
        }
    };

    public void toemInnsendtListe() {
        innsendt.clear();

    }

    public void clear() {
        this.behandlinger.clear();
    }
}
