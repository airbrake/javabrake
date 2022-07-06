# Spring Boot Sample Application for JavaBrake

## About the application

The example application provides three GET endpoints:

1. `/date` - returns server date and time.
2. `/locations` - returns list of available locations.
3. `/weather/{locationName}` - returns the weather details for the locations.

## Steps to run the API

1. Install the dependencies for the application

    ```
    mvn install
    ```

2. You must get both `project_id` & `project_key`.

    Find your `project_id` and `project_key` in your Airbrake account and replace them in `application.properties` file.

    ```java
    airbrake.project.id=project_id
    airbrake.project.key=project_key
    ```

3. Run the application

4. To retrieve the responses, append the endpoints to the localhost URL.

    Use the below curl commands to interact with the endpoints. 

    ```bash
    curl "http://localhost:8080/date" 
    curl "http://localhost:8080/locations"
    curl "http://localhost:8080/weather/<austin/pune/santabarbara>"
    ```

    The below curl command will raise `404 Not Found` error.

    ```bash
    curl -I "http://localhost:8080/weather"
    ```

    The below curl command will raise `500 Internal server error` error.

    ```bash
    # Should produce an intentional HTTP 500 error and report the error to Airbrake (since `washington` is in the supported cities list but there is no data for `washington`, an `if` condition is bypassed and the `data` variable is used but not initialized)
    curl -I "http://localhost:8080/weather/washington"
    ```

    Or you can use Postman application.