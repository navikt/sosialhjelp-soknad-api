package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;


import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLAlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.consumer.fillager.FillagerService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.List;

import static java.util.stream.Collectors.*;

@Component
public class AlternativRepresentasjonService {

    @Inject
    private FillagerService fillagerService;
    @Inject
    private WebSoknadConfig config;
    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;


    public List<AlternativRepresentasjon> hentAlternativeRepresentasjoner(WebSoknad soknad, NavMessageSource messageSource) {
        List<AlternativRepresentasjonTransformer> transformers = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getTransformers(messageSource);

        soknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(config.hentStruktur(soknad.getskjemaNummer()));
        return transformers.stream().map(transformer -> transformer.apply(soknad)).collect(toList());
    }

    public void lagreTilFillager(String brukerBehandlingId, String aktoerId, List<AlternativRepresentasjon> alternativeRepresentasjoner) {
        for (AlternativRepresentasjon r : alternativeRepresentasjoner) {
            fillagerService.lagreFil(brukerBehandlingId,
                    r.getUuid(),
                    aktoerId,
                    new ByteArrayInputStream(r.getContent()));
        }
    }

    public List<XMLAlternativRepresentasjon> lagXmlFormat(List<AlternativRepresentasjon> alternativeRepresentasjoner) {
        return alternativeRepresentasjoner.stream().map(r ->
                new XMLAlternativRepresentasjon()
                        .withFilnavn(r.getFilnavn())
                        .withFilstorrelse(r.getContent().length + "")
                        .withMimetype(r.getMimetype())
                        .withUuid(r.getUuid()))
                .collect(toList());

    }
}