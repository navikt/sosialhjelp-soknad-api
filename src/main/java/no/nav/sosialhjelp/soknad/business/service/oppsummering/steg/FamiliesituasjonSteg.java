package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Svar;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.SvarType;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.booleanVerdiFelt;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.createSvar;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.fulltnavn;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.harBarnMedKilde;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.isNotNullOrEmtpy;

public class FamiliesituasjonSteg {

    public Steg get(JsonInternalSoknad jsonInternalSoknad) {
        var familie = jsonInternalSoknad.getSoknad().getData().getFamilie();

        return new Steg.Builder()
                .withStegNr(4)
                .withTittel("familiebolk.tittel")
                .withAvsnitt(
                        List.of(
                                sivilstatusAvsnitt(familie.getSivilstatus()),
                                forsorgerpliktAvsnitt(familie.getForsorgerplikt())
                        )
                )
                .build();
    }

    private Avsnitt sivilstatusAvsnitt(JsonSivilstatus sivilstatus) {
        return new Avsnitt.Builder()
                .withTittel("system.familie.sivilstatus.sporsmal")
                .withSporsmal(sivilstatusSporsmal(sivilstatus))
                .build();
    }

    private List<Sporsmal> sivilstatusSporsmal(JsonSivilstatus sivilstatus) {
        var harUtfyltSivilstatusSporsmal = sivilstatus != null && sivilstatus.getKilde() != null;
        var harSystemEktefelle = harUtfyltSivilstatusSporsmal && sivilstatus.getKilde().equals(JsonKilde.SYSTEM) && sivilstatus.getStatus().equals(JsonSivilstatus.Status.GIFT);
        var harSystemEktefelleMedAdressebeskyttelse = harSystemEktefelle && Boolean.TRUE.equals(sivilstatus.getEktefelleHarDiskresjonskode());
        var harBrukerUtfyltSivilstatus = harUtfyltSivilstatusSporsmal && !harSystemEktefelle && sivilstatus.getKilde().equals(JsonKilde.BRUKER);
        var harBrukerUtfyltEktefelle = harBrukerUtfyltSivilstatus && sivilstatus.getStatus().equals(JsonSivilstatus.Status.GIFT) && sivilstatus.getEktefelle() != null;

        var sporsmal = new ArrayList<Sporsmal>();

        if (!harUtfyltSivilstatusSporsmal) {
            sporsmal.add(brukerSivilstatusSporsmal(false, null));
        }

        if (harBrukerUtfyltSivilstatus && !harBrukerUtfyltEktefelle) {
            sporsmal.add(brukerSivilstatusSporsmal(true, sivilstatus.getStatus()));
        }

        if (harBrukerUtfyltEktefelle) {
            sporsmal.add(brukerRegistrertEktefelle(sivilstatus));
        }

        if (harSystemEktefelleMedAdressebeskyttelse) {
            sporsmal.add(systemEktefelleMedAdressebeskyttelseSporsmal());
        }

        if (harSystemEktefelle && !harSystemEktefelleMedAdressebeskyttelse) {
            sporsmal.add(systemEktefelleSporsmal(sivilstatus));
        }

        return sporsmal;
    }

    private Sporsmal brukerSivilstatusSporsmal(boolean erUtfylt, JsonSivilstatus.Status status) {
        return new Sporsmal.Builder()
                .withTittel("familie.sivilstatus.sporsmal")
                .withErUtfylt(erUtfylt)
                .withFelt(erUtfylt ?
                        singletonList(
                                new Felt.Builder()
                                        .withSvar(createSvar(statusToTekstKey(status), SvarType.LOCALE_TEKST))
                                        .withType(Type.CHECKBOX)
                                        .build()
                        ) :
                        null
                )
                .build();
    }

    private String statusToTekstKey(JsonSivilstatus.Status status) {
        String key;
        switch (status) {
            case ENKE:
                key = "familie.sivilstatus.enke";
                break;
            case GIFT:
                key = "familie.sivilstatus.gift";
                break;
            case SKILT:
                key = "familie.sivilstatus.skilt";
                break;
            case SAMBOER:
                key = "familie.sivilstatus.samboer";
                break;
            case SEPARERT:
                key = "familie.sivilstatus.separert";
                break;
            case UGIFT:
            default:
                key = "familie.sivilstatus.ugift";
                break;
        }
        return key;
    }

