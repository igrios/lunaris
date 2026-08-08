package com.lunaris.ansenuza.infrastructure.web.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/** Acepta tanto un texto como un arreglo de textos en payloads públicos. */
public class StringOrStringListDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isArray()) {
            return StreamSupport.stream(node.spliterator(), false)
                    .filter(JsonNode::isValueNode)
                    .map(JsonNode::asText)
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.joining(", "));
        }
        if (node.isValueNode()) {
            return node.asText();
        }
        return context.handleUnexpectedToken(String.class, parser).toString();
    }
}
