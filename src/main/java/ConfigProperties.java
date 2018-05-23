import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;


public class ConfigProperties {
    private static ConfigProperties instance;
    private String runId;
    private String versionApp;
    private String iOSVersion;
    private String testRailURL;
    private String userName;
    private String password;
    private String reportPath;


    public static synchronized ConfigProperties getInstance() throws Exception {
        if (instance == null) {
            instance = new ConfigProperties();
        }
        return instance;
    }

    private ConfigProperties() throws Exception {
        init();
    }

    private void init() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
        Properties prop = new Properties();
        try {
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        runId = prop.getProperty("run_id");
        versionApp = prop.getProperty("version");
        iOSVersion = prop.getProperty("ios_version");
        testRailURL = prop.getProperty("testrail_url");
        userName = prop.getProperty("username");
        password = convertPassword(prop.getProperty("password"));
        reportPath = prop.getProperty("path_to_report");
    }

    private String convertPassword(String checksum) {
        byte[] bytes;
        try {
            bytes = Hex.decodeHex(checksum.toCharArray());
            return new String(bytes, "UTF-8");
        } catch (DecoderException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getRunId() {
        return runId;
    }

    public String getVersionApp() {
        return versionApp;
    }

    public String getiOSVersion() {
        return iOSVersion;
    }

    public String getTestRailURL() {
        return testRailURL;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getReportPath() {
        return reportPath;
    }



}
