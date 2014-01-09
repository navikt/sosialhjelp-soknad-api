module.exports = {
    get: function() {
        return {
            elem: element(by.id('arbeidsforhold')),
            validerbolk: element(by.css('#arbeidsforhold .spm-knapper button')),
            ikkejobbet: element(by.css('#arbeidsforhold #har-ikke-jobbet input'))
        }
    }
};
