module.exports = function(config) {
  config.set({

    // base path, that will be used to resolve files and exclude
    basePath: '../',


    // frameworks to use
    frameworks: ['ng-scenario'],


    // list of files / patterns to load in the browser
    files: [
      '../test/e2e/**/*.js',
      '../js/app/*.js',
    ],


    // list of files to exclude
    exclude: [

    ],


    // enable / disable colors in the output (reporters and logs)
    colors: true,


    // level of logging
    // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
    logLevel: config.LOG_INFO,


    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: ['Chrome', 'Firefox'],

    proxies: {
		'/': 'http://localhost:8000/'
	},

	plugins: [
		'karma-chrome-launcher',
        'karma-firefox-launcher',
		'karma-jasmine',
        'karma-ng-scenario'
	],
    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 60000,


    junitReporter: {
	  outputFile: 'test_out/e2e.xml',
	  suite: 'e2e'
	}
  });
};
