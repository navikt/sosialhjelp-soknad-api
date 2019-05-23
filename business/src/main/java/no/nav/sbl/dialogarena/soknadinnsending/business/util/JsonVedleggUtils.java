package no.nav.sbl.dialogarena.soknadinnsending.business.util;

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg;
import no.nav.sbl.sosialhjelp.domain.SoknadUnderArbeid;

import java.util.ArrayList;
import java.util.List;

public class JsonVedleggUtils {
    public static List<JsonVedlegg> getVedleggFromInternalSoknad(SoknadUnderArbeid soknadUnderArbeid)  {
        return soknadUnderArbeid.getJsonInternalSoknad().getVedlegg() == null ? new ArrayList<>() :
                soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg() == null ? new ArrayList<>() :
                        soknadUnderArbeid.getJsonInternalSoknad().getVedlegg().getVedlegg();
    }
}
