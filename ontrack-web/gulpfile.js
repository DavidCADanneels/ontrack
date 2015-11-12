var gulp = require('gulp');
var less = require('gulp-less');
var minifyCss = require('gulp-minify-css');
var concat = require('gulp-concat');
var liveReload = require('gulp-livereload');
var inject = require('gulp-inject');
var jshint = require('gulp-jshint');
var uglify = require('gulp-uglify');
var series = require('stream-series');
var templateCache = require('gulp-angular-templatecache');
var ngAnnotate = require('gulp-ng-annotate');
var ngFilesort = require('gulp-angular-filesort');
var debug = require('gulp-debug');
var minimist = require('minimist');
var del = require('del');

// Arguments

var knownOptions = {
    string: 'version',
    default: {version: 'snapshot'}
};

var options = minimist(process.argv.slice(2), knownOptions);

// Paths

var web = 'src';
var webPath = './' + web;
var lessResources = webPath + '/less/*.less';
var jsResources = webPath + '/app/**/*.js';
var templateResources = webPath + '/app/**/*.html';
var indexResource = webPath + '/index.html';
var vendor = './vendor';

var build = 'build/web';

var buildPath = build + '/tmp';
var buildTemplates = buildPath + '/templates';
var buildAngular = buildPath + '/angular';

var outputPath = build + '/main';
var output = './' + outputPath;
var outputCss = './' + outputPath + '/css';
var outputJs = './' + outputPath + '/js';
var outputFonts = './' + outputPath + '/fonts';

// Vendor resources

var vendorResources = [
    'jquery/dist/jquery.js',
    'jquery-ui/jquery-ui.js',
    'angular/angular.js',
    'angular-ui-router/release/angular-ui-router.js',
    'angular-ui-sortable/sortable.js',
    'angular-multi-select/angular-multi-select.js',
    'angular-taglist/js/angular-taglist-directive.js',
    'angular-sanitize/angular-sanitize.js',
    'angular-bootstrap/ui-bootstrap-tpls.js',
    'moment/min/moment.min.js'
].map(function (rel) {
        return vendor + '/' + rel;
    });

// TODO Vendor CSS
//'vendor/angular-multi-select/angular-multi-select.css',
//'vendor/angular-taglist/css/angular-taglist-directive.css'

// TODO Copy of assets

// TODO Copy of fonts

// Cleaning

gulp.task('clean', function () {
    return del([build]);
});

// Javascript handling

gulp.task('lint', function () {
    return gulp.src(jsResources)
        .pipe(debug({title: 'lint:'}))
        .pipe(jshint())
        .pipe(jshint.reporter('default'));
});

gulp.task('templates', function () {
    return gulp.src(templateResources)
        .pipe(debug({title: 'templates:'}))
        .pipe(templateCache({module: 'ci', root: 'app/'}))
        .pipe(gulp.dest(buildTemplates));
});

/**
 * Sorted and annotated Angular files
 */
gulp.task('js:angular', ['lint', 'templates'], function () {
    return gulp.src([buildTemplates + '/*.js', jsResources])
        .pipe(debug({title: 'js:angular:input'}))
        .pipe(ngAnnotate())
        .pipe(ngFilesort())
        .pipe(concat('ci-angular.js'))
        .pipe(gulp.dest(buildAngular))
        .pipe(debug({title: 'js:angular:output'}));
});

gulp.task('js:concat', ['js:angular'], function () {
    var jsSource = vendorResources;
    jsSource.push(buildAngular + '/*.js');
    return gulp.src(jsSource)
        .pipe(debug({title: 'js:concat:input'}))
        .pipe(concat('ci-' + options.version + '.js'))
        .pipe(uglify())
        .pipe(gulp.dest(outputJs))
        .pipe(debug({title: 'js:concat:output'}))
        ;
});

// Translating Less into Minified CSS

gulp.task('less', function () {
    return gulp.src(lessResources)
        .pipe(debug({title: 'less:'}))
        .pipe(less())
        .pipe(minifyCss())
        .pipe(concat('ci-' + options.version + '.css'))
        .pipe(gulp.dest(outputCss))
        .pipe(liveReload());
});

// Fonts

gulp.task('fonts', function () {
    return gulp.src(vendor + '/font-awesome/fonts/*.*')
        .pipe(gulp.dest(outputFonts));
});

// Injection in index.html

gulp.task('index:dev', ['less', 'fonts', 'templates'], function () {
    var cssSources = gulp.src([outputCss + '/*.css'], {read: false});
    var vendorSources = gulp.src(vendorResources, {read: false});
    var appSources = gulp.src([buildTemplates + '/*.js', jsResources]).pipe(ngFilesort());

    return gulp.src(indexResource)
        .pipe(debug({title: 'index:dev:input'}))
        .pipe(inject(
            series(
                cssSources,
                vendorSources,
                appSources
            ),
            {relative: false, ignorePath: [outputPath, web, buildPath], addRootSlash: false}))
        .pipe(gulp.dest(output))
        .pipe(debug({title: 'index:dev:output'}));
});

gulp.task('index:prod', ['less', 'fonts', 'templates', 'js:concat'], function () {
    var cssSources = gulp.src([outputCss + '/*.css'], {read: false});
    var jsSources = gulp.src(outputJs + '/*.js', {read: false});

    return gulp.src(indexResource)
        .pipe(debug({title: 'index:prod:input'}))
        .pipe(inject(
            series(
                cssSources,
                jsSources
            ),
            {relative: false, ignorePath: [outputPath, web, buildPath], addRootSlash: false}))
        .pipe(gulp.dest(output))
        .pipe(debug({title: 'index:prod:output'}));
});

// Default build

gulp.task('dev', ['index:dev']);

gulp.task('default', ['index:prod']);

// Watch setup

gulp.task('watch', function () {
    liveReload.listen();
    gulp.watch(lessResources, ['less']);
    gulp.watch(indexResource, ['index:dev']);
    gulp.watch(jsResources, ['lint', 'index:dev']);
    gulp.watch(templateResources, ['templates']);
});
