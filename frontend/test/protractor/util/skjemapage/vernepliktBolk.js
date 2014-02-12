module.exports = {
    get: function() {
        return {
            elem: element(by.id('verneplikt')),
            open: function() {
                element(by.id('verneplikt')).getAttribute('class').then(function(klasse) {
                    if (klasse.indexOf('open') < 0) {
                        element(by.css('#verneplikt .accordion-toggle')).click();
                    }
                });
            },
            validerbolk: element(by.css('#verneplikt .spm-knapper button')),
            avtjentVerneplikt: {
                nei: element.all(this.by.css('[data-nav-faktum=ikkeavtjentverneplikt] input')).get(0),
                ja: element.all(this.by.css('[data-nav-faktum=ikkeavtjentverneplikt] input')).get(1)
            }
        }
    }
};
