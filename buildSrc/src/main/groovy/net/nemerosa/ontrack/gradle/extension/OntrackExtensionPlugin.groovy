package net.nemerosa.ontrack.gradle.extension

import com.moowork.gradle.node.task.NodeTask
import com.moowork.gradle.node.task.NpmTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class OntrackExtensionPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "[ontrack] Applying Ontrack plugin to ${project.path}"

        /**
         * Project's configuration
         */

        project.extensions.create('ontrack', OntrackExtension)

        /**
         * Checks the project's configuration
         */

        project.tasks.create('ontrackCheck', OntrackCheck)

        /**
         * NPM setup
         */

        project.apply plugin: 'com.moowork.node'
        project.node {
            version = '4.2.2'
            npmVersion = '4.2.2'
            download = true
        }

        /**
         * NPM tasks
         */

        project.ext {
            cacheDir = project.gradle.getGradleUserHomeDir() as String ?: "${System.getProperty("user.home")}/.cache/gradle"
        }

        project.tasks.create('npmCacheConfig', NpmTask) {
            description "Configure the NPM cache"
            def npmCacheDir = "${project.ext.cacheDir}/caches/npm"
            outputs.files project.file(npmCacheDir)
            args = [ 'config', 'set', 'cache', npmCacheDir ]
        }

        project.tasks.create('npmPrepareFiles') {
            description "Copies the package.json & gulpfile.js in the build directory of the extension module"
            doLast {
                project.mkdir 'build'
                // package.json
                project.file('build/package.json').text = getClass().getResourceAsStream('/extension/package.json').text
                // gulpfile.js
                project.file('build/gulpfile.js').text = getClass().getResourceAsStream('/extension/gulpfile.js').text
            }
        }

        project.tasks.create('npmPackages', NpmTask) {
            dependsOn project.tasks.npmCacheConfig
            dependsOn project.tasks.npmPrepareFiles
            description "Install Node.js packages"
            workingDir = project.file('build')
            args = [ 'install' ]
            inputs.files project.file('build/package.json')
            outputs.files project.file('build/node_modules')
        }

        /**
         * Gulp call
         */

        project.tasks.create('web', NodeTask) {
            dependsOn 'npmPackages'
            workingDir = project.file('build')
            script = project.file('build/node_modules/gulp/bin/gulp')
            args = [
                    'default',
                    '--version', project.version,
                    '--src', project.file('src/main/resources/static')
            ]
            inputs.dir project.file('src/main/resources/static')
            ext.outputFile = project.file('build/web/dist/module.js')
            outputs.file outputFile
        }

        /**
         * Update of the JAR task
         */

        project.tasks.jar {
            dependsOn 'web'
            dependsOn 'ontrackCheck'
            from('build/web/dist') {
                into { "static/extension/${project.extensions.ontrack.id}/" }
            }
            exclude 'static/**/*.js'
            exclude 'static/**/*.html'
        }

    }

}
