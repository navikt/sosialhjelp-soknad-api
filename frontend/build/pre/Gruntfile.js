module.exports = function (grunt) {
    var path = require('path');
    var gruntPath = path.join(process.cwd(), 'grunt')
    var resourcePath = 'target/classes/META-INF/resources/';

    grunt.file.setBase('../../');

    require('load-grunt-config')(grunt, {
        configPath: gruntPath,

        // auto grunt.initConfig
        init: true,

        // data passed into config. Can use with <%= key %>
        data: {
            resourcePath: resourcePath
        }
    });
};