module.exports = function (grunt) {
	grunt.initConfig({
		pkg   : grunt.file.readJSON('package.json'),
        html2js: {
            main: {
                src: [
                    'views/dagpenger-singlepage.html',
                    'views/templates/**/*.html',
                    'js/app/**/*.html',
                    'js/common/**/*.html'
                ],
                dest: 'js/app/templates.js'
            }
        },

        htmlbuild: {
            dev: {
                src: 'views/bootstrapTemplate.html',
                dest: 'views/built/bootstrapDev.html',
                options: {
                    beautify: true,
                    relative: false,
                    prefix: '../dev/',
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
                            'js/app/**/*.js',
                            'js/common/**/*.js'
                        ]
                    }
                }
            },
            test: {
                src: 'views/bootstrapTemplate.html',
                dest: 'views/built/bootstrap.html',
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
                            'js/app/**/*.js',
                            'js/common/**/*.js'
                        ]
                    }
                }
            },
            prod: {
                src: 'views/bootstrapTemplateProd.html',
                dest: 'views/built/bootstrap.html',
                options: {
                    beautify: true,
                    relative: false,
                    prefix: '../',
                    scripts: {
                        built: {
                            cwd: 'target/classes/META-INF/resources',
                            files: 'js/built/built<%= grunt.template.today("yyyymmdd") %>.min.js'
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
                    'js/app/**/*.js',
                    'js/common/**/*.js'
				],
				dest  : 'target/classes/META-INF/resources/js/built/built<%= grunt.template.today("yyyymmdd") %>.js',
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
					'target/classes/META-INF/resources/js/built/built<%= grunt.template.today("yyyymmdd") %>.min.js': ['target/classes/META-INF/resources/js/built/built<%= grunt.template.today("yyyymmdd") %>.js']
				}
			}
		},
		watch : {
			js  : {
				files  : [
                    'js/app/**/*.js',
                    'js/common/**/*.js'
                ],
                tasks: 'jshint'
			},
			html: {
				files  : [
                    'js/app/**/*.html',
                    'js/common/**/*.html',
                    'views/templates/**/*.html',
                    'views/dagpenger-singlepage.html'
                ],
                tasks: 'html2js'
			}
		},
		jshint: {
			files  : ['gruntfile.js', 'js/app/**/*.js', 'js/common/**/*.js', 'test/**/*.js'],
			options: {
				ignores: ['js/built/*.js', 'js/app/i18n/**', 'js/app/templates.js', 'test/karma/lib/angular-mocks.js', 'js/common/directives/scrollbar/perfect-scrollbar.js'],
                globals: {
                    it: true,
                    expect: true,
                    describe: true,
                    beforeEach: true,
                    inject: true,
                    angular: true,
                    module: true
                }
			}
		},
		karma : {
			unit: {
				configFile: 'test/karma/karma.conf.js',
                browsers: ['PhantomJS'],
                singleRun: true
			}
		},
//        karma_sonar: {
//            options: {
//
//            },
//            your_target: {
//                project: {
//                    key: 'grunt-sonar',
//                    name: '<%= pkg.name %>',
//                    version: '<%= pkg.version %>'
//                },
//                sources: [
//                    {
//                        path: '...',
//                        prefix: '...',
//                        coverageReport: ''
//                    }
//                ]
//            }
//        },

		maven: {
			warName: '<%= pkg.name %>-frontend.war',
			dist: {
				dest: 'dist',
				src: ['<%= pkg.name %>.js', 'js/**', 'js/test/**']
			},
			maven: {
				src: ['./**']
			},
			watch: {
				tasks: ['default']
			}
		}
	});

	// Load NPM tasks
	grunt.loadNpmTasks('grunt-contrib-watch');
	grunt.loadNpmTasks('grunt-contrib-jshint');
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-html-build');
	grunt.loadNpmTasks('grunt-html2js');

	grunt.loadNpmTasks('grunt-karma');
	grunt.loadNpmTasks('grunt-karma-sonar');
//	grunt.loadTasks('maven-tasks');

	grunt.option('force', true);

	grunt.registerTask('default', ['jshint', 'html2js', 'watch']);
    grunt.registerTask('test', ['jshint', 'html2js', 'karma:unit']);
    grunt.registerTask('hint', ['jshint', 'watch']);
    grunt.registerTask('maven', ['jshint', 'karma:unit', 'html2js', 'htmlbuild:dev']);
    grunt.registerTask('maven-test', ['jshint', 'karma:unit', 'html2js', 'htmlbuild:dev', 'htmlbuild:test']);
	grunt.registerTask('maven-prod', ['html2js', 'karma:unit', 'concat', 'uglify', 'htmlbuild:dev', 'htmlbuild:prod']);
};
