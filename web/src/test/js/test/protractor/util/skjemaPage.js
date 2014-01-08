var Page = require('astrolabe').Page;

module.exports = Page.create({
    url: {
        value: '#/dagpenger'
    },
    reellarbeidssoker: {
        get: function() {
            return {
                elem: element(this.by.id('reellarbeidssoker')),
                validerbolk: element(this.by.css('#reellarbeidssoker .spm-knapper button')),
                deltid: {
                    ja: element.all(this.by.css('[data-nav-faktum=villigdeltid] input')).get(0),
                    nei: element.all(this.by.css('[data-nav-faktum=villigdeltid] input')).get(1)
                },
                pendle: {
                    ja: element.all(this.by.css('[data-nav-faktum=villigpendle] input')).get(0),
                    nei: element.all(this.by.css('[data-nav-faktum=villigpendle] input')).get(1)
                },
                helse: {
                    ja: element.all(this.by.css('[data-nav-faktum=villighelse] input')).get(0),
                    nei: element.all(this.by.css('[data-nav-faktum=villighelse] input')).get(1)
                },
                jobb: {
                    ja: element.all(this.by.css('[data-nav-faktum=villigjobb] input')).get(0),
                    nei: element.all(this.by.css('[data-nav-faktum=villigjobb] input')).get(1)
                }
            }
        }
    },
    arbeidsforhold: {
        get: function() {
            return {
                elem: element(this.by.id('arbeidsforhold')),
                validerbolk: element(this.by.css('#arbeidsforhold .spm-knapper button')),
                ikkejobbet: element(this.by.css('#arbeidsforhold #har-ikke-jobbet input'))
            }
        }
    },
    egennaering: {
        get: function() {
            return {
                elem: element(this.by.id('egennaering')),
                validerbolk: element(this.by.css('#egennaering .spm-knapper button'))
            }
        }
    },
    verneplikt: {
        get: function() {
            return element(this.by.id('verneplikt'));
        }
    },
    utdanning: {
        get: function() {
            return element(this.by.id('utdanning'));
        }
    },
    ytelser: {
        get: function() {
            return element(this.by.id('ytelser'));
        }
    },
    personalia: {
        get: function() {
            return element(this.by.id('personalia'));
        }
    },
    barnetillegg: {
        get: function() {
            return element(this.by.id('barnetillegg'));
        }
    }
});