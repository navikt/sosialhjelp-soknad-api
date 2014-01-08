module.exports = {
    ventTilSideHarLastet: function(cssSelector) {
        var ptor = protractor.getInstance();
        ptor.wait(function() {
            return ptor.driver.findElements(protractor.By.css(cssSelector)).then(function(elements) {
                return elements.length > 0;
            });
        }, 5000);
    },
    sjekkOgHukAvRadioGruppe: function(radioGruppe, valg) {
        radioGruppe[valg].click();
        Object.keys(radioGruppe).forEach(function(key) {
            if (key == valg) {
                expect(radioGruppe[key].getAttribute('checked')).toBeTruthy();
            } else {
                expect(radioGruppe[key].getAttribute('checked')).toBeFalsy();
            }
        });
    }

};