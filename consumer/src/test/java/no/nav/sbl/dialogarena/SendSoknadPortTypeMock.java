package no.nav.sbl.dialogarena;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSBrukerData;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSSoknadData;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.informasjon.WSSoknadDataOppsummering;

public class SendSoknadPortTypeMock implements SendSoknadPortType {

	@Override
	@WebResult(name = "return", targetNamespace = "")
	@RequestWrapper(localName = "ping", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.PingRequest")
	@WebMethod
	@ResponseWrapper(localName = "pingResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.PingResponse")
	public boolean ping() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@RequestWrapper(localName = "sendSoknad", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.SendSoknadRequest")
	@WebMethod
	@ResponseWrapper(localName = "sendSoknadResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.SendSoknadResponse")
	public void sendSoknad(
			@WebParam(name = "soknadId", targetNamespace = "") long soknadId) {
		// TODO Auto-generated method stub

	}

	@Override
	@WebResult(name = "brukerData", targetNamespace = "")
	@RequestWrapper(localName = "hentBrukerData", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.HentBrukerDataRequest")
	@WebMethod
	@ResponseWrapper(localName = "hentBrukerDataResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.HentBrukerDataResponse")
	public List<WSBrukerData> hentBrukerData(
			@WebParam(name = "soknadId", targetNamespace = "") long soknadId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "soknadData", targetNamespace = "")
	@RequestWrapper(localName = "hentSoknadData", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.HentSoknadDataRequest")
	@WebMethod
	@ResponseWrapper(localName = "hentSoknadDataResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.HentSoknadDataResponse")
	public String hentSoknadData(
			@WebParam(name = "soknadId", targetNamespace = "") long soknadId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@RequestWrapper(localName = "avbrytSoknad", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.AvbrytSoknadRequest")
	@WebMethod
	@ResponseWrapper(localName = "avbrytSoknadResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.AvbrytSoknadResponse")
	public void avbrytSoknad(
			@WebParam(name = "soknadId", targetNamespace = "") long soknadId) {
		// TODO Auto-generated method stub

	}

	@Override
	@RequestWrapper(localName = "lagreBrukerData", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.LagreBrukerDataRequest")
	@WebMethod
	@ResponseWrapper(localName = "lagreBrukerDataResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.LagreBrukerDataResponse")
	public void lagreBrukerData(
			@WebParam(name = "soknadId", targetNamespace = "") long soknadId,
			@WebParam(name = "nokkel", targetNamespace = "") String nokkel,
			@WebParam(name = "verdi", targetNamespace = "") String verdi) {
		// TODO Auto-generated method stub

	}

	@Override
	@WebResult(name = "soknadId", targetNamespace = "")
	@RequestWrapper(localName = "startSoknad", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.StartSoknadRequest")
	@WebMethod
	@ResponseWrapper(localName = "startSoknadResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.StartSoknadResponse")
	public long startSoknad(
			@WebParam(name = "soknadGosysId", targetNamespace = "") String soknadGosysId) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	@WebResult(name = "soknadData", targetNamespace = "")
	@RequestWrapper(localName = "hentSoknad", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.HentSoknadRequest")
	@WebMethod
	@ResponseWrapper(localName = "hentSoknadResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.HentSoknadResponse")
	public WSSoknadData hentSoknad(
			@WebParam(name = "soknadId", targetNamespace = "") long soknadId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@WebResult(name = "soknadData", targetNamespace = "")
	@RequestWrapper(localName = "hentSoknadListe", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.HentSoknadListeRequest")
	@WebMethod
	@ResponseWrapper(localName = "hentSoknadListeResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.HentSoknadListeResponse")
	public List<WSSoknadDataOppsummering> hentSoknadListe(
			@WebParam(name = "aktorId", targetNamespace = "") String aktorId) {
		// TODO Auto-generated method stub
		return null;
	}

}
