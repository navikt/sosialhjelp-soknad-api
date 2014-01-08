var util = require('../util/common.js');
var infoPage = require('../util/infoPage.js');
var skjemaPage = require('../util/skjemaPage.js');

describe('krav om dagpenger:', function() {
    var ptor;

    describe('skal kunne gå igjen hele søknaden -', function() {
        ptor = protractor.getInstance();

        it('skal åpne informasjonssiden', function() {
            infoPage.open();
            expect(ptor.getCurrentUrl()).toContain(infoPage.url);
        }, 20000);

        it('skal ha tittel og knapp for å starte søknad på informasjonssiden', function() {
            expect(infoPage.tittel.getText()).toBeDefined();
            expect(infoPage.startknapp.getAttribute('value')).toBeDefined();
        });

        it('skal være på første steg i stegindikator', function() {
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
            expect(skjemaPage.verneplikt.getAttribute('class')).toContain('accordion-group');
            expect(skjemaPage.utdanning.getAttribute('class')).toContain('accordion-group');
            expect(skjemaPage.ytelser.getAttribute('class')).toContain('accordion-group');
            expect(skjemaPage.personalia.getAttribute('class')).toContain('accordion-group');
            expect(skjemaPage.barnetillegg.getAttribute('class')).toContain('accordion-group');
        });

        it('reell arbeidssøker skal validere og lukkes, mens arbeidsforhold skal åpnes, dersom man svarer ja på alle radioknapper og trykker neste', function() {
            var sjekkOgHukAvRadioknapper = function(radioGruppe) {
                expect(radioGruppe.ja.getAttribute('checked')).toBeFalsy();
                expect(radioGruppe.nei.getAttribute('checked')).toBeFalsy();
                util.sjekkOgHukAvRadioGruppe(radioGruppe, 'ja');
            }

            sjekkOgHukAvRadioknapper(skjemaPage.reellarbeidssoker.deltid);
            sjekkOgHukAvRadioknapper(skjemaPage.reellarbeidssoker.pendle);
            sjekkOgHukAvRadioknapper(skjemaPage.reellarbeidssoker.helse);
            sjekkOgHukAvRadioknapper(skjemaPage.reellarbeidssoker.jobb);

            skjemaPage.reellarbeidssoker.validerbolk.click();
            expect(skjemaPage.reellarbeidssoker.elem.getAttribute('class')).not.toContain('open');
            expect(skjemaPage.arbeidsforhold.elem.getAttribute('class')).toContain('open');
        });

        it('arbeidsforhold skal validere og lukkes, mens egen næring skal åpnes, dersom man huker av at man ikke har jobbet og trykker neste', function() {
            expect(skjemaPage.arbeidsforhold.ikkejobbet.getAttribute('checked')).toBeFalsy();
            skjemaPage.arbeidsforhold.ikkejobbet.click();
            expect(skjemaPage.arbeidsforhold.ikkejobbet.getAttribute('checked')).toBeTruthy();

            skjemaPage.arbeidsforhold.validerbolk.click();
            expect(skjemaPage.arbeidsforhold.elem.getAttribute('class')).not.toContain('open');
            expect(skjemaPage.egennaering.elem.getAttribute('class')).toContain('open');
        });
    });

});