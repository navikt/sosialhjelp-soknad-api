var util = require('../util/common.js');

var Page = require('astrolabe').Page;

module.exports = Page.create({
    url: { value: '#/informasjonsside'},
    open: {
        value: function() {
            this.go();
            util.ventTilSideHarLastet('#informasjonsside');
        }
    },
    tittel: {
        get: function() {
            return element(by.css('h2'));
        }
    },
    startknapp: {
        get: function() {
            return element(by.css('input.knapp-hoved'));
        }
    },
    startSoknad: {
        value: function() {
            this.startknapp.click();
            util.ventTilSideHarLastet('[data-ng-form=dagpengerForm]');
        }
    },
    aktivtSteg: {
        get: function() {
            return element.all(by.repeater('steg in data.liste')).get(0);
        }
    }
});