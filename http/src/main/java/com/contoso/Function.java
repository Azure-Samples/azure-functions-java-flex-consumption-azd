package com.contoso;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/httpget". Invoke it using "curl" command in bash:
     * curl "http://localhost:7071/api/httpget?name=Awesome%20Developer"
     */
    @FunctionName("httpget")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.FUNCTION)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String name = Optional.ofNullable(request.getQueryParameters().get("name")).orElse("World");

        return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
    }

    /**
     * This function listens at endpoint "/api/httppost". Invoke it using "curl" command in bash:
     * curl -i -X POST http://localhost:7071/api/httppost -H "Content-Type: text/json" -d "{\"name\": \"Awesome Developer\", \"age\": \"25\"}"
     */
    @FunctionName("httppost")
    public HttpResponseMessage runPost(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.FUNCTION)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a POST request.");

        // Parse request body
        String name;
        Integer age;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(request.getBody().orElse("{}"));
            name = Optional.ofNullable(jsonNode.get("name")).map(JsonNode::asText).orElse(null);
            age = Optional.ofNullable(jsonNode.get("age")).map(JsonNode::asInt).orElse(null);
            if (name == null || age == null) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Please provide both name and age in the request body.").build();
            }
        } catch (Exception e) {
            context.getLogger().severe("Error parsing request body: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Error parsing request body").build();
        }

        return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name +"! You are " + age +" years old.").build();
    }
}
