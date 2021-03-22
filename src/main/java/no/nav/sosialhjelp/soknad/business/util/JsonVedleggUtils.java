package no.nav.sosialhjelp.soknad.business.util;

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.consumer.norg.NorgService;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JsonVedleggUtils {
    public static final String FEATURE_UTVIDE_VEDLEGGJSON = "sosialhjelp.soknad.utvide-vedlegg-json";

    private static final Logger log = LoggerFactory.getLogger(JsonVedleggUtils.class);

    private JsonVedleggUtils() {
    }

    public static final String ANNET = "annet";

    public static List<JsonVedlegg> getVedleggFromInternalSoknad(SoknadUnderArbeid soknadUnderArbeid)  {
        return soknadUnderArbeid.getJsonInternalSoknad().getVedlegg() == null ? new ArrayList<>() :
                soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg() == null ? new ArrayList<>() :
                        soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg();
    }

    public static boolean isVedleggskravAnnet(SoknadMetadata.VedleggMetadata vedlegg) {
        return ANNET.equals(vedlegg.skjema) && ANNET.equals(vedlegg.tillegg);
    }

    public static void addHendelseTypeAndHendelseReferanse(JsonVedleggSpesifikasjon jsonVedleggSpesifikasjon, boolean isUtvideVedleggJsonFeatureActive) {
        if (isUtvideVedleggJsonFeatureActive) {
            log.info("hendelsetype og hendelsereferanse blir inkludert i vedlegg.json");
            jsonVedleggSpesifikasjon.getVedlegg().forEach(vedlegg -> {
                vedlegg.setHendelseType(JsonVedlegg.HendelseType.SOKNAD);
                vedlegg.setHendelseReferanse(UUID.randomUUID().toString());
            });
        }
    }
}
