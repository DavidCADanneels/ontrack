package net.nemerosa.ontrack.dsl

interface Branch extends ProjectEntity {

    String getProject()

    def call(Closure closure)

    // Branch structure

    PromotionLevel promotionLevel(String name, String description)

    ValidationStamp validationStamp(String name, String description)

    Build build(String name, String description)

    // Filters

    List<Build> filter(String filterType, Map<String, ?> filterConfig)

    List<Build> standardFilter(Map<String, ?> filterConfig)

    List<Build> getLastPromotedBuilds()

    // Templating

    def template(Closure closure)
}