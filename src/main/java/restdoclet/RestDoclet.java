package restdoclet;


import com.sun.javadoc.Doclet;
import com.sun.javadoc.RootDoc;
import restdoclet.collector.Collector;
import restdoclet.collector.jaxrs.JaxRSCollector;
import restdoclet.collector.spring.SpringCollector;
import restdoclet.model.ClassDescriptor;
import restdoclet.writer.SimpleHtmlWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static restdoclet.Configuration.getOptionLength;

public class RestDoclet extends Doclet {

    private static final Collection<Collector> collectors = Arrays.<Collector>asList(
            new SpringCollector(),
            new JaxRSCollector()
    );

    /**
     * Generate documentation here.
     * This method is required for all doclets.
     *
     * @return true on success.
     */
    public static boolean start(RootDoc root) {

        Configuration config = new Configuration(root.options());

        Collection<ClassDescriptor> classDescriptors = new ArrayList<ClassDescriptor>();

        for (Collector collector : collectors)
            classDescriptors.addAll(collector.getDescriptors(root));

        new SimpleHtmlWriter().write(classDescriptors, config);

        return true;
    }

    /**
     * Required to validate command line options.
     * @param option option name
     * @return option length
     */
    public static int optionLength(String option) {
        return getOptionLength(option);
    }

}
