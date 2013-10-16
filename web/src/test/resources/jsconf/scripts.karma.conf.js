// Karma configuration
// Generated on Thu Sep 05 2013 15:48:54 GMT+0200 (Central Europe Daylight Time)

module.exports = function(config) {
  config.set({

    // base path, that will be used to resolve files and exclude
    basePath: '',


    // frameworks to use
    frameworks: ['jasmine'],


    // list of files / patterns to load in the browser
    files: [
      '../../../main/webapp/js/lib/angular.js',
      '../jslib/angular-mocks.js',
      '../jslib/angular-resource.js',
      '../../../main/webapp/js/app/*.js',
      '../../../main/webapp/js/app/directives/**/*.js',
      '../../../test/js/test/unit/*.js'
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

    //plugins: ['karma-phantomjs-runner', 'karma-jasmine'],

    // If browser does not capture in given timeout [ms], kill it
    captureTimeout: 60000,


    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false,


    preprocessors : {
  //    'js/*.js' : ['coverage']
    },

    coverageReporter : {
        type: 'html',
        dir : 'target/karma-coverage'
    },

   junitReporter : {
      outputFile: 'target/surefire-reports/TEST-karma.xml'
   }

  });
};
