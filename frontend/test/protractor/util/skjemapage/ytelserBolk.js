module.exports = {
    get: function() {
        return {
            elem: element(by.id('ytelser')),
            open: function() {
                element(by.id('ytelser')).getAttribute('class').then(function(klasse) {
                    if (klasse.indexOf('open') < 0) {
                        element(by.css('#ytelser .accordion-toggle')).click();
                    }
                });
            },
            validerbolk: element(by.css('#ytelser .spm-knapper button')),
            andreYtelser: {
                nei: element(by.css('[data-nav-faktum=ingenYtelse] input'))
            },
            andreYtelserNav: {
                nei: element(by.css('[data-nav-faktum=ingennavytelser] input'))
            },
            avtaleArbeidsgiver: {
                nei: element.all(this.by.css('[data-nav-faktum=ikkeavtale] input')).get(0),
                ja: element.all(this.by.css('[data-nav-faktum=ikkeavtale] input')).get(1)
            }
        }
    }
};
