package no.nav.sbl.dialogarena.sendsoknad.mockmodul.organisasjon;

import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.FinnOrganisasjonForMangeForekomster;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.FinnOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.FinnOrganisasjonsendringerListeUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentNoekkelinfoOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentNoekkelinfoOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.HentOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.OrganisasjonV4;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.ValiderOrganisasjonOrganisasjonIkkeFunnet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.binding.ValiderOrganisasjonUgyldigInput;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.UstrukturertNavn;
import no.nav.tjeneste.virksomhet.organisasjon.v4.informasjon.Virksomhet;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.FinnOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.FinnOrganisasjonResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.FinnOrganisasjonsendringerListeRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.FinnOrganisasjonsendringerListeResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentNoekkelinfoOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentNoekkelinfoOrganisasjonResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonsnavnBolkRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentOrganisasjonsnavnBolkResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentVirksomhetsOrgnrForJuridiskOrgnrBolkRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.HentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.ValiderOrganisasjonRequest;
import no.nav.tjeneste.virksomhet.organisasjon.v4.meldinger.ValiderOrganisasjonResponse;

public class OrganisasjonMock implements OrganisasjonV4 {

	@Override
	public void ping() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FinnOrganisasjonResponse finnOrganisasjon(FinnOrganisasjonRequest request)
			throws FinnOrganisasjonForMangeForekomster, FinnOrganisasjonUgyldigInput {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HentOrganisasjonsnavnBolkResponse hentOrganisasjonsnavnBolk(HentOrganisasjonsnavnBolkRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HentOrganisasjonResponse hentOrganisasjon(HentOrganisasjonRequest request)
			throws HentOrganisasjonOrganisasjonIkkeFunnet, HentOrganisasjonUgyldigInput {
		HentOrganisasjonResponse response = new HentOrganisasjonResponse();
        Organisasjon organisasjon = new Virksomhet();
        UstrukturertNavn value = new UstrukturertNavn();
        value.getNavnelinje().add("Mock navn arbeidsgiver");
        value.getNavnelinje().add("");
        value.getNavnelinje().add("");
        value.getNavnelinje().add("");
        organisasjon.setNavn(value);
        response.setOrganisasjon(organisasjon);
		return response;
	}

	@Override
	public HentNoekkelinfoOrganisasjonResponse hentNoekkelinfoOrganisasjon(HentNoekkelinfoOrganisasjonRequest request)
			throws HentNoekkelinfoOrganisasjonOrganisasjonIkkeFunnet, HentNoekkelinfoOrganisasjonUgyldigInput {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValiderOrganisasjonResponse validerOrganisasjon(ValiderOrganisasjonRequest request)
			throws ValiderOrganisasjonOrganisasjonIkkeFunnet, ValiderOrganisasjonUgyldigInput {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HentVirksomhetsOrgnrForJuridiskOrgnrBolkResponse hentVirksomhetsOrgnrForJuridiskOrgnrBolk(
			HentVirksomhetsOrgnrForJuridiskOrgnrBolkRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FinnOrganisasjonsendringerListeResponse finnOrganisasjonsendringerListe(
			FinnOrganisasjonsendringerListeRequest request) throws FinnOrganisasjonsendringerListeUgyldigInput {
		// TODO Auto-generated method stub
		return null;
	}

}
