module.exports = {
    options: {
        separator: ';'
    },
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
        'js/dagpenger/**/!(initDev).js',
        'target/classes/META-INF/resources/js/dagpenger/templates.js',
        'js/common/**/!(templates).js'
    ],
    dest: 'target/classes/META-INF/resources/' + '<%= jsBuilt %>',
    nonull: true
};