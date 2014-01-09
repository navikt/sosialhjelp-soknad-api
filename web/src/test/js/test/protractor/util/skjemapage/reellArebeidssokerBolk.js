module.exports = {
    get: function() {
        return {
            elem: element(by.id('reellarbeidssoker')),
            validerbolk: element(by.css('#reellarbeidssoker .spm-knapper button')),
            deltid: {
                ja: element.all(by.css('[data-nav-faktum=villigdeltid] input')).get(0),
                nei: element.all(by.css('[data-nav-faktum=villigdeltid] input')).get(1)
            },
            pendle: {
                ja: element.all(by.css('[data-nav-faktum=villigpendle] input')).get(0),
                nei: element.all(by.css('[data-nav-faktum=villigpendle] input')).get(1)
            },
            helse: {
                ja: element.all(by.css('[data-nav-faktum=villighelse] input')).get(0),
                nei: element.all(by.css('[data-nav-faktum=villighelse] input')).get(1)
            },
            jobb: {
                ja: element.all(by.css('[data-nav-faktum=villigjobb] input')).get(0),
                nei: element.all(by.css('[data-nav-faktum=villigjobb] input')).get(1)
            }
        }
    }
};
