package no.nav.sbl.dialogarena.sendsoknad.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.nav.sbl.dialogarena.sendsoknad.domain.exception.UgyldigDelstegEndringException;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("PMD.TooManyMethods")
public class WebSoknad implements Serializable {
    private Long soknadId;
    private String skjemaNummer;
    private Integer versjon;
    private String uuid;
    private String brukerBehandlingId;
    private String behandlingskjedeId;
    // Trenger ekstra annotering fordi vi midlertidig har mellomlagrede soknader på gammelt format (faktaListe) i henvendelse.
    // Denne kan fjernes når gamle mellomlagrede søknader er borte i henvendelse
    @XmlElements({
            @XmlElement(name = "faktaListe", type = Faktum.class),
            @XmlElement(name = "fakta", type = Faktum.class)
    })
    private List<Faktum> fakta;
    private SoknadInnsendingStatus status;
    private String aktoerId;
    private DateTime opprettetDato;
    private DateTime sistLagret;
    private DelstegStatus delstegStatus;
    private List<Vedlegg> vedlegg;
    private String journalforendeEnhet;
    private String soknadPrefix;
    private String soknadUrl;
    private String fortsettSoknadUrl;
    private Steg[] stegliste;


    public WebSoknad() {
        fakta = new ArrayList<>();
        vedlegg = new ArrayList<>();
    }

    public static WebSoknad startSoknad() {
        return new WebSoknad().medStatus(SoknadInnsendingStatus.UNDER_ARBEID).medDelstegStatus(DelstegStatus.OPPRETTET);
    }

    public static WebSoknad startEttersending(String behandlingsId) {
        return new WebSoknad()
                .medStatus(SoknadInnsendingStatus.UNDER_ARBEID)
                .medDelstegStatus(DelstegStatus.ETTERSENDING_OPPRETTET)
                .medBehandlingId(behandlingsId)
                .medOppretteDato(DateTime.now());
    }

    public DateTime getSistLagret() {
        return sistLagret;
    }

    public String getJournalforendeEnhet() {
        return journalforendeEnhet;
    }

