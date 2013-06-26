rest-doclet
===========

This java doclet allows for auto generation of REST documentation from Spring and JAX-RS annotated classes.

How it works
------------
There is no special configurations required in your code to allow this doclet to extract basic information about your REST endpoints if they use Spring or JAX-RS annotations.  Simply using annotations the doclet can extract basic information about how an endpoint should be called.  Additionally all documentation of the endpoints including their query parameters and path parameters are extracted directly from the javadoc comments in each class and method.

Using the following example Spring endpoint description the doclet will recognize one endpoint("/example/name") with a single path parameter("name") and a single query parameter("normalize").
```java
/**
 * Example Rest Definition
 */
@Controller
@RequestMapping("/example")
public class Example {

    /**
     * Retrieves information about a name.
     * @param name Name to be retrieved
     * @param normalize Will normalize the name
     */
    @RequestMapping(value = "/{name}", method = GET)
    @ResponseBody
    public String getName(@PathVariable String name, @RequestParam(required = false) boolean normalize) {
        return (normalize ? name.toLowerCase() : name);
    }
}
```

Additional Tags
---------------
There are some limitations to using simple annotations and javadocs.  The rest-doclet allows you to customize the behavior of the rest generation process via the use of special javadoc tags.  

 * @ignore - Can be used at the class or method level and will exclude that information from the documentation.
 * @contextPath [value] - Used in the class javadoc to define what the context path to prepend to the relative path of the endpoint.
 * @name [value] - Used in the class javadoc to allow for you to override the name of the grouping for the endpoints in a class.  This will default to the class name if not defined.
 * @pathVar [name] [description] - Used in the method javadocs to override the description of the path parameter with the given name.  This will default to the @param javadoc description for the variable representing that path parameter if not defined.
 * @queryParam [name] [description] - Used in the method javadocs to override the description of the query parameter with the given name.  This will default to the @param javadoc description for the variable representing that query parameter if not defined.

Using the following example shows how these can be used with the previous Spring endpoint example.
```java
/**
 * Example Rest Grouping
 * @name Examples
 * @contextPath /examples
 */
@Controller
@RequestMapping("/metrics")
public class Example {

    /**
     * Retrieves information about a name.
     * @param userId Name to be retrieved
     * @param normalize Will normalize the name
     * @pathVar name Name of the user to retrieve data for
     * @queryParam normalize If set to "true" this data will be normalized before being returned.
     */
    @RequestMapping(value = "/{name}", method = GET)
    @ResponseBody
    public String getName(@PathVariable("name") String userId, @RequestParam(required = false) boolean normalize) {
        return (normalize ? userId.toLowerCase() : userId);
    }
}
```

Command Line Options
--------------------
There is additionally a few command line options to set global options.
 * -t [title] - Allows the title to be specifice for the HTML page. Default is "REST Endpoint Descriptions"
 * -f [filename] - Allows the filename to be specified for the HTML page. Default is "./index.html"
 * -stylesheet - Allows for a different stylesheet to be attached to the HTML page.


Generating the documentation
----------------------------
1.  Maven
Configure the javadoc plugin to use a custom doclet.  The following shows how to set up a report set for rest documentation.
  ```xml
  <reporting>
      <plugins>
          <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-javadoc-plugin</artifactId>
              <reportSets>
                  <reportSet>
                      <id>restdoc</id>
                      <configuration>
                          <doctitle>REST ${project.version} Documentation</doctitle>
                          <windowtitle>REST ${project.version} Documentation</windowtitle>
                          <name>REST Endpoints</name>
                          <description>REST ${project.version} Documentation</description>
                          <doclet>restdoclet.RestDoclet</doclet>
                          <docletArtifact>
                              <groupId>rest-doclet</groupId>
                              <artifactId>rest-doclet</artifactId>
                              <version>0.1-SNAPSHOT</version>
                          </docletArtifact>
                          <useStandardDocletOptions>false</useStandardDocletOptions>
                          <destDir>restdoc</destDir>
                          <additionalparam>-t "REST ${project.version} Documentation"</additionalparam>
                      </configuration>
                      <reports>
                          <report>javadoc</report>
                      </reports>
                  </reportSet>
              </reportSets>
          </plugin>
      </plugins>
  </reporting>
  
  ```
2.  Using the javadoc command
  ```
  > javadoc -doclet restdoclet.RestDoclet –docletpath rest-doclet.jar -t "My Rest Endpoints" endpoint.package.name
  ```
  For a more complete example on using the javadoc command see [Using the javadoc command] (http://docs.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html#runningjavadoc)

