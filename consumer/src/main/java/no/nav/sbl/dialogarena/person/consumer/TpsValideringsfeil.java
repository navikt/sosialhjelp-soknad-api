package no.nav.sbl.dialogarena.person.consumer;

import no.nav.tjeneste.virksomhet.behandlebrukerprofil.v1.OppdaterKontaktinformasjonOgPreferanserUgyldigInput;
import org.apache.commons.collections15.Transformer;

import java.util.List;

import static java.util.Arrays.asList;
import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.exists;
import static no.nav.modig.lang.collections.PredicateUtils.where;
import static no.nav.modig.lang.option.Optional.optional;
import static no.nav.sbl.dialogarena.person.consumer.transform.Transform.feilaarsakkode;

public enum TpsValideringsfeil {

    MIDLERTIDIG_ADRESSE_LIK_FOLKEREGISTRERT("ugyldig-input.midlertidig-lik-folkeregistrert", "TPS_T5A00001", "TPS_T5A00006"),
    UGYLDIG_POSTNUMMER("ugyldig-input.ugyldig-postnummer", "TPS_T63D0043"),
    UKJENT(null, new String[0]);

    public final List<String> feilkoder;
    public final String feilmeldingMsgKey;

    private TpsValideringsfeil(String feilmeldingMsgKey, String ... feilkoder) {
		this.feilkoder = asList(feilkoder);
		this.feilmeldingMsgKey = feilmeldingMsgKey;
	}

	static TpsValideringsfeil fra(OppdaterKontaktinformasjonOgPreferanserUgyldigInput exception) {
        String feilaarsak = optional(exception.getFaultInfo()).map(feilaarsakkode()).getOrElse(null);
        return on(values()).filter(where(KODER, exists(feilaarsak))).head().getOrElse(UKJENT);
    }

    private static final Transformer<TpsValideringsfeil, List<String>> KODER = new Transformer<TpsValideringsfeil, List<String>>() {
        @Override
        public List<String> transform(TpsValideringsfeil feil) {
            return feil.feilkoder;
        }
    };

}
