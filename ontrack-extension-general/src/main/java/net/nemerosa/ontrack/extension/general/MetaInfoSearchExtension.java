package net.nemerosa.ontrack.extension.general;

import net.nemerosa.ontrack.extension.api.SearchExtension;
import net.nemerosa.ontrack.extension.support.AbstractExtension;
import net.nemerosa.ontrack.model.structure.*;
import net.nemerosa.ontrack.ui.controller.URIBuilder;
import net.nemerosa.ontrack.ui.support.AbstractSearchProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class MetaInfoSearchExtension extends AbstractExtension implements SearchExtension {

    private final URIBuilder uriBuilder;
    private final PropertyService propertyService;
    private final StructureService structureService;

    @Autowired
    public MetaInfoSearchExtension(GeneralExtensionFeature extensionFeature, URIBuilder uriBuilder, PropertyService propertyService, StructureService structureService) {
        super(extensionFeature);
        this.uriBuilder = uriBuilder;
        this.propertyService = propertyService;
        this.structureService = structureService;
    }

    @Override
    public SearchProvider getSearchProvider() {
        return new AbstractSearchProvider(uriBuilder) {
            @Override
            public boolean isTokenSearchable(String token) {
                return MetaInfoSearchExtension.this.isTokenSearchable(token);
            }

            @Override
            public Collection<SearchResult> search(String token) {
                return MetaInfoSearchExtension.this.search(token);
            }
        };
    }

    protected boolean isTokenSearchable(String token) {
        return StringUtils.indexOf(token, ":") > 0;
    }

    protected Collection<SearchResult> search(String token) {
        if (isTokenSearchable(token)) {
            String name = StringUtils.substringBefore(token, ":");
            String value = StringUtils.substringAfter(token, ":");
            // Searchs for all entities with the value
            Collection<ProjectEntity> entities = propertyService.searchWithPropertyValue(
                    MetaInfoPropertyType.class,
                    (entityType, id) -> entityType.getEntityFn(structureService).apply(id),
                    metaInfoProperty -> metaInfoProperty.matchNameValue(name, value)
            );
            // Returns search results
            return entities.stream()
                    .map(entity -> toSearchResult(entity, name))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    protected SearchResult toSearchResult(ProjectEntity entity, String name) {
        // Gets the property value for the meta info name (required)
        String value = propertyService.getProperty(entity, MetaInfoPropertyType.class).getValue().getValue(name)
                .orElseThrow(() -> new IllegalStateException("Expecting to have a meta info property"));
        // OK
        return new SearchResult(
                entity.getEntityDisplayName(),
                String.format("%s -> %s", name, value),
                uriBuilder.getEntityURI(entity),
                uriBuilder.getEntityPage(entity),
                100
        );
    }
}
