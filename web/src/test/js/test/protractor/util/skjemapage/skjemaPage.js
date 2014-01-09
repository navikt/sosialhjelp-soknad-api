var util = require('../common.js');
var Page = require('astrolabe').Page;

module.exports = Page.create({
    url: {
        value: '#/dagpenger'
    },
    reellarbeidssoker: require('./reellArebeidssokerBolk.js'),
    arbeidsforhold: require('./arbeidsforholdBolk.js'),
    egennaering: require('./egennaeringBolk.js'),
    verneplikt: require('./vernepliktBolk.js'),
    utdanning: require('./utdanningBolk.js'),
    ytelser: require('./ytelserBolk.js'),
    personalia: require('./personaliaBolk.js'),
    barnetillegg: require('./barnetilleggBolk.js'),
    validerSkjemaSide: {
        get: function() {
            return element(this.by.css('.ferdig-skjema a'));
        }
    },
    gaaTilVedlegg: {
        value: function() {
            this.validerSkjemaSide.click();
            util.ventTilSideHarLastet('#vedlegg');
        }
    }
});