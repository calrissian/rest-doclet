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
package restdoclet.writer.simple;

import restdoclet.Configuration;
import restdoclet.model.ClassDescriptor;
import restdoclet.model.Endpoint;
import restdoclet.model.PathVar;
import restdoclet.model.QueryParam;

import java.io.*;
import java.util.Collection;

import static restdoclet.util.CommonUtils.*;

public class SimpleHtmlWriter implements restdoclet.writer.Writer {
    public static final String OUTPUT_OPTION_NAME = "legacy";
    private static final String DEFAULT_STYLESHEET = "default-stylesheet.css";

    @Override
    public void write(Collection<ClassDescriptor> classDescriptors, Configuration config) throws IOException {

        if (config.isdefaultStyleSheet())
            generateStyleSheet(config);

        writeHtml(classDescriptors, config);
    }

    private static void generateStyleSheet(Configuration config) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {

            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_STYLESHEET);
            out = new FileOutputStream(new File(config.getStyleSheet()));

            copy(in, out);

        } finally {
            close(in, out);
        }
    }

    private static void writeHtml(Collection<ClassDescriptor> classDescriptors, Configuration config) throws IOException {

        PrintWriter out = null;

        try {
            out = new PrintWriter(new File(".", "index.html"));

            out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\" ?>");
            out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"");
            out.println("    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");

            out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");

            out.println("<head>");
            out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\" />");
            out.println("<title>"+ config.getDocumentTitle() +"</title>");
            out.println("<link rel='stylesheet' type='text/css' href=' " + config.getStyleSheet() + "'/>");
            out.println("</head>");

            out.println("<body>");

            out.println("<div id=\"wrapper\">");
            out.println("<div id=\"container\">");

            out.println("<h1>"+ config.getDocumentTitle() +"</h1>");
            out.println("<hr />");

            for (ClassDescriptor classDescriptor : classDescriptors) {
                out.println("<div id='" + classDescriptor.getName().replace(" ", "_") + "'>");
                out.println("<h3>" + classDescriptor.getName() + "</h3>" );
                out.print("<div class=\"bean_description\">" + classDescriptor.getDescription() + "</div>");

                for (Endpoint endpoint: classDescriptor.getEndpoints()) {
                    out.println("<table class=\"endpoint\">");
                    out.println("<colgroup>");
                    out.println("<col style=\"width: 10%;\" />");
                    out.println("<col style=\"width: 90%;\" />");
                    out.println("</colgroup>");
                    out.println("<tr>");
                    out.println("<th>Method</th>");
                    out.println("<th>Path</th>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("<td class=\"field_format\">" + endpoint.getHttpMethod() + "</td>");
                    out.println("<td class=\"field_format\">" + endpoint.getPath() + "</td>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("<th colspan=\"2\">REST Point Information</th>");
                    out.println("</tr>");
                    out.println("<tr>");
                    out.println("<td colspan=\"2\">");

                    if (!isEmpty(endpoint.getPathVars())) {

                        out.println("<div class=\"info_title\">Path Variables</div>");
                        out.println("<table width=\"100%\" class=\"list\">");
                        for (PathVar pathVar : endpoint.getPathVars()) {
                            out.println("<tr>");
                            out.println("<td class=\"code_format\">" + pathVar.getName() + "</td>");
                            out.println("<td class=\"descr_format\">" + pathVar.getDescription() + "</td>");
                            out.println("</tr>");
                        }
                        out.println("</table>");
                    }

                    if (!isEmpty(endpoint.getQueryParams())) {

                        out.println("<div class=\"info_title\">Query Parameters</div>");
                        out.println("<table width=\"100%\" class=\"list\">");
                        for (QueryParam queryParam : endpoint.getQueryParams()) {
                            out.println("<tr>");
                            out.println("<td class=\"code_format\">" + queryParam.getName() + (queryParam.isRequired() ? " (required)" : "") + "</td>");
                            out.println("<td class=\"descr_format\">" + queryParam.getDescription() + "</td>");
                            out.println("</tr>");
                        }
                        out.println("</table>");
                    }

                    if (endpoint.getRequestBody() != null &&
                            !isEmpty(endpoint.getRequestBody().getDescription())) {
                        out.println("<div class=\"info_title\">Request Body</div>");
                        out.println("<table width=\"100%\" class=\"list\">");
                        out.println("<tr>");
                        out.println("<td class=\"code_format\">" + endpoint.getRequestBody().getName() + "</td>");
                        out.println("<td class=\"descr_format\">" + endpoint.getRequestBody().getDescription() + "</td>");
                        out.println("</tr>");
                        out.println("</table>");
                    }

                    if (!isEmpty(endpoint.getConsumes())) {
                        out.println("<div class=\"info_title\">Consumes</div>");
                        out.println("<table width=\"100%\" class=\"list\">");
                        for (String acceptType : endpoint.getConsumes()) {
                            out.println("<tr>");
                            out.println("<td class=\"code_format\">" + acceptType + "</td>");
                            out.println("</tr>");
                        }
                        out.println("</table>");
                    }

                    if (!isEmpty(endpoint.getProduces())) {
                        out.println("<div class=\"info_title\">Produces</div>");
                        out.println("<table width=\"100%\" class=\"list\">");
                        for (String outputType : endpoint.getProduces()) {
                            out.println("<tr>");
                            out.println("<td class=\"code_format\">" + outputType + "</td>");
                            out.println("</tr>");
                        }
                        out.println("</table>");
                    }

                    out.println("<div class=\"info_title\">Description</div>");
                    out.println("<div class=\"info_text\">" + endpoint.getDescription() + "</div>");
                    out.println("</td>");
                    out.println("</tr>");
                    out.println("</table>");

                }

                out.println("</div>");
                out.println("<hr />");
            }

            out.println("</div>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");

        } finally {
            close(out);
        }
    }



}
