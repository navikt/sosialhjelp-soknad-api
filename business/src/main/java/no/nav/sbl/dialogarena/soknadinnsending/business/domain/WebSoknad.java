package no.nav.sbl.dialogarena.soknadinnsending.business.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.nav.sbl.dialogarena.soknadinnsending.business.domain.exception.UgyldigDelstegEndringException;
import org.apache.commons.collections15.Predicate;
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

import static no.nav.modig.lang.collections.IterUtils.on;
import static no.nav.modig.lang.collections.PredicateUtils.not;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.DelstegStatus.ETTERSENDING_OPPRETTET;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg.ER_ANNET_VEDLEGG;
import static no.nav.sbl.dialogarena.soknadinnsending.business.domain.Vedlegg.ER_LASTET_OPP;
import static no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.AAPGjenopptakInformasjon.erAapGjenopptak;
import static no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.AAPOrdinaerInformasjon.erAapOrdinaer;
import static no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.DagpengerGjenopptakInformasjon.erDagpengerGjenopptak;
import static no.nav.sbl.dialogarena.soknadinnsending.business.kravdialoginformasjon.DagpengerOrdinaerInformasjon.erDagpengerOrdinaer;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("PMD.TooManyMethods")
public class WebSoknad implements Serializable {
    private Long soknadId;
    private String skjemaNummer;
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
                .medDelstegStatus(ETTERSENDING_OPPRETTET)
                .medBehandlingId(behandlingsId)
                .medOppretteDato(DateTime.now());
    }

    public DateTime getSistLagret() {
        return sistLagret;
    }

    public String getJournalforendeEnhet() {
        return journalforendeEnhet;
    }

    public void setJournalforendeEnhet(String journalforendeEnhet) {
        this.journalforendeEnhet = journalforendeEnhet;
    }

    public String getBehandlingskjedeId() {
        return behandlingskjedeId;
    }

    public void setBehandlingskjedeId(String behandlingskjedeId) {
        this.behandlingskjedeId = behandlingskjedeId;
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

    public void setFakta(List<Faktum> fakta) {
        this.fakta = fakta;
    }

    public final WebSoknad leggTilFaktum(Faktum faktum) {
        this.fakta.add(faktum);
        return this;
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

    public WebSoknad medFortsettSoknadUrl(String url){
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
        return on(fakta).filter(new Predicate<Faktum>() {
            @Override
            public boolean evaluate(Faktum faktum) {
                return faktum.getKey().equals(key);
            }
        }).collect();
    }

    public Faktum getFaktumMedKey(final String key) {
        for (Faktum faktum : fakta) {
            if (faktum.getKey().equals(key)) {
                return faktum;
            }
        }
        return null;
    }

    /**
     * Returnerer liste over vedlegg som er lastet opp i denne behandlingen.
     *
     * @return liste over vedlegg som er lastet opp i nåværende behandling
     */
    public List<Vedlegg> getOpplastedeVedlegg() {
        return on(vedlegg).filter(new Predicate<Vedlegg>() {
            @Override
            public boolean evaluate(Vedlegg vedlegg) {
                return vedlegg.getStorrelse() > 0;
            }
        }).collect();
    }

    public List<Vedlegg> getInnsendteVedlegg() {
        return on(vedlegg).filter(new Predicate<Vedlegg>() {
            @Override
            public boolean evaluate(Vedlegg vedlegg) {
                return vedlegg.getInnsendingsvalg().er(Vedlegg.Status.LastetOpp);
            }
        }).collect();
    }

    public List<Vedlegg> getIkkeInnsendteVedlegg() {
        return on(vedlegg).filter(new Predicate<Vedlegg>() {
            @Override
            public boolean evaluate(Vedlegg vedlegg) {
                return vedlegg.getInnsendingsvalg().erIkke(Vedlegg.Status.LastetOpp);
            }
        }).collect();
    }

    public List<Faktum> getFaktaMedKeyOgPropertyLikTrue(final String key, final String propertyKey) {
        return on(fakta).filter(new Predicate<Faktum>() {
            @Override
            public boolean evaluate(Faktum faktum) {
                return faktum.getKey().equals(key) && faktum.getProperties().get(propertyKey) != null && faktum.getProperties().get(propertyKey).equals("true");
            }
        }).collect();
    }

    public List<Faktum> getFaktaSomStarterMed(final String key) {
        return on(fakta).filter(new Predicate<Faktum>() {
            @Override
            public boolean evaluate(Faktum faktum) {
                return faktum.getKey().startsWith(key);
            }
        }).collect();

    }

    public List<Faktum> getFaktaMedKeyOgParentFaktum(final String key, final Long parentFaktumId) {
        return on(fakta).filter(new Predicate<Faktum>() {
            @Override
            public boolean evaluate(Faktum faktum) {
                return faktum.getKey().equals(key) && faktum.getParrentFaktum().equals(parentFaktumId);
            }
        }).collect();
    }

    public Faktum getFaktaMedKeyOgProperty(final String key, final String property, final String value) {
        return on(fakta).filter(new Predicate<Faktum>() {
            @Override
            public boolean evaluate(Faktum faktum) {
                return faktum.getKey().equals(key) && faktum.matcherUnikProperty(property, value);
            }
        }).head().getOrElse(null);
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

    public Vedlegg hentVedleggMedUID(String uuid) {
        for (Vedlegg v : vedlegg) {
            if (v.getFillagerReferanse().equals(uuid)) {
                return v;
            }
        }
        return null;
    }

    public boolean erEttersending() {
        return delstegStatus != null && delstegStatus.erEttersending();
    }

    public boolean erDagpengeSoknad() {
        return (erOrdinaerDagpengeSoknad() || erGjenopptak()) && !this.erEttersending();
    }

    public boolean erOrdinaerDagpengeSoknad() {
        return this.skjemaNummer != null && erDagpengerOrdinaer(this.skjemaNummer);
    }

    public boolean erGjenopptak() {
        return this.skjemaNummer != null && erDagpengerGjenopptak(this.skjemaNummer);
    }

    public boolean erAapSoknad() {
        return this.skjemaNummer != null && (erAapOrdinaer(this.skjemaNummer) || erAapGjenopptak(this.skjemaNummer));
    }

    public boolean harAnnetVedleggSomIkkeErLastetOpp() {
        return !on(vedlegg)
                .filter(ER_ANNET_VEDLEGG)
                .filter(not(ER_LASTET_OPP))
                .collect()
                .isEmpty();
    }
    public boolean erUnderArbeid() {
        return status.equals(SoknadInnsendingStatus.UNDER_ARBEID);
    }

    public boolean erAvbrutt() {
        return status.equals(SoknadInnsendingStatus.AVBRUTT_AV_BRUKER) || status.equals(SoknadInnsendingStatus.AVBRUTT_AUTOMATISK);
    }

    public WebSoknad medSoknadPrefix(String prefix) {
        soknadPrefix = prefix;
        return this;
    }
    public String getSoknadPrefix() {
        return soknadPrefix;
    }
    public String getSoknadUrl() {
        return soknadUrl;
    }

    public String getFortsettSoknadUrl() {
        return fortsettSoknadUrl;
    }

    public void validerDelstegEndring(DelstegStatus nyStatus) {
        if(delstegStatus.erEttersending() && !nyStatus.erEttersending()){
            throw new UgyldigDelstegEndringException(String.format("Kan ikke endre status fra %s til %s", delstegStatus, nyStatus), "soknad.delsteg.endring.ettersending");
        }
    }

    public Faktum finnFaktum(final Long faktumId) {
        return on(fakta).filter(new Predicate<Faktum>() {
            @Override
            public boolean evaluate(Faktum faktum) {
                return faktum.getFaktumId().equals(faktumId);
            }
        }).head().getOrElse(null);
    }
}
