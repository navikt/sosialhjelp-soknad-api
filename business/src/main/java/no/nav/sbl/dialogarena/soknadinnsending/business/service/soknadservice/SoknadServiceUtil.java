package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLAlternativRepresentasjon;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLAlternativRepresentasjonListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.collections.predicate.InstanceOf;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FaktaService;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.apache.commons.collections15.Transformer;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static java.util.UUID.randomUUID;
import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.LASTET_OPP;
import static no.nav.modig.core.context.SubjectHandler.getSubjectHandler;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.not;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Faktum.FaktumType.SYSTEMREGISTRERT;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.UNDER_ARBEID;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.convertToXmlVedleggListe;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.journalforendeEnhet;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.kvittering;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice.StaticMetoder.skjemanummer;

public class SoknadServiceUtil {

    public static void validerSkjemanummer(String navSoknadId, KravdialogInformasjonHolder kravdialogInformasjonHolder) {
        if (!kravdialogInformasjonHolder.hentAlleSkjemanumre().contains(navSoknadId)) {
            throw new ApplicationException("Ikke gyldig skjemanummer " + navSoknadId);
        }
    }

    public static XMLAlternativRepresentasjonListe opprettAlternativRepresentasjoner(WebSoknad soknad, KravdialogInformasjonHolder kravdialogInformasjonHolder, FillagerService fillagerService) {
        List<Transformer<WebSoknad, AlternativRepresentasjon>> transformers = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getTransformers();
        XMLAlternativRepresentasjonListe xmlAlternativRepresentasjonListe = new XMLAlternativRepresentasjonListe();

        List<XMLAlternativRepresentasjon> alternativRepresentasjonListe = xmlAlternativRepresentasjonListe.getAlternativRepresentasjon();

        for (Transformer<WebSoknad, AlternativRepresentasjon> transformer : transformers) {
            AlternativRepresentasjon altrep = transformer.transform(soknad);
            fillagerService.lagreFil(soknad.getBrukerBehandlingId(),
                    altrep.getUuid(),
                    soknad.getAktoerId(),
                    new ByteArrayInputStream(altrep.getContent()));

            alternativRepresentasjonListe.add(new XMLAlternativRepresentasjon()
                    .withFilnavn(altrep.getFilnavn())
                    .withFilstorrelse(altrep.getContent().length + "")
                    .withMimetype(altrep.getMimetype())
                    .withUuid(altrep.getUuid()));
        }
        return xmlAlternativRepresentasjonListe;
    }


    public static void sendSoknad(WebSoknad soknad, byte[] pdf, FillagerService fillagerService, VedleggService vedleggService,
                                  KravdialogInformasjonHolder kravdialogInformasjonHolder, HenvendelseService henvendelseService, SoknadRepository repository, Logger logger) {
        long soknadId = soknad.getSoknadId();
        if (soknad.erEttersending() && soknad.getOpplastedeVedlegg().size() <= 0) {
            logger.error("Kan ikke sende inn ettersendingen med ID {0} uten å ha lastet opp vedlegg", soknad.getBrukerBehandlingId());
            throw new ApplicationException("Kan ikke sende inn ettersendingen uten å ha lastet opp vedlegg");
        }

        if (soknad.harAnnetVedleggSomIkkeErLastetOpp()) {
            logger.error("Kan ikke sende inn behandling (ID: {0}) med Annet vedlegg (skjemanummer N6) som ikke er lastet opp", soknad.getBrukerBehandlingId());
            throw new ApplicationException("Kan ikke sende inn behandling uten å ha lastet opp alle  vedlegg med skjemanummer N6");
        }

        logger.info("Lagrer søknad som fil til henvendelse for behandling {}", soknad.getBrukerBehandlingId());
        fillagerService.lagreFil(soknad.getBrukerBehandlingId(), soknad.getUuid(), soknad.getAktoerId(), new ByteArrayInputStream(pdf));

        List<Vedlegg> vedleggForventninger = vedleggService.hentVedleggOgKvittering(soknad);

        String skjemanummer = skjemanummer(soknad);
        XMLHovedskjema hovedskjema = new XMLHovedskjema()
                .withInnsendingsvalg(LASTET_OPP.toString())
                .withSkjemanummer(skjemanummer)
                .withFilnavn(skjemanummer)
                .withMimetype("application/pdf")
                .withFilstorrelse("" + pdf.length)
                .withUuid(soknad.getUuid())
                .withJournalforendeEnhet(journalforendeEnhet(soknad));

        hovedskjema.withAlternativRepresentasjonListe(opprettAlternativRepresentasjoner(soknad, kravdialogInformasjonHolder, fillagerService));

        henvendelseService.avsluttSoknad(soknad.getBrukerBehandlingId(), hovedskjema, convertToXmlVedleggListe(vedleggForventninger));
        repository.slettSoknad(soknadId);
    }

