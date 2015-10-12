package no.nav.sbl.dialogarena.soknadinnsending.business.service;

import org.mockserver.mock.action.ExpectationCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AktivitetServiceIntegrationCallback implements ExpectationCallback{
    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        Matcher matcher = Pattern.compile("<MessageID.*>(.*)</MessageID>", Pattern.MULTILINE).matcher(httpRequest.getBodyAsString());
        matcher.find();
        String messageId = matcher.group(1);
        System.out.println(messageId);
        return HttpResponse.response().withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Header><wsa:To xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">http://www.w3.org/2005/08/addressing/anonymous</wsa:To><wsa:Action xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">http://nav.no/tjeneste/virksomhet/sakOgAktivitet/v1/sakOgAktivitet_v1/finnAktivitetsinformasjonListe/Fault/personIkkeFunnet</wsa:Action><wsa:MessageID xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">uuid:9cf4856d-2fc8-484b-8510-58dc87dc7778</wsa:MessageID><wsa:RelatesTo xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" + messageId + "</wsa:RelatesTo><work:WorkContext xmlns:work=\"http://oracle.com/weblogic/soap/workarea/\">rO0ABXdfACl3ZWJsb2dpYy5hcHAubmF2LXByb3ZpZGVyLXNhay1hcmVuYS1lYXItMgAAANYAAAAjd2VibG9naWMud29ya2FyZWEuU3RyaW5nV29ya0NvbnRleHQABTIuNi45AAA=</work:WorkContext></S:Header><S:Body><ns2:Fault xmlns:ns2=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns3=\"http://www.w3.org/2003/05/soap-envelope\"><faultcode>ns2:Server</faultcode><faultstring>Person med f&#248;dselsnummer 15121285972 finnes ikke i Arena</faultstring><detail><ns2:finnAktivitetsinformasjonListepersonIkkeFunnet xmlns:ns2=\"http://nav.no/tjeneste/virksomhet/sakOgAktivitet/v1\"><feilkilde>ARENA.SakOgAktivitet:finnAktivitetsinformasjonListe</feilkilde><feilaarsak>-20807</feilaarsak><feilmelding>Person med f&#248;dselsnummer 15121285972 finnes ikke i Arena</feilmelding><tidspunkt>2015-10-08T09:27:39.067+02:00</tidspunkt></ns2:finnAktivitetsinformasjonListepersonIkkeFunnet></detail></ns2:Fault></S:Body></S:Envelope>");
    }
}
