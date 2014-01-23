package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import org.joda.time.DateTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WebSoknad implements Serializable {

    private Long soknadId;
    private String skjemaNummer;
    private String uuid;
    private String brukerBehandlingId;
    private Map<String, Faktum> fakta;
    private SoknadInnsendingStatus status;
    private String aktoerId;
    private DateTime opprettetDato;
    private DateTime sistLagret;
    private DelstegStatus delstegStatus;

    private static final List<String> LIST_FAKTUM = asList("barn", "barnetillegg", "ikkebarneinntekt", "barneinntekttall", "orgnummer", "arbeidsforhold", "sluttaarsak");

    public Long getSistLagret() {
        if (sistLagret != null) {
            return sistLagret.getMillis();
        } else {
            return null;
        }
    }

    public void setSistLagret(DateTime sistLagret) {
        this.sistLagret = sistLagret;
    }

    public DelstegStatus getDelstegStatus() {
        return delstegStatus;
    }

    public void setDelstegStatus(DelstegStatus delstegStatus) {
        this.delstegStatus = delstegStatus;
    }

    public WebSoknad() {
        uuid = randomUUID().toString();
        fakta = new LinkedHashMap<>();
    }

    public final String getUuid() {
        return uuid;
    }

    public final Long getSoknadId() {
        return soknadId;
    }

    public final void setSoknadId(Long soknadId) {
        this.soknadId = soknadId;
    }

    public final String getskjemaNummer() {
        return skjemaNummer;
    }

    public final void setskjemaNummer(String skjemaNummer) {
        this.skjemaNummer = skjemaNummer;
    }

    public final Map<String, Faktum> getFakta() {
        return fakta;
    }

    public final void leggTilFakta(Map<String, Faktum> fakta) {
        this.fakta.putAll(fakta);
    }

    public final void leggTilFaktum(String key, Faktum faktum) {
        this.fakta.put(key, faktum);
    }

    public String getBrukerBehandlingId() {
        return brukerBehandlingId;
    }

    public void setBrukerBehandlingId(String brukerBehandlingId) {
        this.brukerBehandlingId = brukerBehandlingId;
    }

    public long antallFakta() {
        long antallFaktum = 0;
        if (fakta != null) {
            antallFaktum = fakta.size();
        }
        return antallFaktum;
    }

    public String getAktoerId() {
        return aktoerId;
    }

    @Override
    public String toString() {
        return "WebSoknad [soknadId=" + soknadId + ", skjemaNummer=" + skjemaNummer
                + ", brukerBehandlingId=" + brukerBehandlingId + ", fakta="
                + fakta + "]";
    }

    public SoknadInnsendingStatus getStatus() {
        return status;
    }

    public static WebSoknad startSoknad() {
        return new WebSoknad();
    }

    public WebSoknad medAktorId(String aktorId) {
        this.aktoerId = aktorId;
        return this;
    }

    public WebSoknad medskjemaNummer(String skjemaNummer) {
        this.skjemaNummer = skjemaNummer;
        return this;
    }

    public WebSoknad medBehandlingId(String behandlingsId) {
        this.brukerBehandlingId = behandlingsId;
        return this;
    }

    public WebSoknad opprettetDato(DateTime opprettetDato) {
        this.opprettetDato = opprettetDato;
        return this;
    }

    public WebSoknad sistLagret(Timestamp sistLagret) {
        if (sistLagret != null) {
            this.sistLagret = new DateTime(sistLagret.getTime());
        } else {
            this.sistLagret = null;
        }

        return this;
    }

    public Long getOpprettetDato() {
        return opprettetDato.getMillis();
    }

    public WebSoknad medId(long id) {
        this.soknadId = id;
        return this;
    }

    public WebSoknad medStatus(SoknadInnsendingStatus status) {
        this.status = status;
        return this;
    }

    public WebSoknad medBrukerData(List<Faktum> brukerData) {
        fakta = new HashMap<>();

        for (Faktum faktum : brukerData) {
            if (LIST_FAKTUM.contains(faktum.getKey())) {
                Faktum nyttFaktum = wrapFaktumIFaktumListeObjekt(faktum, fakta);
                fakta.put(faktum.getKey(), nyttFaktum);
            } else {
                fakta.put(faktum.getKey(), faktum);
            }
        }
        return this;

    }

    private Faktum wrapFaktumIFaktumListeObjekt(Faktum faktum, Map<String, Faktum> fakta) {
        if (fakta.containsKey(faktum.getKey())) {
            Faktum eksisterendeFaktum = fakta.get(faktum.getKey());
            List<Faktum> valueList = eksisterendeFaktum.getValuelist();
            valueList.add(faktum);
            eksisterendeFaktum.setValuelist(valueList);
            return eksisterendeFaktum;
        } else {
            Faktum nyttFaktum = faktum.cloneFaktum();
            List<Faktum> valuelist = new ArrayList<>();
            valuelist.add(faktum);
            nyttFaktum.setValuelist(valuelist);
            return nyttFaktum;
        }
    }


    public WebSoknad medDelstegStatus(DelstegStatus delstegStatus) {
        this.delstegStatus = delstegStatus;
        return this;
    }

}
