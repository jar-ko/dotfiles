package org.example;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import org.apache.commons.lang3.StringUtils;

//    implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.14.2'
//    implementation 'com.fasterxml.jackson.core:jackson-databind:2.14.2'
//    implementation 'io.swagger.parser.v3:swagger-parser:2.1.12'
//    implementation 'org.apache.commons:commons-lang3:3.12.0'

public class XsltGenerator {
    private static final String SWAGGER_YAML_PATH = "/swagger.yaml";
    private static final String PATH = "/pet";
    private static final Method METHOD = Method.POST;
    private static final RequestResponse REQUEST_RESPONSE = RequestResponse.REQUEST;
    private static final String MEDIA_TYPE = "application/json";
    private static final String SUCCESSFUL_RESPONSE_CODE = "200";

    public static void main(String[] args) {
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolveFully(true);

        PathItem pathItem = new OpenAPIV3Parser()
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

        parseSchema(schema, "jsonObject", 0);
    }

    private static void parseSchema(Schema<?> schema, String path, int prepend) {
        System.out.println(StringUtils.leftPad("", prepend, " ") + path + " " + schema.getType());
        if ("object".equalsIgnoreCase(schema.getType())) {
            schema
                    .getProperties()
                    .forEach((key, value) -> parseSchema(value, path + "/" + key, prepend + 2));
        } else if ("array".equalsIgnoreCase(schema.getType())) {
            if ("object".equalsIgnoreCase(schema.getItems().getType())) {
                parseSchema(schema.getItems(), path, prepend + 2);
            } else {
                System.out.println(StringUtils.leftPad("", prepend, " ") + path + " " + schema.getItems().getType());
            }
        } else if (schema.getOneOf() != null) {
            System.out.println(StringUtils.leftPad("", prepend, " ") + "# oneOf start");
            schema
                    .getOneOf()
                    .forEach(it -> parseSchema(it, path, prepend + 2));
            System.out.println(StringUtils.leftPad("", prepend, " ") + "# oneOf end");
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
                .filter(it -> SUCCESSFUL_RESPONSE_CODE.equalsIgnoreCase(it.getKey()))
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
