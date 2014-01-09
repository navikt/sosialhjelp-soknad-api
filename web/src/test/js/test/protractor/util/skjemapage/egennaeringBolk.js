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
            validerbolk: element(this.by.css('#egennaering .spm-knapper button')),
            driverEgennaering: {
                nei: element.all(this.by.css('[data-nav-faktum=ikkeegennaering] input')).get(0),
                ja: element.all(this.by.css('[data-nav-faktum=ikkeegennaering] input')).get(1)
            }
        }
    }
};
