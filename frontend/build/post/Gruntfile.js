module.exports = function (grunt) {
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
            resourcePath: resourcePath
        },
        loadGruntTasks: false
    });
};