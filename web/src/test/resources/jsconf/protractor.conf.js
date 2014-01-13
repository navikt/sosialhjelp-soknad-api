exports.config = {
    seleniumAddress: 'http://a34apvl016.devillo.no:4444/wd/hub',

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