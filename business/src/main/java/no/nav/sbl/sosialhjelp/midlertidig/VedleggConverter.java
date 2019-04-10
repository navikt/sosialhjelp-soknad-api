package no.nav.sbl.sosialhjelp.midlertidig;

import no.nav.sbl.dialogarena.sendsoknad.domain.Vedlegg;
import no.nav.sbl.dialogarena.soknadinnsending.business.service.VedleggService;
import no.nav.sbl.sosialhjelp.domain.OpplastetVedlegg;
import no.nav.sbl.sosialhjelp.domain.VedleggType;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
public class VedleggConverter {

    @Inject
    private VedleggService vedleggService;

    public List<OpplastetVedlegg> mapVedleggListeTilOpplastetVedleggListe(Long soknadUnderArbeidId, String eier, List<Vedlegg> vedleggListe) {
        if (soknadUnderArbeidId == null || isEmpty(eier) || vedleggListe == null) {
            return null;
        }
        final List<Vedlegg> vedleggSomErLastetOpp = vedleggListe.stream()
                .filter(Objects::nonNull)
                .filter(vedlegg -> vedlegg.getInnsendingsvalg().er(Vedlegg.Status.LastetOpp))
                .collect(Collectors.toList());

        if (vedleggSomErLastetOpp.isEmpty()) {
            return new ArrayList<>();
        }
        List<OpplastetVedlegg> opplastedeVedlegg = new ArrayList<>();
        for (Vedlegg vedlegg : vedleggSomErLastetOpp) {
            if (isNotEmpty(vedlegg.getFilnavn())) {
                opplastedeVedlegg.add(mapVedleggTilOpplastetVedlegg(soknadUnderArbeidId, eier, vedlegg));
            }
        }
        return opplastedeVedlegg;
    }

    OpplastetVedlegg mapVedleggTilOpplastetVedlegg(Long soknadUnderArbeidId, String eier, Vedlegg vedlegg) {
        Vedlegg vedleggMedInnhold = vedleggService.hentVedlegg(vedlegg.getVedleggId(), true);
        if (vedleggMedInnhold == null || vedleggMedInnhold.getData() == null) {
            return null;
        }
        byte[] data = vedleggMedInnhold.getData();
        return new OpplastetVedlegg().withSoknadId(soknadUnderArbeidId)
                .withVedleggType(new VedleggType(vedlegg.getSkjemaNummer() + "|" + vedlegg.getSkjemanummerTillegg()))
                .withData(data)
                .withEier(eier)
                .withFilnavn(vedlegg.getFilnavn())
                .withSha512(vedlegg.getSha512());
    }
}
