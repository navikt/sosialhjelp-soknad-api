module.exports = function (grunt) {
    var timestamp = grunt.template.today("yyyymmddHHMMss");
//    grunt.initConfig({
//        hub: {
//            options: {
//                concurrent: 1
//            },
//            default: {
//                src: ['build/pre/Gruntfile.js', 'build/!(pre)/Gruntfile.js', 'build/post/Gruntfile.js']
//            },
//            prod: {
//                src: ['build/pre/Gruntfile.js', 'build/!(pre|post)/Gruntfile.js', 'build/post/Gruntfile.js'],
//                tasks: ['prod']
//            }
//        }
//    });
//    grunt.loadNpmTasks('grunt-hub');
//    grunt.registerTask('default', ['hub:default']);
//    grunt.registerTask('prod', ['hub:prod']);

    grunt.initConfig({
        run_grunt: {
            options: {
                concurrent: 1,
                gruntOptions: {
                    timestamp: timestamp,
                    force: true
                }
            },
            default: {
                src: ['build/pre/Gruntfile.js', 'build/!(pre)/Gruntfile.js', 'build/post/Gruntfile.js']
            },
            prod: {
                options: {
                    task: ['prod']
                },
                src: ['build/pre/Gruntfile.js', 'build/!(pre)/Gruntfile.js', 'build/post/Gruntfile.js']
            }
        }
    });

    grunt.loadNpmTasks('grunt-run-grunt');
    grunt.registerTask('default', ['run_grunt:default']);
    grunt.registerTask('prod', ['run_grunt:prod']);

//    var timestamp = grunt.template.today("yyyymmddHHMMss");
//    var path = require('path');
//    require('load-grunt-config')(grunt, {
//        configPath: path.join(process.cwd(), 'grunt/dagpenger'),
//
//        // auto grunt.initConfig
//        init: true,
//
//        // data passed into config. Can use with <%= key %>
//        data: {
//            timestamp: timestamp
//        }
//    });
//
//
//	grunt.initConfig({
//		pkg   : grunt.file.readJSON('package.json'),
//        builtminNameSendsoknad: 'target/classes/META-INF/resources/js/built/built_sendsoknad' + timestamp + '.min.js',
//        builtminNameEttersending: 'target/classes/META-INF/resources/js/built/built_ettersending' + timestamp + '.min.js',
//        builtminNameGjenopptak: 'target/classes/META-INF/resources/js/built/built_gjenopptak' + timestamp + '.min.js',
//        builtminNameUtslagskriterierDagpenger: 'target/classes/META-INF/resources/js/built/built_utslagskriterier_dagpenger' + timestamp + '.min.js',
//        html2js: {
//            main: {
//                src: [
//                    'views/dagpenger/**/*.html',
//                    'views/common/**/*.html',
//                    'views/ettersending/**/*.html',
//                    'views/templates/**/*.html',
//                    'js/dagpenger/**/*.html',
//                    'js/ettersending/**/*.html',
//                    'js/common/**/*.html'
//                ],
//                dest: 'target/classes/META-INF/resources/js/dagpenger/templates.js'
//            },
//            ettersending: {
//                src: [
//                    'views/common/**/*.html',
//                    'views/ettersending/**/*.html',
//                    'views/templates/**/*.html',
//                    'js/ettersending/**/*.html',
//                    'js/common/**/*.html'
//                ],
//                dest: 'target/classes/META-INF/resources/js/ettersending/templates.js'
//            },
//            gjenopptak: {
//                src: [
//                    'views/common/**/*.html',
//                    'views/gjenopptak/**/*.html',
//                    'views/templates/**/*.html',
//                    'js/gjenopptak/**/*.html',
//                    'js/common/**/*.html'
//                ],
//                dest: 'target/classes/META-INF/resources/js/gjenopptak/templates.js'
//            },
//            utslagskriterierDagpenger: {
//                src: [
//                    'views/common/**/*.html',
//                    'views/utslagskriterier/dagpenger/**/*.html',
//                    'views/templates/**/*.html',
//                    'js/utslagskriterier/dagpenger/**/*.html',
//                    'js/common/**/*.html'
//                ],
//                dest: 'target/classes/META-INF/resources/js/utslagskriterier/dagpenger/templates.js'
//            }
//        },
//
//        htmlbuild: {
//            dev_sendsoknad: {
//                src: 'views/bootstrapTemplate.html',
//                dest: 'target/classes/META-INF/resources/views/built/bootstrapDevDagpenger.html',
//                options: {
//                    beautify: true,
//                    relative: false,
//                    prefix: '../',
//                    scripts: {
//                        angular: [
//                            'js/lib/angular/angular.js',
//                            'js/lib/angular/angular-*.js'
//                        ],
//                        fileupload: [
//                            'js/lib/jquery/jquery-ui-1.10.3.custom.js',
//                            'js/lib/jquery/cors/jquery.xdr-transport.js',
//                            'js/lib/jquery/cors/jquery.postmessage-transport.js',
//                            'js/lib/jquery/jquery.iframe-transport.js',
//                            'js/lib/jquery/jquery.fileupload.js',
//                            'js/lib/jquery/jquery.fileupload-process.js',
//                            'js/lib/jquery/jquery.fileupload-validate.js',
//                            'js/lib/jquery/jquery.fileupload-angular.js'
//                        ],
//                        libs: 'js/lib/*.js',
//                        app: [
//                            'js/dagpenger/**/*.js',
//                            'js/common/**/*.js'
//                        ]
//                    }
//                }
//            },
//            prod_sendsoknad: {
//                src: 'views/bootstrapTemplateProd.html',
//                dest: 'target/classes/META-INF/resources/views/built/bootstrapDagpenger.html',
//                options: {
//                    beautify: true,
//                    relative: false,
//                    prefix: '../',
//                    scripts: {
//                        built: {
//                            cwd: 'target/classes/META-INF/resources',
//                            files: 'js/built/built_sendsoknad' + timestamp + '.min.js'
//                        }
//                    }
//                }
//            },
//            dev_ettersending: {
//                src: 'views/bootstrapTemplate.html',
//                dest: 'target/classes/META-INF/resources/views/built/bootstrapDevEttersending.html',
//                options: {
//                    beautify: true,
//                    relative: false,
//                    prefix: '../',
//                    scripts: {
//                        angular: [
//                            'js/lib/angular/angular.js',
//                            'js/lib/angular/angular-*.js'
//                        ],
//                        fileupload: [
//                            'js/lib/jquery/jquery-ui-1.10.3.custom.js',
//                            'js/lib/jquery/cors/jquery.xdr-transport.js',
//                            'js/lib/jquery/cors/jquery.postmessage-transport.js',
//                            'js/lib/jquery/jquery.iframe-transport.js',
//                            'js/lib/jquery/jquery.fileupload.js',
//                            'js/lib/jquery/jquery.fileupload-process.js',
//                            'js/lib/jquery/jquery.fileupload-validate.js',
//                            'js/lib/jquery/jquery.fileupload-angular.js'
//                        ],
//                        libs: 'js/lib/*.js',
//                        app: [
//                            'js/ettersending/**/*.js',
//                            'js/common/**/*.js'
//                        ]
//                    }
//                }
//            },
//            prod_ettersending: {
//                src: 'views/bootstrapTemplateProd.html',
//                dest: 'target/classes/META-INF/resources/views/built/bootstrapEttersending.html',
//                options: {
//                    beautify: true,
//                    relative: false,
//                    prefix: '../',
//                    scripts: {
//                        built: {
//                            cwd: 'target/classes/META-INF/resources',
//                            files: 'js/built/built_ettersending' + timestamp + '.min.js'
//                        }
//                    }
//                }
//            },
//            dev_utslagskriterier_dagpenger: {
//                src: 'views/bootstrapTemplate.html',
//                dest: 'target/classes/META-INF/resources/views/built/bootstrapDevUtslagskriterierDagpenger.html',
//                options: {
//                    beautify: true,
//                    relative: false,
//                    prefix: '../',
//                    scripts: {
//                        angular: [
//                            'js/lib/angular/angular.js',
//                            'js/lib/angular/angular-*.js'
//                        ],
//                        fileupload: [
//                            'js/lib/jquery/jquery-ui-1.10.3.custom.js',
//                            'js/lib/jquery/cors/jquery.xdr-transport.js',
//                            'js/lib/jquery/cors/jquery.postmessage-transport.js',
//                            'js/lib/jquery/jquery.iframe-transport.js',
//                            'js/lib/jquery/jquery.fileupload.js',
//                            'js/lib/jquery/jquery.fileupload-process.js',
//                            'js/lib/jquery/jquery.fileupload-validate.js',
//                            'js/lib/jquery/jquery.fileupload-angular.js'
//                        ],
//                        libs: 'js/lib/*.js',
//                        app: [
//                            'js/utslagskriterier/dagpenger/**/*.js',
//                            'js/common/**/*.js'
//                        ]
//                    }
//                }
//            },
//            prod_utslagskriterier_dagpenger: {
//                src: 'views/bootstrapTemplateProd.html',
//                dest: 'target/classes/META-INF/resources/views/built/bootstrapUtslagskritererDagpenger.html',
//                options: {
//                    beautify: true,
//                    relative: false,
//                    prefix: '../',
//                    scripts: {
//                        built: {
//                            cwd: 'target/classes/META-INF/resources',
//                            files: 'js/built/built_utslagskriterier_dagpenger' + timestamp + '.min.js'
//                        }
//                    }
//                }
//            },
//            dev_gjenopptak: {
//                src: 'views/bootstrapTemplate.html',
//                dest: 'target/classes/META-INF/resources/views/built/bootstrapDevGjenopptak.html',
//                options: {
//                    beautify: true,
//                    relative: false,
//                    prefix: '../',
//                    scripts: {
//                        angular: [
//                            'js/lib/angular/angular.js',
//                            'js/lib/angular/angular-*.js'
//                        ],
//                        fileupload: [
//                            'js/lib/jquery/jquery-ui-1.10.3.custom.js',
//                            'js/lib/jquery/cors/jquery.xdr-transport.js',
//                            'js/lib/jquery/cors/jquery.postmessage-transport.js',
//                            'js/lib/jquery/jquery.iframe-transport.js',
//                            'js/lib/jquery/jquery.fileupload.js',
//                            'js/lib/jquery/jquery.fileupload-process.js',
//                            'js/lib/jquery/jquery.fileupload-validate.js',
//                            'js/lib/jquery/jquery.fileupload-angular.js'
//                        ],
//                        libs: 'js/lib/*.js',
//                        app: [
//                            'js/gjenopptak/**/*.js',
//                            'js/common/**/*.js'
//                        ]
//                    }
//                }
//            },
//            prod_gjenopptak: {
//                src: 'views/bootstrapTemplateProd.html',
//                dest: 'target/classes/META-INF/resources/views/built/bootstrapGjenopptak.html',
//                options: {
//                    beautify: true,
//                    relative: false,
//                    prefix: '../',
//                    scripts: {
//                        built: {
//                            cwd: 'target/classes/META-INF/resources',
//                            files: 'js/built/built_gjenopptak' + timestamp + '.min.js'
//                        }
//                    }
//                }
//            }
//        },
//		concat: {
//			options: {
//				separator: ';'
//			},
//			sendsoknad: {
//				src   : [
//                    'js/lib/angular/angular.js',
//                    'js/lib/angular/angular-*.js',
//                    'js/lib/jquery/jquery-ui-1.10.3.custom.js',
//                    'js/lib/jquery/cors/jquery.xdr-transport.js',
//                    'js/lib/jquery/cors/jquery.postmessage-transport.js',
//                    'js/lib/jquery/jquery.iframe-transport.js',
//                    'js/lib/jquery/jquery.fileupload.js',
//                    'js/lib/jquery/jquery.fileupload-process.js',
//                    'js/lib/jquery/jquery.fileupload-validate.js',
//                    'js/lib/jquery/jquery.fileupload-angular.js',
//                    'js/lib/*.js',
//                    'js/dagpenger/**/!(initDev).js',
//                    'target/classes/META-INF/resources/js/dagpenger/templates.js',
//                    'js/common/**/!(templates).js'
//				],
//				dest: 'target/classes/META-INF/resources/js/built/built_sendsoknad' + timestamp + '.js',
//				nonull: true
//			},
//            ettersending: {
//                src   : [
//                    'js/lib/angular/angular.js',
//                    'js/lib/angular/angular-*.js',
//                    'js/lib/jquery/jquery-ui-1.10.3.custom.js',
//                    'js/lib/jquery/cors/jquery.xdr-transport.js',
//                    'js/lib/jquery/cors/jquery.postmessage-transport.js',
//                    'js/lib/jquery/jquery.iframe-transport.js',
//                    'js/lib/jquery/jquery.fileupload.js',
//                    'js/lib/jquery/jquery.fileupload-process.js',
//                    'js/lib/jquery/jquery.fileupload-validate.js',
//                    'js/lib/jquery/jquery.fileupload-angular.js',
//                    'js/lib/*.js',
//                    'target/classes/META-INF/resources/js/ettersending/templates.js',
//                    'js/ettersending/**/!(initDev).js',
//                    'js/common/**/!(templates).js'
//                ],
//                dest: 'target/classes/META-INF/resources/js/built/built_ettersending' + timestamp + '.js',
//                nonull: true
//            },
//            gjenopptak: {
//                src   : [
//                    'js/lib/angular/angular.js',
//                    'js/lib/angular/angular-*.js',
//                    'js/lib/jquery/jquery-ui-1.10.3.custom.js',
//                    'js/lib/jquery/cors/jquery.xdr-transport.js',
//                    'js/lib/jquery/cors/jquery.postmessage-transport.js',
//                    'js/lib/jquery/jquery.iframe-transport.js',
//                    'js/lib/jquery/jquery.fileupload.js',
//                    'js/lib/jquery/jquery.fileupload-process.js',
//                    'js/lib/jquery/jquery.fileupload-validate.js',
//                    'js/lib/jquery/jquery.fileupload-angular.js',
//                    'js/lib/*.js',
//                    'target/classes/META-INF/resources/js/gjenopptak/templates.js',
//                    'js/gjenopptak/**/!(initDev).js',
//                    'js/common/**/!(templates).js'
//                ],
//                dest: 'target/classes/META-INF/resources/js/built/built_gjenopptak' + timestamp + '.js',
//                nonull: true
//            },
//            utslagskriterier_dagpenger: {
//                src   : [
//                    'js/lib/angular/angular.js',
//                    'js/lib/angular/angular-*.js',
//                    'js/lib/jquery/jquery-ui-1.10.3.custom.js',
//                    'js/lib/jquery/cors/jquery.xdr-transport.js',
//                    'js/lib/jquery/cors/jquery.postmessage-transport.js',
//                    'js/lib/jquery/jquery.iframe-transport.js',
//                    'js/lib/jquery/jquery.fileupload.js',
//                    'js/lib/jquery/jquery.fileupload-process.js',
//                    'js/lib/jquery/jquery.fileupload-validate.js',
//                    'js/lib/jquery/jquery.fileupload-angular.js',
//                    'js/lib/*.js',
//                    'target/classes/META-INF/resources/js/utslagskriterier/dagpenger/templates.js',
//                    'js/gjenopptak/**/!(initDev).js',
//                    'js/common/**/!(templates).js'
//                ],
//                dest: 'target/classes/META-INF/resources/js/built/built_utslagskriterier_dagpenger' + timestamp + '.js',
//                nonull: true
//            }
//		},
//        ngmin:  {
//            prod: {
//                cwd: 'target/classes/META-INF/resources/js/built',
//                expand: true,
//                src: ['**/*.js'],
//                dest: 'target/classes/grunt'
//            }
//        },
//		uglify: {
//			options: {
//				banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> */\n',
//                mangle: false
//			},
//			my_target: {
//				files: {
//                    '<%= builtminNameSendsoknad %>': ['target/classes/grunt/built_sendsoknad' + timestamp + '.js'],
//                    '<%= builtminNameEttersending %>': ['target/classes/grunt/built_ettersending' + timestamp + '.js'],
//                    '<%= builtminNameGjenopptak %>': ['target/classes/grunt/built_gjenopptak' + timestamp + '.js'],
//                    '<%= builtminNameUtslagskriterierDagpenger %>': ['target/classes/grunt/built_utslagskriterier_dagpenger' + timestamp + '.js']
//				}
//			}
//		},
//		jshint: {
//			files  : ['gruntfile.js', 'js/dagpenger/**/*.js', 'js/ettersending/**/*.js', 'js/gjenopptak/**/*.js', 'js/utslagskriterier/**/*.js', 'js/common/**/*.js', 'test/**/*.js'],
//			options: {
//				ignores: ['js/built/*.js', 'js/dagpenger/templates.js', 'js/ettersending/templates.js', 'js/gjenopptak/templates.js', 'js/utslagskriterier/dagpenger/templates.js', 'test/karma/lib/angular-mocks.js', 'js/common/tredjeparts/**/*.js'],
//                globals: {
//                    it: true,
//                    expect: true,
//                    describe: true,
//                    beforeEach: true,
//                    inject: true,
//                    angular: true,
//                    module: true,
//                    Date: true
//                }
//			}
//		},
//		karma : {
//			unit: {
//				configFile: 'test/karma/karma.conf.js',
//                browsers: ['PhantomJS'],
//                singleRun: true
//			},
//            local: {
//                configFile: 'test/karma/karma.conf.js',
//                singleRun: true
//            }
//		},
//
//        clean: ['target/classes/META-INF/resources/js/built', 'target/classes/grunt']
//    });
//
//	// Load NPM tasks
//    require('load-grunt-tasks')(grunt);
//
//	grunt.option('force', true);
//
//	grunt.registerTask('default', ['jshint', 'htmlbuild:dev_sendsoknad', 'htmlbuild:dev_ettersending', 'htmlbuild:dev_gjenopptak', 'htmlbuild:dev_utslagskriterier_dagpenger']);
//    grunt.registerTask('maven', ['jshint', 'karma:unit', 'html2js', 'htmlbuild:dev_sendsoknad', 'htmlbuild:dev_ettersending', 'htmlbuild:dev_gjenopptak', 'htmlbuild:dev_utslagskriterier_dagpenger']);
//    grunt.registerTask('mavenTest', ['karma:unit']);
//	grunt.registerTask('maven-prod', ['clean', 'html2js', 'concat:sendsoknad', 'concat:ettersending', 'concat:gjenopptak', 'concat:utslagskriterier_dagpenger', 'ngmin', 'uglify', 'htmlbuild:dev_sendsoknad', 'htmlbuild:prod_sendsoknad', 'htmlbuild:dev_ettersending', 'htmlbuild:prod_ettersending', 'htmlbuild:dev_gjenopptak', 'htmlbuild:prod_gjenopptak', 'htmlbuild:prod_utslagskriterier_dagpenger', 'htmlbuild:dev_utslagskriterier_dagpenger']);
};




