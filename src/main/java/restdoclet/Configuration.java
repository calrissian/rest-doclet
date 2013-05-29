/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package restdoclet;

public class Configuration {

    private enum ConfigOption {
        TITLE("t", "REST Endpoint Descriptions"),
        FILENAME("f", "./index.html"),
        STYLESHEET("stylesheet", "./stylesheet.css");

        private String option;
        private String defaultValue;

        private ConfigOption(String option, String defaultValue) {
            this.option = "-" + option;
            this.defaultValue = defaultValue;
        }

        public String getOption() {
            return option;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }

    private String[][] options;

    public Configuration(String[][] options) {
        this.options = options;
    }

    public String getDocumentTitle() {
        return getOption(ConfigOption.TITLE);
    }

    public String getOutputFileName() {
        return getOption(ConfigOption.FILENAME);
    }

    public String getStyleSheet() {
        return getOption(ConfigOption.STYLESHEET);
    }

    public boolean isdefaultStyleSheet() {
        return getOption(ConfigOption.STYLESHEET.getOption(), null) == null;
    }

    private String getOption(ConfigOption configOption) {
        return getOption(configOption.getOption(), configOption.getDefaultValue());
    }

    private String getOption(String name, String defaultValue) {
        for (String[] option : options) {
            if (option[0].equals(name)) {
                return option[1];
            }
        }
        return defaultValue;
    }

    public static int getOptionLength(String option) {

        for (ConfigOption configOption : ConfigOption.values())
            if (option.equals(configOption.getOption()))
                return 2;

        return 0;
    }
}
