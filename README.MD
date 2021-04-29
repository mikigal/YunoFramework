# YunoFramework
Simple web framework with built-in HTTP server

## Features
  - Built-in NIO based HTTP server
  - Easy to use API
  - Routing
  - Middleware

## Example
```java
public class TestApplication {

    public static void main(String[] args) {
        Yuno yuno = Yuno.builder()
                .threads(4) // How many threads will use NIO server?
                .maxRequestSize(1024 * 1024 * 20) // Set maximum request size to 20MB
                .build();

        // Register middleware with priority 0.
        // Middlewares will be called from lowest to highest priority
        yuno.middleware(MyHandlers::middle, 0); 
        
        yuno.get("/", MyHandlers::root); // Register route with method GET at /
        yuno.listen(":8080"); // Let's start Yuno!
    }
}

public class MyHandlers {

    // Middleware
    public static void middle(Request request, Response response) throws Exception {
        System.out.println("Called middleware");
    }

    // Endpoint
    public static void root(Request request, Response response) throws Exception {
    	String name = request.param("name"); // Get value from "name" parameter (from URL)
        String accept = request.header("Accept"); // Get value from "Accept" header
    	
        if (name == null || !name.equals("mikigal")) {
            response.setStatus(HttpStatus.BAD_REQUEST); // Set response status to 400
            response.setHeader("Foo", "Bar"); // Set header "Foo" to "Bar"
            response.close(); // Let's tell client to close connection, instead of stay with keep-alive
            return;
        }
        
        response.html("<html><head></head><body><p>Hello!</p></body></html>"); // Send HTML code
        response.json(new MyObject("foo", "bar")); // Send serialized MyObjects as JSON
        response.file(new File("/Users/mikigal/Desktop/image.png")); // Send file
        response.binary(someBytesInArray, "application/octet-stream"); // Send byte array with selected Content-Type
        response.redirect("/example", HttpStatus.MOVED_PERMANENTLY); // Redirect to /example
    }
}
```