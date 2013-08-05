package no.nav.sbl.dialogarena.soknad.common;

import org.apache.commons.collections15.Transformer;
import org.apache.wicket.request.mapper.parameter.INamedParameters;

public class SoknadId implements Transformer<INamedParameters, Long> {
    @Override
    public final Long transform(INamedParameters iNamedParameters) {
        return iNamedParameters.get("soknadId").toLong();
    }

    public static Long get(INamedParameters iNamedParameters) {
        return new SoknadId().transform(iNamedParameters);
    }
}