package no.nav.sbl.dialogarena.dokumentinnsending.common;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import static no.nav.sbl.dialogarena.dokumentinnsending.pages.felles.validators.BehandlingsIdValidator.validerBehandlingsId;

public class DokumentinnsendingParameters extends PageParameters {

    public DokumentinnsendingParameters() {
    }

    public DokumentinnsendingParameters(String behandlingsId) {
        super();
        behandlingsId(behandlingsId);
    }

    public String behandlingsId() {
        return validerBehandlingsId(get("brukerBehandlingId").toString());
    }


    public final DokumentinnsendingParameters behandlingsId(String brukerBehandlingId) {
        set("brukerBehandlingId", brukerBehandlingId, getPosition("brukerBehandlingId"));
        return this;
    }

    public final DokumentinnsendingParameters scrollTo(Long dokumentforventningId) {
        set("scrollTo", dokumentforventningId);
        return this;
    }

    public DokumentinnsendingParameters set(String name, Object value) {
        super.set(name, value);
        return this;
    }
}
