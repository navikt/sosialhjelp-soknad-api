package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLHovedskjema;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadataListe;
import no.nav.modig.core.exception.ApplicationException;
import no.nav.modig.lang.collections.predicate.InstanceOf;
import no.nav.modig.lang.option.Optional;
import no.nav.sbl.dialogarena.soknadinnsending.business.db.soknad.SoknadRepository;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.WebSoknad;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.henvendelse.HenvendelseService;
import no.nav.tjeneste.domene.brukerdialog.fillager.v1.meldinger.WSInnhold;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSHentSoknadResponse;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.List;

import static javax.xml.bind.JAXB.unmarshal;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadInnsendingStatus.UNDER_ARBEID;

@Component
public class SoknadServiceUtil {

    @Inject
    private HenvendelseService henvendelseService;

    @Inject
    private FillagerService fillagerService;

    @Inject
    private SoknadRepository repository;

    @Inject
    private VedleggService vedleggService;

    public WebSoknad hentFraHenvendelse(String behandlingsId, boolean hentFaktumOgVedlegg) {
        WSHentSoknadResponse wsSoknadsdata = henvendelseService.hentSoknad(behandlingsId);

        Optional<XMLMetadata> hovedskjemaOptional = on(((XMLMetadataListe) wsSoknadsdata.getAny()).getMetadata())
                .filter(new InstanceOf<XMLMetadata>(XMLHovedskjema.class)).head();
        XMLHovedskjema hovedskjema = (XMLHovedskjema) hovedskjemaOptional.getOrThrow(new ApplicationException("Kunne ikke hente opp søknad"));

        SoknadInnsendingStatus status = SoknadInnsendingStatus.valueOf(wsSoknadsdata.getStatus());
        if (status.equals(UNDER_ARBEID)) {
            fillagerService = fillagerService;
            WebSoknad soknadFraFillager = unmarshal(new ByteArrayInputStream(this.fillagerService.hentFil(hovedskjema.getUuid())), WebSoknad.class);
            repository = repository;
            repository.populerFraStruktur(soknadFraFillager);
            List<WSInnhold> innhold = fillagerService.hentFiler(soknadFraFillager.getBrukerBehandlingId());
            vedleggService = vedleggService;
            vedleggService.populerVedleggMedDataFraHenvendelse(soknadFraFillager, innhold);
            if (hentFaktumOgVedlegg) {
                return repository.hentSoknadMedData(behandlingsId);
            } else {
                return repository.hentSoknad(behandlingsId);
            }
        } else {
            // søkndadsdata er slettet i henvendelse, har kun metadata
            return new WebSoknad().medBehandlingId(behandlingsId).medStatus(status).medskjemaNummer(hovedskjema.getSkjemanummer());
        }
    }

}
