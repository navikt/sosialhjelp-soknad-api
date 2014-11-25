module.exports = function (grunt) {
    var timestamp = grunt.template.today("yyyymmddHHMMss");
    grunt.initConfig({
        run_grunt: {
            options: {
                concurrent: 1,
                gruntOptions: {
                    timestamp: timestamp,
                    force: true
                },
                debugCli: true
            },
            default: {
                src: ['build/pre/Gruntfile.js', 'build/dagpenger/Gruntfile.js', 'build/ettersending/Gruntfile.js', 'build/gjenopptak/Gruntfile.js', 'build/utslagskriterier/Gruntfile.js', 'build/post/Gruntfile.js']
            },
            prod: {
                options: {
                    task: ['prod']
                },
                src: ['build/pre/Gruntfile.js', 'build/dagpenger/Gruntfile.js', 'build/ettersending/Gruntfile.js', 'build/gjenopptak/Gruntfile.js', 'build/utslagskriterier/Gruntfile.js', 'build/post/Gruntfile.js']
            }
        },
        karma : {
            unit: {
                configFile: 'test/karma/karma.conf.js',
                browsers: ['PhantomJS'],
                singleRun: true
            },
            local: {
                configFile: 'test/karma/karma.conf.js',
                singleRun: true
            }
        }
    });

    grunt.option('force', true);
    grunt.loadNpmTasks('grunt-run-grunt');
    grunt.loadNpmTasks('grunt-karma');
    grunt.registerTask('default', ['run_grunt:default']);
    grunt.registerTask('prod', ['run_grunt:prod', 'karma:unit']);
    grunt.registerTask('test', ['karma:unit']);
};