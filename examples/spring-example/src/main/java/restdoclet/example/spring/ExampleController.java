package restdoclet.example.spring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

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

    int count = 0;
    Map<String, String> userColors = new HashMap<String, String>();

    /**
     * Simply adds the value provided via the query parameter to a running total.
     *
     * @param value Value to be added to a running total.
     * @return The current total.
     */
    @RequestMapping(value = "/add", method = POST)
    @ResponseBody
    public int postExample(@RequestParam int value) {
        count += value;
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
    @RequestMapping("/user/{name}/color")
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
    @RequestMapping(value = "/user/{name}/color", method = {POST, PUT})
    @ResponseBody
    public String setColor(@PathVariable("name") String userId, @RequestParam(value = "color", required = true) String value) {
        userColors.put(userId, value);
        return getColor(userId, false);
    }

}
