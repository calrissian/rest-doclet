/*******************************************************************************
 * Copyright (C) 2014 The Calrissian Authors
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
package org.calrissian.restdoclet;


import com.sun.javadoc.Doclet;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.RootDoc;
import org.calrissian.restdoclet.collector.Collector;
import org.calrissian.restdoclet.collector.jaxrs.JaxRSCollector;
import org.calrissian.restdoclet.collector.spring.SpringCollector;
import org.calrissian.restdoclet.model.ClassDescriptor;
import org.calrissian.restdoclet.writer.Writer;
import org.calrissian.restdoclet.writer.simple.SimpleHtmlWriter;
import org.calrissian.restdoclet.writer.swagger.SwaggerWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.calrissian.restdoclet.Configuration.getOptionLength;

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

        Writer writer;
        if (config.getOutputFormat().equals(SwaggerWriter.OUTPUT_OPTION_NAME))
            writer = new SwaggerWriter();
        else
            writer = new SimpleHtmlWriter();


        try {
            writer.write(classDescriptors, config);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Required to validate command line options.
     * @param option option name
     * @return option length
     */
    public static int optionLength(String option) {
        return getOptionLength(option);
    }

    /**
     * NOTE: Without this method present and returning LanguageVersion.JAVA_1_5,
     *       Javadoc will not process generics because it assumes LanguageVersion.JAVA_1_1
     * @return language version (hard coded to LanguageVersion.JAVA_1_5)
     */
    public static LanguageVersion languageVersion() {
        return LanguageVersion.JAVA_1_5;
    }
}
