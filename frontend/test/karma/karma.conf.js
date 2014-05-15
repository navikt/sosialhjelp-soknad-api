// Karma configuration
// Generated on Thu Sep 05 2013 15:48:54 GMT+0200 (Central Europe Daylight Time)

module.exports = function (config) {
    config.set({

        // base path, that will be used to resolve files and exclude
        basePath: '',

        // frameworks to use
        frameworks: ['jasmine'],

        preprocessors: {
            '../../js/dagpenger/**/*.js': ['coverage'],
            '../../js/common/**/*.js': ['coverage'],
            '../../js/ettersending/**/*.js': ['coverage'],
            '../../js/**/*.html': 'ng-html2js'
        },

        // list of files / patterns to load in the browser
        files: [
            '../../js/lib/jquery/jquery-1.10.2.js',
            '../../js/lib/jquery/jquery-ui.js',
            '../../js/lib/angular/angular.js',
            'lib/*.js',
            '../../js/lib/angular/angular-resource.js',
            '../../js/lib/angular/angular-sanitize.js',
            '../../js/lib/angular/angular-cookies.js',
            '../../js/lib/angular/angular-route.js',
            '../../js/lib/bindonce.js',
            '../../js/dagpenger/**/*.js',
            '../../js/ettersending/**/*.js',
            '../../js/common/**/*.js',
            '../../js/lib/jquery/jquery.iframe-transport.js',
            '../../js/lib/jquery/jquery.fileupload.js',
            '../../js/lib/jquery/jquery.fileupload-process.js',
            '../../js/lib/jquery/jquery.fileupload-validate.js',
            '../../js/lib/jquery/jquery.fileupload-angular.js',
            'test/**/*.js',
            '../../js/**/*.html'
        ],

        // list of files to exclude
        exclude: [
            '../../js/dagpenger/**/templates.js'
        ],

        // test results reporter to use
        // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
        reporters: ['progress', 'junit', 'coverage'],


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
       browsers: ['PhantomJS', 'Chrome'],

        // If browser does not capture in given timeout [ms], kill it
        captureTimeout: 60000,


        // Continuous Integration mode
        // if true, it capture browsers, run tests and exit
        singleRun: false,

        plugins: [
            'karma-jasmine',
            'karma-phantomjs-launcher',
            'karma-chrome-launcher',
            'karma-firefox-launcher',
            'karma-ie-launcher',
            'karma-junit-reporter',
            'karma-coverage',
            'karma-ng-html2js-preprocessor'
        ],

        ngHtml2JsPreprocessor: {
            moduleName: 'templates-main',
            cacheIdFromPath: function(filepath) {
                var idx = filepath.indexOf('js/');
                var path = '../' + filepath.substring(idx);
                return path;
            }
        },

        coverageReporter: {
            type: 'lcovonly',
            dir: '../../target/karma-coverage'
        },

        junitReporter: {
            outputFile: '../../target/surefire-reports/TEST-karma.xml',
            suite: ''
        }

    });
};
