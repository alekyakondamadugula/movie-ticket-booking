# Movie Ticket Booking System

A college demonstration project made of three independent Spring Boot REST microservices.

## Microservices architecture

| Service | Port | API |
| --- | ---: | --- |
| user-service | 8081 | `/users`, `/users/login` |
| movie-service | 8082 | `/movies` |
| booking-service | 8083 | `/bookings` |

Each service uses its own in-memory H2 database. Run the services in separate terminals for local use.

## Technologies

Java 17, Spring Boot 3.2.5, Spring Web, Spring Data JPA, H2, Maven, Docker, Kubernetes, Jenkins, Karate.

## Folder structure

```text
user-service/       Spring Boot application, tests, Dockerfile
movie-service/      Spring Boot application, tests, Dockerfile
booking-service/    Spring Boot application, tests, Dockerfile
karate/             Separate Karate API test project
kubernetes/         Deployment and ClusterIP Service manifests
frontend/           Plain HTML, CSS, and JavaScript demo UI
Jenkinsfile          CI pipeline
```

## Frontend demo

Start all three services, then from the project root serve the static page with Python:

```sh
python -m http.server 5500 --directory frontend
```

Open `http://localhost:5500` in a browser. The UI loads movies, registers and signs in users, creates and cancels bookings, and looks up a booking by ID. Login checks credentials through `POST /users/login`; the user ID is kept in browser local storage. The controllers allow browser requests from this local demo origin. Movie cards show a fixed demo seat count; the existing services do not track seat inventory.

## Run services

From the project root, start each in a separate terminal:

```sh
mvn -f user-service/pom.xml spring-boot:run
mvn -f movie-service/pom.xml spring-boot:run
mvn -f booking-service/pom.xml spring-boot:run
```

The APIs are available at `http://localhost:8081/users`, `http://localhost:8082/movies`, and `http://localhost:8083/bookings`.

## Maven testing

Run the existing test suite from each service directory:

```sh
mvn -f user-service/pom.xml test
mvn -f movie-service/pom.xml test
mvn -f booking-service/pom.xml test
```

## Docker

With Docker installed, build the JARs and images from the project root:

```sh
mvn -f user-service/pom.xml package && docker build -t user-service:latest user-service
mvn -f movie-service/pom.xml package && docker build -t movie-service:latest movie-service
mvn -f booking-service/pom.xml package && docker build -t booking-service:latest booking-service
```

Run each image with its service port:

```sh
docker run --rm -p 8081:8081 user-service:latest
docker run --rm -p 8082:8082 movie-service:latest
docker run --rm -p 8083:8083 booking-service:latest
```

## Kubernetes

Build the three images first, then apply the manifests from the project root:

```sh
kubectl apply -f kubernetes/
kubectl get deployments,services,pods
```

To remove the demo resources:

```sh
kubectl delete -f kubernetes/
```

The manifests use `ClusterIP`; access a service locally with `kubectl port-forward service/user-service 8081:8081` (or the corresponding service and port).

## Jenkins pipeline

Configure a Jenkins Pipeline job to use this repository's `Jenkinsfile`. The agent needs Git, Maven, Java 17, and Docker. The pipeline checks out the repository, runs `mvn clean package` for all services (including tests), then builds all three images.

## Karate API tests

Start all three services, then run from the project root:

```sh
cd karate && mvn test
```

The tests call the existing list endpoints. Override the base URLs with Maven properties when needed, for example `mvn test -Duser.url=http://localhost:8081 -Dmovie.url=http://localhost:8082 -Dbooking.url=http://localhost:8083`.
