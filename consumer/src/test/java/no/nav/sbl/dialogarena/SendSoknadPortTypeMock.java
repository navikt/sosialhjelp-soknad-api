package no.nav.sbl.dialogarena;

import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.SendSoknadPortType;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSEmpty;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSSoknadsdata;
import no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSStartSoknadRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import java.util.List;

public class SendSoknadPortTypeMock implements SendSoknadPortType {

    @Override
    @RequestWrapper(localName = "ping", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSEmpty")
    @WebMethod
    @ResponseWrapper(localName = "pingResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSEmpty")
    public void ping() {
        // TODO Auto-generated method stub
        
    }

    @Override
    @SOAPBinding(parameterStyle = ParameterStyle.BARE)
    @WebResult(name = "sendSoknadResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", partName = "parameters")
    @WebMethod
    public WSEmpty sendSoknad(@WebParam(partName = "parameters", name = "sendSoknad", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1") WSSoknadsdata parameters) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @SOAPBinding(parameterStyle = ParameterStyle.BARE)
    @WebResult(name = "mellomlagreSoknadResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", partName = "parameters")
    @WebMethod
    public WSEmpty mellomlagreSoknad(@WebParam(partName = "parameters", name = "mellomlagreSoknad", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1") WSSoknadsdata parameters) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @SOAPBinding(parameterStyle = ParameterStyle.BARE)
    @WebResult(name = "hentSoknadResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", partName = "parameters")
    @WebMethod
    public WSSoknadsdata hentSoknad(@WebParam(partName = "parameters", name = "hentSoknad", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1") WSBehandlingsId parameters) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @RequestWrapper(localName = "avbrytSoknad", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSBehandlingsId")
    @WebMethod
    @ResponseWrapper(localName = "avbrytSoknadResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", className = "no.nav.tjeneste.domene.brukerdialog.sendsoknad.v1.meldinger.WSEmpty")
    public void avbrytSoknad(@WebParam(name = "behandlingsId", targetNamespace = "") String behandlingsId) {
        // TODO Auto-generated method stub
        
    }

    @Override
    @SOAPBinding(parameterStyle = ParameterStyle.BARE)
    @WebResult(name = "startSoknadResponse", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1", partName = "parameters")
    @WebMethod
    public WSBehandlingsId startSoknad(@WebParam(partName = "parameters", name = "startSoknad", targetNamespace = "http://nav.no/tjeneste/domene/brukerdialog/sendsoknad/v1") WSStartSoknadRequest parameters) {
        // TODO Auto-generated method stub
        return null;
    }


	
}
