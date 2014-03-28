/*
 * A QueryHek takes a path to input configuration file (including file name) and path to output folder
 * and generates a XML or JSON file by querying HEK API. A query is constructed from the given search conditions
 * in input configuration file
 * @author Karthik Ganesan Pillai
 * @version 02/08/2012
 * @Owner Data Mining Lab, Montana State University
 * 
 */


import java.io.*;
import org.json.*;
import java.net.*;

/**
 *
 * @author karthik
 */
public class QueryHek {
   //Result files are stored in this directory
    public String opDirectory;
    //Input to program, that is processed to construct query
    public String inputFileName;
    //Variable for holding file names 
    public String fileName;
    //Start and End time of search
    String startTime, endTime;
    String startHour, startMinute, startSecond, endHour, endMinute, endSecond;
    //Variables to store start date of search
    String startYear, startMonth, startDay;
    //Variables to store end date of search
    String endYear, endMonth, endDay;
    //Stores different event types of the search
    String[] eventModule;
    //Stores the coordinate system of the search
    String[] eventCoordSystem;
    //Variable to store filter condition, values and operators on these values
    String[] filterCondition;
    String[] filterValues;
    String[] filterOperators;
    //A variable to determine the number of filter conditions, helps to build the query
    int filterCount;
    //Variables for wrting to a file
    FileWriter fStream;
    BufferedWriter out;
    //Variable to set debug option
    int debug = 0;
    //Variables to set XML and JSON option for output format
    int xmlFlag = 0;
    int jsonFlag = 0;
    //Number of required parameter for input
    final static int REQUIRED_PARAMETERS = 6;
    //URL encoding scheme
    final static String ENCODING_SCHEME = "UTF-8";
    //Result limit of results for each page
    final static int RESULT_LIMIT = 200;
    //To kepp count of total events downloaded for the given search conditions
    int recordCount = 0;
    //Varibale to process incoming stream values, and append characters accordingly
    boolean firstDelimiter = false;
    //Variables to filter events. eventstarttime will be of events will be filtered based on these operators.
    String eventStartTimeBeginOperator = ">=";
    String eventStartTimeEndOperator = "<";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        QueryHek m = new QueryHek();

        if (args.length < 2) {
            System.out.println("Invalid number of argumens: Please enter output folder path, followed by input file path and XML or JSON option");
            return;
        } else if (args.length == 4) {
            m.opDirectory = args[0];
            m.inputFileName = args[1];
            if (args[2].contentEquals("XML")) {
                m.xmlFlag = 1;
            } else if (args[2].contentEquals("JSON")) {
                m.jsonFlag = 1;
            }
            m.debug = 1;

        } else if (args.length == 3) {
            m.opDirectory = args[0];
            m.inputFileName = args[1];
            if (args[2].contentEquals("XML")) {
                m.xmlFlag = 1;
            } else if (args[2].contentEquals("JSON")) {
                m.jsonFlag = 1;
            }
            m.debug = 0;
        } else {
            System.out.println("Invalid argumens: Please enter output folder path, followed by input file path and XML or JSON option");
            return;
        }


