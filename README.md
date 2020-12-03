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
- to run the image: sudo docker run -e "SPRING_PROFILES_ACTIVE=local" -p 8080:8080 --detach --name akspoc -t akspoc:0.0.1-SNAPSHOT
        - we will reach the container on 8080, first port after the -p (see curl cmd below).
        - The request will be forwarded to the app's port on 8080, second port after the :.
        - if the image is already running:
                - sudo docker stop akspoc
                - sudo docker rm akspoc
- to test the image: curl -k -v -H "Accept:application/hal+json" -H "Accept-Language:en-US" -H "Cache-Control:no-store" -X GET 'http://localhost:8080/greeting'
        - 200 {"msg":"Hello local","time":"2020-11-22T10:48:45.542553Z"}


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
                - Downloading client to "/usr/local/bin/kubectl" from "https://storage.googleapis.com/kubernetes-release/release/v1.19.4/bin/linux/amd64/kubectl"
                Please ensure that /usr/local/bin is in your search PATH, so the `kubectl` command can be found.
                Downloading client to "/tmp/tmpzyhc6lqs/kubelogin.zip" from "https://github.com/Azure/kubelogin/releases/download/v0.0.7/kubelogin.zip"
                Please ensure that /usr/local/bin is in your search PATH, so the `kubelogin` command can be found.
        - echo $PATH
                - confirms that /usr/local/bin is in my search PATH.
- connect to cluster using kubectl:
        - az aks get-credentials --resource-group myResourceGroup --name myAKSCluster
                - Merged "myAKSCluster" as current context in /home/philippe/.kube/config
        - To verify the connection to your cluster, run 'kubectl get nodes' to return a list of the cluster nodes:
                - NAME                                STATUS   ROLES   AGE   VERSION
                  aks-nodepool1-39371405-vmss000000   Ready    agent   20h   v1.18.10
                  aks-nodepool1-39371405-vmss000001   Ready    agent   20h   v1.18.10


# Deploy an application in my (AKS) cluster (https://docs.microsoft.com/en-us/azure/aks/tutorial-kubernetes-deploy-application)
- Clean up if you want to redeploy from scratch / limit Azure charges when not in use:
        - kubectl delete service akspoc
        - kubectl delete deployment akspoc
- Deploy the application:
        - kubectl create deployment akspoc --image=pbpoc.azurecr.io/akspoc:0.0.1-SNAPSHOT --dry-run=client -o=yaml > deploymentToK8s.yaml
        - manual edit to add some CPU requests and limits:
                - I used as an example: https://github.com/Azure-Samples/azure-voting-app-redis/blob/master/azure-vote-all-in-one-redis.yaml
                - replace L23 = resources: {} with the below:
                        resources:
                          requests:
                            cpu: 250m
                          limits:
                            cpu: 500m
        - kubectl apply -f deploymentToK8s.yaml
                - deployment.apps/akspoc created
        - kubectl expose deployment akspoc --type=LoadBalancer --port=8080 --dry-run=client -o=yaml > expose.yaml
        - kubectl apply -f expose.yaml
                - service/akspoc created
- Test the application:
        - To reveal the EXTERNAL-IP: kubectl get service akspoc --watch
                - NAME     TYPE           CLUSTER-IP    EXTERNAL-IP    PORT(S)          AGE
                  akspoc   LoadBalancer   XXX           YYY            8080:32517/TCP   41s
        - curl -k -v -H "Accept:application/hal+json" -H "Accept-Language:en-US" -H "Cache-Control:no-store" -X GET 'http://EXTERNAL-IP:8080/greeting'
                - 200 {"msg":"Hello prod","time":"2020-11-22T10:58:35.981061Z"}


# Scale applications in Azure Kubernetes Service (https://docs.microsoft.com/en-us/azure/aks/tutorial-kubernetes-scale)
- Manually scale pods:
        - To see the number and state of pods in your cluster: kubectl get pods
                NAME                      READY     STATUS    RESTARTS   AGE
                akspoc-...                 1/1     Running   0          37m
        - To increase the number of pods to 2 for akspoc: kubectl scale --replicas=2 deployment/akspoc
                - kubectl get pods -> now shows 2 pods.
        - To decrease the number of pods back down to 1 for akspoc: kubectl scale --replicas=1 deployment/akspoc
- Autoscale pods:
        - az aks show --resource-group myResourceGroup --name myAKSCluster --query kubernetesVersion --output table        
                - Result
                --------
                1.18.10 -> The Metrics Server is used to provide resource utilization to Kubernetes, and is automatically deployed in AKS clusters versions 1.10 and higher.
        - To use the autoscaler, all containers in your pods and your pods must have CPU requests and limits defined:
                - see the manual edit in 'Deploy the application' above.
        - To autoscale the number of pods. If average CPU utilization across all pods exceeds 50% of their requested usage, the autoscaler increases the pods up to a maximum of 3 instances. A minimum of 1 instances is defined for the deployment:
                - kubectl autoscale deployment akspoc --cpu-percent=50 --min=1 --max=3
                        - horizontalpodautoscaler.autoscaling/akspoc autoscaled
                - An alternative to the cmd above is to define a manifest file. See autoscalerBehaviour.yaml.
                        - kubectl apply -f autoscalerBehaviour.yaml
        - To see the status of the autoscaler: kubectl get hpa
                - NAME         REFERENCE           TARGETS   MINPODS   MAXPODS   REPLICAS   AGE
                akspoc       Deployment/akspoc   1%/50%    1         3         1          5m53s
                akspoc-hpa   Deployment/akspoc   1%/50%    1         3         1          47s
- Manually scale AKS nodes:
        - to increase from our initial 2 nodes to 3: az aks scale --resource-group myResourceGroup --name myAKSCluster --node-count 3


# Update an application in Azure Kubernetes Service (AKS) (https://docs.microsoft.com/en-us/azure/aks/tutorial-kubernetes-app-update)
- TODO Start here: make sure that 0.0.1-SNAPSHOT is running in AKS.
- Update the application code:
        - in HelloController, change from Hello %s to Hello And Bye %s
- Update the application version to 0.0.2-SNAPSHOT


# TODOs:
- Rather than deleting the AKS service & deployment, work out the cmd(s) to stop/start them.
- Test my Docker image fully with snyk (Transfer notes from file into here). Attempt to clear some vulnerabilities. 
- So far, we have deployed to K8S using port 8080 and without specifying a Spring profile:
        - try to apply the local profile
        - try to use a different port
- Add a DB (or another container) in the mix and use docker-compose.
- Is using an access key for the ACR the best way forward? -> see 'prerequisite' under 'log into the Azure Container Registry' in the notes above.
- try to create an AKS cluster with a managed identity -> see current error above.
