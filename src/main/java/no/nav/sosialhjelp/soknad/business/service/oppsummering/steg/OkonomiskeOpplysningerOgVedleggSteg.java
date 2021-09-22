package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Vedlegg;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.nimbusds.oauth2.sdk.util.CollectionUtils.isNotEmpty;


public class OkonomiskeOpplysningerOgVedleggSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var okonomi = jsonInternalSoknad.getSoknad().getData().getOkonomi();
        var vedlegg = jsonInternalSoknad.getVedlegg();

        return new Steg.Builder()
                .withStegNr(8)
                .withTittel("opplysningerbolk.tittel")
                .withAvsnitt(okonomiOgVedleggAvsnitt(okonomi, vedlegg))
                .build();
    }

    private List<Avsnitt> okonomiOgVedleggAvsnitt(JsonOkonomi okonomi, JsonVedleggSpesifikasjon vedleggSpesifikasjon) {
        var inntektAvsnitt = new Avsnitt.Builder()
                .withTittel("inntektbolk.tittel")
                .withSporsmal(inntekterSporsmal(okonomi))
                .build();

        var utgifterAvsnitt = new Avsnitt.Builder()
                .withTittel("utgifterbolk.tittel")
                .withSporsmal(utgifterSporsmal(okonomi))
                .build();

        var vedleggAvsnitt = new Avsnitt.Builder()
                .withTittel("vedlegg.oppsummering.tittel")
                .withSporsmal(vedleggSporsmal(vedleggSpesifikasjon))
                .build();

        return List.of(inntektAvsnitt, utgifterAvsnitt, vedleggAvsnitt);
    }

    private List<Sporsmal> inntekterSporsmal(JsonOkonomi okonomi) {
        // todo
        return Collections.emptyList();
    }

    private List<Sporsmal> utgifterSporsmal(JsonOkonomi okonomi) {
        // todo
        return Collections.emptyList();
    }

    private List<Sporsmal> vedleggSporsmal(JsonVedleggSpesifikasjon vedleggSpesifikasjon) {
        return vedleggSpesifikasjon.getVedlegg().stream()
                .map(it -> new Sporsmal.Builder()
                        .withTittel(getTittelFrom(it.getType(), it.getTilleggsinfo()))
                        .withErUtfylt(true) // evt null
                        .withFelt(getFelter(it))
                        .build()
                )
                .collect(Collectors.toList());
    }

    private String getTittelFrom(String type, String tilleggsinfo) {
        return String.format("vedlegg.%s.%s.tittel", type, tilleggsinfo);
    }

    private List<Felt> getFelter(JsonVedlegg vedlegg) {
        Felt felt;
        if ("LastetOpp".equals(vedlegg.getStatus()) && isNotEmpty(vedlegg.getFiler())) {
            felt = new Felt.Builder()
                    .withVedlegg(
                            vedlegg.getFiler().stream()
                                    .map(it -> new Vedlegg.Builder()
                                            .withFilnavn(it.getFilnavn())
                                            .build()
                                    )
                                    .collect(Collectors.toList())
                    )
                    .build();
        } else {
            felt = new Felt.Builder()
                    .withType(Type.TEKST)
                    .withSvar("VedleggAlleredeSendt".equals(vedlegg.getStatus()) ?
                            "opplysninger.vedlegg.alleredelastetopp" :
                            "vedlegg.oppsummering.ikkelastetopp"
                    )
                    .build();
        }
        return Collections.singletonList(felt);
    }
}
