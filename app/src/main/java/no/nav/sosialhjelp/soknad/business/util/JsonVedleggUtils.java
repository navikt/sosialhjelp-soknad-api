package no.nav.sosialhjelp.soknad.business.util;

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sosialhjelp.soknad.business.domain.SoknadMetadata;
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid;

import java.util.ArrayList;
import java.util.List;

public class JsonVedleggUtils {

    public static final String ANNET = "annet";

    public static List<JsonVedlegg> getVedleggFromInternalSoknad(SoknadUnderArbeid soknadUnderArbeid)  {
        return soknadUnderArbeid.getJsonInternalSoknad().getVedlegg() == null ? new ArrayList<>() :
                soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg() == null ? new ArrayList<>() :
                        soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg();
    }

    public static boolean isVedleggskravAnnet(SoknadMetadata.VedleggMetadata vedlegg) {
        return ANNET.equals(vedlegg.skjema) && ANNET.equals(vedlegg.tillegg);
    }
}