        m.readFileInfo(m.inputFileName);
        m.readInput(m.inputFileName);
        if (m.xmlFlag == 1) {
            m.processHek();
        } else if (m.jsonFlag == 1) {
            m.processJsonHek();
        }
        System.out.println("Total record count " + m.recordCount);
        System.out.println("Program completed");

    }

    /**
     *     A function to gather input file information; that is to find out the number of filter conditions in input file.
     */
    public void readFileInfo(String fn) {
        String strLine = "";
        //Total records in input file
        int recordCount = 0;
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fStream = new FileInputStream(fn);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String input = "";

            //Read File Line By Line
            while ((input = br.readLine()) != null) {
                //System.out.println(input);
                recordCount = recordCount + 1;
            }

            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

        //Calculate the number of filter conditions in input file and initialize corresponding variables
        filterCount = recordCount - REQUIRED_PARAMETERS;
        filterCondition = new String[filterCount];
        filterValues = new String[filterCount];
        filterOperators = new String[filterCount];

    }

    /**
     *     Process input file and initialize variables to construct query
     */
    public void readInput(String fn) {
        String strLine = "";
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new FileInputStream(fn);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String input = "";

            //Read File Line By Line
            while ((input = br.readLine()) != null) {
                //Construct a string with input, \n will be used as delimiter in later processing
                strLine = strLine + input + "\n";

            }

            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

        //Parse strLine, based on \n, and generate tokens
        String delims = "[\n]+";
        String tokens[] = strLine.split(delims);

        //filter condition count
        int fc = 0;
        //Parse tokens based on :, to initialize variables that are used to construct query
        delims = "[:]+";

        //Loop through the tokens, and extract information about "Parameter-Value" and "Parameter-Operator,Value"
        for (int i = 0; i < tokens.length; i++) {

            //Variable holding value of each token
            String t[] = tokens[i].split(delims);
            //Variable used as a delimiter
            String ds = "";

            //Extract year, month and day information from parameter "Start Date"
            if (t[0].contains("Start Date")) {
                ds = "[-]+";
                String[] ymd = t[1].split(ds);
                startYear = ymd[0];
                startMonth = ymd[1];
                startDay = ymd[2];
            } //Extract year, month and day information from parameter "End Date"
            else if (t[0].contains("End Date")) {
                ds = "[-]+";
                String[] ymd = t[1].split(ds);
                endYear = ymd[0];
                endMonth = ymd[1];
                endDay = ymd[2];
            } //Extract hour, minute and second information from parameter "Start Time"
            else if (t[0].contains("Start Time")) {
                startTime = t[1];
                ds = "[-]+";
                String[] hms = t[1].split(ds);
                startHour = hms[0];
                startMinute = hms[1];
                startSecond = hms[2];
            } //Extract hour, minute and second information from parameter "End Time"
            else if (t[0].contains("End Time")) {
                endTime = t[1];
                ds = "[-]+";
                String[] hms = t[1].split(ds);
                endHour = hms[0];
                endMinute = hms[1];
                endSecond = hms[2];
            } //Extract event from parameter "Event Type"
            else if (t[0].contains("Event Type")) {
                ds = "[,]+";
                eventModule = t[1].split(ds);

            } //Extract region, x1, x2, y1, and y2 values from parameter "Spatial Region"
            else if (t[0].contains("Spatial Region")) {
                ds = "[,]+";
                eventCoordSystem = t[1].split(ds);

            } //Extract filter values from "Parameter-Operator,Value" triplet and
            //encode value in URL format
            else {
                filterCondition[fc] = t[0].trim();
                ds = "[,]+";
                String[] opVal = t[1].split(ds);
                filterOperators[fc] = opVal[0].trim();
                try {
                    filterValues[fc] = URLEncoder.encode(opVal[1].trim(), ENCODING_SCHEME);
                } catch (UnsupportedEncodingException ex) {
                    System.err.println("Error: " + ex.getMessage());
                }
                fc = fc + 1;
            }


        }

    }

    /**
     * Construct query and query HEK using queryHEK, search results are aggregated for the entire time period
     * and generate a XML file
     */
    public void processHek() {
        try {
            //Variable to check for maximum results retrieved
            boolean maxResult = false;
            //Variable to open a file for the first time, when a event is found
            boolean firstTime = true;
            int pageCount = 1;
            //Temporary file for processing data
            File f;
            fileName = "Temp" + eventModule[0].trim() + "_event_startdate=" + startYear.trim() + "-" + startMonth.trim() + "-" + startDay.trim() + "T" + startTime.trim() + "event_enddate=" + endYear.trim() + "-" + endMonth.trim() + "-" + endDay.trim() + "T" + endTime.trim() + ".xml";
            f = new File(opDirectory + fileName);
            //Loop through search, as longs records are retrieved. Page count increases for each search.
            while (!maxResult) {

                //Construct query for this search from values extracted
                String query = "https://www.lmsal.com/hek/her?cosec=1&&cmd=search&type=column";

                if (eventModule != null) {
                    query = query + "&event_type=";
                    for (int i = 0; i < eventModule.length; i++) {
                        if (i != eventModule.length - 1) {
                            query = query + eventModule[i].trim() + ",";
                        } else {
                            query = query + eventModule[i].trim();
                        }
                    }
                }
                query = query + "&event_region=all";

                if (eventCoordSystem != null) {
                    query = query + "&event_coordsys=" + eventCoordSystem[0].trim() + "&x1=" + eventCoordSystem[1].trim() + "&x2=" + eventCoordSystem[2].trim() + "&y1=" + eventCoordSystem[3].trim() + "&y2=" + eventCoordSystem[4].trim();

                }

                query = query + "&result_limit="+RESULT_LIMIT+"&page=" + pageCount + "&event_starttime=" + startYear.trim() + "-" + startMonth.trim() + "-" + startDay.trim() + "T" + startHour.trim()+":"+ startMinute.trim() + ":"+ startSecond.trim() + "&event_endtime=" + endYear.trim() + "-" + endMonth.trim() + "-" + endDay.trim() + "T" + endHour.trim()+":"+ endMinute.trim() + ":"+ endSecond.trim();

                for (int i = 0; i < filterCount; i++) {
                    query = query + "&param" + i + "=" + filterCondition[i] + "&op" + i + "=" + filterOperators[i] + "&value" + i + "=" + filterValues[i];
                }

                query = query + "&param" + filterCount + "=" + "event_starttime" + "&op" + filterCount + "=" + eventStartTimeBeginOperator + "&value" + filterCount + "=" + startYear.trim() + "-" + startMonth.trim() + "-" + startDay.trim() + "T" + startHour.trim()+":"+ startMinute.trim() + ":"+ startSecond.trim();
                query = query + "&param" + filterCount + "=" + "event_starttime" + "&op" + (filterCount+1) + "=" + eventStartTimeEndOperator + "&value" + (filterCount+1) + "=" + endYear.trim() + "-" + endMonth.trim() + "-" + endDay.trim() + "T" + endHour.trim()+":"+ endMinute.trim() + ":"+ endSecond.trim();

                

                if (debug == 1) {
                    System.out.println("Query : " + query);
                }
                if (queryHekForRecords(query) == 0) {
                    maxResult = true;
                } else {
                    //Open a file for writing results
                    if (firstTime) {
                        fStream = new FileWriter(f);
                        out = new BufferedWriter(fStream);
                        firstTime = false;

                    }
                    //Retrieve events from HEK
                    queryHek(query);
                    pageCount = pageCount + 1;
                }

            }
            //If events are retreived close the file, that was opened, and process the file to generate a XML file
            if (!firstTime) {
                out.flush();
                out.close();

                String XmlFileName = eventModule[0].trim() + "_event_startdate=" + startYear.trim() + "-" + startMonth.trim() + "-" + startDay.trim() + "T" + startTime.trim() + "event_enddate=" + endYear.trim() + "-" + endMonth.trim() + "-" + endDay.trim() + "T" + endTime.trim() + ".xml";
                fStream = new FileWriter(new File(opDirectory + XmlFileName));
                out = new BufferedWriter(fStream);
                out.write("<html>\n");
                out.write("<body>\n");

                FileInputStream fStream1 = new FileInputStream(opDirectory + fileName);
                // Get the object of DataInputStream
                DataInputStream in = new DataInputStream(fStream1);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String input = "";

                //Read File Line By Line
                while ((input = br.readLine()) != null) {
                    //System.out.println(input);
                    out.write(input);
                }

                //Close the input stream
                in.close();

                //Delete the temporary file
                f.delete();


                out.write("</body>\n");
                out.write("</html>\n");

                out.flush();
                out.close();
            } else {
                System.out.println("No Records Found");
            }


        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

    }

    /**
     *      Check whether events for the search
     */
    int queryHekForRecords(String url) {

        int count = 0;

        try {
            URL hek = new URL(url);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    hek.openStream()));

            String inputLine;

            String c = "<result>";

            while ((inputLine = in.readLine()) != null) {

                if (inputLine.contains(c)) {
                    count = count + 1;
                }
            }
            in.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        if (debug == 1) {
            System.out.println("Record count " + count + "\n");

        }
        //Keep track of total records
        recordCount = recordCount + count;
        return count;

    }

    /**
     *     Process the given url and update the file
     */
    void queryHek(String url) {
        try {
            URL hek = new URL(url);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    hek.openStream()));

            String inputLine;


            while ((inputLine = in.readLine()) != null) {
                //Only write the actual events
                if (!inputLine.contains("html") && !inputLine.contains("body")) {
                    out.write(inputLine);
                }
            }

            in.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Construct query and query HEK using queryHEK, search results are aggregated for the entire time period
     * and generate a JSON file
     */
    public void processJsonHek() {
        try {
            
            //Variable to check for maximum results retrieved
            boolean maxResult = false;
            //Variable to open a file for the first time, when a event is found
            boolean firstTime = true;
            int pageCount = 1;
            //Temporary file for processing data
            File f;
            fileName = "Temp" + eventModule[0].trim() + "_event_startdate=" + startYear.trim() + "-" + startMonth.trim() + "-" + startDay.trim() + "T" + startTime.trim() + "event_enddate=" + endYear.trim() + "-" + endMonth.trim() + "-" + endDay.trim() + "T" + endTime.trim() + ".json";
            f = new File(opDirectory + fileName);

            //Loop through search, as longs records are retrieved. Page count increases for each search.
            while (!maxResult) {

                //Construct query for this search
                String query = "https://www.lmsal.com/hek/her?cosec=2&cmd=search&type=column";

                if (eventModule != null) {
                    query = query + "&event_type=";
                    for (int i = 0; i < eventModule.length; i++) {
                        if (i != eventModule.length - 1) {
                            query = query + eventModule[i].trim() + ",";
                        } else {
                            query = query + eventModule[i].trim();
                        }
                    }
                }
                query = query + "&event_region=all";
                if (eventCoordSystem != null) {
                    query = query + "&event_coordsys=" + eventCoordSystem[0].trim() + "&x1=" + eventCoordSystem[1].trim() + "&x2=" + eventCoordSystem[2].trim() + "&y1=" + eventCoordSystem[3].trim() + "&y2=" + eventCoordSystem[4].trim();

                }

                query = query + "&result_limit="+RESULT_LIMIT+"&page=" + pageCount + "&event_starttime=" + startYear.trim() + "-" + startMonth.trim() + "-" + startDay.trim() + "T" + startHour.trim()+":"+ startMinute.trim() + ":"+ startSecond.trim() + "&event_endtime=" + endYear.trim() + "-" + endMonth.trim() + "-" + endDay.trim() + "T" + endHour.trim()+":"+ endMinute.trim() + ":"+ endSecond.trim();
                

                for (int i = 0; i < filterCount; i++) {
                    query = query + "&param" + i + "=" + filterCondition[i] + "&op" + i + "=" + filterOperators[i] + "&value" + i + "=" + filterValues[i];
                }

                query = query + "&param" + filterCount + "=" + "event_starttime" + "&op" + filterCount + "=" + eventStartTimeBeginOperator + "&value" + filterCount + "=" + startYear.trim() + "-" + startMonth.trim() + "-" + startDay.trim() + "T" + startHour.trim()+":"+ startMinute.trim() + ":"+ startSecond.trim();
                query = query + "&param" + filterCount + "=" + "event_starttime" + "&op" + (filterCount+1) + "=" + eventStartTimeEndOperator + "&value" + (filterCount+1) + "=" + endYear.trim() + "-" + endMonth.trim() + "-" + endDay.trim() + "T" + endHour.trim()+":"+ endMinute.trim() + ":"+ endSecond.trim();


                if (debug == 1) {
                    System.out.println("Query : " + query);
                }
                if (queryHekForJsonRecords(query) == 0) {
                    maxResult = true;

                } else {
                    //Open a file for writing results
                    if (firstTime) {
                        fStream = new FileWriter(f);
                        out = new BufferedWriter(fStream);
                        firstTime = false;

                    }
                    //Retrieve events from HEK
                    queryHekForJason(query);
                    pageCount = pageCount + 1;

                }

            }
            //If events are retreived close the file, that was opened, and process the file to generate a XML file
            if (!firstTime) {
                out.flush();
                out.close();

                String fileName1 = eventModule[0].trim() + "_event_startdate=" + startYear.trim() + "-" + startMonth.trim() + "-" + startDay.trim() + "T" + startTime.trim() + "event_enddate=" + endYear.trim() + "-" + endMonth.trim() + "-" + endDay.trim() + "T" + endTime.trim() + ".json";
                fStream = new FileWriter(new File(opDirectory + fileName1));
                out = new BufferedWriter(fStream);
                out.write("{");
                out.write("\"result\":[");


                FileInputStream fStream1 = new FileInputStream(opDirectory + fileName);
                // Get the object of DataInputStream
                DataInputStream in = new DataInputStream(fStream1);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String input = "";

                //Read File Line By Line
                while ((input = br.readLine()) != null) {
                    //System.out.println(input);
                    out.write(input);
                }

                //Close the input stream
                in.close();

                //Delete temporary file
                f.delete();

                out.write("],");
                out.write("\"association\": [ ],");
                out.write("\"overmax\": false");
                out.write("}");

                out.flush();
                out.close();
            } else {
                System.out.println("No Records Found");
            }




        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

    }

    /**
     *      Check whether events for the search
     */
    int queryHekForJsonRecords(String url) {

        int count = 0;
        String result = "";
        JSONObject jobject = null;
        try {
            URL hek = new URL(url);


            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    hek.openStream()));

            StringBuilder sb = new StringBuilder();
            String inputLine;

            //Build a string from streamed data
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
            //Convert the built string for further processing
            result = sb.toString();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        //From generated string, process "results" and calculate number of events
        try {
            jobject = new JSONObject(result);
            JSONArray res = jobject.getJSONArray("result");
            count = res.length();
        } catch (JSONException e) {
            System.err.println("Error: " + e.getMessage());
        }

        if (debug == 1) {
            System.out.println("Record count " + count + "\n");
        }

        //Keep track of total records
        recordCount = recordCount + count;
        return count;

    }

    /**
     *     Process the given url and update the file
     */
    void queryHekForJason(String url) {

        String result = "";
        JSONObject jobject = null;

        try {
            URL hek = new URL(url);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    hek.openStream()));

            StringBuilder sb = new StringBuilder();

            String inputLine;

            //Build a string from streamed data
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine + "\n");
            }

            in.close();
            //Convert the built string for further processing
            result = sb.toString();

            //From generated string, process "results" and write to file
            try {
                jobject = new JSONObject(result);
                JSONArray res = jobject.getJSONArray("result");
                //Extract only the events, and discard others
                String s = res.toString().substring(1, res.toString().length() - 1);
                if (!firstDelimiter) {
                    out.write(s);
                    firstDelimiter = true;
                } else {
                    out.write("," + s);
                }

            } catch (JSONException e) {
                System.out.println("Error: " + e.toString());
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
