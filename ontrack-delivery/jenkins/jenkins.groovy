/**
 * List of global parameters:
 *
 * - JDK8u20
 *
 * List of plug-ins:
 *
 * - Delivery pipeline
 * - Build pipeline
 * - Parameterized trigger
 * - Git
 * - Folders
 * - Set build name
 * - Xvfb
 * - Ontrack
 */

/**
 * Variables
 */

def REPOSITORY = 'nemerosa/ontrack'
def PROJECT = 'ontrack'
def LOCAL_REPOSITORY = '/var/lib/jenkins/repository/ontrack/2.0'

/**
 * Folder for the project (making sure)
 */

folder {
    name PROJECT
}

/**
 * Generation for all branches
 */

URL branchApi = new URL("https://api.github.com/repos/${REPOSITORY}/branches")
def branches = new groovy.json.JsonSlurper().parse(branchApi.newReader())

branches.each {
    def BRANCH = it.name
    // Branch type
    def branchType
    int pos = BRANCH.indexOf('/')
    if (pos > 0) {
        branchType = BRANCH.substring(0, pos)
    } else {
        branchType = BRANCH
    }
    println "BRANCH = ${BRANCH}"
    println "\tBranchType = ${branchType}"
    // Keeps only some version types
    if (['master', 'feature', 'release', 'hotfix'].contains(branchType)) {
        // Normalised branch name
        def NAME = BRANCH.replaceAll(/[^A-Za-z0-9\.\-_]/, '-')
        println "\tGenerating ${NAME}..."
        // Folder for the branch
        folder {
            name "${PROJECT}/${PROJECT}-${NAME}"
        }

        // Quick check job
        job {
            name "${PROJECT}/${PROJECT}-${NAME}/${PROJECT}-${NAME}-01-quick"
            logRotator(numToKeep = 40)
            deliveryPipelineConfiguration('Commit', 'Quick check')
            jdk 'JDK8u20'
            scm {
                git {
                    remote {
                        url 'git@github.com:nemerosa/ontrack.git'
                        branch "origin/${BRANCH}"
                    }
                    localBranch "${BRANCH}"
                }
            }
            triggers {
                scm 'H/5 * * * *'
            }
            steps {
                gradle 'displayVersion writeVersion test --info'
            }
            publishers {
                archiveJunit("**/build/test-results/*.xml")
                downstreamParameterized {
                    trigger("${PROJECT}/${PROJECT}_${NAME}/${PROJECT}_${NAME}-02-package", 'SUCCESS', false) {
                        gitRevision(true)
                    }
                }
            }
        }

        // Package job
        job {
            name "${PROJECT}/${PROJECT}-${NAME}/${PROJECT}-${NAME}-02-package"
            logRotator(numToKeep = 40)
            deliveryPipelineConfiguration('Commit', 'Package')
            jdk 'JDK8u20'
            scm {
                git {
                    remote {
                        url 'git@github.com:nemerosa/ontrack.git'
                        branch "origin/${BRANCH}"
                    }
                    wipeOutWorkspace()
                    localBranch "${BRANCH}"
                }
            }
            steps {
                gradle 'clean displayVersion writeVersion test integrationTest release  --info --profile'
                conditionalSteps {
                    condition {
                        status('SUCCESS', 'SUCCESS')
                    }
                    runner('Fail')
                    shell """\
# Copies the JAR to a local directory
ontrack-delivery/archive.sh --source=\${WORKSPACE} --destination=${LOCAL_REPOSITORY}
"""
                }
                environmentVariables {
                    propertiesFile 'version.properties'
                }
            }
            publishers {
                archiveJunit("**/build/test-results/*.xml")
                downstreamParameterized {
                    trigger("${PROJECT}/${PROJECT}_${NAME}/${PROJECT}_${NAME}-11-acceptance-local", 'SUCCESS', false) {
                        propertiesFile('version.properties')
                    }
                }
            }
            configure { node ->
                node / 'buildWrappers' / 'org.jenkinsci.plugins.xvfb.XvfbBuildWrapper' {
                    'installationName'('default')
                    'screen'('1024x768x24')
                    'displayNameOffset'('1')
                }
                node / 'publishers' / 'net.nemerosa.ontrack.jenkins.OntrackBuildNotifier' {
                    'project'('ontrack')
                    'branch'(NAME)
                    'build'('${ONTRACK_VERSION_BUILD}')
                }
            }
        }

        view(type: DeliveryPipelineView) {
            name "${PROJECT}/${PROJECT}-${NAME}/Pipeline"
            pipelineInstances(4)
            enableManualTriggers()
            showChangeLog()
            updateInterval(5)
            pipelines {
                component("ontrack-${NAME}", "${PROJECT}/${PROJECT}-${NAME}/${PROJECT}-${NAME}-01-quick")
            }
        }


    } else {
        println "\tSkipping."
    }
}
