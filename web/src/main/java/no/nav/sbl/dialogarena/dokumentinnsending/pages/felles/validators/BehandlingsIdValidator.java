package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.validators;

import no.nav.modig.core.exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public final class BehandlingsIdValidator {

    private static final Logger LOG = LoggerFactory.getLogger(BehandlingsIdValidator.class);
    private static final Pattern PATTERN = Pattern.compile("[A-Za-z0-9-]{4,100}");

    public static String validerBehandlingsId(String behandlingsId) {

        if (!PATTERN.matcher(behandlingsId).matches()) {
            LOG.error("Forsøk på å åpne siden for behandling med ugyldig behandlingsId: {}", behandlingsId);
            throw new ApplicationException("Behandlings-IDen har ikke gyldig format");
        }
        return behandlingsId;
    }
}