    public String getBehandlingskjedeId() {
        return behandlingskjedeId;
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

    public final Integer getVersjon() { return versjon; }

    private void setVersjon(Integer versjon) { this.versjon = versjon; }

    public List<Vedlegg> getVedlegg() {
        if (vedlegg == null) {
            vedlegg = new ArrayList<>();
        }
        return vedlegg;
    }

    public void setVedlegg(List<Vedlegg> vedlegg) {
        this.vedlegg = vedlegg;
    }

    public final void setSkjemaNummer(String skjemaNummer) {
        this.skjemaNummer = skjemaNummer;
    }

    public List<Faktum> getFakta() {
        return fakta;
    }

    public void setFakta(List<Faktum> nyeFakta) {
        if (nyeFakta == null) {
            fakta = new ArrayList<>();
        } else {
            fakta = nyeFakta;
        }
    }

    public Locale getSprak() {
        Faktum sprakFaktum = this.getFaktumMedKey("skjema.sprak");
        return sprakFaktum == null ? new Locale("nb", "NO") : LocaleUtils.toLocale(sprakFaktum.getValue());
    }

    public String getBrukerBehandlingId() {
        return brukerBehandlingId;
    }

    public void setBrukerBehandlingId(String brukerBehandlingId) {
        this.brukerBehandlingId = brukerBehandlingId;
    }


    public String getAktoerId() {
        return aktoerId;
    }

    public SoknadInnsendingStatus getStatus() {
        return status;
    }

    public WebSoknad medAktorId(String aktorId) {
        this.aktoerId = aktorId;
        return this;
    }

    public WebSoknad medskjemaNummer(String skjemaNummer) {
        setSkjemaNummer(skjemaNummer);
        return this;
    }

    public WebSoknad medVersjon(Integer versjon) {
        setVersjon(versjon);
        return this;
    }

    public WebSoknad medBehandlingId(String behandlingsId) {
        setBrukerBehandlingId(behandlingsId);
        return this;
    }

    public WebSoknad medFaktum(Faktum faktum) {
        getFakta().add(faktum);
        return this;
    }

    public WebSoknad medOppretteDato(DateTime opprettetDato) {
        this.opprettetDato = opprettetDato;
        return this;
    }

    public WebSoknad medBehandlingskjedeId(String behandlingskjedeId) {
        this.behandlingskjedeId = behandlingskjedeId;
        return this;
    }

    public WebSoknad medJournalforendeEnhet(String journalforendeEnhet) {
        this.journalforendeEnhet = journalforendeEnhet;
        return this;
    }

    public WebSoknad medSoknadUrl(String url) {
        this.soknadUrl = url;
        return this;
    }

    public WebSoknad medStegliste(Steg[] stegliste) {
        this.stegliste = stegliste;
        return this;
    }

    public WebSoknad medFortsettSoknadUrl(String url) {
        this.fortsettSoknadUrl = url;
        return this;
    }

    public WebSoknad sistLagret(Timestamp sistLagret) {
        if (sistLagret != null) {
            setSistLagret(new DateTime(sistLagret.getTime()));
        } else {
            this.sistLagret = null;
        }
        return this;
    }

    public DateTime getOpprettetDato() {
        return opprettetDato;
    }

    public WebSoknad medId(long id) {
        this.soknadId = id;
        return this;
    }

    public WebSoknad medStatus(SoknadInnsendingStatus status) {
        this.status = status;
        return this;
    }

    public WebSoknad medUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public WebSoknad medBrukerData(List<Faktum> brukerData) {
        setFakta(new ArrayList<>(brukerData));
        return this;
    }

    public WebSoknad medVedlegg(List<Vedlegg> vedlegg) {
        this.vedlegg = new ArrayList<>(vedlegg);
        return this;
    }

    public WebSoknad medDelstegStatus(DelstegStatus delstegStatus) {
        setDelstegStatus(delstegStatus);
        return this;
    }

    public List<Faktum> getFaktaMedKey(final String key) {
        return getFakta().stream().filter(faktum -> faktum.getKey().equals(key)).collect(toList());
    }

    public Faktum getFaktumMedKey(final String key) {
        for (Faktum faktum : fakta) {
            if (faktum.getKey().equals(key)) {
                return faktum;
            }
        }
        return null;
    }

    public Faktum getFaktumMedId(final String id) {
        for (Faktum faktum : fakta) {
            if (faktum.getFaktumId().toString().equals(id)) {
                return faktum;
            }
        }
        return null;
    }

    public String getValueForFaktum(final String key) {
        return Optional.ofNullable(getFaktumMedKey(key))
                .map(Faktum::getValue)
                .orElse("");
    }

    public Faktum getFaktaMedKeyOgProperty(final String key, final String property, final String value) {
        return getFakta().stream()
                .filter(faktum -> faktum.getKey().equals(key) && faktum.matcherUnikProperty(property, value))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        WebSoknad rhs = (WebSoknad) obj;
        return new EqualsBuilder()
                .append(this.soknadId, rhs.soknadId)
                .append(this.skjemaNummer, rhs.skjemaNummer)
                .append(this.uuid, rhs.uuid)
                .append(this.brukerBehandlingId, rhs.brukerBehandlingId)
                .append(this.fakta, rhs.fakta)
                .append(this.status, rhs.status)
                .append(this.aktoerId, rhs.aktoerId)
                .append(this.opprettetDato, rhs.opprettetDato)
                .append(this.sistLagret, rhs.sistLagret)
                .append(this.delstegStatus, rhs.delstegStatus)
                .append(this.vedlegg, rhs.vedlegg)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(soknadId)
                .append(skjemaNummer)
                .append(uuid)
                .append(brukerBehandlingId)
                .append(fakta)
                .append(status)
                .append(aktoerId)
                .append(opprettetDato)
                .append(sistLagret)
                .append(delstegStatus)
                .append(vedlegg)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("soknadId", soknadId)
                .append("skjemaNummer", skjemaNummer)
                .append("uuid", uuid)
                .append("brukerBehandlingId", brukerBehandlingId)
                .append("fakta", fakta)
                .append("status", status)
                .append("aktoerId", aktoerId)
                .append("medOppretteDato", opprettetDato)
                .append("sistLagret", sistLagret)
                .append("delstegStatus", delstegStatus)
                .append("vedlegg", vedlegg)
                .toString();
    }

    public boolean erEttersending() {
        return delstegStatus != null && delstegStatus.erEttersending();
    }

    public WebSoknad medSoknadPrefix(String prefix) {
        soknadPrefix = prefix;
        return this;
    }

    public void validerDelstegEndring(DelstegStatus nyStatus) {
        if (delstegStatus.erEttersending() && !nyStatus.erEttersending()) {
            throw new UgyldigDelstegEndringException(String.format("Kan ikke endre status fra %s til %s", delstegStatus, nyStatus), "soknad.delsteg.endring.ettersending");
        }
    }

    public Faktum finnFaktum(final Long faktumId) {
        return getFakta().stream()
                .filter(faktum -> faktum.getFaktumId() != null
                        && faktum.getFaktumId().equals(faktumId))
                .findFirst().orElse(null);
    }
}
