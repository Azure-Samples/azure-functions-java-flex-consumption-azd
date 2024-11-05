<!--
---
description: This repository contains an Azure Functions HTTP trigger quickstart written in Java and deployed to Azure Functions Flex Consumption using the Azure Developer CLI (AZD). This sample uses managed identity and a virtual network to insure it's secure by default.
page_type: sample
products:
- azure-functions
- azure
- entra-id
urlFragment: starter-http-trigger-java
languages:
- java
- bicep
- azdeveloper
---
-->

# Azure Functions Java HTTP Trigger using AZD

This repository contains an Azure Functions HTTP trigger reference sample written in Java and deployed to Azure using Azure Developer CLI (`azd`). The sample uses managed identity and a virtual network to make sure deployment is secure by default. You can opt out of a VNet being used in the sample by setting SKIP_VNET to true in the parameters.

<!---[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://github.com/codespaces/new?hide_repo_select=true&ref=main&repo=575770869)-->

This source code supports the article [Quickstart: Create and deploy functions to Azure Functions using the Azure Developer CLI](https://learn.microsoft.com/azure/azure-functions/create-first-function-azure-developer-cli?pivots=programming-language-java).

## Prerequisites

+ [Java Developer Kit (JDK)](https://learn.microsoft.com/azure/developer/java/fundamentals/java-support-on-azure) version 17:
  + Other supported Java versions require updates to the pom.xml file.
  + The local `JAVA_HOME` environment variable must be set to the install location of the correct version of the JDK.
+ [Apache Maven](https://maven.apache.org/), version 3.0 or above.
+ [Azure Functions Core Tools](https://learn.microsoft.com/azure/azure-functions/functions-run-local?pivots=programming-language-java#install-the-azure-functions-core-tools)
+ [Azure Developer CLI (`azd`)](https://learn.microsoft.com/azure/developer/azure-developer-cli/install-azd)
+ To use Visual Studio Code to run and debug locally:
  + [Visual Studio Code](https://code.visualstudio.com/)
  + [Azure Functions extension](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azurefunctions)

## Initialize the local project

You can initialize a project from this `azd` template in one of these ways:

+ Use this `azd init` command from an empty local (root) folder:

    ```shell
    azd init --template azure-functions-java-flex-consumption-azd
    ```

    Supply an environment name, such as `flexquickstart` when prompted. In `azd`, the environment is used to maintain a unique deployment context for your app.

+ Clone the GitHub template repository locally using the `git clone` command:

    ```shell
    git clone https://github.com/Azure-Samples/azure-functions-java-flex-consumption-azd.git
    cd azure-functions-java-flex-consumption-azd
    ```

    You can also clone the repository from your own fork in GitHub.

## Prepare your local environment

Navigate to the `http` app folder and create a file in that folder named _local.settings.json_ that contains this JSON data:

```json
{
    "IsEncrypted": false,
    "Values": {
        "AzureWebJobsStorage": "UseDevelopmentStorage=true",
        "FUNCTIONS_WORKER_RUNTIME": "java"
    }
}
```

## Run your app from the terminal

1. From the `http` folder, run these commands to start the Functions host locally:

    ```bash
    mvn clean package
    mvn azure-functions:run
    ```

1. From your HTTP test tool in a new terminal (or from your browser), call the HTTP GET endpoint: <http://localhost:7071/api/httpget>

1. Test the HTTP POST trigger with a payload using your favorite secure HTTP test tool. This example runs in the `http` folder and uses the `curl` tool with payload data from the [`testdata.json`](./http/testdata.json) project file:

    ```shell
    curl -i http://localhost:7071/api/httppost -H "Content-Type: text/json" -d "@testdata.json"
    ```

1. When you're done, press Ctrl+C in the terminal window to stop the `func.exe` host process.

## Run your app using Visual Studio Code

1. From the root directory run the `code .` code command to open the project in Visual Studio Code.
1. Press **Run/Debug (F5)** to run in the debugger.
1. Send GET and POST requests to the `httpget` and `httppost` endpoints respectively using your HTTP test tool (or browser for `httpget`). If you have the [RestClient](https://marketplace.visualstudio.com/items?itemName=humao.rest-client) extension installed, you can execute requests directly from the [`test.http`](./http/test.http) project file.

## Source Code

The source code for the GET and POST functions is found in the [`Function.java`](./http/src/main/java/com/contoso/Function.java) file. The function is identified as an Azure Function by use of the `@FunctionName` and `@HttpTrigger` annotations from the `azure.functions.java.library.version` library in the POM.

This code defines an HTTP GET triggered function:  

```java
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

This code defines an HTTP POST triggered function, which expects a JSON payload with `name` and `age` values in the request.

```java
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
```

## Deploy to Azure

Run this command to provision the function app, with any required Azure resources, and deploy your code:

```shell
azd up
```

Alternatively, you can opt-out of a VNet being used in the sample. To do so, use `azd env` to configure `SKIP_VNET` to `true` before running `azd up`:

```bash
azd env set SKIP_VNET true
azd up
```

You're prompted to supply these required deployment parameters:

| Parameter | Description |
| ---- | ---- |
| _Environment name_ | An environment that's used to maintain a unique deployment context for your app. You won't be prompted if you created the local project using `azd init`.|
| _Azure subscription_ | Subscription in which your resources are created.|
| _Azure location_ | Azure region in which to create the resource group that contains the new Azure resources. Only regions that currently support the Flex Consumption plan are shown.|

After publish completes successfully, `azd` provides you with the URL endpoints of your new functions, but without the function key values required to access the endpoints. To learn how to obtain these same endpoints along with the required function keys, see [Invoke the function on Azure](https://learn.microsoft.com/azure/azure-functions/create-first-function-azure-developer-cli?pivots=programming-language-dotnet#invoke-the-function-on-azure) in the companion article [Quickstart: Create and deploy functions to Azure Functions using the Azure Developer CLI](https://learn.microsoft.com/azure/azure-functions/create-first-function-azure-developer-cli?pivots=programming-language-dotnet).

## Redeploy your code

You can run the `azd up` command as many times as you need to both provision your Azure resources and deploy code updates to your function app.

>[!NOTE]
>Deployed code files are always overwritten by the latest deployment package.

## Clean up resources

When you're done working with your function app and related resources, you can use this command to delete the function app and its related resources from Azure and avoid incurring any further costs:

```shell
azd down
```
