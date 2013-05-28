package restdoclet.collector;


import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import restdoclet.Configuration;
import restdoclet.model.ClassDescriptor;
import restdoclet.model.EndpointDescriptor;

import java.util.ArrayList;
import java.util.Collection;

import static restdoclet.util.CommonUtils.isEmpty;
import static restdoclet.util.TagUtils.CONTEXT_TAG;
import static restdoclet.util.TagUtils.NAME_TAG;
import static restdoclet.util.TagUtils.IGNORE_TAG;

public abstract class AbstractCollector implements Collector{

    protected abstract boolean shouldIgnoreClass(ClassDoc classDoc, Configuration config);
    protected abstract Collection<EndpointDescriptor> getEndpoints(String contextPath, ClassDoc classDoc, Configuration config);

    /**
     * Will generate aggregate all the rest endpoint class descriptors.
     * @param rootDoc
     * @return
     */
    @Override
    public Collection<ClassDescriptor> getDescriptors(RootDoc rootDoc, Configuration config) {
        Collection<ClassDescriptor> classDescriptors = new ArrayList<ClassDescriptor>();

        //Loop through all of the classes and if it contains endpoints then add it to the set of descriptors.
        for (ClassDoc classDoc : rootDoc.classes()) {
            ClassDescriptor descriptor = getClassDescriptor(classDoc, config);
            if (descriptor != null && !isEmpty(descriptor.getEndpoints()))
                classDescriptors.add(descriptor);
        }

        return classDescriptors;
    }

    /**
     * Will generate a single class descriptor and all the endpoints for that class.
     *
     * If any class contains the special javadoc tag {@link restdoclet.util.TagUtils.IGNORE_TAG} it will be excluded.
     * @param classDoc
     * @return
     */
    protected ClassDescriptor getClassDescriptor(ClassDoc classDoc, Configuration config) {

        //If the ignore tag is present or this type of class should be ignored then simply ignore this class
        if (shouldIgnoreClass(classDoc, config) || !isEmpty(classDoc.tags(IGNORE_TAG)))
            return null;

        Collection<EndpointDescriptor> endpoints = getEndpoints(getContextPath(classDoc), classDoc, config);

        //If there are no endpoints then no use in providing documentation.
        if (isEmpty(endpoints))
            return null;

        String name = getName(classDoc);
        String description = getDescription(classDoc);

        return new ClassDescriptor(
                (name == null ? "" : name),
                endpoints,
                (description == null ? "" : description)
        );
    }

    /**
     * Will get the initial context path to use for all rest endpoint.
     *
     * This looks for the value in a special javadoc tag {@link restdoclet.util.TagUtils.CONTEXT_TAG}
     *
     * @param classDoc
     * @return
     */
    protected String getContextPath(ClassDoc classDoc) {
        if(!isEmpty(classDoc.tags(CONTEXT_TAG))) {
            return classDoc.tags(CONTEXT_TAG)[0].text();
        }
        return "";
    }

    /**
     * Will get the display name for the class.
     *
     * This looks for the value in a special javadoc tag {@link restdoclet.util.TagUtils.NAME_TAG}
     *
     * @param classDoc
     * @return
     */
    protected String getName(ClassDoc classDoc) {
        Tag[] tags = classDoc.tags(NAME_TAG);
        String name;
        if (tags != null && tags.length > 0)
            return tags[0].text();
        else
            return classDoc.typeName();
    }

    /**
     * Will get the description for the class.
     * @param classDoc
     * @return
     */
    protected String getDescription(ClassDoc classDoc) {
        return classDoc.commentText();
    }
}
