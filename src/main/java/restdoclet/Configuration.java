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
