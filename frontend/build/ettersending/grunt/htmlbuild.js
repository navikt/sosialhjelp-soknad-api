module.exports = {
    dev: {
        src: '../../views/bootstrapTemplate.html',
        dest: '<%= resourcePath %>views/built/bootstrapDevEttersending.html',
        options: {
            beautify: true,
            relative: false,
            prefix: '../',
            scripts: {
                angular: {
                    cwd: '../../',
                    files: [
                        'js/lib/angular/angular.js',
                        'js/lib/angular/angular-*.js'
                    ]
                },
                fileupload: {
                    cwd: '../../',
                    files: [
                        'js/lib/jquery/jquery-ui-1.10.3.custom.js',
                        'js/lib/jquery/cors/jquery.xdr-transport.js',
                        'js/lib/jquery/cors/jquery.postmessage-transport.js',
                        'js/lib/jquery/jquery.iframe-transport.js',
                        'js/lib/jquery/jquery.fileupload.js',
                        'js/lib/jquery/jquery.fileupload-process.js',
                        'js/lib/jquery/jquery.fileupload-validate.js',
                        'js/lib/jquery/jquery.fileupload-angular.js'
                    ]
                },
                libs: {
                    cwd: '../../',
                    files: ['js/lib/*.js']
                },
                app: {
                    cwd: '../../',
                    files: [
                        'js/ettersending/**/*.js',
                        'js/common/**/*.js'
                    ]
                }
            }
        }
    },
    prod: {
        src: '../../views/bootstrapTemplateProd.html',
        dest: '<%= resourcePath %>views/built/bootstrapEttersending.html',
        options: {
            beautify: true,
            relative: false,
            prefix: '../',
            scripts: {
                built: {
                    cwd: '<%= resourcePath %>',
                    files: '<%= jsBuilt %>'
                }
            }
        }
    }
};