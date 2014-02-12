module.exports = {
    get: function() {
        return {
            elem: element(by.id('arbeidsforhold')),
            open: function() {
                element(by.id('arbeidsforhold')).getAttribute('class').then(function(klasse) {
                    if (klasse.indexOf('open') < 0) {
                        element(by.css('#arbeidsforhold .accordion-toggle')).click();
                    }
                });
            },
            validerbolk: element(by.css('#arbeidsforhold .spm-knapper button')),
            harjobbet: {
                nei: element.all(by.css('[data-nav-faktum=arbeidstilstand] input')).get(0),
                varierende: element.all(by.css('[data-nav-faktum=arbeidstilstand] input')).get(1),
                ja: element.all(by.css('[data-nav-faktum=arbeidstilstand] input')).get(2)
            }
        }
    }
};
