# POC using AKS


# To build:
mvn clean install


# To run:
- Run config in IntelliJ:
       - pointing to Application
       - VM Options = -Dspring.profiles.active=dev
- Or: mvn clean package spring-boot:run -DskipTests=true -Dspring-boot.run.profiles=dev


# To test:
- with curl:
        - functional endpoints:
                - curl -k -v -H "Content-Type: application/json" -d '{"email":"zz@email.com", "password":"pwd1", "role":"Developer"}' -X POST 'http://localhost:8080/customers'
                        - HTTP/1.1 201 Created
                        - Location: /customers/5
                - curl -k -v -H "Accept:application/hal+json" -H "Accept-Language:en-US" -H "Cache-Control:no-store" -X GET 'http://localhost:8080/customers' 
                        - 200 [{"id":1,"email":"A@email.com","password":"tester123","role":"Tester"},{"id":2,"email":"B@email.com","password":"tester123","role":"Tester"},{"id":3,"email":"C@email.com","password":"tester123","role":"Tester"},{"id":4,"email":"D@email.com","password":"tester123","role":"Tester"},{"id":5,"email":"zz@email.com","password":"pwd1","role":"Developer"}]
        - annotated endpoints:
                - curl -k -v -H "Content-Type: application/json" -d '{"email":"zz@email.com", "password":"pwd1", "role":"Developer"}' -X POST 'http://localhost:8080/annotatedCustomers'
                         - 201 {"id":5,"email":"zz@email.com","password":"pwd1","role":"Developer"}
                - curl -k -v -H "Accept:application/hal+json" -H "Accept-Language:en-US" -H "Cache-Control:no-store" -X GET 'http://localhost:8080/annotatedCustomers' 
                         - 200 [{"id":1,"email":"A@email.com","password":"tester123","role":"Tester"},{"id":2,"email":"B@email.com","password":"tester123","role":"Tester"},{"id":3,"email":"C@email.com","password":"tester123","role":"Tester"},{"id":4,"email":"D@email.com","password":"tester123","role":"Tester"},{"id":5,"email":"zz@email.com","password":"pwd1","role":"Developer"}]         
                - curl -k -v -H "Accept:application/hal+json" -H "Accept-Language:en-US" -H "Cache-Control:no-store" -X GET 'http://localhost:8080/annotatedCustomers/1'
                         - 200 {"id":1,"email":"A@email.com","password":"tester123","role":"Tester"}
- with project reactiveWebClient


# TODO:
See https://intellij-support.jetbrains.com/hc/en-us/articles/206544879-Selecting-the-JDK-version-the-IDE-will-run-under
    - choose runtime app
https://docs.microsoft.com/en-us/azure/aks/tutorial-kubernetes-prepare-app
