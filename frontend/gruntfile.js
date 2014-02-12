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
                src: 'views/DagpengerIndex.html',
                dest: 'views/Dagpenger.html',
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
                src: 'views/DagpengerIndex.html',
                dest: 'views/Dagpenger.html',
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
                src: 'views/DagpengerIndexProd.html',
                dest: 'views/Dagpenger.html',
                options: {
                    beautify: true,
                    relative: false,
                    prefix: '../',
                    scripts: {
                        built: 'js/built/built.js'
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
				dest  : 'js/built/built.js',
				nonull: true
			}
		},
		uglify: {
			options: {
				banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> */\n'
			},
			my_target: {
				files: {
					'js/built/built.min.js': ['js/built/built.js']
				}
			}
		},
		watch : {
			js  : {
				files  : ['js/js/**'],
				options: {
					livereload: true
				}
			},
			html: {
				files  : ['js/views/**'],
				options: {
					livereload: true
				}
			},
			css : {
				files  : ['js/css/**'],
				options: {
					livereload: true
				}
			}
		},
		jshint: {
			files  : ['gruntfile.js', 'js/js/**/*.js', 'test/karma/**/*.js', 'app/**/*.js'],
			options: {
				ignores: ['js/built/built.js', 'js/js/i18n/**']
			}
		},
		karma : {
			unit: {
				configFile: 'test/karma/karma.conf.js'
			}
		},
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
	grunt.loadTasks('maven-tasks');

	grunt.option('force', true);

	grunt.registerTask('default', ['jshint', 'watch']);
	grunt.registerTask('maven', ['jshint', 'karma:unit', 'html2js', 'htmlbuild:dev']);
	grunt.registerTask('test', ['jshint', 'html2js', 'karma:unit']);
	grunt.registerTask('prod', ['html2js', 'concat', 'htmlbuild:prod', 'uglify']);
};
