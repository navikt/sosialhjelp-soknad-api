var Page = require('astrolabe').Page;
var util = require('../common.js');

module.exports = Page.create({
    url: { value: '#/vedlegg'},
    aktivtSteg: {
        get: function() {
            return element.all(by.repeater('steg in data.liste')).get(2);
        }
    },
    leggTilVedlegg: {
        get: function() {
            return element(by.css('.leggtilekstra button'));
        }
    },
    videreKnapp: {
        get: function() {
            return element(by.css('.ferdig-skjema a'));
        }
    },
    gaaTilOppsummering: {
        value: function() {
            this.videreKnapp.click();
            util.ventTilSideHarLastet('#oppsummering');
        }
    }
});