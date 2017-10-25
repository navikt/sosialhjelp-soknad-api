package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;


import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLSoknadMetadata;
import no.nav.melding.domene.brukerdialog.behandlingsinformasjon.v1.XMLSoknadMetadata.Verdi;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.EkstraMetadataTransformer;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Component
public class EkstraMetadataService {

    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;

    public XMLSoknadMetadata hentEkstraMetadata(WebSoknad soknad) {
        List<EkstraMetadataTransformer> transformers = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer())
                .getMetadataTransformers();

        XMLSoknadMetadata soknadMetadata = new XMLSoknadMetadata();

        transformers.stream()
                .map(transformer -> transformer.apply(soknad))
                .forEach(map -> {
                    for (Map.Entry<String, String> v : map.entrySet()) {
                        soknadMetadata.withVerdi(new Verdi(v.getKey(), v.getValue()));
                    }
                });

        return soknadMetadata;
    }

    public String finnMetadataVerdi(List<XMLMetadata> metadataListe, String key) {
        return metadataListe.stream()
                .filter(xmlMetadata -> xmlMetadata instanceof XMLSoknadMetadata)
                .map(xmlMetadata -> (XMLSoknadMetadata) xmlMetadata)
                .findFirst()
                .map(soknadMetadata -> soknadMetadata.getVerdi().stream()
                        .filter(verdi -> key.equals(verdi.getKey()))
                        .findFirst()
                        .map(Verdi::getValue)
                        .orElse(null)
                ).orElse(null);
    }

}