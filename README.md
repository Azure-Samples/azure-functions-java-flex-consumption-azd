---
description: This repository contains an Azure Functions HTTP trigger quickstart written in Java and deployed to Azure Functions Flex Consumption using the Azure Developer CLI (AZD). This sample uses managed identity and a virtual network to insure it is secure by default.
page_type: sample
products:
- azure-functions
- azure
urlFragment: starter-http-trigger-java
languages:
- java
- bicep
- azdeveloper
---

# Starter template for Flex Consumption plan apps | Azure Functions

This sample template provides a set of basic HTTP trigger functions in java that are ready to run locally and can be easily deployed to a function app in Azure Functions.  

[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://github.com/codespaces/new?hide_repo_select=true&ref=main&repo=575770869)

## Run in your local environment

The project is designed to run on your local computer, provided you have met the [required prerequisites](#prerequisites). You can run the project locally in these environments:

+ [Using Azure Functions Core Tools (CLI)](#using-azure-functions-core-tools-cli)
+ [Using Visual Studio Code](#using-visual-studio-code)
+ The [Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli) version 2.4 or later.
+ The [Java Developer Kit](https://learn.microsoft.com/en-us/azure/developer/java/fundamentals/java-support-on-azure) 17, 21(Linux only). The JAVA_HOME environment variable must be set to the install location of the correct version of the JDK
+ [Apache Maven](https://maven.apache.org/), version 3.0 or above.

### Prerequisites

+ [Azure Functions Core Tools](https://learn.microsoft.com/azure/azure-functions/functions-run-local?tabs=v4%2Cmacos%2Ccsharp%2Cportal%2Cbash#install-the-azure-functions-core-tools)
+ Install Maven version 3.0 or above 
+ [Java Developer Kit](https://learn.microsoft.com/en-us/azure/developer/java/fundamentals/java-support-on-azure), version 8, 11, 17, 21(Linux only). The JAVA_HOME environment variable must be set to the install location of the correct version of the JDK

### Run on your local environment

1) Install the above mentioned pre-requisites in your local

```bash
git clone https://github.com/Azure-Samples/azure-functions-java-flex-consumption-azd.git
cd azure-functions-java-flex-consumption-azd/http
```

2) Add this local.settings.json file to this folder to simplify local development

```bash
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "FUNCTIONS_WORKER_RUNTIME": "java"
    }
}
```
3) Open a new terminal, remain in the path '/http' and do the following to do a clean build:

```bash
mvn clean package
```

4) Start the function app

```bash
mvn azure-functions:run
```

2) Test a Web hook or GET using the browser to open http://localhost:7071/api/http


## Source Code

The key code that makes tthese functions work is in `http/src/main/java/com/contoso/Function.java`.  The function is identified as an Azure Function by use of the `@FunctionName` and `@HttpTrigger` annotations from the `azure.functions.java.library.version` library in the POM. 

This code shows a HTTP GET triggered function.  

```java
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
```
This code shows a HTTP POST triggered function which expects a json object with `name` and `age` values in the request.

```java
/**
 * This function listens at endpoint "/api/httppost". Invoke it using "curl" command in bash:
 * curl -d "{\"name\": \"Awesome Developer\", \"age\": \"25\"}" -H "Content-Type: application/json" "http://localhost:7071/api/httppost"
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
    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(request.getBody().orElse("{}"));
        name = Optional.ofNullable(jsonNode.get("name")).map(JsonNode::asText).orElse(null);
        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Error: 'name' parameter is missing").build();
        }
    } catch (Exception e) {
        context.getLogger().severe("Error parsing request body: " + e.getMessage());
        return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("Error parsing request body").build();
    }

    return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
}
```

## Deploy to Azure

### Provision the Azure resources
The easiest way to provision this app is using the [Azure Dev CLI aka AZD](https://aka.ms/azd). If you open this repo in GitHub CodeSpaces the AZD tooling is already preinstalled.

To provision all resources:
You will be prompted for Azure subscription, and an Azure location.

```bash
cd /azure-functions-java-flex-consumption-azd

azd provision -e "<provide_environment_name>"
```
Make a note of AZURE_FUNCTION_NAME from .azure/<environment_name>/.env file

```bash
export AZURE_FUNCTION_NAME=<function_name_env_file>
```

### To Deploy the application:

```bash
cd http/target/azure-functions/contoso-functions
func azure functionapp publish $AZURE_FUNCTION_NAME
```
