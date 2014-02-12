// Karma configuration
// Generated on Thu Sep 05 2013 15:48:54 GMT+0200 (Central Europe Daylight Time)

module.exports = function (config) {
    config.set({

        // base path, that will be used to resolve files and exclude
        basePath: '',


        // frameworks to use
        frameworks: ['jasmine'],


        // list of files / patterns to load in the browser
        files: [
            '../../js/lib/jquery/jquery-1.10.2.js',
            '../../js/lib/jquery/jquery-ui.js',
            'lib/TimeoutBoxMock.js',
            '../../js/lib/angular/angular.js',
            'lib/angular-mocks.js',
            '../../js/lib/angular/angular-resource.js',
            '../../js/lib/angular/angular-sanitize.js',
            '../../js/lib/angular/angular-cookies.js',
            '../../js/lib/bindonce.js',
            '../../js/app/**/*.js',
            '../../js/common/**/*.js',

            '../../js/lib/jquery/jquery.iframe-transport.js',
            '../../js/lib/jquery/jquery.fileupload.js',
            '../../js/lib/jquery/jquery.fileupload-process.js',
            '../../js/lib/jquery/jquery.fileupload-validate.js',
            '../../js/lib/jquery/jquery.fileupload-angular.js',
            'test/*.js'
        ],

        // list of files to exclude
        exclude: [

        ],

        // test results reporter to use
        // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
        reporters: ['progress'],


        // web server port
        port: 9876,


        // enable / disable colors in the output (reporters and logs)
        colors: true,


        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,


        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        browsers: ['Chrome'],
//        browsers: ['Chrome', 'Firefox', 'IE'],

        //plugins: ['karma-phantomjs-runner', 'karma-jasmine'],

        // If browser does not capture in given timeout [ms], kill it
        captureTimeout: 60000,


        // Continuous Integration mode
        // if true, it capture browsers, run tests and exit
        singleRun: true,

        plugins: [
            'karma-jasmine',
            'karma-phantomjs-launcher',
            'karma-chrome-launcher',
            'karma-firefox-launcher',
            'karma-ie-launcher'
        ],

        coverageReporter: {
            type: 'html',
            dir: 'target/karma-coverage'
        },

        junitReporter: {
            outputFile: 'target/surefire-reports/TEST-karma.xml'
        }

    });
};
