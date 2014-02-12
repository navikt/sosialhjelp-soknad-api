module.exports = {
    get: function() {
        return {
            elem: element(by.id('egennaering')),
            open: function() {
                element(by.id('egennaering')).getAttribute('class').then(function(klasse) {
                    if (klasse.indexOf('open') < 0) {
                        element(by.css('#egennaering .accordion-toggle')).click();
                    }
                });
            },
            validerbolk: element(by.css('#egennaering .spm-knapper button')),
            driverEgennaering: {
                nei: element.all(by.css('[data-nav-faktum=driverEgennaering] input')).get(0),
                ja: element.all(by.css('[data-nav-faktum=driverEgennaering] input')).get(1)
            },
            driverGardsbruk: {
                nei: element.all(by.css('[data-nav-faktum=gardsbruk] input')).get(0),
                ja: element.all(by.css('[data-nav-faktum=gardsbruk] input')).get(1)
            },
            driverFangstEllerFiske: {
                nei: element.all(by.css('[data-nav-faktum=inntektFangstogfiske] input')).get(0),
                ja: element.all(by.css('[data-nav-faktum=inntektFangstogfiske] input')).get(1)
            }
        }
    }
};
