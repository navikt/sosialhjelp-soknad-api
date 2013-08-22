package no.nav.sbl.dialogarena.dokumentinnsending.common;

import org.apache.commons.collections15.Transformer;
import org.apache.wicket.request.mapper.parameter.INamedParameters;

import static no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.validators.BehandlingsIdValidator.validerBehandlingsId;

/**
 * Klasse som brukes til å hente brukerbehandlings id fra path.
 * Validerer også at dette er en korrekt verdi
 */
public final class BrukerBehandlingId implements Transformer<INamedParameters, String> {
    private BrukerBehandlingId() {
    }

    @Override
    public String transform(INamedParameters pageParameters) {
        return pageParameters.get("brukerBehandlingId").toString();
    }

    public static String get(INamedParameters pageParameters) {
        return validerBehandlingsId(new BrukerBehandlingId().transform(pageParameters));
    }
}
