# POC using AKS


# To build:
mvn clean install


# To run:
- Run config in IntelliJ:
    - pointing to AkspocApplication
    - VM Options = -Dspring.profiles.active=local
- Or: mvn clean package spring-boot:run -DskipTests=true -Dspring-boot.run.profiles=local


# To test:
- with curl:
    - curl -k -v -H "Accept:application/hal+json" -H "Accept-Language:en-US" -H "Cache-Control:no-store" -X GET 'http://localhost:8080/greeting' 
        - 200 {"msg":"Hello local","time":"2020-10-29T20:07:03.464160Z"}


# To create a Docker image and test locally
- to create the image: sudo mvn clean spring-boot:build-image
- to verify that the image has been built: sudo docker images -> akspoc, 0.0.1-SNAPSHOT
- to run the image: sudo docker run -e "SPRING_PROFILES_ACTIVE=local" -p 8000:8080 --detach --name akspoc -t akspoc:0.0.1-SNAPSHOT
    - we will reach the container on 8000 (see curl cmd below). The request will be forwarded to the app's port on 8080.
- to test the image: curl -k -v -H "Accept:application/hal+json" -H "Accept-Language:en-US" -H "Cache-Control:no-store" -X GET 'http://localhost:8000/greeting'
    - 200 {"msg":"Hello local","time":"2020-10-29T20:16:53.758937Z"}
    
    
# TODO:
https://docs.microsoft.com/en-us/azure/aks/tutorial-kubernetes-prepare-app
