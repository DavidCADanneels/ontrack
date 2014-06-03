package net.nemerosa.ontrack.ui.resource;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public interface ResourceObjectMapper {

    ObjectMapper getObjectMapper();

    ResourceContext getResourceContext();

    String write(Object o) throws JsonProcessingException;

    void write(JsonGenerator jgen, Object o) throws IOException;

    String write(Object o, Class<?> view) throws JsonProcessingException;

    void write(JsonGenerator jgen, Object o, Class<?> view) throws IOException;
}
