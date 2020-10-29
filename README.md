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
        

# TODO:
https://docs.microsoft.com/en-us/azure/aks/tutorial-kubernetes-prepare-app