//module.exports = function (grunt) {
//    var timestamp = grunt.template.today("yyyymmddHHMMss");
//    grunt.initConfig({
//        pkg   : grunt.file.readJSON('package.json'),
//        builtminNameSendsoknad: 'target/classes/META-INF/resources/js/built/built_sendsoknad' + timestamp + '.min.js',
//        builtminNameEttersending: 'target/classes/META-INF/resources/js/built/built_ettersending' + timestamp + '.min.js',
//        html2js: {
//            main: {
//                src: [
//                    'views/dagpenger/**/*.html',
//                    'views/common/**/*.html',
//                    'views/ettersending/**/*.html',
//                    'views/templates/**/*.html',
//                    'js/dagpenger/**/*.html',
//                    'js/ettersending/**/*.html',
//                    'js/common/**/*.html'
//                ],
//                dest: 'target/classes/META-INF/resources/js/dagpenger/templates.js'
//            },
//            ettersending: {
//                src: [
//                    'views/common/**/*.html',
//                    'views/ettersending/**/*.html',
//                    'views/templates/**/*.html',
//                    'js/ettersending/**/*.html',
//                    'js/common/**/*.html'
//                ],
//                dest: 'target/classes/META-INF/resources/js/ettersending/templates.js'
//            }
//        },
//
//        htmlbuild: {
//            dev_sendsoknad: {
//                src: 'views/bootstrapTemplate.html',
//                dest: 'target/classes/META-INF/resources/views/built/bootstrapDev.html',
//                options: {
//                    beautify: true,
//                    relative: false,
//                    prefix: '../',
//                    scripts: {
//                        angular: [
//                            'js/lib/angular/angular.js',
//                            'js/lib/angular/angular-*.js'
//                        ],
//                        fileupload: [
//                            'js/lib/jquery/jquery-ui-1.10.3.custom.js',
//                            'js/lib/jquery/cors/jquery.xdr-transport.js',
//                            'js/lib/jquery/cors/jquery.postmessage-transport.js',
//                            'js/lib/jquery/jquery.iframe-transport.js',
//                            'js/lib/jquery/jquery.fileupload.js',
//                            'js/lib/jquery/jquery.fileupload-process.js',
//                            'js/lib/jquery/jquery.fileupload-validate.js',
//                            'js/lib/jquery/jquery.fileupload-angular.js'
//                        ],
//                        libs: 'js/lib/*.js',
//                        app: [
//                            'js/dagpenger/**/*.js',
//                            'js/common/**/*.js'
//                        ]
//                    }
//                }
//            },
//            prod_sendsoknad: {
//                src: 'views/bootstrapTemplateProd.html',
//                dest: 'target/classes/META-INF/resources/views/built/bootstrap.html',
//                options: {
//                    beautify: true,
//                    relative: false,
//                    prefix: '../',
//                    scripts: {
//                        built: {
//                            cwd: 'target/classes/META-INF/resources',
//                            files: 'js/built/built_sendsoknad' + timestamp + '.min.js'
//                        }
//                    }
//                }
//            },
//            dev_ettersending: {
//                src: 'views/bootstrapTemplate.html',
//                dest: 'target/classes/META-INF/resources/views/built/bootstrapDevEttersending.html',
//                options: {
//                    beautify: true,
//                    relative: false,
//                    prefix: '../',
//                    scripts: {
//                        angular: [
//                            'js/lib/angular/angular.js',
//                            'js/lib/angular/angular-*.js'
//                        ],
//                        fileupload: [
//                            'js/lib/jquery/jquery-ui-1.10.3.custom.js',
//                            'js/lib/jquery/cors/jquery.xdr-transport.js',
//                            'js/lib/jquery/cors/jquery.postmessage-transport.js',
//                            'js/lib/jquery/jquery.iframe-transport.js',
//                            'js/lib/jquery/jquery.fileupload.js',
//                            'js/lib/jquery/jquery.fileupload-process.js',
//                            'js/lib/jquery/jquery.fileupload-validate.js',
//                            'js/lib/jquery/jquery.fileupload-angular.js'
//                        ],
//                        libs: 'js/lib/*.js',
//                        app: [
//                            'js/ettersending/**/*.js',
//                            'js/common/**/*.js'
//                        ]
//                    }
//                }
//            },
//            prod_ettersending: {
//                src: 'views/bootstrapTemplateProd.html',
//                dest: 'target/classes/META-INF/resources/views/built/bootstrapEttersending.html',
//                options: {
//                    beautify: true,
//                    relative: false,
//                    prefix: '../',
//                    scripts: {
//                        built: {
//                            cwd: 'target/classes/META-INF/resources',
//                            files: 'js/built/built_ettersending' + timestamp + '.min.js'
//                        }
//                    }
//                }
//            }
//        },
//        concat: {
//            options: {
//                separator: ';'
//            },
//            sendsoknad: {
//                src   : [
//                    'js/lib/angular/angular.js',
//                    'js/lib/angular/angular-*.js',
//                    'js/lib/jquery/jquery-ui-1.10.3.custom.js',
//                    'js/lib/jquery/cors/jquery.xdr-transport.js',
//                    'js/lib/jquery/cors/jquery.postmessage-transport.js',
//                    'js/lib/jquery/jquery.iframe-transport.js',
//                    'js/lib/jquery/jquery.fileupload.js',
//                    'js/lib/jquery/jquery.fileupload-process.js',
//                    'js/lib/jquery/jquery.fileupload-validate.js',
//                    'js/lib/jquery/jquery.fileupload-angular.js',
//                    'js/lib/*.js',
//                    'js/dagpenger/**/!(initDev).js',
//                    'target/classes/META-INF/resources/js/dagpenger/templates.js',
//                    'js/common/**/!(templates).js'
//                ],
//                dest: 'target/classes/META-INF/resources/js/built/built_sendsoknad' + timestamp + '.js',
//                nonull: true
//            },
//            ettersending: {
//                src   : [
//                    'js/lib/angular/angular.js',
//                    'js/lib/angular/angular-*.js',
//                    'js/lib/jquery/jquery-ui-1.10.3.custom.js',
//                    'js/lib/jquery/cors/jquery.xdr-transport.js',
//                    'js/lib/jquery/cors/jquery.postmessage-transport.js',
//                    'js/lib/jquery/jquery.iframe-transport.js',
//                    'js/lib/jquery/jquery.fileupload.js',
//                    'js/lib/jquery/jquery.fileupload-process.js',
//                    'js/lib/jquery/jquery.fileupload-validate.js',
//                    'js/lib/jquery/jquery.fileupload-angular.js',
//                    'js/lib/*.js',
//                    'target/classes/META-INF/resources/js/ettersending/templates.js',
//                    'js/ettersending/**/!(initDev).js',
//                    'js/common/**/!(templates).js'
//                ],
//                dest: 'target/classes/META-INF/resources/js/built/built_ettersending' + timestamp + '.js',
//                nonull: true
//            }
//        },
//        uglify: {
//            options: {
//                banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> */\n',
//                mangle: true
//            },
//            my_target: {
//                files: {
//                    '<%= builtminNameSendsoknad %>': ['target/classes/META-INF/resources/js/built/built_sendsoknad' + timestamp + '.js'],
//                    '<%= builtminNameEttersending %>': ['target/classes/META-INF/resources/js/built/built_ettersending' + timestamp + '.js']
//                }
//            }
//        },
//        jshint: {
//            files  : ['gruntfile.js', 'js/dagpenger/**/*.js', 'js/ettersending/**/*.js', 'js/common/**/*.js', 'test/**/*.js'],
//            options: {
//                ignores: ['js/built/*.js', 'js/dagpenger/templates.js', 'js/ettersending/templates.js', 'test/karma/lib/angular-mocks.js', 'js/common/tredjeparts/**/*.js'],
//                globals: {
//                    it: true,
//                    expect: true,
//                    describe: true,
//                    beforeEach: true,
//                    inject: true,
//                    angular: true,
//                    module: true,
//                    Date: true
//                }
//            }
//        },
//        karma : {
//            unit: {
//                configFile: 'test/karma/karma.conf.js',
//                browsers: ['PhantomJS'],
//                singleRun: true
//            },
//            local: {
//                configFile: 'test/karma/karma.conf.js',
//                singleRun: true
//            }
//        },
//
//        clean: ['target/classes/META-INF/resources/js/built'],
//
//        ngAnnotate: {
//            options: {
//                singleQuotes: true
//            },
//            sendsoknad: {
//                files: [
//                    {
//                        expand: true,
//                        src: ['target/classes/META-INF/resources/js/built/*.js']
//                    }
//                ]
//            }
//        }
//    });
//
//    // Load NPM tasks
//    grunt.loadNpmTasks('grunt-contrib-jshint');
//    grunt.loadNpmTasks('grunt-contrib-concat');
//    grunt.loadNpmTasks('grunt-contrib-uglify');
//    grunt.loadNpmTasks('grunt-contrib-clean');
//    grunt.loadNpmTasks('grunt-html-build');
//    grunt.loadNpmTasks('grunt-html2js');
//    grunt.loadNpmTasks('grunt-karma');
//    grunt.loadNpmTasks('grunt-ng-annotate');
//
//    grunt.option('force', true);
//
//    grunt.registerTask('default', ['jshint', 'htmlbuild:dev_sendsoknad', 'htmlbuild:dev_ettersending']);
//    grunt.registerTask('maven', ['jshint', 'karma:unit', 'html2js', 'htmlbuild:dev_sendsoknad', 'htmlbuild:dev_ettersending']);
//    grunt.registerTask('mavenTest', ['karma:unit']);
//    grunt.registerTask('maven-prod', ['clean', 'html2js', 'concat:sendsoknad', 'concat:ettersending', 'ngAnnotate', 'uglify', 'htmlbuild:dev_sendsoknad', 'htmlbuild:prod_sendsoknad', 'htmlbuild:dev_ettersending', 'htmlbuild:prod_ettersending']);
//};