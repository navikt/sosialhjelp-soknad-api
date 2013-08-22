package no.nav.sbl.dialogarena.dokumentinnsending.pages.felles;

import no.nav.modig.wicket.events.NamedEventPayload;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.link.Link;

public class EventLink extends Link<Void> {

    private String event;
    private String behandlingsId;

    public EventLink(String id, String event, String behandlingsId) {
        super(id);
        this.event = event;
        this.behandlingsId = behandlingsId;
    }

    @Override
    public void onClick() {
        send(getPage(), Broadcast.BREADTH, new NamedEventPayload(event, behandlingsId));
    }
}
