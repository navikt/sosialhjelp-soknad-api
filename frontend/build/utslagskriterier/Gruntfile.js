module.exports = function (grunt) {
    var timestamp = grunt.option('timestamp');
    var path = require('path');
    var appName = path.basename(process.cwd());
    var jsBuilt = 'js/built/built_' + appName + timestamp + '.js';
    var resourcePath = '../../target/classes/META-INF/resources/';

    var cwd = process.cwd();
    process.chdir('../../');
    require('load-grunt-tasks')(grunt);
    var pkg = grunt.file.readJSON('package.json');
    process.chdir(cwd);

    require('load-grunt-config')(grunt, {
        // auto grunt.initConfig
        init: true,

        // data passed into config. Can use with <%= key %>
        data: {
            pkg: pkg,
            timestamp: timestamp,
            jsBuilt: jsBuilt,
            resourcePath: resourcePath
        },
        loadGruntTasks: false
    });
};