# USPTO Sample Application
This is a sample application using SpringBoot and Thymeleaf. The goal of this project is to show some of the features
of CloudFoundry :cloud: .

## MicroServices Demonstration
In order to demo the concept of "micro-services", this sample app contains two services:
* datagov-crawler
* patent-search-app

Each project needs to be built independently using the `mvn package` command. Both services will expect to interact with each other using a "data" service. This is expected to be called `patent-data-service`. 

The `manifest.xml` file contains further details such as:

* YML inheritance info. 
* Enforcement of a buildpack to use in CloudFoundry.
* Sample of how to deploy multiple projects in CloudFoundry.
* Usage of "no-route" applications.

To learn more about `manifest` files, I recommend using a generator tool such as the one available in the references below (see [1]).

### Build Instructions
In order to demo the micro-services demo, follow these steps:

1. Clone this repository. 
2. Create a data service called "patent-data-service" (SQL store). 
3. Build micro-services 
  1. Go to the `datagov-crawler` project by running `cd datagov-crawler`. 
  2. Build project by running `mvn clean package`. 
  3. Go to the `patent-search-app` by running `cd ../patent-search-app`. 
  4. Build project by running `mvn clean package`. 
  5. Go back to parent folder by running `cd ..`. 
3. Execute the `cf push` command.

:warning: In the event that a `host already assigned` error is detected, please update your manifest file (`manifest.yml`) with a different name.

### Micro-Services Summary
The `datagov-crawler` application polls the data.gov site for valid "patent" data. Once a file is detected, it will be processed in-memory and uploaded in the "patent-data-service" data store. Notice that this application has `no-route` set so that CloudFoundry can use it as a "process" application.

The `patent-search-app` application is a web based application that simply queries data in a relational store, ideally the same data store used by the `datagov-crawler`. By default, this application uses an "in-memory" database, but when bound to a data service, it will use that database for querying purposes.

See individual project's README.md file for further information on each project's usage.

References:

1. http://cfmanigen.mybluemix.net/

