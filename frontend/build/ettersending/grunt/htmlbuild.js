module.exports = {
    dev: {
        src: 'views/bootstrapTemplate.html',
        dest: 'target/classes/META-INF/resources/views/built/bootstrapDevEttersending.html',
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
                    'js/ettersending/**/*.js',
                    'js/common/**/*.js'
                ]
            }
        }
    },
    prod: {
        src: 'views/bootstrapTemplateProd.html',
        dest: 'target/classes/META-INF/resources/views/built/bootstrapEttersending.html',
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