package org.example;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;

import java.util.List;

public class XsltGenerator {
    private static final String SWAGGER_YAML_PATH = "/swagger.yaml";
    private static final String PATH = "/pet";
    private static final Method METHOD = Method.POST;
    private static final RequestResponse REQUEST_RESPONSE = RequestResponse.REQUEST;
    private static final String MEDIA_TYPE = "application/json";
    private static final List<String> SUCCESSFUL_RESPONSES_CODES = List.of("200");

    public static void main(String[] args) {
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolveFully(true);

        PathItem pathItem = new OpenAPIParser()
                .readLocation(SWAGGER_YAML_PATH, null, parseOptions)
                .getOpenAPI()
                .getPaths()
                .get(PATH);

        Schema<?> schema = getContent(pathItem)
                .entrySet()
                .stream()
                .filter(it -> it.getKey().contains(MEDIA_TYPE))
                .findFirst()
                .orElseThrow()
                .getValue()
                .getSchema();

        parseSchema(schema, "jsonObject");
    }

    private static void parseSchema(Schema<?> schema, String path) {
        System.out.println(path + " " + schema.getType());
        if ("object".equalsIgnoreCase(schema.getType())) {
            schema
                    .getProperties()
                    .forEach((key, value) -> parseSchema(value, path + "/" + key));
        } else if ("array".equalsIgnoreCase(schema.getType())) {
            if ("object".equalsIgnoreCase(schema.getItems().getType())) {
                parseSchema(schema.getItems(), path);
            } else {
                System.out.println(path + " " + schema.getItems().getType());
            }
        }
    }

    private static Content getContent(PathItem pathItem) {
        if (REQUEST_RESPONSE == RequestResponse.REQUEST) {
            return getOperation(pathItem)
                    .getRequestBody()
                    .getContent();
        }

        return getOperation(pathItem)
                .getResponses()
                .entrySet()
                .stream()
                .filter(it -> SUCCESSFUL_RESPONSES_CODES.contains(it.getKey()))
                .findFirst()
                .orElseThrow()
                .getValue()
                .getContent();
    }

    private static Operation getOperation(PathItem pathItem) {
        switch (METHOD) {
            case POST:
                return pathItem.getPost();
            case GET:
                return pathItem.getGet();
            case PUT:
                return pathItem.getPut();
            case DELETE:
                return pathItem.getDelete();
        }
        throw new RuntimeException("Unsupported method " + METHOD);
    }

    private enum Method {
        POST, GET, PUT, DELETE
    }

    private enum RequestResponse {
        REQUEST, RESPONSE
    }
}
