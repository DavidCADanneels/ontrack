package net.nemerosa.ontrack.ui.resource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import org.apache.commons.lang3.Validate;

import java.io.IOException;

import static java.lang.String.format;

public class ResourceDecoratorSerializer<T> extends BeanSerializerBase {

    private final ResourceContext resourceContext;
    private final ResourceDecorator<T> resourceDecorator;

    public ResourceDecoratorSerializer(BeanSerializerBase serializer, ResourceDecorator<T> resourceDecorator, ResourceContext resourceContext) {
        super(serializer);
        this.resourceDecorator = resourceDecorator;
        this.resourceContext = resourceContext;
    }

    protected ResourceDecoratorSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter, ResourceContext resourceContext, ResourceDecorator<T> resourceDecorator) {
        super(src, objectIdWriter);
        this.resourceContext = resourceContext;
        this.resourceDecorator = resourceDecorator;
    }

    protected ResourceDecoratorSerializer(BeanSerializerBase src, String[] toIgnore, ResourceContext resourceContext, ResourceDecorator<T> resourceDecorator) {
        super(src, toIgnore);
        this.resourceContext = resourceContext;
        this.resourceDecorator = resourceDecorator;
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
        return new ResourceDecoratorSerializer<T>(
                this,
                objectIdWriter,
                resourceContext,
                resourceDecorator
        );
    }

    @Override
    protected BeanSerializerBase withIgnorals(String[] toIgnore) {
        return new ResourceDecoratorSerializer<T>(
                this,
                toIgnore,
                resourceContext,
                resourceDecorator
        );
    }

    @Override
    protected BeanSerializerBase asArraySerializer() {
        throw new UnsupportedOperationException("asArraySerializer");
    }

    @Override
    protected BeanSerializerBase withFilterId(Object filterId) {
        throw new UnsupportedOperationException("withFilterId");
    }

    @Override
    public void serialize(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        // Checks the type
        Validate.isTrue(
                resourceDecorator.appliesFor(bean.getClass()),
                format(
                        "The bean class <%s> cannot be processed by the <%s> decorator.",
                        bean.getClass().getName(),
                        resourceDecorator.getClass().getName()
                )
        );
        @SuppressWarnings("unchecked")
        T t = (T) bean;

        // Starting the serialization
        jgen.writeStartObject();

        // Default fields
        serializeFields(bean, jgen, provider);

        // Decorations
        for (Link link : resourceDecorator.links(t, resourceContext)) {
            jgen.writeObjectField(link.getName(), link.getHref());
        }

        // End
        jgen.writeEndObject();
    }

}
