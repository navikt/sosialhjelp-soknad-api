package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;


import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.EkstraMetadataTransformer;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class EkstraMetadataService {

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    public List<XMLMetadata> hentEkstraMetadata(WebSoknad soknad) {
        List<EkstraMetadataTransformer> transformers = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer())
                .getMetadataTransformers();

        return transformers.stream()
                .map(transformer -> transformer.apply(soknad))
                .collect(toList());
    }

}