package no.nav.sbl.dialogarena.dokumentinnsending.pages.innsendingkvittering;

import no.nav.modig.core.exception.ApplicationException;
import no.nav.sbl.dialogarena.dokumentinnsending.common.BrukerBehandlingId;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.Dokument;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.DokumentSoknad;
import no.nav.sbl.dialogarena.dokumentinnsending.domain.SoknadStatus;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.base.mainbasepage.MainBasePage;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.DittNavLink;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.DokumentListeInnsendingsvalg;
import no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.StegIndikator;
import no.nav.sbl.dialogarena.dokumentinnsending.service.SoknadService;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import javax.inject.Inject;
import java.util.List;

/**
 * Viser en kvitteringsside for hvilke dokumenter som er sendt inn til NAV og hvilke dokumenter som mangler
 */
public class InnsendingKvitteringPage extends MainBasePage {

    @Inject
    private SoknadService soknadService;


    public InnsendingKvitteringPage() {
        this(new PageParameters());
    }

    public InnsendingKvitteringPage(PageParameters parameters) {
        super(parameters);

        final String behandlingsId = BrukerBehandlingId.get(parameters);

        setDefaultModel(new CompoundPropertyModel<>(new LoadableDetachableModel<InnsendingKvitteringViewModel>() {
            @Override
            protected InnsendingKvitteringViewModel load() {
                DokumentSoknad soknad = soknadService.hentSoknad(behandlingsId);
                return new InnsendingKvitteringViewModel(soknad, cmsContentRetriever);
            }
        }));

        add(new StegIndikator("stegIndikator"));

        IModel<List<Dokument>> innsendteDokumenter = new LoadableDetachableModel<List<Dokument>>() {
            @Override
            protected List<Dokument> load() {
                InnsendingKvitteringViewModel defaultModelObject = (InnsendingKvitteringViewModel) InnsendingKvitteringPage.this.getDefaultModelObject();
                return defaultModelObject.innsendteDokumenter;
            }
        };
        IModel<List<Dokument>> ikkeSendteDokumenter = new LoadableDetachableModel<List<Dokument>>() {
            @Override
            protected List<Dokument> load() {
                InnsendingKvitteringViewModel defaultModelObject = (InnsendingKvitteringViewModel) InnsendingKvitteringPage.this.getDefaultModelObject();
                return defaultModelObject.ikkeSendteDokumenter;
            }
        };

        add(new Label("sideTittel"));
        add(new Label("innsendtDato"));
        add(new Label("kvitteringTekst"));
        add(new Label("kvittering.innsendt.gaaTilDittNAV", cmsContentRetriever.hentTekst("kvittering.innsendt.gaaTilDittNAV")));
        add(new Label("beskrivelse.ettersending.gaaTilLenke", cmsContentRetriever.hentTekst("bekreftelsesside.beskrivelse")));
        add(new Label("beskrivelse.ettersending.tekst", cmsContentRetriever.hentTekst("beskrivelse.ettersending.tekst")));
        add(new InnsendteDokumenterBekreftelse("dokumenterAlert", innsendteDokumenter));
        add(new DokumentListeInnsendingsvalg("innsendteDokumenter", innsendteDokumenter, "dokumentliste.sendteDokumenter", false));
        add(new DokumentListeInnsendingsvalg("ikkeSendteDokumenter", ikkeSendteDokumenter, "dokumentliste.manglendeDokumenter", false));
        add(new ExternalLink("skjema", cmsContentRetriever.hentTekst("beskrivelse.ettersending.lenke"), cmsContentRetriever.hentTekst("beskrivelse.ettersending.lenkeTekst")));
        add(new DittNavLink("dittNav", "kvittering.innsendt.dittNAV"));
    }

    @Override
    protected void onConfigure() {
        validerAtTilstandTillaterAApneSiden();
        super.onConfigure();
    }

    // Validerer at søknaden har tilstand som tillater å gå til denne siden
    private void validerAtTilstandTillaterAApneSiden() {
        InnsendingKvitteringViewModel viewModel = (InnsendingKvitteringViewModel) getDefaultModelObject();

        if (viewModel.status.erIkke(SoknadStatus.FERDIG)) {
            throw new ApplicationException("Henvendelsen er ikke avsluttet");
        } else if (viewModel.innsendteDokumenter.isEmpty()) {
            throw new ApplicationException("Henvendelsen har ingen opplastede dokumenter");
        }
    }
}