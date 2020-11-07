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


# To create a Docker image and test it locally
- to create the image: sudo mvn clean spring-boot:build-image
- to verify that the image has been built: sudo docker images -> akspoc, 0.0.1-SNAPSHOT
- to run the image: sudo docker run -e "SPRING_PROFILES_ACTIVE=local" -p 8000:8080 --detach --name akspoc -t akspoc:0.0.1-SNAPSHOT
    - we will reach the container on 8000 (see curl cmd below). The request will be forwarded to the app's port on 8080.
- to test the image: curl -k -v -H "Accept:application/hal+json" -H "Accept-Language:en-US" -H "Cache-Control:no-store" -X GET 'http://localhost:8000/greeting'
    - 200 {"msg":"Hello local","time":"2020-10-29T20:16:53.758937Z"}
    
    
# To deploy the Docker image to an Azure Container Registry (https://docs.microsoft.com/en-us/azure/aks/tutorial-kubernetes-prepare-acr)
- install the Azure CLI following https://docs.microsoft.com/en-us/cli/azure/install-azure-cli-apt:
    - sudo apt remove azure-cli -y && sudo apt autoremove -y
    - sudo apt-get update
    - sudo apt-get install ca-certificates curl apt-transport-https lsb-release gnupg
    - curl -sL https://packages.microsoft.com/keys/microsoft.asc |
          gpg --dearmor |
          sudo tee /etc/apt/trusted.gpg.d/microsoft.gpg > /dev/null
    - AZ_REPO=$(lsb_release -cs)
      echo "deb [arch=amd64] https://packages.microsoft.com/repos/azure-cli/ $AZ_REPO main" |
          sudo tee /etc/apt/sources.list.d/azure-cli.list
    - TODO - still to run when groovy appears at https://packages.microsoft.com/repos/azure-cli/dists/ (I raised https://github.com/Azure/azure-cli/issues/15828):
           - sudo apt-get update
           - sudo apt-get install azure-cli
           - az login
           - etc.
    

# TODO:
https://docs.microsoft.com/en-us/azure/aks/tutorial-kubernetes-prepare-app
