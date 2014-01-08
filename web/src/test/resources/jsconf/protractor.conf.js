/*
 * npm install protractor -g --save-dev
 * npm install selenium -g --save-dev
 * npm install selenium-webdriver -g --save-dev
 * npm install selenium-chromedriver -g --save-dev
 */

exports.config = {
    seleniumAddress: 'http://a34apvl016.devillo.no:4444/wd/hub',
//    seleniumAddress: 'http://localhost:4444/wd/hub',
//    seleniumServerJar: '../selenium-server-standalone-2.20.0.jar',
//    chromeDriver: '../chromedriver',
    baseUrl: 'http://a34duvw22201.devillo.no:8181/sendsoknad/soknad/Dagpenger',

    capabilities: {
        'browserName': 'firefox'
    },

    specs: [
        '../../js/test/protractor/specs/*.js'
    ],

    jasmineNodeOpts: {
        showColors: true
    }
}