    private Sporsmal brukerRegistrertEktefelle(JsonSivilstatus sivilstatus) {
        // todo: som dette eller som liste av sporsmal?
        var ektefelle = sivilstatus.getEktefelle();
        var erUtfylt = isNotNullOrEmtpy(ektefelle.getNavn().getFornavn()) &&
                isNotNullOrEmtpy(ektefelle.getNavn().getEtternavn()) &&
                isNotNullOrEmtpy(ektefelle.getFodselsdato()) &&
                isNotNullOrEmtpy(ektefelle.getPersonIdentifikator()) &&
                sivilstatus.getBorSammenMed() != null;

        var map = new LinkedHashMap<String, Svar>();
        map.put("familie.sivilstatus.gift.ektefelle.navn.label", createSvar(fulltnavn(ektefelle.getNavn()), SvarType.TEKST));
        map.put("familie.sivilstatus.gift.ektefelle.fnr.label", createSvar(ektefelle.getFodselsdato(), SvarType.DATO));
        map.put("familie.sivilstatus.gift.ektefelle.pnr.label", createSvar(personnummerFraFnr(ektefelle), SvarType.TEKST));
        map.put("familie.sivilstatus.gift.ektefelle.borsammen.sporsmal", createSvar(borSammenMedSvar(sivilstatus), SvarType.LOCALE_TEKST));

        return new Sporsmal.Builder()
                .withTittel("familie.sivilstatus.gift.ektefelle.sporsmal")
                .withErUtfylt(erUtfylt)
                .withFelt(singletonList(
                        new Felt.Builder()
                                .withType(Type.SYSTEMDATA_MAP) //selv om dette ikke er systemdata?!?
                                .withLabelSvarMap(map)
                                .build()
                ))
                .build();
    }

    private String personnummerFraFnr(JsonEktefelle ektefelle) {
        if (ektefelle.getPersonIdentifikator() != null && ektefelle.getPersonIdentifikator().length() == 11) {
            return ektefelle.getPersonIdentifikator().substring(6, 11);
        }
        return ektefelle.getPersonIdentifikator();
    }

    private String borSammenMedSvar(JsonSivilstatus sivilstatus) {
        return sivilstatus.getBorSammenMed() != null ? borSammenKey(sivilstatus) : null;
    }

    private String borSammenKey(JsonSivilstatus sivilstatus) {
        return Boolean.TRUE.equals(sivilstatus.getBorSammenMed()) ? "familie.sivilstatus.gift.ektefelle.borsammen.true" : "familie.sivilstatus.gift.ektefelle.borsammen.false";
    }

    private Sporsmal systemEktefelleMedAdressebeskyttelseSporsmal() {
        return new Sporsmal.Builder()
                .withTittel("system.familie.sivilstatus")
                .withErUtfylt(true)
                .withFelt(
                        singletonList(
                                new Felt.Builder()
                                        .withSvar(createSvar("system.familie.sivilstatus.ikkeTilgang.label", SvarType.LOCALE_TEKST))
                                        .withType(Type.SYSTEMDATA)
                                        .build()
                        )
                )
                .build();
    }

    private Sporsmal systemEktefelleSporsmal(JsonSivilstatus sivilstatus) {
        var ektefelle = sivilstatus.getEktefelle();
        var labelSvarMap = new LinkedHashMap<String, Svar>();
        if (ektefelle.getNavn() != null) {
            labelSvarMap.put("system.familie.sivilstatus.gift.ektefelle.navn", createSvar(fulltnavn(ektefelle.getNavn()), SvarType.TEKST));
        }
        if (ektefelle.getFodselsdato() != null) {
            labelSvarMap.put("system.familie.sivilstatus.gift.ektefelle.fodselsdato", createSvar(ektefelle.getFodselsdato(), SvarType.DATO));
        }
        if (sivilstatus.getFolkeregistrertMedEktefelle() != null) {
            labelSvarMap.put("system.familie.sivilstatus.gift.ektefelle.folkereg", createSvar(Boolean.TRUE.equals(sivilstatus.getFolkeregistrertMedEktefelle()) ? "system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.true" : "system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.false", SvarType.LOCALE_TEKST));
        }

        return new Sporsmal.Builder()
                .withTittel("system.familie.sivilstatus.infotekst")
                .withErUtfylt(true)
                .withFelt(
                        singletonList(
                                new Felt.Builder()
                                        .withType(Type.SYSTEMDATA_MAP)
                                        .withLabelSvarMap(labelSvarMap)
                                        .build()
                        )
                )
                .build();
    }

    private Avsnitt forsorgerpliktAvsnitt(JsonForsorgerplikt forsorgerplikt) {
        return new Avsnitt.Builder()
                .withTittel("familierelasjon.faktum.sporsmal")
                .withSporsmal(forsorgerpliktSporsmal(forsorgerplikt))
                .build();
    }

