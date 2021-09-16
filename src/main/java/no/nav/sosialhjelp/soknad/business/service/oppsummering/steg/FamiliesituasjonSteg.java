package no.nav.sosialhjelp.soknad.business.service.oppsummering.steg;

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad;
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonAnsvar;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonBarnebidrag;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonForsorgerplikt;
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Avsnitt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Felt;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Sporsmal;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Steg;
import no.nav.sosialhjelp.soknad.web.rest.ressurser.oppsummering.dto.Type;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.nav.sosialhjelp.soknad.business.service.oppsummering.steg.StegUtils.fulltnavn;

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
        var harBrukerUtfyltSivilstatus = harUtfyltSivilstatusSporsmal && sivilstatus.getKilde().equals(JsonKilde.BRUKER);
        var harBrukerUtfyltEktefelle = harBrukerUtfyltSivilstatus && sivilstatus.getStatus().equals(JsonSivilstatus.Status.GIFT) && sivilstatus.getEktefelle() != null;

        // ikke folkereg ektefelle -> ikke utfylt sporsmal
        if (!harUtfyltSivilstatusSporsmal) {
            return singletonList(brukerSivilstatusSporsmal(false, null));
        }

        // brukerreg sivilstatus ulik ektefelle
        if (harBrukerUtfyltSivilstatus && !harBrukerUtfyltEktefelle) {
            return singletonList(brukerSivilstatusSporsmal(true, sivilstatus.getStatus()));
        }

        // brukerreg ektefelle med potensielt ikke utfylte felter
        if (harBrukerUtfyltEktefelle) {
            // todo implement
//            return brukerUtfyltEktefelleSporsmal(sivilstatus);
        }

        // har systemregistrert ektefelle med adr.beskyttelse
        if (harSystemEktefelleMedAdressebeskyttelse) {
            return singletonList(systemEktefelleMedAdressebeskyttelseSporsmal());
        }

        // har systemregistrert ektefelle
        if (harSystemEktefelle) {
            return singletonList(systemEktefelleSporsmal(sivilstatus));
        }

        return emptyList();
    }

    private Sporsmal brukerSivilstatusSporsmal(boolean erUtfylt, JsonSivilstatus.Status status) {
        return new Sporsmal.Builder()
                .withTittel("familie.sivilstatus.sporsmal")
                .withErUtfylt(erUtfylt)
                .withFelt(erUtfylt ?
                        singletonList(
                                new Felt.Builder()
                                        .withSvar(statusToTekstKey(status))
                                        .withType(Type.CHECKBOX)
                                        .build()
                        ) :
                        null
                )
                .build();
    }

    private String statusToTekstKey(JsonSivilstatus.Status status) {
        switch (status) {
            case ENKE:
                return "familie.sivilstatus.enke";
            case GIFT:
                return "familie.sivilstatus.gift";
            case SKILT:
                return "familie.sivilstatus.skilt";
            case SAMBOER:
                return "familie.sivilstatus.samboer";
            case SEPARERT:
                return "familie.sivilstatus.separert";
            case UGIFT:
            default:
                return "familie.sivilstatus.ugift";
        }
    }

    private Sporsmal systemEktefelleMedAdressebeskyttelseSporsmal() {
        return new Sporsmal.Builder()
                .withTittel("system.familie.sivilstatus")
                .withErUtfylt(true)
                .withFelt(
                        singletonList(
                                new Felt.Builder()
                                        .withSvar("system.familie.sivilstatus.ikkeTilgang.label")
                                        .withType(Type.SYSTEMDATA)
                                        .build()
                        )
                )
                .build();
    }

    private Sporsmal systemEktefelleSporsmal(JsonSivilstatus sivilstatus) {
        var ektefelle = sivilstatus.getEktefelle();
        var labelSvarMap = new LinkedHashMap<String, String>();
        labelSvarMap.put("system.familie.sivilstatus.label", null);
        if (ektefelle.getNavn() != null) {
            labelSvarMap.put("system.familie.sivilstatus.gift.ektefelle.navn", fulltnavn(ektefelle.getNavn()));
        }
        if (ektefelle.getFodselsdato() != null) {
            labelSvarMap.put("system.familie.sivilstatus.gift.ektefelle.fodselsdato", ektefelle.getFodselsdato());
        }
        if (sivilstatus.getFolkeregistrertMedEktefelle() != null) {
            labelSvarMap.put("system.familie.sivilstatus.gift.ektefelle.folkereg", Boolean.TRUE.equals(sivilstatus.getFolkeregistrertMedEktefelle()) ? "system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.true" : "system.familie.sivilstatus.gift.ektefelle.folkeregistrertsammen.false");
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

        if (!harSystemBarn || !harBrukerBarn) {
            addIngenRegistrerteBarnSporsmal(sporsmal);
        }

        if (harSystemBarn) {
            forsorgerplikt.getAnsvar().stream()
                    .filter(barn -> barn.getBarn().getKilde().equals(JsonKilde.SYSTEM))
                    .forEach(barn -> {
                                addSystemBarnSporsmal(sporsmal, barn);
                                addDeltBostedSporsmal(sporsmal, barn);
                            }
                    );
        }

        if (harBrukerBarn) {
            // todo brukerregistrerte barn
        }

        if (harSystemBarn || harBrukerBarn) {
            addBarneBidragSporsmal(forsorgerplikt, sporsmal);
        }

        return sporsmal;
    }

    private boolean harBarnMedKilde(JsonForsorgerplikt forsorgerplikt, JsonKilde kilde) {
        var harForsorgerplikt = forsorgerplikt.getHarForsorgerplikt() != null && forsorgerplikt.getHarForsorgerplikt().getVerdi().equals(Boolean.TRUE);

        return harForsorgerplikt && forsorgerplikt.getHarForsorgerplikt().getKilde().equals(kilde) &&
                forsorgerplikt.getAnsvar() != null && forsorgerplikt.getAnsvar().stream().anyMatch(barn -> barn.getBarn().getKilde().equals(kilde));
    }

    private void addIngenRegistrerteBarnSporsmal(ArrayList<Sporsmal> sporsmal) {
        sporsmal.add(
                new Sporsmal.Builder()
                        .withTittel("familierelasjon.ingen_registrerte_barn_tittel")
                        .withErUtfylt(true)
                        .withFelt(
                                singletonList(
                                        new Felt.Builder()
                                                .withSvar("familierelasjon.ingen_registrerte_barn_tekst")
                                                .withType(Type.SYSTEMDATA)
                                                .build()
                                )
                        )
                        .build()
        );
    }

    private void addSystemBarnSporsmal(ArrayList<Sporsmal> sporsmal, JsonAnsvar barn) {
        var labelSvarMap = new LinkedHashMap<String, String>();
        if (barn.getBarn().getNavn() != null) {
            labelSvarMap.put("familie.barn.true.barn.navn.label", fulltnavn(barn.getBarn().getNavn()));
        }
        if (barn.getBarn().getFodselsdato() != null) {
            labelSvarMap.put("familierelasjon.fodselsdato", barn.getBarn().getFodselsdato());
        }
        if (barn.getErFolkeregistrertSammen() != null) {
            labelSvarMap.put("familierelasjon.samme_folkeregistrerte_adresse", Boolean.TRUE.equals(barn.getErFolkeregistrertSammen().getVerdi()) ? "system.familie.barn.true.barn.folkeregistrertsammen.true" : "system.familie.barn.true.barn.folkeregistrertsammen.false");
        }

        sporsmal.add(
                new Sporsmal.Builder()
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
                        .build()
        );
    }

    private void addDeltBostedSporsmal(ArrayList<Sporsmal> sporsmal, JsonAnsvar barn) {
        if (Boolean.TRUE.equals(barn.getErFolkeregistrertSammen().getVerdi())) {
            var harUtfyltDeltBostedSporsmal = barn.getHarDeltBosted() != null && barn.getHarDeltBosted().getVerdi() != null;
            var svar = harUtfyltDeltBostedSporsmal && Boolean.TRUE.equals(barn.getHarDeltBosted().getVerdi()) ? "system.familie.barn.true.barn.deltbosted.true" : "system.familie.barn.true.barn.deltbosted.false";

            sporsmal.add(
                    new Sporsmal.Builder()
                            .withTittel("system.familie.barn.true.barn.deltbosted.sporsmal")
                            .withErUtfylt(harUtfyltDeltBostedSporsmal)
                            .withFelt(harUtfyltDeltBostedSporsmal ?
                                    singletonList(
                                            new Felt.Builder()
                                                    .withSvar(svar)
                                                    .withType(Type.CHECKBOX)
                                                    .build()
                                    ) :
                                    null)
                            .build()
            );
        }
    }

    private void addBarneBidragSporsmal(JsonForsorgerplikt forsorgerplikt, ArrayList<Sporsmal> sporsmal) {
        var erUtfylt = forsorgerplikt.getBarnebidrag() != null && forsorgerplikt.getBarnebidrag().getVerdi() != null;
        sporsmal.add(
                new Sporsmal.Builder()
                        .withTittel("familie.barn.true.barnebidrag.sporsmal")
                        .withErUtfylt(erUtfylt)
                        .withFelt(erUtfylt ?
                                singletonList(
                                        new Felt.Builder()
                                                .withSvar(verdiToTekstKey(forsorgerplikt.getBarnebidrag().getVerdi()))
                                                .withType(Type.CHECKBOX)
                                                .build()
                                ) :
                                null
                        )
                        .build()
        );
    }

    private String verdiToTekstKey(JsonBarnebidrag.Verdi verdi) {
        switch (verdi) {
            case MOTTAR:
                return "familie.barn.true.barnebidrag.mottar";
            case BETALER:
                return "familie.barn.true.barnebidrag.betaler";
            case BEGGE:
                return "familie.barn.true.barnebidrag.begge";
            case INGEN:
            default:
                return "familie.barn.true.barnebidrag.ingen";
        }
    }

}
