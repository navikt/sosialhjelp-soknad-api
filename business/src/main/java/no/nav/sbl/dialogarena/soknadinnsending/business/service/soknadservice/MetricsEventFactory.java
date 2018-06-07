package no.nav.sbl.dialogarena.soknadinnsending.business.service.soknadservice;

import no.nav.metrics.Event;
import no.nav.metrics.MetricsFactory;

public class MetricsEventFactory {
    Event createEvent(String name) {
        return MetricsFactory.createEvent(name);
    }
}