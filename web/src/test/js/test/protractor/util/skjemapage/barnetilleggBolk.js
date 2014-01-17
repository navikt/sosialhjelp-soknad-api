module.exports = {
    get: function() {
        return {
            elem: element(by.id('barnetillegg')),
            open: function() {
                element(by.id('barnetillegg')).getAttribute('class').then(function(klasse) {
                    if (klasse.indexOf('open') < 0) {
                        element(by.css('#barnetillegg .accordion-toggle')).click();
                    }
                });
            },
            validerbolk: element(by.css('#barnetillegg .spm-knapper button'))
        }
    }
};
