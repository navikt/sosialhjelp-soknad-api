package no.nav.sbl.dialogarena.dokumentinnsending.pages.hjelp;

import no.nav.sbl.dialogarena.dokumentinnsending.common.SpraakKode;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.base.modalbasepage.ModalBasePage;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import no.nav.sbl.dialogarena.soknad.kodeverk.KodeverkSkjema;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;

public class HjelpPage extends ModalBasePage {
    private static final Logger LOGGER = LoggerFactory.getLogger(HjelpPage.class);


    @Inject
    private SoknadService soknadService;


    public HjelpPage(PageParameters parameters) {
        super(parameters);

        add(new Label("tabTittel", cmsContentRetriever.hentTekst("hjelp.sideTittel")));
        add(new Label("tittel", cmsContentRetriever.hentTekst("hjelp.tittel")));
        add(new Label("beskrivelse", cmsContentRetriever.hentTekst("hjelp.beskrivelse")));
        add(new DokumentlisteHjelp("dokumenter", soknadService.hentSoknad(behandlingsId).getDokumenter()));
    }

    private class DokumentlisteHjelp extends ListView<Dokument> {
        public DokumentlisteHjelp(String id, List<Dokument> dokumenter) {
            super(id, dokumenter);
        }

        @Override
        protected void populateItem(ListItem<Dokument> item) {
            item.add(new Label("dokumentnavn", new PropertyModel(item.getModel(), "navn")));
            item.add(new Label("dokumentBeskrivelse", hentBeskrivelseFraCms(item.getModel().getObject().getKodeverk())));
        }
    }

    private String hentBeskrivelseFraCms(KodeverkSkjema skjema) {
        if (StringUtils.isBlank(skjema.getBeskrivelse())) {
            return "";
        }

        try {
            //return cmsContentRetriever.hentTekst(skjema.getBeskrivelse());//, SpraakKode.gjeldendeSpraak().name().toLowerCase());

            String spraakKode = SpraakKode.gjeldendeSpraak().name().toLowerCase();
            return cmsContentRetriever.hentSkjemaBeskrivelse(skjema.getBeskrivelse(), spraakKode);

        } catch (Exception ex) {
            LOGGER.warn("Fant ikke tekst for kode i cms. ");
            return "N/A";
        }
    }
}
