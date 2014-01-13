module.exports = {
    get: function() {
        return {
            elem: element(by.id('utdanning')),
            open: function() {
                element(by.id('utdanning')).getAttribute('class').then(function(klasse) {
                    if (klasse.indexOf('open') < 0) {
                        element(by.css('#utdanning .accordion-toggle')).click();
                    }
                });
            },
            validerbolk: element(by.css('#utdanning .spm-knapper button')),
            underUtdanning: {
                nei: element.all(by.css('[data-nav-faktum=utdanning] input')).get(0),
                jaSisteSeksMaaneder: element.all(by.css('[data-nav-faktum=utdanning] input')).get(1),
                jaNaa: element.all(by.css('[data-nav-faktum=utdanning] input')).get(2)
            }
        }
    }
};