    public static void populerVedleggMedDataFraHenvendelse(WebSoknad soknad, List<WSInnhold> innhold, VedleggRepository vedleggRepository) {
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

    public static void populerSoknadFraHenvendelse(XMLHovedskjema hovedskjema, FillagerService fillagerService, SoknadRepository repository, VedleggRepository vedleggRepository) {
        byte[] bytes = fillagerService.hentFil(hovedskjema.getUuid());
        WebSoknad soknad = JAXB.unmarshal(new ByteArrayInputStream(bytes), WebSoknad.class);
        repository.populerFraStruktur(soknad);
        List<WSInnhold> innhold = fillagerService.hentFiler(soknad.getBrukerBehandlingId());
        populerVedleggMedDataFraHenvendelse(soknad, innhold, vedleggRepository);
    }

    public static WebSoknad lagEttersendingFraWsSoknad(WSHentSoknadResponse opprinneligInnsending, DateTime innsendtDato,
                                                       HenvendelseService henvendelseService, SoknadRepository repository, FaktaService faktaService, VedleggRepository vedleggRepository,
                                                       VedleggService vedleggService) {
        String ettersendingsBehandlingId = henvendelseService.startEttersending(opprinneligInnsending);
        WSHentSoknadResponse wsEttersending = henvendelseService.hentSoknad(ettersendingsBehandlingId);

        String behandlingskjedeId;
        if (opprinneligInnsending.getBehandlingskjedeId() != null) {
            behandlingskjedeId = opprinneligInnsending.getBehandlingskjedeId();
        } else {
            behandlingskjedeId = opprinneligInnsending.getBehandlingsId();
        }

        WebSoknad soknad = WebSoknad.startEttersending(ettersendingsBehandlingId);
        String mainUid = randomUUID().toString();
        List<XMLMetadata> xmlVedleggListe = ((XMLMetadataListe) wsEttersending.getAny()).getMetadata();
        List<XMLMetadata> filtrertXmlVedleggListe = on(xmlVedleggListe).filter(not(kvittering())).collect();

        Optional<XMLMetadata> hovedskjema = on(filtrertXmlVedleggListe).filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class)).head();
        if (!hovedskjema.isSome()) {
            throw new ApplicationException("Kunne ikke hente opp hovedskjema for søknad");
        }
        XMLHovedskjema xmlHovedskjema = (XMLHovedskjema) hovedskjema.get();

        soknad.medUuid(mainUid)
                .medAktorId(getSubjectHandler().getUid())
                .medskjemaNummer(xmlHovedskjema.getSkjemanummer())
                .medBehandlingskjedeId(behandlingskjedeId)
                .medJournalforendeEnhet(xmlHovedskjema.getJournalforendeEnhet());

        Long soknadId = repository.opprettSoknad(soknad);
        soknad.setSoknadId(soknadId);

        Faktum soknadInnsendingsDato = new Faktum()
                .medSoknadId(soknadId)
                .medKey("soknadInnsendingsDato")
                .medValue(String.valueOf(innsendtDato.getMillis()))
                .medType(SYSTEMREGISTRERT);
        faktaService.lagreSystemFaktum(soknadId, soknadInnsendingsDato);
        soknad.setFakta(repository.hentAlleBrukerData(soknadId));

        soknad.setVedlegg(vedleggService.hentVedleggOgPersister(new XMLMetadataListe(filtrertXmlVedleggListe), soknadId));

        return soknad;
    }

    public static WebSoknad hentFraHenvendelse(String behandlingsId, boolean hentFaktumOgVedlegg,
                                               HenvendelseService henvendelseService, FillagerService fillagerService, SoknadRepository repository, VedleggRepository vedleggRepository) {
        WSHentSoknadResponse wsSoknadsdata = henvendelseService.hentSoknad(behandlingsId);

        XMLMetadataListe vedleggListe = (XMLMetadataListe) wsSoknadsdata.getAny();
        Optional<XMLMetadata> hovedskjemaOptional = on(vedleggListe.getMetadata()).filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class)).head();
        XMLHovedskjema hovedskjema = (XMLHovedskjema) hovedskjemaOptional.getOrThrow(new ApplicationException("Kunne ikke hente opp søknad"));

        WebSoknad soknad;
        SoknadInnsendingStatus status = SoknadInnsendingStatus.valueOf(wsSoknadsdata.getStatus());
        if (status.equals(UNDER_ARBEID)) {
            populerSoknadFraHenvendelse(hovedskjema, fillagerService, repository, vedleggRepository);
            if (hentFaktumOgVedlegg) {
                soknad = repository.hentSoknadMedData(behandlingsId);
            } else {
                soknad = repository.hentSoknad(behandlingsId);
            }
        } else {
            // søkndadsdata er slettet i henvendelse, har kun metadata
            soknad = new WebSoknad().medBehandlingId(behandlingsId).medStatus(status).medskjemaNummer(hovedskjema.getSkjemanummer());
        }
        return soknad;
    }

    public static WebSoknad hentSoknadFraDbEllerHenvendelse(String behandlingsId, SoknadRepository repository, HenvendelseService henvendelseService, FillagerService fillagerService, VedleggRepository vedleggRepository) {
        WebSoknad soknad = repository.hentSoknad(behandlingsId);
        if (soknad == null) {
            soknad = hentFraHenvendelse(behandlingsId, false, henvendelseService, fillagerService, repository, vedleggRepository);
        }
        return soknad;
    }

}
