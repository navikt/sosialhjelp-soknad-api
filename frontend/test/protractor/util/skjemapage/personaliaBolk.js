module.exports = {
    get: function() {
        return {
            elem: element(by.id('personalia')),
            open: function() {
                element(by.id('personalia')).getAttribute('class').then(function(klasse) {
                    if (klasse.indexOf('open') < 0) {
                        element(by.css('#personalia .accordion-toggle')).click();
                    }
                });
            },
            validerbolk: element(by.css('#personalia .spm-knapper button'))
        }
    }
};
