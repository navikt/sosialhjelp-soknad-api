package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLAlternativRepresentasjon;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLAlternativRepresentasjonListe;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.vedlegg.VedleggRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.util.List;

import static no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLInnsendingsvalg.LASTET_OPP;
import static no.nav.sbl.dialogarena.common.kodeverk.Kodeverk.KVITTERING;
import static no.nav.sbl.dialogarena.soknadinnsending.business.service.Transformers.convertToXmlVedleggListe;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.getJournalforendeEnhet;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.DagpengerUtils.getSkjemanummer;
import static no.nav.sbl.dialogarena.soknadinnsending.business.util.PersonaliaUtils.adresserOgStatsborgerskap;

public class SoknadServiceUtil {

    private static final String AAP_INTERNASJONAL = "2101";

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

    public static String skjemanummer(WebSoknad soknad) {
        return soknad.erDagpengeSoknad() ? getSkjemanummer(soknad) : soknad.getskjemaNummer();
    }

    public static List<Vedlegg> hentVedleggOgKvittering(WebSoknad soknad, VedleggRepository vedleggRepository) {
        List<Vedlegg> vedleggForventninger = soknad.getVedlegg();
        Vedlegg kvittering = vedleggRepository.hentVedleggForskjemaNummer(soknad.getSoknadId(), null, KVITTERING);
        if (kvittering != null) {
            vedleggForventninger.add(kvittering);
        }
        return vedleggForventninger;
    }

    public static void sendSoknad(WebSoknad soknad, byte[] pdf, FillagerService fillagerService, VedleggRepository vedleggRepository,
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

        List<Vedlegg> vedleggForventninger = hentVedleggOgKvittering(soknad, vedleggRepository);

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

    private static String journalforendeEnhet(WebSoknad soknad) {
        String journalforendeEnhet;

        if (soknad.erDagpengeSoknad()) {
            journalforendeEnhet = getJournalforendeEnhet(soknad);
        } else if (soknad.erAapSoknad() && adresserOgStatsborgerskap(soknad).harUtenlandskAdresse()) {
            journalforendeEnhet = AAP_INTERNASJONAL;
        } else {
            journalforendeEnhet = soknad.getJournalforendeEnhet();
        }
        return journalforendeEnhet;
    }

}
