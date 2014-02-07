module.exports = function (grunt) {

	grunt.initConfig({
		pkg   : grunt.file.readJSON('package.json'),
		concat: {
			options: {
				separator: ';'
			},
			dist   : {
				src   : [
					'public/lib/angular/*.js',
					'public/lib/bindonce.js',
					'public/js/controllers/**/*.js',
					'public/js/directives/**/*.js',
					'public/js/common/**/*.js',
					'public/js/i18n/**/*.js'
				],
				dest  : 'public/built/built.js',
				nonull: true
			}
		},
		uglify: {
			options: {
				banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> */\n'
			},
			my_target: {
				files: {
					'public/built/built.min.js': ['public/built/built.js']
				}
			}
		},
		watch : {
			js  : {
				files  : ['public/js/**'],
				options: {
					livereload: true
				}
			},
			html: {
				files  : ['public/views/**'],
				options: {
					livereload: true
				}
			},
			css : {
				files  : ['public/css/**'],
				options: {
					livereload: true
				}
			}
		},
		jshint: {
			files  : ['gruntfile.js', 'public/js/**/*.js', 'test/karma/**/*.js', 'app/**/*.js'],
			options: {
				ignores: ['public/built/built.js', 'public/js/i18n/**']
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

	grunt.loadNpmTasks('grunt-karma');
	grunt.loadTasks('maven-tasks');

	grunt.option('force', true);

	grunt.registerTask('default', ['jshint', 'watch']);
	grunt.registerTask('test', ['jshint', 'karma:unit']);
	grunt.registerTask('prod', ['concat', 'uglify']);
};
