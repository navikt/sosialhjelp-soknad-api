module.exports = function (grunt) {
    grunt.file.setBase('../../');
    var timestamp = grunt.template.today("yyyymmddHHMMss");
    var jsBuilt = 'js/built/built_sendsoknad' + timestamp + '.js';
    var path = require('path');
    require('load-grunt-config')(grunt, {
        configPath: path.join(process.cwd(), 'build/dagpenger/grunt'),

        // auto grunt.initConfig
        init: true,

        // data passed into config. Can use with <%= key %>
        data: {
            timestamp: timestamp,
            jsBuilt: jsBuilt
        }
    });
};