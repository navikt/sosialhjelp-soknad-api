package no.nav.sbl.dialogarena.dokumentinnsending.pages.session;

import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;


public class DokumentinnsendingSession extends WebSession {

    public DokumentinnsendingSession(Request request) {
        super(request);
    }

    public static DokumentinnsendingSession get() {
        return (DokumentinnsendingSession) Session.get();
    }
}
