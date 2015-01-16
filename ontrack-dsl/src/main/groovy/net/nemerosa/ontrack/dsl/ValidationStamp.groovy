package net.nemerosa.ontrack.dsl

interface ValidationStamp extends ProjectEntity {

    String getProject()

    String getBranch()

    def call(Closure closure)

    /**
     * Sets the image
     */
    def image(Object o)

    def image(Object o, String contentType)

    /**
     * Gets the image
     */
    Document getImage()

}