    private List<Sporsmal> forsorgerpliktSporsmal(JsonForsorgerplikt forsorgerplikt) {
        var harSystemBarn = harBarnMedKilde(forsorgerplikt, JsonKilde.SYSTEM);
        var harBrukerBarn = harBarnMedKilde(forsorgerplikt, JsonKilde.BRUKER);

        var sporsmal = new ArrayList<Sporsmal>();

        if (!harSystemBarn && !harBrukerBarn) {
            sporsmal.add(ingenRegistrerteBarnSporsmal());
        }

        if (harSystemBarn) {
            forsorgerplikt.getAnsvar().stream()
                    .filter(barn -> barn.getBarn().getKilde().equals(JsonKilde.SYSTEM))
                    .forEach(barn -> {
                                sporsmal.add(systemBarnSporsmal(barn));
                                if (Boolean.TRUE.equals(barn.getErFolkeregistrertSammen().getVerdi())) {
                                    sporsmal.add(deltBostedSporsmal(barn));
                                }
                            }
                    );
        }

        if (harBrukerBarn) {
            // todo brukerregistrerte barn. pt ikke støtte for dette i søknad
        }

        if (harSystemBarn || harBrukerBarn) {
            sporsmal.add(barneBidragSporsmal(forsorgerplikt));
        }

        return sporsmal;
    }

    private Sporsmal ingenRegistrerteBarnSporsmal() {
        return new Sporsmal.Builder()
                .withTittel("familierelasjon.ingen_registrerte_barn_tittel")
                .withErUtfylt(true)
                .withFelt(
                        singletonList(
                                new Felt.Builder()
                                        .withSvar(createSvar("familierelasjon.ingen_registrerte_barn_tekst", SvarType.LOCALE_TEKST))
                                        .withType(Type.SYSTEMDATA)
                                        .build()
                        )
                )
                .build();
    }

    private Sporsmal systemBarnSporsmal(JsonAnsvar barn) {
        var labelSvarMap = new LinkedHashMap<String, Svar>();
        if (barn.getBarn().getNavn() != null) {
            labelSvarMap.put("familie.barn.true.barn.navn.label", createSvar(fulltnavn(barn.getBarn().getNavn()), SvarType.TEKST));
        }
        if (barn.getBarn().getFodselsdato() != null) {
            labelSvarMap.put("familierelasjon.fodselsdato", createSvar(barn.getBarn().getFodselsdato(), SvarType.DATO));
        }
        if (barn.getErFolkeregistrertSammen() != null) {
            labelSvarMap.put("familierelasjon.samme_folkeregistrerte_adresse", createSvar(Boolean.TRUE.equals(barn.getErFolkeregistrertSammen().getVerdi()) ? "system.familie.barn.true.barn.folkeregistrertsammen.true" : "system.familie.barn.true.barn.folkeregistrertsammen.false", SvarType.LOCALE_TEKST));
        }

        return new Sporsmal.Builder()
                .withTittel("familie.barn.true.barn.sporsmal")
                .withErUtfylt(true)
                .withFelt(
                        singletonList(
                                new Felt.Builder()
                                        .withType(Type.SYSTEMDATA_MAP)
                                        .withLabelSvarMap(labelSvarMap)
                                        .build()
                        )
                )
                .build();
    }

    private Sporsmal deltBostedSporsmal(JsonAnsvar barn) {
        var harUtfyltDeltBostedSporsmal = barn.getHarDeltBosted() != null && barn.getHarDeltBosted().getVerdi() != null;
        var harSvartJaDeltBosted = harUtfyltDeltBostedSporsmal && Boolean.TRUE.equals(barn.getHarDeltBosted().getVerdi());

        return new Sporsmal.Builder()
                .withTittel("system.familie.barn.true.barn.deltbosted.sporsmal")
                .withErUtfylt(harUtfyltDeltBostedSporsmal)
                .withFelt(harUtfyltDeltBostedSporsmal ?
                        booleanVerdiFelt(harSvartJaDeltBosted, "system.familie.barn.true.barn.deltbosted.true", "system.familie.barn.true.barn.deltbosted.false") :
                        null
                )
                .build();

    }

    private Sporsmal barneBidragSporsmal(JsonForsorgerplikt forsorgerplikt) {
        var erUtfylt = forsorgerplikt.getBarnebidrag() != null && forsorgerplikt.getBarnebidrag().getVerdi() != null;
        return new Sporsmal.Builder()
                .withTittel("familie.barn.true.barnebidrag.sporsmal")
                .withErUtfylt(erUtfylt)
                .withFelt(erUtfylt ?
                        singletonList(
                                new Felt.Builder()
                                        .withSvar(createSvar(verdiToTekstKey(forsorgerplikt.getBarnebidrag().getVerdi()), SvarType.LOCALE_TEKST))
                                        .withType(Type.CHECKBOX)
                                        .build()
                        ) :
                        null
                )
                .build();
    }

    private String verdiToTekstKey(JsonBarnebidrag.Verdi verdi) {
        String key;
        switch (verdi) {
            case MOTTAR:
                key = "familie.barn.true.barnebidrag.mottar";
                break;
            case BETALER:
                key = "familie.barn.true.barnebidrag.betaler";
                break;
            case BEGGE:
                key = "familie.barn.true.barnebidrag.begge";
                break;
            case INGEN:
            default:
                key = "familie.barn.true.barnebidrag.ingen";
                break;
        }
        return key;
    }

}
