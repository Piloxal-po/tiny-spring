# Tiny Spring

[![](https://jitpack.io/v/Piloxal-po/tiny-spring.svg)](https://jitpack.io/#Piloxal-po/tiny-spring)

**Tiny Spring** is a lightweight and modular Web framework for Java 21, designed as an extension
of [Tiny Bean](https://github.com/Piloxal-po/tiny-bean). It provides an embedded Web server (Tomcat) and simplified REST
endpoint management, mimicking the ease of use of Spring Boot but with a minimal footprint.

## 🚀 Key Features

* **Embedded Server**: Integrates **Apache Tomcat** (via `tomcat-embed-core`) which starts automatically after the
  application context loads.
* **Automatic Discovery**: Automatically scans and loads classes annotated with `@Endpoint`.
* **JSON Support**: Automatic configuration of **Jackson** (`ObjectMapper`) for serialization/deserialization. If no
  `ObjectMapper` is defined by the user, Tiny Spring provides a default one.
* **Managed Lifecycle**: Uses Tiny Bean's `@AfterContextLoad` hooks to orchestrate server startup (Configuration ->
  Initialization -> Startup).
* **Error Handling**: Integrates error handling servlets (`ErrorServlet`).
* **Centralized Configuration**: Supports configuration via `SpringConfiguration`.

## 📦 Installation

### 1. Add the JitPack Repository

This project is distributed via [JitPack](https://jitpack.io). To use it, you must add the JitPack repository to your
build file.

For Maven, add this to your `pom.xml`:

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### 2. Add the Tiny-Spring Dependency

Add the following dependency to your `pom.xml` file:

```xml

<dependency>
    <groupId>com.github.Piloxal-po</groupId>
    <artifactId>tiny-spring</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🛠️ Usage

### 1. Entry Point

Since Tiny Spring relies on Tiny Bean, your application starts via `ApplicationRunner`:

```java
import com.github.oxal.runner.ApplicationRunner;

public class Main {
    public static void main(String[] args) {
        // Starts the context, scans packages, and launches Tomcat
        ApplicationRunner.run(Main.class, args);
    }
}
```

### 2. Creating an Endpoint and Handling Requests

Annotate your controller classes with `@Endpoint`. Use HTTP annotations (`@Get`, `@Post`, `@Put`, `@Delete`) to map your
methods.

You can retrieve request data via:

* `@PathParam`: For URL variables.
* `@QueryParam`: For query parameters (e.g., `?id=1`).
* `@RequestBody`: For the request body (automatically deserialized from JSON).

#### Full Example

```java
import com.github.oxal.spring.enumeration.Endpoint;
import com.github.oxal.spring.enumeration.operator.Get;
import com.github.oxal.spring.enumeration.operator.Post;
import com.github.oxal.spring.enumeration.operator.Put;
import com.github.oxal.spring.enumeration.operator.Delete;
import com.github.oxal.spring.enumeration.param.PathParam;
import com.github.oxal.spring.enumeration.param.QueryParam;
import com.github.oxal.spring.enumeration.param.RequestBody;

import java.util.List;

@Endpoint
public class UserController {

    // Retrieving a path parameter (Path Param)
    @Get("/users/{id}")
    public User getUser(@PathParam("id") String id) {
        return userService.findById(id);
    }

    // Retrieving a query parameter (Query Param)
    // Example: GET /users/search?name=Alice
    @Get("/users/search")
    public List<User> searchUsers(@QueryParam("name") String name) {
        return userService.findByName(name);
    }

    // Retrieving the request body (Request Body)
    @Post("/users")
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }

    @Put("/users/{id}")
    public User updateUser(@PathParam("id") String id, @RequestBody User user) {
        return userService.update(id, user);
    }

    @Delete("/users/{id}")
    public void deleteUser(@PathParam("id") String id) {
        userService.delete(id);
    }
}
```

### 3. JSON Configuration

Tiny Spring checks if a bean of type `ObjectMapper` exists. If not, it creates a default one. You can provide your own
simply by declaring a producer method in your Tiny Bean configuration.

## ⚙️ Internal Architecture

* **Provider**: `TinySpringProvider` ensures that the `com.github.oxal.spring` package is scanned by the framework.
* **LoadContext**: This is the core of the orchestration.
    * `TOMCAT_LOAD`: Initializes Tomcat and registers servlets.
    * `TOMCAT_RUN`: Starts the server and puts the thread in wait mode (`await`).

## 📋 Prerequisites

* Java 21
* Maven

## License

This project is under proprietary license (see LICENSE file).
