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
        - workaround as, at the time, there was no package for Ubuntu Groovy Gorilla (see https://github.com/Azure/azure-cli/issues/15828):
                - AZ_REPO=focal
                - echo "deb [arch=amd64] https://packages.microsoft.com/repos/azure-cli/ $AZ_REPO main" | sudo tee /etc/apt/sources.list.d/azure-cli.list
                - sudo apt-get update
                - sudo apt-get install azure-cli
                - az login
                    - it opens a browser and loads an Azure sign-in page.
        - az --version
                - azure-cli 2.14.2 so all good as the tutorial says 2.0.53 or later.
- create an Azure Container Registry:
        - prerequisite: I had to create an Azure account.
        - in a terminal window:
                - az login
                - az group create --name myResourceGroup --location uksouth
                - az acr create --resource-group myResourceGroup --name myACR --sku Basic
- log into the Azure Container Registry:
        - prerequisite:
            - in the Portal UI, select myResourceGroup and then myACR
            - Settings -> Access keys & Activate
        - sudo az acr login --name myACR
            - username & pwd = the ones obtained in the prerequisite
- tag a container image:
        - sudo docker images -> list of your current local images.
        - az acr list --resource-group myResourceGroup --query "[].{acrLoginServer:loginServer}" --output table
                AcrLoginServer
                ----------------
                myACR.azurecr.io
        - sudo docker tag akspoc:0.0.1-SNAPSHOT myACR.azurecr.io/akspoc:0.0.1-SNAPSHOT
        - sudo docker images -> to verify the tag has been applied
- push images to registry:
        - sudo docker push myACR.azurecr.io/akspoc:0.0.1-SNAPSHOT
- list images in registry:
        - az acr repository list --name myACR --output table
                Result
                --------
                akspoc
        - az acr repository show-tags --name myACR --repository akspoc --output table
                Result
                --------------
                0.0.1-SNAPSHOT


# Deploy an Azure Kubernetes Service (AKS) cluster (https://docs.microsoft.com/en-us/azure/aks/tutorial-kubernetes-deploy-cluster)
- create a Kubernetes cluster:
        - if you choose to use a service principal (NOT recommended but followed for now as the recommended version gives me an error):
                - as we do not specify a service principal in the below cmd, one is automatically created. It is granted the right to pull images from the Azure Container Registry (ACR) created in the previous step.
                - az aks create --resource-group myResourceGroup --name myAKSCluster --node-count 2 --generate-ssh-keys --attach-acr myACR
                        - after a while, we get a JSON response.
        - if you choose to use a managed identity (recommended option for easier management):
                - az aks create -g myResourceGroup -n myManagedCluster --enable-managed-identity
                - TODO Currently failing with:
                BadRequestError: Operation failed with status: 'Bad Request'. Details: Provisioning of resource(s) for container service myManagedCluster in resource group myRG failed. 
                Message: Operation could not be completed as it results in exceeding approved Total Regional Cores quota. Additional details - Deployment Model: Resource Manager, 
                Location: uksouth, Current Limit: 4, Current Usage: 0, Additional Required: 6, (Minimum) New Limit Required: 6. Submit a request for Quota increase at 
                https://aka.ms/ProdportalCRP/?#create/Microsoft.Support/Parameters/%7B%22subId%22:%2211913e2a-0c78-4eed-a6b8-98065785110f%22,%22pesId%22:%2206bfd9d3-516b-d5c6-5802-169c800dec89%22,%22supportTopicId%22:%22e12e3d1d-7fa0-af33-c6d0-3c50df9658a3%22%7D 
                by specifying parameters listed in the ‘Details’ section for deployment to succeed. 
                Please read more about quota limits at https://docs.microsoft.com/en-us/azure/azure-supportability/regional-quota-requests.. Details: 
- install the Kubernetes CLI:
        - sudo az aks install-cli
                Downloading client to "/usr/local/bin/kubectl" from "https://storage.googleapis.com/kubernetes-release/release/v1.19.4/bin/linux/amd64/kubectl"
                Please ensure that /usr/local/bin is in your search PATH, so the `kubectl` command can be found.
                Downloading client to "/tmp/tmpzyhc6lqs/kubelogin.zip" from "https://github.com/Azure/kubelogin/releases/download/v0.0.7/kubelogin.zip"
                Please ensure that /usr/local/bin is in your search PATH, so the `kubelogin` command can be found.
        - TODO: start here -> /usr/local/bin is in your search PATH?
- connect to cluster using kubectl:
        - az aks get-credentials --resource-group myResourceGroup --name myAKSCluster
        
        
# TODO:
- Is using an access key for the ACR the best way forward? -> see 'prerequisite' under 'log into the Azure Container Registry' in the notes above.
- try to create an AKS cluster with a managed identity -> see current error above.
- https://docs.microsoft.com/en-us/azure/aks/tutorial-kubernetes-prepare-app
