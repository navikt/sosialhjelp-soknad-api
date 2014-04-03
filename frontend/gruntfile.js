module.exports = function (grunt) {
    var timestamp = grunt.template.today("yyyymmddHHMMss");
	grunt.initConfig({
		pkg   : grunt.file.readJSON('package.json'),
        builtminName: 'target/classes/META-INF/resources/js/built/built' + timestamp + '.min.js',
        html2js: {
            main: {
                src: [
                    'views/dagpenger/dagpenger-skjema.html',
                    'views/templates/**/*.html',
                    'js/dagpenger/**/*.html',
                    'js/ettersending/**/*.html',
                    'js/common/**/*.html'
                ],
                dest: 'target/classes/META-INF/resources/js/dagpenger/templates.js'
            }
        },

        htmlbuild: {
            dev: {
                src: 'views/bootstrapTemplate.html',
                dest: 'target/classes/META-INF/resources/views/built/bootstrapDev.html',
                options: {
                    beautify: true,
                    relative: false,
                    prefix: '../',
                    scripts: {
                        angular: [
                            'js/lib/angular/angular.js',
                            'js/lib/angular/angular-*.js'
                        ],
                        fileupload: [
                            'js/lib/jquery/jquery-ui-1.10.3.custom.js',
                            'js/lib/jquery/cors/jquery.xdr-transport.js',
                            'js/lib/jquery/cors/jquery.postmessage-transport.js',
                            'js/lib/jquery/jquery.iframe-transport.js',
                            'js/lib/jquery/jquery.fileupload.js',
                            'js/lib/jquery/jquery.fileupload-process.js',
                            'js/lib/jquery/jquery.fileupload-validate.js',
                            'js/lib/jquery/jquery.fileupload-angular.js'
                        ],
                        libs: 'js/lib/*.js',
                        app: [
                            'js/dagpenger/**/*.js',
                            'js/ettersending/**/*.js',
                            'js/common/**/*.js'
                        ]
                    }
                }
            },
            test: {
                src: 'views/bootstrapTemplate.html',
                dest: 'target/classes/META-INF/resources/views/built/bootstrap.html',
                options: {
                    beautify: true,
                    relative: false,
                    prefix: '../',
                    scripts: {
                        angular: [
                            'js/lib/angular/angular.js',
                            'js/lib/angular/angular-*.js'
                        ],
                        fileupload: [
                            'js/lib/jquery/jquery-ui-1.10.3.custom.js',
                            'js/lib/jquery/cors/jquery.xdr-transport.js',
                            'js/lib/jquery/cors/jquery.postmessage-transport.js',
                            'js/lib/jquery/jquery.iframe-transport.js',
                            'js/lib/jquery/jquery.fileupload.js',
                            'js/lib/jquery/jquery.fileupload-process.js',
                            'js/lib/jquery/jquery.fileupload-validate.js',
                            'js/lib/jquery/jquery.fileupload-angular.js'
                        ],
                        libs: 'js/lib/*.js',
                        app: [
                            'js/dagpenger/**/*.js',
                            'js/ettersending/**/*.js',
                            'js/common/**/*.js'
                        ]
                    }
                }
            },
            prod: {
                src: 'views/bootstrapTemplateProd.html',
                dest: 'target/classes/META-INF/resources/views/built/bootstrap.html',
                options: {
                    beautify: true,
                    relative: false,
                    prefix: '../',
                    scripts: {
                        built: {
                            cwd: 'target/classes/META-INF/resources',
                            files: 'js/built/built' + timestamp + '.min.js'
                        }
                    }
                }
            }
        },
		concat: {
			options: {
				separator: ';'
			},
			dist   : {
				src   : [
                    'js/lib/angular/angular.js',
                    'js/lib/angular/angular-*.js',
                    'js/lib/jquery/jquery-ui-1.10.3.custom.js',
                    'js/lib/jquery/cors/jquery.xdr-transport.js',
                    'js/lib/jquery/cors/jquery.postmessage-transport.js',
                    'js/lib/jquery/jquery.iframe-transport.js',
                    'js/lib/jquery/jquery.fileupload.js',
                    'js/lib/jquery/jquery.fileupload-process.js',
                    'js/lib/jquery/jquery.fileupload-validate.js',
                    'js/lib/jquery/jquery.fileupload-angular.js',
                    'js/lib/*.js',
                    'js/dagpenger/**/!(templates).js',
                    'js/dagpenger/**/!(initDev).js',
                    'target/classes/META-INF/resources/js/dagpenger/templates.js',
                    'js/ettersending/**/!(templates).js',
                    'js/ettersending/**/!(initDev).js',
                    'target/classes/META-INF/resources/js/ettersending/templates.js',
                    'js/common/**/*.js'
				],
				dest  : 'target/classes/META-INF/resources/js/built/built' + timestamp + '.js',
				nonull: true
			}
		},
		uglify: {
			options: {
				banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> */\n',
                mangle: false
			},
			my_target: {
				files: {
                    '<%= builtminName %>': ['target/classes/META-INF/resources/js/built/built' + timestamp + '.js']
				}
			}
		},
		watch : {
			js  : {
				files  : [
                    'js/dagpenger/**/*.js',
                    'js/ettersending/**/*.js',
                    'js/common/**/*.js'
                ],
                tasks: 'jshint'
			},
			html: {
				files  : [
                    'js/dagpenger/**/*.html',
                    'js/ettersending/**/*.html',
                    'js/common/**/*.html',
                    'views/templates/**/*.html',
                    'views/dagpenger/dagpenger-skjema.html'
                ],
                tasks: ['html2js', 'htmlbuild:dev']
			},
            testHtml: {
                files  : [
                    'js/dagpenger/**/*.html',
                    'js/ettersending/**/*.html',
                    'js/common/**/*.html',
                    'views/templates/**/*.html',
                    'views/dagpenger/dagpenger-skjema.html'
                ],
                tasks: ['html2js', 'karma:local']
            }
		},
		jshint: {
			files  : ['gruntfile.js', 'js/dagpenger/**/*.js', 'js/ettersending/**/*.js', 'js/common/**/*.js', 'test/**/*.js'],
			options: {
				ignores: ['js/built/*.js', 'js/dagpenger/templates.js', 'js/ettersending/templates.js', 'test/karma/lib/angular-mocks.js', 'js/common/directives/scrollbar/perfect-scrollbar.js'],
                globals: {
                    it: true,
                    expect: true,
                    describe: true,
                    beforeEach: true,
                    inject: true,
                    angular: true,
                    module: true,
                    Date: true
                }
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
		},

        clean: ['target/classes/META-INF/resources/js/built']
    });

	// Load NPM tasks
	grunt.loadNpmTasks('grunt-contrib-watch');
	grunt.loadNpmTasks('grunt-contrib-jshint');
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-contrib-clean');
	grunt.loadNpmTasks('grunt-html-build');
	grunt.loadNpmTasks('grunt-html2js');
	grunt.loadNpmTasks('grunt-karma');

	grunt.option('force', true);

	grunt.registerTask('default', ['jshint', 'htmlbuild:dev']);
    grunt.registerTask('hint', ['jshint', 'watch']);
    grunt.registerTask('maven', ['jshint', 'karma:unit', 'html2js', 'htmlbuild:dev']);
    grunt.registerTask('maven-test', ['karma:unit']);
	grunt.registerTask('maven-prod', ['clean', 'html2js', 'concat', 'uglify', 'htmlbuild:dev', 'htmlbuild:prod']);
};
