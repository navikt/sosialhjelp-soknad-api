/*
 * npm install protractor -g --save-dev
 * npm install selenium -g --save-dev
 */

exports.config = {
    seleniumServerJar: '../selenium-server-standalone-2.20.0.jar',
    chromeDriver: '../chromedriver',
    baseUrl: 'http://localhost:8181/sendsoknad/soknad/Dagpenger',

    capabilities: {
        'browserName': 'chrome'
    },

    specs: [
        '../../js/test/protractor/*.js'
    ]
}