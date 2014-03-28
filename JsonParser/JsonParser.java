
    /**
     * A JSONParser takes a JSON formatted input file or  a directory that will be recursively searched for JSON formatted files or
     * a directory that will be searched for JSON formatted files and path to an output folder, where a tab-delimited file(s) for each JSON file will be saved.
     * @author Karthik Ganesan Pillai
     * @version 02/09/2012
     * @Owner Data Mining Lab, Montana State University
     */
    import java.io.BufferedReader;
    import java.io.BufferedWriter;
    import java.io.DataInputStream;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.FileNotFoundException;
    import java.io.FileWriter;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.util.ArrayList;
    import java.util.Iterator;
    import java.util.List;
    import org.json.*;

    public class JsonParser {

        //Formatted output records are stored in this variable
        List records;
        List badRecords;
        //Variable used to chop of the last tab character from string
        public char LF = '\t';
        //variable used to construct the header of tab separated file
        boolean firsTime = true;
        int count = 0;
        int recordCount = 0;
        //output directory of csv files
        String writeDir;
        String inDir;
        //to maintain current record of XML
        private String tempRec="";
        //to construct header for tab separated file
        private String tempFirstRecord="";
        private String tempHeaderRecord = "";
        private char recordSeparator = '\t';
        private int tempFirstRecordLength = 0;
        private int currentRecordLength = 0;
        private int recordsWithLessColumns = 0;
        //Recursive flag, to process directory
        private boolean recursive = false;

    /**
     * Constructor for JSONParser
     */
    public JsonParser() {
        records = new ArrayList();
        recordCount = 0;
        badRecords = null;
        badRecords = new ArrayList();
        tempFirstRecordLength = 0;
        currentRecordLength = 0;
        recordsWithLessColumns = 0;
    }

    /*
     * Initializes variables, before processing new file
     */

    public void initialize() {
         records = null;
         records = new ArrayList();
         recordCount = 0;
         badRecords = null;
         badRecords = new ArrayList();
         tempFirstRecordLength = 0;
         currentRecordLength = 0;
         recordsWithLessColumns = 0;
    }

    //A utility to chop of last tab character from string
    public String chop(String str) {
        if (str == null) {
            return null;
        }
        int strLen = str.length();
        if (strLen < 2) {
            return "";
        }
        int lastIdx = strLen - 1;
        String ret = str.substring(0, lastIdx);
        char last = str.charAt(lastIdx);
        if (last == LF) {

            return ret.substring(0, lastIdx);

        }
        return ret;
    }

        /**
         * Process the given current input directory for XML files
         * @param dir
         */
        public void runParser(String dir) {

            processDirectory(new File(dir));
        }

        /**
         * Write the parsed JSON in file
         * @param fn
         */
        public void writeToFile(String fn) {
            String nLine = "";

            try {
                // Create file
                FileWriter fStream = new FileWriter(fn);
                BufferedWriter out = new BufferedWriter(fStream);


                Iterator it = records.iterator();
                int count = 0;
                while (it.hasNext()) {
                    out.write(it.next().toString());
                    count = count + 1;

                }


                out.flush();
                //Close the output stream
                out.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

        }



        /**
         * Write the parsed JSON in file
         * @param fn
         */
        public void writeToFileBadRecords(String fn) {
            String nLine = "";

            try {
                // Create file
                FileWriter fStream = new FileWriter(fn);
                BufferedWriter out = new BufferedWriter(fStream);


                Iterator it = badRecords.iterator();
                int count = 0;
                while (it.hasNext()) {
                    out.write(it.next().toString());
                    count = count + 1;

                }


                out.flush();
                //Close the output stream
                out.close();
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

        }






        /**
         * Process directory for xml files and write the parsed values
         * It handles to search just the directory for XML files, or just given XML file, or search the directory recursively for
         * XML files
         * @param dir
         */

        public void processDirectory(File dir) {
            if (dir.isFile()) {
                try {
                    File f = dir.getAbsoluteFile();
                    String fName = f.getAbsolutePath();

                    if (fName.endsWith(".json")) {
                        System.out.println("Parsing file " + fName);
                        initialize();
                        parseDocument(fName);
                        String rfn = new String(f.getName());

                        rfn = rfn.replace("json", "txt");

                        System.out.println("Writing records to file "+rfn);
                        writeToFile(writeDir + rfn);
                        if(recordsWithLessColumns > 0)
                        {
                           System.out.println("Writing "+recordsWithLessColumns+" bad records to file : "+ "BadRecords_"+rfn);
                           writeToFileBadRecords(writeDir + "BadRecords_"+rfn);
                        }
                    }

                } catch (FileNotFoundException fe) {
                    System.err.println("Error: " + fe.getMessage());
                } catch (JSONException fe) {
                    System.err.println("Error: " + fe.getMessage());
                }
            }

            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    File f1 = new File(dir, children[i]);
                    if(f1.isDirectory() && recursive)
                        processDirectory(new File(dir, children[i]));
                    else if (f1.isFile())
                        processDirectory(new File(dir, children[i]));
                }
            }
        }

        /**
         * Process given JSON formatted file, and generate a tab delimited file
         * @param fn
         * @throws FileNotFoundException
         * @throws JSONException
         */
    private void parseDocument(String fn) throws FileNotFoundException, JSONException {

        FileInputStream fStream1 = null;
        String result="";
        JSONObject jobject = null;
        try {
            fStream1 = new FileInputStream(fn);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fStream1);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String inputLine;

            //Build a string from streamed data
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
            //Convert the built string for further processing
            result = sb.toString();
        } catch (IOException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
        finally {
            try {
                fStream1.close();
            } catch (IOException ex) {
                System.err.println("Error: " + ex.getMessage());
            }
        }

        //From generated string, process "results" and write to file
         try{
             //Create a JSON object from string generated
                jobject = new JSONObject(result);
                //Retrieve "result" parameter values from JSON object
                //to create an JSON array and create a string representation of it
                JSONArray res = jobject.getJSONArray("result");
                String s = res.toString();
                String s1 = s.substring(1, s.length()-1);
                //Generate header row of tab delimited file by processing each paramter-value pair
                //in JSON object
                JSONObject joRecord = res.getJSONObject(0);
                String [] jsonRecord = joRecord.getNames(joRecord);
                tempFirstRecord = "";
               
                for(int a=0; a < jsonRecord.length; a++)
                {
                    //Generate header row values for "refs" parameter values by tagging them with count (k)
                    if(jsonRecord[a].contentEquals("refs"))
                    {
                        JSONArray jarr = joRecord.getJSONArray(jsonRecord[a]);
                        for(int k=0; k< jarr.length(); k++)
                        {
                            JSONObject jo1 = jarr.getJSONObject(k);
                            String [] temparr2 = jo1.getNames(jo1);
                            for(int l=0; l< temparr2.length; l++)
                            {
                                tempFirstRecord = tempFirstRecord + (temparr2[l])+"_"+k+recordSeparator;

                            }
                        }
                    }
                    //Generate header row values for other parameters
                    else
                    {
                        tempFirstRecord = tempFirstRecord + jsonRecord[a] + recordSeparator;
                    }
                }
                tempFirstRecord = this.chop(tempFirstRecord) + "\n";
                records.add(tempFirstRecord);

                //Find the number of columns found from header record
                String tokens[] = tempFirstRecord.split(recordSeparator+"+");

                tempFirstRecordLength = tokens.length;
                
                //Loop through each event in "result" and generate records
                for(int i=0; i< res.length(); i++)
                {
                    tempHeaderRecord = "";
                    tempRec = "";
                    JSONObject jo = res.getJSONObject(i);
                    String [] tempRecord = jo.getNames(jo);

                    //Generate header rows for bad records

                    for(int a=0; a < tempRecord.length; a++)
                    {
                        //Generate header row values for "refs" parameter values by tagging them with count (k)
                        if(tempRecord[a].contentEquals("refs"))
                        {
                            JSONArray jarr = jo.getJSONArray(tempRecord[a]);
                            for(int k=0; k< jarr.length(); k++)
                            {
                                JSONObject jo1 = jarr.getJSONObject(k);
                                String [] temparr2 = jo1.getNames(jo1);
                                for(int l=0; l< temparr2.length; l++)
                                {
                                    tempHeaderRecord = tempHeaderRecord + (temparr2[l])+"_"+k+recordSeparator;

                                }
                            }
                        }
                        //Generate header row values for other parameters
                        else
                        {
                            tempHeaderRecord = tempHeaderRecord + tempRecord[a] + recordSeparator;
                        }
                    }
                    tempHeaderRecord = this.chop(tempHeaderRecord) + "\n";


                    //Generate non header rows

                    for(int j=0; j< tempRecord.length; j++)
                    {
                        //Generate records for all parameters except "refs"
                        if(!tempRecord[j].contentEquals("refs"))
                        {
                            tempRec = tempRec + jo.get(tempRecord[j])+recordSeparator;
                            currentRecordLength = currentRecordLength + 1;
                        }
                        //Generate records for parameter "refs"
                        if(tempRecord[j].contentEquals("refs"))
                        {
                             JSONArray jarr = jo.getJSONArray(tempRecord[j]);
                             for(int k=0; k< jarr.length(); k++)
                             {
                                 JSONObject jo1 = jarr.getJSONObject(k);
                                 String [] tempRecord2 = jo1.getNames(jo1);
                                 for(int l=0; l< tempRecord2.length; l++)
                                 {
                                        tempRec = tempRec + jo1.get(tempRecord2[l])+recordSeparator;
                                        currentRecordLength = currentRecordLength + 1;
                                 }
                             }
                            }
                    }
                    tempRec = this.chop(tempRec) + "\n";
                    

                    if(currentRecordLength != tempFirstRecordLength)
                    {
                        //System.out.println("Found rogue events" + tempFirstRecordLength + "  "+currentRecordLength);
                        recordsWithLessColumns = recordsWithLessColumns + 1;
                        badRecords.add(tempHeaderRecord);
                        badRecords.add(tempRec);
                    }
                    else
                    {
                        records.add(tempRec);

                    }
                    currentRecordLength = 0;
                }

                //System.out.println("Rogue records count : "+recordsWithLessColumns);
                


            }catch(JSONException e){
                System.err.println("Error: " + e.getMessage());
            }

    }

    /**
     * Takes an input directory and "R" for recursive processing of directory or an input directory or input XML filename (along with path),
     * and a path of output folder
     * @param args
     */
        public static void main(String[] args) {
            JsonParser spe = new JsonParser();
            
            if (args.length < 2) {
                System.out.println("Invalid number of arguments: Please pass input directory and followed by output directory path");
            }
            if (args.length == 2) {
                File f1, f2;
                f1 = new File(args[0]);
                f2 = new File(args[1]);
                if((f1.isDirectory() || f1.isFile()) && (f2.isDirectory()))
                {
                    spe.inDir = args[0];
                    spe.writeDir = args[1];
                    spe.runParser(spe.inDir);
                    f1.delete();
                    f2.delete();
                }
                else
                {
                    System.out.println("Invalid arguments; Please read README file for more information");
                }
            }
            if (args.length == 3) {
                File f1, f2;
                f1 = new File(args[0]);
                f2 = new File(args[2]);

                if((f1.isDirectory()) && (f2.isDirectory()) && args[1].contentEquals("R"))
                {
                    spe.inDir = args[0];
                    spe.writeDir = args[2];
                    spe.recursive = true;
                    spe.runParser(spe.inDir);
                    f1.delete();
                    f2.delete();
                }
                else
                {
                    System.out.println("Invalid arguments; Please read README file for more information");
                }
            }
        }

    }
