package restdoclet.writer;


import restdoclet.Configuration;
import restdoclet.model.ClassDescriptor;

import java.util.Collection;

public interface Writer {

    public void write(Collection<ClassDescriptor> classDescriptors, Configuration config);

}
