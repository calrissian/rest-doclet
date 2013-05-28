package restdoclet.collector;

import com.sun.javadoc.RootDoc;
import restdoclet.Configuration;
import restdoclet.model.ClassDescriptor;

import java.util.Collection;

public interface Collector {

    Collection<ClassDescriptor> getDescriptors(RootDoc rootDoc, Configuration config);


}
