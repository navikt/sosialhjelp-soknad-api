var Page = require('astrolabe').Page;
var util = require('../common.js');

module.exports = Page.create({
    url: { value: '#/oppsummering'},
    sendKnapp: {
        get: function() {
            return element(by.css('[data-ng-click*=sendSoknad]'));
        }
    },
    sendSoknad: {
        value: function() {
            this.sendKnapp.click();
            util.ventTilSideHarLastet('#kvittering');
        }
    }
});