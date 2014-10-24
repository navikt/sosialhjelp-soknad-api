module.exports = function (grunt) {
    var timestamp = grunt.option('timestamp');
    var path = require('path');
    var gruntPath = path.join(process.cwd(), 'grunt')
    var appName = path.basename(process.cwd());
    var jsBuilt = 'js/built/built_' + appName + timestamp + '.js';
    var resourcePath = 'target/classes/META-INF/resources/';

    grunt.file.setBase('../../');
    var pkg = grunt.file.readJSON('package.json');

    require('load-grunt-config')(grunt, {
        configPath: gruntPath,

        // auto grunt.initConfig
        init: true,

        // data passed into config. Can use with <%= key %>
        data: {
            pkg: pkg,
            timestamp: timestamp,
            jsBuilt: jsBuilt,
            resourcePath: resourcePath
        }
    });
};