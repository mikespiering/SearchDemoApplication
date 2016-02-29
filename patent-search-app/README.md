# Patent Search Sample Application
This sample application demonstrates the use of SpringBoot and Thymeleaf. These can be easily deployed in CloudFoundry and can demonstrate powerful features such as:
 
* Push Applications. 
* Scaling Application. 
* Service Binding Operations. 
* HA features. 

## Building the Application
Run the command `mvn package` 

## Demo Features
### Pushing Applications
Run the command `cf push`.  

:bulb: Notice that the `manifest.yml` file will be used for this. Furthermore, it will deploy the sample application with an "in-memory" database with "dummy" data.

### Scaling Applications
Run the command `cf scale -i 5 -m 1GB -f`. This will scale the app to 5 instances each with 1GB of memory. 

:bulb: It helps a lot when you are able to show "real-time" scaling operations using the "watch" command on a console. If you are doing this, I recommend setting `CF_COLORS=false`. 

### Bind Data Service
Run `cf bind-service <app-name> patent-data-service` followed by `cf restage`, thus showing usage of the new data set in the newly bound "service". For example, in PWS, you need to run `cf cs elephantsql turtle patent-data-service`. This binding process will not staged (active) until the `cf restage` command has been issued.

:warning: To test using real data, it is __strongly__ recommended to build the "micro-service" project in the parent project. In particular, the "datagov-crawler" as it will load valid data into this data service.

### DevOps Use Case
The DevOps demonstration will use the following components: 
 
1. CF Maven plugin to deploy to CloudFoundry. 
2. Jenkins server to build a pipeline. 
3. 2 different CloudFoundry instances:  
  * PWS for integration testing. 
  * vSphere (Fed2) CF environment as the production environment. 

#### Jenkins Server Setup
You can setup your own Jenkins server or use a pre-defined vagrant file available in the repository referenced in [1]. If you use this reference, then running `vagrant up` will get you a jenkins server running on `localhost:8082`. 

Configure Jenkins to use the following settings.xml (this contains the CF configuration you'll need): 
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          http://maven.apache.org/xsd/settings-1.0.0.xsd">
	<servers>
		<server>
			<id>fed2-cf-instance</id>
			<username>xxx</username>
			<password>xxx</password>
		</server>
		<server>
			<id>pws-cf-instance</id>
			<username>xxx</username>
			<password>xxx</password>
		</server>
	</servers>
</settings>
```
If using the `vagrant` image, you can place this file next to the `VagrantFile` and reference it in Jenkins from `/vagrant/settings.xml`. 

Configure maven and jdk in jenkins as well as the private git repo (this is optional is you clone the private repo and make it public). If using the private repo, you'll need to setup "deploy" keys. Google is your friend there. 

The 3 Jenkins jobs are: 

1. Build (mvn package). 
2. Deploy to PWS (mvn -P pws cf:push). 
3. Deploy to Fed2 (mvn -P fed2 cf:push). 

![Jenkins Jobs](images/jenkins-jobs.png?raw=true "Jenkins Job List")

Step 1 just runs local tests. Step 2 deploys to PWS using an "in-memory" database. Step 3 deploys to vSphere (Fed2) using a mySQL database (previously configured from the microservices demo).

![Jenkins Build Pipeline](images/build-pipeline-view.png?raw=true "Build Pipeline Sample")

Key takeaways:

* Multi-Cloud deployments with no code changes. 
* Integration testing happens using a "in-memory" database. 
* Dynamic binding of data services to a "production" database (participate in a micro-service architecture).

References:  

1. https://github.com/albertoaflores/jenkins-vagrant-server. 
2. https://github.com/cloudfoundry/cf-java-client/tree/master/cloudfoundry-maven-plugin. 

