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
package restdoclet.example.spring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Example implementation of a REST endpoints using spring to highlight documentation features.
 *
 * @name Spring REST documentation examples.
 * @contextPath /example
 */
@Controller
@RequestMapping(value = {"/foo", "/bar"})
public class ExampleController {

    /**
     * This class provides 3 example endpoints descriptions with a mix of annotations and javadoc overloads.
     *
     * The top level request mapping defines to path values that means that the amount of documented
     * endpoints will double.  For example the endpoint defined by the "/add" endpoint will actually be
     * listed as both "/foo/add" and "/bar/add".  The same is true for when there are multiple HTTP methods.
     * defined.
     *
     * With Spring if no method is defined, then the documentation will default to a GET method.
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
    @RequestMapping(value = "/add", method = POST, produces = "text/plain")
    @ResponseBody
    public int postExample(@RequestParam int[] value) {
        count += value[0];
        return count;
    }

    /**
     * Retrieves the stored color for a particular user.
     *
     * @param userId user id of the user.
     * @param normalize Determines whether the result will be standardized
     * @pathVar name Name of the user to retrieve the color for
     * @queryParam normalize If set to "true" the name of the color will be normalized before being returned.
     */
    @RequestMapping(value = "/user/{name}/color", produces = "text/plain")
    @ResponseBody
    public String getColor(@PathVariable("name") String userId, @RequestParam(required = false) boolean normalize) {
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
     * @queryParam color The color to give to the user
     */
    @RequestMapping(value = "/user/{name}/color", method = {POST, PUT}, consumes = "text/plain", produces = "text/plain")
    @ResponseBody
    public Set<String> setColor(@PathVariable("name") String userId, @RequestBody List<String> value) {
        userColors.put(userId, value.get(0));
        //return getColor(userId, false);
        return new LinkedHashSet<String>();
    }

}
