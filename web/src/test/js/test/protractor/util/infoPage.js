var util = require('../util/common.js');

var Page = require('astrolabe').Page;

module.exports = Page.create({
    url: { value: '#/informasjonsside'},
    open: {
        value: function() {
            this.go();
        }
    },
    tittel: {
        get: function() {
            return element(this.by.css('h2'));
        }
    },
    startknapp: {
        get: function() {
            return element(this.by.css('input.knapp-hoved'));
        }
    },
    startSoknad: {
        value: function() {
            this.startknapp.click();
            util.ventTilSideHarLastet('[data-ng-form=dagpengerForm]');
        }
    },
    stegindikator: {
        get: function() {
            return element(this.by.css('.stegindikator'));
        }
    },
    aktivtSteg: {
        get: function() {
            return element.all(this.by.repeater('steg in data.liste')).get(0);
        }
    }
});