package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;


import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.EkstraMetadataTransformer;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EkstraMetadataService {

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    public Map<String, String> hentEkstraMetadata(WebSoknad soknad) {
        List<EkstraMetadataTransformer> transformers = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer())
                .getMetadataTransformers();

        Map<String, String> metaData = new HashMap<>();

        transformers.stream()
                .map(transformer -> transformer.apply(soknad))
                .forEach(metaData::putAll);

        return metaData;
    }

}