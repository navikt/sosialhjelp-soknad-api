package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;


import no.nav.sbl.dialogarena.sendsoknad.domain.AlternativRepresentasjon;
import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon.KravdialogInformasjonHolder;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.soknadinnsending.business.WebSoknadConfig;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.SoknadMetadata.FilData;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.FillagerService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class AlternativRepresentasjonService {

    @Inject
    private FillagerService fillagerService;
    @Inject
    private WebSoknadConfig config;
    @Inject
    private KravdialogInformasjonHolder kravdialogInformasjonHolder;


    public List<AlternativRepresentasjon> hentAlternativeRepresentasjoner(WebSoknad soknad, NavMessageSource messageSource) {
        List<AlternativRepresentasjonTransformer> transformers = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getTransformers(messageSource, soknad);

        soknad.fjernFaktaSomIkkeSkalVaereSynligISoknaden(config.hentStruktur(soknad.getskjemaNummer()));
        return transformers.stream().map(transformer -> transformer.apply(soknad)).collect(toList());
    }

    public List<AlternativRepresentasjon> legacyHentAlternativeRepresentasjoner(WebSoknad soknad, NavMessageSource messageSource) {
        List<AlternativRepresentasjonTransformer> transformers = kravdialogInformasjonHolder.hentKonfigurasjon(soknad.getskjemaNummer()).getTransformers(messageSource, soknad);

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

    public List<FilData> lagXmlFormat(List<AlternativRepresentasjon> alternativeRepresentasjoner) {
        return alternativeRepresentasjoner.stream().map(r -> {
            FilData f = new FilData();
            f.filnavn = r.getFilnavn();
            f.filStorrelse = "" + r.getContent().length;
            f.mimetype = r.getMimetype();
            f.filUuid = r.getUuid();
            return f;
        }).collect(toList());
    }
}