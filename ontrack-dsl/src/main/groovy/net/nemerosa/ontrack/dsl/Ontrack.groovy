package net.nemerosa.ontrack.dsl

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import net.nemerosa.ontrack.dsl.http.OTHttpClient
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity

/**
 * Entry point for the DSL.
 */
class Ontrack {

    /**
     * HTTP client
     */
    private final OTHttpClient httpClient
    private final JsonSlurper jsonSlurper = new JsonSlurper()

    /**
     * Construction of the Ontrack client, based on a raw HTTP client
     */
    Ontrack(OTHttpClient httpClient) {
        this.httpClient = httpClient
    }

    Project project(String name) {
        // Gets the list of projects and looks for an existing project
        def projectNode = get("structure/projects").resources.find {
            it.name == name
        }
        // If project exists, loads it
        if (projectNode) {
            new Project(
                    this,
                    get(projectNode._self)
            )
        }
        // If it does not exist, creates it
        else {
            new Project(
                    this,
                    post(
                            'structure/projects/create',
                            [
                                    name       : name,
                                    description: ''
                            ]
                    )
            )
        }
    }

    Project project(String name, Closure closure) {
        def project = project(name)
        project.call(closure)
        project
    }

    Branch branch(String project, String branch) {
        new Branch(
                this,
                get("structure/entity/branch/${project}/${branch}")
        )
    }

    PromotionLevel promotionLevel(String project, String branch, String promotionLevel) {
        new PromotionLevel(
                this,
                get("structure/entity/promotionLevel/${project}/${branch}/${promotionLevel}")
        )
    }

    ValidationStamp validationStamp(String project, String branch, String validationStamp) {
        new ValidationStamp(
                this,
                get("structure/entity/validationStamp/${project}/${branch}/${validationStamp}")
        )
    }

    Build build(String project, String branch, String build) {
        new Build(
                this,
                get("structure/entity/build/${project}/${branch}/${build}")
        )
    }

    def configure(Closure closure) {
        Config configResource = new Config(this)
        closure.delegate = configResource
        closure()
    }

    def get(String url) {
        httpClient.get(url) { jsonSlurper.parseText(it) }
    }

    def post(String url, Object data) {
        httpClient.post(
                url,
                new StringEntity(
                        new JsonBuilder(data).toPrettyString(),
                        ContentType.create("application/json", "UTF-8")
                )
        ) { jsonSlurper.parseText(it) }
    }

    def put(String url, Object data) {
        httpClient.put(
                url,
                new StringEntity(
                        new JsonBuilder(data).toPrettyString(),
                        ContentType.create("application/json", "UTF-8")
                )
        ) { jsonSlurper.parseText(it) }
    }

    def upload(String url, String name, Object o) {
        upload(url, name, o, 'application/x-octet-stream')
    }

    def upload(String url, String name, Object o, String contentType) {
        Document document
        String fileName = 'file'
        if (o instanceof Document) {
            document = o as Document
        } else if (o instanceof URL) {
            URL u = o as URL
            fileName = u.file
            def connection = u.openConnection()
            document = new Document(
                    connection.getContentType(),
                    connection.inputStream.bytes
            )
        } else if (o instanceof File) {
            File file = o as File
            fileName = file.name
            document = new Document(
                    contentType,
                    file.bytes
            )
        } else if (o instanceof byte[]) {
            document = new Document(
                    contentType,
                    o as byte[]
            )
        } else if (o instanceof String) {
            def path = o as String
            if (path.startsWith('classpath:')) {
                path = path - 'classpath:'
                URL u = getClass().getResource(path)
                upload(url, name, u, contentType)
                return
            } else {
                try {
                    URL u = new URL(path)
                    upload(url, name, u, contentType)
                    return
                } catch (MalformedURLException ignored) {
                    File file = new File(path)
                    upload(url, name, file, contentType)
                    return
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported document type: ${o}")
        }
        httpClient.upload(
                url,
                name,
                fileName,
                document,
        ) { it ? jsonSlurper.parseText(it) : [:] }
    }

    Document download(String url) {
        httpClient.download(url)
    }
}
