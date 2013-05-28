package restdoclet;


import com.sun.javadoc.Doclet;
import com.sun.javadoc.RootDoc;
import restdoclet.collector.spring.SpringCollector;
import restdoclet.model.ClassDescriptor;
import restdoclet.writer.SimpleHtmlWriter;

import java.util.Collection;

public class RestDoclet extends Doclet {

    /**
     * Generate documentation here.
     * This method is required for all doclets.
     *
     * @return true on success.
     */
    public static boolean start(RootDoc root) {

        Configuration config = new Configuration(root.options());

        Collection<ClassDescriptor> classDescriptors =  new SpringCollector().getDescriptors(root, config);

        new SimpleHtmlWriter().write(classDescriptors, config);

        return true;
    }

    /**
     * Required to validate command line options.
     * @param option option name
     * @return option length
     */
    public static int optionLength(String option) {
        return Configuration.getOptionLength(option);
    }

}
