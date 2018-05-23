import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import org.apache.log4j.BasicConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ResultParser {

    private static final Logger LOG = LoggerFactory.getLogger(ResultParser.class);
    private static ConfigProperties properties;
    private static APIClient client;

    //Different error messages
    private static String message400 ="TestRail API returned HTTP 400";
    private static int count = 0;

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        properties = ConfigProperties.getInstance();
        for (String testCaseId: getPassedCases())  {
            LOG.info("C" + testCaseId);

            client = new APIClient(properties.getTestRailURL());// connect to testrail
            client.setUser(properties.getUserName());
            client.setPassword(properties.getPassword());

            Map data = new HashMap();
            data.put("status_id", new Integer(1)); //add PASSED result
            data.put("comment", "Run by automation on Email+ build "+properties.getVersionApp()); //add comment
            try {
                client.sendPost("add_result_for_case/"+properties.getRunId()+"/"+testCaseId+"", data);
                LOG.info("Marking test case "+testCaseId+" as PASSED");
                count++;
            }
            catch (APIException e) {
                if (e.getMessage().contains(message400)) {
                    LOG.warn("Test case with ID "+testCaseId+ " is not found in current test suite. Skipping this case.");
                    continue;
                }
                else {
                    LOG.warn("Timeout reached on test case: " + testCaseId + " Reconnecting after 20 seconds");
                    Thread.sleep(5000);
                    client.sendPost("add_result_for_case/" + properties.getRunId() + "/" + testCaseId + "", data);
                    LOG.info("Marking test case " + testCaseId + " as PASSED");
                    count++;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        LOG.info("Total cases marked as PASSED: "+count);
    }

    public static Set<String> getPassedCases() throws Exception {
        properties = ConfigProperties.getInstance();
        File input = new File(properties.getReportPath());
        Set<String> cases = new HashSet<String>();
        try {
            Document doc = Jsoup.parse(input, "UTF-8");
            for (int i=0; i<doc.select("table").size(); i++){
                Element table = doc.select("table").get(i);
                Elements rows = table.select("tr");
                Element row = rows.get(0);
                if (row.text().equals("PASSED TESTS")) {
                    Elements passedRows = table.select("tr");
                    for (int j = 2; j < passedRows.size(); j++) { //first 2 rows are the col names so skip it.
                        Element passedRow = passedRows.get(j);
                        Element col = passedRow.select("td").get(0);
                        Element id = col.select("b").get(0);
                        String test_id = id.text();
                        Matcher matcher = Pattern.compile("C(\\d+)").matcher(test_id);
                        while (matcher.find()) {
                            LOG.info("Case id: " + matcher.group(1));
                            cases.add(matcher.group(1).replace("C",""));

                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.info("Passed tests are parsed!");
        return cases;
    }
}