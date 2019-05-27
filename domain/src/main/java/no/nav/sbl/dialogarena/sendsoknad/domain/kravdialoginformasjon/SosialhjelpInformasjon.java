package no.nav.sbl.dialogarena.sendsoknad.domain.kravdialoginformasjon;


import no.nav.sbl.dialogarena.sendsoknad.domain.WebSoknad;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.AlternativRepresentasjonTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.EkstraMetadataTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.FiksMetadataTransformer;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SosialhjelpTilJson;
import no.nav.sbl.dialogarena.sendsoknad.domain.transformer.sosialhjelp.SosialhjelpVedleggTilJson;
import no.nav.sbl.dialogarena.soknadsosialhjelp.message.NavMessageSource;
import org.springframework.context.MessageSource;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class SosialhjelpInformasjon extends KravdialogInformasjon.DefaultOppsett {

    public static final String SKJEMANUMMER = "NAV 35-18.01";

    public String getSoknadTypePrefix() {
        return "soknadsosialhjelp";
    }

    public String getSoknadUrlKey() {
        return "soknadsosialhjelp.path";
    }

    public String getFortsettSoknadUrlKey() {
        return "soknadsosialhjelp.fortsett.path";
    }

    public List<String> getSkjemanummer() {
        return asList(SKJEMANUMMER);
    }

    @Override
    public List<AlternativRepresentasjonTransformer> getTransformers(MessageSource messageSource, WebSoknad soknad) {
        if (soknad.erEttersending()) {
            return singletonList(new SosialhjelpVedleggTilJson());
        }

        return asList(new SosialhjelpVedleggTilJson(), new SosialhjelpTilJson((NavMessageSource) messageSource));
    }

    @Override
    public List<EkstraMetadataTransformer> getMetadataTransformers(WebSoknad soknad) {
        if (soknad.erEttersending()) {
            return emptyList();
        }
        return singletonList(new FiksMetadataTransformer());
    }

    @Override
    public String getBundleName() {
        return "soknadsosialhjelp";
    }

    @Override
    public boolean brukerNyOppsummering() {
        return true;
    }

    @Override
    public boolean skalSendeMedFullSoknad() {
        return true;
    }

    @Override
    public SoknadType getSoknadstype() {
        return SoknadType.SEND_SOKNAD_KOMMUNAL;
    }

    @Override
    public String getKvitteringTemplate() {
        return "/skjema/kvittering/kvittering";
    }
}