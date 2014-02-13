var util = require('../util/common.js');
var infoPage = require('../util/infoPage.js');
var skjemaPage = require('../util/skjemapage/skjemaPage.js');
var vedleggPage = require('../util/vedleggpage/vedleggPage.js');
var oppsummeringPage = require('../util/oppsummeringpage/oppsummeringPage.js');
var kvitteringPage = require('../util/kvitteringpage/kvitteringPage.js');

describe('krav om dagpenger:', function() {
    var ptor;

    describe('skal kunne gå igjennom hele søknaden -', function() {
        ptor = protractor.getInstance();

        it('skal åpne informasjonssiden', function() {
            infoPage.open();
            expect(ptor.getCurrentUrl()).toContain(infoPage.url);
        }, 20000);

        it('skal ha tittel og knapp for å starte søknad på informasjonssiden', function() {
            expect(infoPage.tittel.getText()).toBeDefined();
            expect(infoPage.startknapp.getAttribute('value')).toBeDefined();
        });

        it('skal være på første steg i stegindikator når man er på informasjonssiden', function() {
            expect(infoPage.aktivtSteg.getAttribute('class')).toContain('aktiv');
        });

        it('skal kunne starte søknad', function() {
            infoPage.startSoknad();
            expect(ptor.getCurrentUrl()).toContain(skjemaPage.url);
        });

        it('skjemaside skal inneholde alle bolkene og reell arbeidssøker skal være åpen', function() {
            expect(skjemaPage.reellarbeidssoker.elem.getAttribute('class')).toContain('accordion-group', 'open');
            expect(skjemaPage.arbeidsforhold.elem.getAttribute('class')).toContain('accordion-group');
            expect(skjemaPage.egennaering.elem.getAttribute('class')).toContain('accordion-group');
            expect(skjemaPage.verneplikt.elem.getAttribute('class')).toContain('accordion-group');
            expect(skjemaPage.utdanning.elem.getAttribute('class')).toContain('accordion-group');
            expect(skjemaPage.ytelser.elem.getAttribute('class')).toContain('accordion-group');
            expect(skjemaPage.personalia.elem.getAttribute('class')).toContain('accordion-group');
            expect(skjemaPage.barnetillegg.elem.getAttribute('class')).toContain('accordion-group');
        });

        it('skal være på andre steg i stegindikator når man er på skjemasiden', function() {
            expect(skjemaPage.aktivtSteg.getAttribute('class')).toContain('aktiv');
        });

        it('reell arbeidssøker skal validere og lukkes, mens arbeidsforhold skal åpnes, dersom man svarer ja på alle radioknapper og trykker neste', function() {
            skjemaPage.reellarbeidssoker.open();
            util.sjekkAtIngenRadioknapperErHuketAvOgVelgJa(skjemaPage.reellarbeidssoker.deltid);
            util.sjekkAtIngenRadioknapperErHuketAvOgVelgJa(skjemaPage.reellarbeidssoker.pendle);
            util.sjekkAtIngenRadioknapperErHuketAvOgVelgJa(skjemaPage.reellarbeidssoker.helse);
            util.sjekkAtIngenRadioknapperErHuketAvOgVelgJa(skjemaPage.reellarbeidssoker.jobb);

            skjemaPage.reellarbeidssoker.validerbolk.click();
            expect(skjemaPage.reellarbeidssoker.elem.getAttribute('class')).not.toContain('open');
            expect(skjemaPage.arbeidsforhold.elem.getAttribute('class')).toContain('open');
        });

        it('arbeidsforhold skal validere og lukkes, mens egen næring skal åpnes, dersom man huker av at man ikke har jobbet og trykker neste', function() {
            skjemaPage.arbeidsforhold.open();
            util.sjekkAtIngenRadioknapperErHuketAvOgVelgNei(skjemaPage.arbeidsforhold.harjobbet);

            skjemaPage.arbeidsforhold.validerbolk.click();
            expect(skjemaPage.arbeidsforhold.elem.getAttribute('class')).not.toContain('open');
            expect(skjemaPage.egennaering.elem.getAttribute('class')).toContain('open');
        });

        it('egen næring skal validere og lukkes, mens verneplikt skal åpnes, dersom man svarer at man ikke driver egen næring og trykker neste', function() {
            skjemaPage.egennaering.open();
            util.sjekkAtIngenRadioknapperErHuketAvOgVelgNei(skjemaPage.egennaering.driverEgennaering);
            util.sjekkAtIngenRadioknapperErHuketAvOgVelgNei(skjemaPage.egennaering.driverGardsbruk);
            util.sjekkAtIngenRadioknapperErHuketAvOgVelgNei(skjemaPage.egennaering.driverFangstEllerFiske);

            skjemaPage.egennaering.validerbolk.click();
            expect(skjemaPage.egennaering.elem.getAttribute('class')).not.toContain('open');
            expect(skjemaPage.verneplikt.elem.getAttribute('class')).toContain('open');
        });

        it('verneplikt skal validere og lukkes, mens utdanning skal åpnes, dersom man svarer at man ikke har avtjent verneplikt nylig og trykker neste', function() {
            skjemaPage.verneplikt.open();
            util.sjekkAtIngenRadioknapperErHuketAvOgVelgNei(skjemaPage.verneplikt.avtjentVerneplikt);

            skjemaPage.verneplikt.validerbolk.click();
            expect(skjemaPage.verneplikt.elem.getAttribute('class')).not.toContain('open');
            expect(skjemaPage.utdanning.elem.getAttribute('class')).toContain('open');
        });

        it('utdanning skal validere og lukkes, mens andre ytelser skal åpnes, dersom man svarer at man ikke er under utdanning og trykker neste', function() {
            skjemaPage.utdanning.open();
            util.sjekkAtIngenRadioknapperErHuketAvOgVelgNei(skjemaPage.utdanning.underUtdanning);

            skjemaPage.utdanning.validerbolk.click();
            expect(skjemaPage.utdanning.elem.getAttribute('class')).not.toContain('open');
            expect(skjemaPage.ytelser.elem.getAttribute('class')).toContain('open');
        });

        it('andre ytelser skal validere og lukkes, mens personalia skal åpnes, dersom man svarer at man ikke får andre ytelser og trykker neste', function() {
            skjemaPage.ytelser.open();
            skjemaPage.ytelser.andreYtelser.nei.click();
            skjemaPage.ytelser.andreYtelserNav.nei.click();

            util.sjekkAtIngenRadioknapperErHuketAvOgVelgNei(skjemaPage.ytelser.avtaleArbeidsgiver);

            skjemaPage.ytelser.validerbolk.click();
            expect(skjemaPage.ytelser.elem.getAttribute('class')).not.toContain('open');
            expect(skjemaPage.personalia.elem.getAttribute('class')).toContain('open');
        });

        it('personalia skal validere og lukkes, mens barnetillegg skal åpnes, når man trykker neste', function() {
            skjemaPage.personalia.open();

            skjemaPage.personalia.validerbolk.click();
            expect(skjemaPage.personalia.elem.getAttribute('class')).not.toContain('open');
            expect(skjemaPage.barnetillegg.elem.getAttribute('class')).toContain('open');
        });

        it('barnetillegg skal valideres og lukkes når man trykker neste', function() {
            skjemaPage.barnetillegg.open();

            skjemaPage.barnetillegg.validerbolk.click();
            expect(skjemaPage.barnetillegg.elem.getAttribute('class')).not.toContain('open');
        });

        it('skjema skal valideres og vedleggssiden skal åpnes når man trykker på gå til vedleggsknappen', function() {
            skjemaPage.gaaTilVedlegg();
            expect(ptor.getCurrentUrl()).toContain(vedleggPage.url);
        });

        it('skal være på tredje steg i stegindikator når man er på vedleggsiden', function() {
            expect(vedleggPage.aktivtSteg.getAttribute('class')).toContain('aktiv');
        });

        it('vedleggssiden skal ha knapp for å legge til ekstra vedlegg og knapp for å gå til oppsummering', function() {
            expect(vedleggPage.leggTilVedlegg.getAttribute('value')).toBeDefined();
            expect(vedleggPage.videreKnapp.getAttribute('href')).toContain(oppsummeringPage.url);
        });

        it('skal kunne gå fra vedleggside til oppsummeringside', function() {
            vedleggPage.gaaTilOppsummering();

            // TODO: Legg inn denne sjekke i oppsummeringspage
            expect(ptor.getCurrentUrl()).toContain(oppsummeringPage.url);
        });

        it('oppsummeringssiden skal ha knapp for å sende søknaden', function() {
            expect(oppsummeringPage.sendKnapp.getAttribute('value')).toBeDefined();
        });

        it('skal kunne sende inn søknaden', function() {
            oppsummeringPage.sendSoknad();

            expect(ptor.getCurrentUrl()).toContain(kvitteringPage.url);
        });
    });

});