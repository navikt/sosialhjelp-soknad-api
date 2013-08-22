package no.nav.sbl.dialogarena.dokumentinnsending.service;

import no.nav.sbl.dialogarena.dokumentinnsending.builder.PersonFactory;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.BrukerprofilPortType;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserRequest;
import no.nav.tjeneste.virksomhet.brukerprofil.v1.meldinger.XMLHentKontaktinformasjonOgPreferanserResponse;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

public class PersonServiceStub implements BrukerprofilPortType{


	@Override
	@WebResult(name = "response", targetNamespace = "")
	@RequestWrapper(localName = "hentKontaktinformasjonOgPreferanser", targetNamespace = "http://nav.no/tjeneste/virksomhet/brukerprofil/v1/", className = "no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanser")
	@WebMethod
	@ResponseWrapper(localName = "hentKontaktinformasjonOgPreferanserResponse", targetNamespace = "http://nav.no/tjeneste/virksomhet/brukerprofil/v1/", className = "no.nav.tjeneste.virksomhet.brukerprofil.v1.HentKontaktinformasjonOgPreferanserResponse")
	public XMLHentKontaktinformasjonOgPreferanserResponse hentKontaktinformasjonOgPreferanser(
			@WebParam(name = "request", targetNamespace = "") XMLHentKontaktinformasjonOgPreferanserRequest request)
			throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet {
		
		String ident = erIdentifikasjonGyldig(request);
		
		
		return PersonFactory.lagPerson(ident);
	}


	private String erIdentifikasjonGyldig(XMLHentKontaktinformasjonOgPreferanserRequest request) throws HentKontaktinformasjonOgPreferanserPersonIkkeFunnet{
		String ident = request.getIdent();
		if (ident == null || ident.length() < 11) {
			throw new HentKontaktinformasjonOgPreferanserPersonIkkeFunnet("Fant ikke person med ident " + ident);
		}
		return ident;
	}

	@Override
	@RequestWrapper(localName = "ping", targetNamespace = "http://nav.no/tjeneste/virksomhet/brukerprofil/v1/", className = "no.nav.tjeneste.virksomhet.brukerprofil.v1.Ping")
	@WebMethod
	@ResponseWrapper(localName = "pingResponse", targetNamespace = "http://nav.no/tjeneste/virksomhet/brukerprofil/v1/", className = "no.nav.tjeneste.virksomhet.brukerprofil.v1.PingResponse")
	public void ping() {}

}
