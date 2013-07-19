/*******************************************************************************
 * Copyright (c) 2013 Edward Wagner. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package restdoclet.example.jaxrs;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Example implementation of a REST endpoints using JAX-RS to highlight documentation features.
 *
 * @name JAX-RS REST documentation examples.
 * @contextPath /example
 */
@Path("/foo")
public class ExampleController {

    /**
     * This class provides 3 example endpoints descriptions with a mix of annotations and javadoc overloads.
     *
     */

    private static int count = 0;
    private static Map<String, String> userColors = new HashMap<String, String>();

    /**
     * Simply adds the value provided via the query parameter to a running total.
     *
     * @param value Value to be added to a running total.
     * @return The current total.
     */
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/add")
    public String postExample(@QueryParam("value") int value) {
        count += value;
        return Integer.toString(count);
    }

    /**
     * Retrieves the stored color for a particular user.
     *
     * @param userId user id of the user.
     * @param normalize Determines whether the result will be standardized
     * @pathVar name Name of the user to retrieve the color for
     * @queryParam normalize If set to "true" the name of the color will be normalized before being returned.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/user/{name}/color")
    public String getColor(@PathParam("name") String userId, @QueryParam("normalize") boolean normalize) {
        if (!userColors.containsKey(userId))
            return "";

        return (normalize ? userColors.get(userId).toLowerCase() : userColors.get(userId));
    }

    /**
     * Stores the color for a particular user
     *
     * @param userId User id of the user.
     * @param value The color value to store for the user
     * @pathVar name Name of the user to store the color for
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/user/{name}/color")
    public String setColor(@PathParam("name") String userId, String value) {
        userColors.put(userId, value);
        return getColor(userId, false);
    }

}
