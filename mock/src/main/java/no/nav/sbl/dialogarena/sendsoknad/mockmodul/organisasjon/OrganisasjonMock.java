package no.nav.sbl.dialogarena.sendsoknad.mockmodul.organisasjon;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.*;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Virksomhet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.*;

public class OrganisasjonMock implements OrganisasjonV4 {

    @Override
    public void ping() {
    }

    @Override
    public FinnOrganisasjonResponse finnOrganisasjon(FinnOrganisasjonRequest request)
            throws FinnOrganisasjonForMangeForekomster, FinnOrganisasjonUgyldigInput {
        throw new UnsupportedOperationException();
    }

    @Override
    public HentOrganisasjonsnavnBolkResponse hentOrganisasjonsnavnBolk(HentOrganisasjonsnavnBolkRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HentOrganisasjonResponse hentOrganisasjon(HentOrganisasjonRequest request)
            throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput {
        HentOrganisasjonResponse response = new HentOrganisasjonResponse();
        Organisasjon organisasjon = new Virksomhet();
        UstrukturertNavn value = new UstrukturertNavn();
        value.getNavnelinje().add("Mock navn arbeidsgiver");
        organisasjon.setNavn(value);
        response.setOrganisasjon(organisasjon);
        return response;
    }

    @Override
    public HentNoekkelinfoOrganisasjonResponse hentNoekkelinfoOrganisasjon(HentNoekkelinfoOrganisasjonRequest request)
            throws HentNoekkelinfoOrganisasjonOrganisasjonIkkeFunnet, HentNoekkelinfoOrganisasjonUgyldigInput {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValiderOrganisasjonResponse validerOrganisasjon(ValiderOrganisasjonRequest request)
            throws ValiderOrganisasjonOrganisasjonIkkeFunnet, ValiderOrganisasjonUgyldigInput {
        throw new UnsupportedOperationException();
    }

    @Override
    public HentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse hentVirksomhetsOrgnrForJuridiskOrgnrBolk(
            HentVirksomhetsOrgnrForJuridiskOrgnrBolkRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FinnOrganisasjonsendringerListeResponse finnOrganisasjonsendringerListe(
            FinnOrganisasjonsendringerListeRequest request) throws FinnOrganisasjonsendringerListeUgyldigInput {
        throw new UnsupportedOperationException();
    }

}
