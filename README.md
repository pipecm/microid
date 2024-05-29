# MicroID
MicroID is a personal project intended for learning purposes. It was built with the following tech stack:
* Java SE 21
* Spring Boot 3.2.5
* Spring Data JPA
* Spring Security
* Gradle 8.7
* MySQL
* Docker
* Docker Compose
* Testcontainers (for integration tests)
* H2 (for integration tests)

## Local Deployment

### Prerequisites

* Java 21 or above
* Docker (with Docker Compose)
* Git
* Postman (or similar)

### Steps

1. Clone the following GitHub repository:
```
git clone https://github.com/pipecm/microid.git
```
2. Move to the project's directory:
```
cd microid
```
3. Deploy locally the project using Docker Compose:
```
sudo docker compose up --build
```
4. After finishing the deplyoment, run this cURL with Postman or other REST manager (No authentication required):
```
curl --location --request GET 'http://localhost:8080/actuator/health'
```
If the deployment is performed successfully, you should get a response similar to this:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 982818799616,
        "free": 871435386880,
        "threshold": 10485760,
        "path": "/home/myuser/Development/microid/.",
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```
## Database info
You can access to the database using a tool like DBeaver or similar.
```
Engine: MySQL
Host: localhost
Port: 3306
Database: microid
User: microidapp
Password: microid
```

## Authentication
After deployment, an initial user with administration privileges was created in the database:
```json
{
    "email": "admin@microid.com",
    "password" : "12345"
}
```
You can perform a login in the API by sending a request like the above one using the following cURL:
```
curl --location --request POST 'http://localhost:8080/login' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "{email}",
    "password" : "{password}"
}'
```
#### Response example
If login is successful, you should get a response like this:
```json
{
  "code": 200,
  "status": "OK",
  "message": "Login successful",
  "body": {
    "email": "admin@microid.com",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBtaWNyb2lkLmNvbSIsImVtYWlsIjoiYWRtaW5AbWljcm9pZC5jb20iLCJyb2xlcyI6WyJVU0VSIiwiQURNSU4iXSwiZXhwIjoxNzcxMDAwNDAyfQ.b-RUGCwl9s1gcfR-hDrFIYEhNTmMWS5y6UL2cbS5WkM",
    "expiration": "2026-02-13T13:33:22"
  }
}
```
For performing operations with authentication required, you have to use the returned token by sending it in the following way:
```
Authorization: Bearer {token}
```
## Users
### User creation
For creating users, you have to execute the following cURL:
```
curl --location --request POST 'http://localhost:8080/users' \
--header 'Content-Type: application/json' \
--data-raw '{
    "email": "admin@microid.com",
    "password": "simplyfelipe",
    "roles" : ["USER", "ADMIN"]
}'
```

| Parameter | Description          | Mandatory | Default value |
|-----------|----------------------|-----------|---------------|
| email     | Email of the user    | true      | N/A           |
| password  | Password of the user | true      | N/A           |
| roles     | List of roles        | false     | ["USER"]      |

If the execution returns a `201 Created` status, the user was created successfully. 

Also, you could verify the user creation in the database, running the following query:
```
SELECT * FROM mid_user WHERE email = '{email}'
```
### User search
For searching users, you have to execute the following cURL as authenticated user:
```
curl --location --request GET 'http://localhost:8080/users?email={email}&active={true|false}&role={role}' \
--header 'Authorization: Bearer {token}' \
--data ''
```
| Parameter | Description                                   | Mandatory |
|-----------|-----------------------------------------------|-----------|
| token     | Header for the bearer token                   | true      |
| email     | Find user with given email                    | false     |
| active    | Find users that are either active or inactive | false     |
| role      | Find users with the given role                | false     |

#### Response example
```json
{
    "code": 200,
    "status": "OK",
    "body": [
        {
            "id": "11ef1d59-0f52-da53-b2cf-0242ac120002",
            "email": "admin@microid.com",
            "active": true,
            "createdOn": "2024-05-29T01:16:21",
            "lastUpdatedOn": "2024-05-29T01:16:21",
            "roles": [
                "USER",
                "ADMIN"
            ],
            "lastLogin": "2024-05-29T12:33:22.837256"
        }
    ]
}
```
### User update
For updating users, you have to execute the following cURL as authenticated user:
```
curl --location --request PUT 'http://localhost:8080/users/{user_id}' \
--header 'Authorization: Bearer {token}' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id" : "{user_id}",
    "email": "{email}",
    "password" : "{password}",
    "roles" : ["{role_1}",..., "{role_n}"],
    "active" : {true|false}
}'
```
| Parameter | Description                      | Mandatory | Default value    |
|-----------|----------------------------------|-----------|------------------|
| token     | Header for the bearer token      | true      | N/A              |
| id        | Identification of the user       | true      | N/A              |
| email     | Email of the user                | false     | Current email    |
| password  | Password of the user             | false     | Current password |
| roles     | List of roles                    | false     | Current roles    |
| active    | Flag for enabling/disabling user | false     | true             |
Use the same SQL query described in the section "User creation" for retrieving the ID of the user that you want to update and for checking if the user was updated correctly.
### User deactivation
For deactivating users, you have to execute the following cURL as authenticated user:
```
curl --location --request DELETE 'http://localhost:8080/users/{user_id}' \
--header 'Authorization: Bearer {token}'
```
| Parameter | Description                      | Mandatory | Default value    |
|-----------|----------------------------------|-----------|------------------|
| token     | Header for the bearer token      | true      | N/A              |
| id        | Identification of the user       | true      | N/A              |

If the execution returns a `200 OK` status, the user was deactivated successfully.

Note: The user information is not physically deleted from the database.

## Swagger (Coming soon)
Upon enabling, you can find the Swagger documentation in the following location
```
http://localhost:8080/api/docs/swagger-ui/index.html
```