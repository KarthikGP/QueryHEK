/*
 * A ExtractAttributes takes a path to input file (including file name), path to input configuration file (including file name) and path to output folder
 * and generates a tab delimited file with columns specified in the configuration file.
 * @author Karthik Ganesan Pillai
 * @version 02/10/2012
 * @Owner Data Mining Lab, Montana State University
 *
 */




import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author karthik
 */
public class ExtractAttributes {

     //Variable used to chop of the last tab character from string
    String fieldSeparator = "\t";
    char LF = '\t';
    String strLine = "";
    //Variable to hold the column names extracted from config file
    String configTokens [];
    //Varriable to hold tokens found in input file
    String foundConfigTokens [];
    int tokensFoundCount = 0;
    //Variable to hold the column names extracted from input file
    String inputFileTokens[];
    //Variable to hold the index of column names extracted from config file and inputfile
    int indexOfTokens [];
    List records;
    String tempRecord = "";
    String inputFileName;
    String configFileName;
    String outputFolder;
    //Variable to check whether columns in config file are in input file to be parsed
    boolean columnFound = false;
    //Recursive flag, to process directory
    private boolean recursive = false;



    public static void main(String[] args) {
        ExtractAttributes ea = new ExtractAttributes();
        if (args.length < 3) {
                System.out.println("Invalid number of arguments: Please pass input directory and followed by output directory path");
            }
       if (args.length == 3) {
            File f1, f2, f3;
            f1 = new File(args[0]);
            f2 = new File(args[1]);
            f3 = new File(args[2]);
            if((f1.isDirectory() || f1.isFile()) && (f2.isFile()) && (f3.isDirectory()))
            {
            	ea.inputFileName = args[0];
                ea.configFileName = args[1];
                ea.outputFolder = args[2];
		ea.readConfigFile(ea.configFileName);
                ea.processDirectory(new File(ea.inputFileName));
            }
            else{
            	System.out.println("Invalid arguments; Please read README file for more information");
            }
        }
       
       if (args.length == 4) {
           File f1, f2, f3;
           f1 = new File(args[0]);
           f2 = new File(args[1]);
           f3 = new File(args[2]);
           if((f1.isDirectory() || f1.isFile()) && (f2.isFile()) && (f3.isDirectory()) && args[3].contentEquals("R"))
           {
		ea.inputFileName = args[0];
		ea.configFileName = args[1];
                ea.outputFolder = args[2];
                ea.recursive = true;
		ea.readConfigFile(ea.configFileName);
                ea.processDirectory(new File(ea.inputFileName));
           }
           else{
           	System.out.println(f2.isFile()+"Invalid arguments; Please read README file for more information");
           }
       }
        
        
    }
    
    /**
     * Process directory for files and write the parsed values
     * It handles to search just the directory for files, or just given input file, or search the directory recursively for
     * files
     * @param dir
     */

    public void processDirectory(File dir) {
        if (dir.isFile()) {
            try {
                File f = dir.getAbsoluteFile();
                String fName = f.getAbsolutePath();
		readInputFileInfo(fName);
                readInputFile(fName);
                if(columnFound)
                    writeToFile(outputFolder+dir.getName()+"-Extracted-Attributes.txt");
            } catch (Exception fe) {
                System.err.println("Error: " + fe.getMessage());
            }
        }

        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                File f1 = new File(dir, children[i]);
                if(f1.isDirectory() && recursive)
                    processDirectory(new File(dir, children[i]));
                else if (f1.isFile()){
                    processDirectory(new File(dir, children[i]));}
            }
        }
    }    



    /**
     *     A function to gather config file information; that is to find out columns in input file.
     */
    public void readConfigFile(String fn) {
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
                strLine = strLine + input +fieldSeparator;
                recordCount = recordCount + 1;
            }

            //Close the input stream
            in.close();
            configTokens = strLine.split(fieldSeparator);

            indexOfTokens = new int[configTokens.length];


        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }

    /**
     *     A function to gather input file information; that is to find out the indexes of columns in input file.
     */
    public void readInputFileInfo(String fn) {
        //Records in input file
        int recordCount = 0;
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fStream = new FileInputStream(fn);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String input = "";
            strLine = "";

            //Read File Line By Line
            while ((input = br.readLine()) != null) {
                //System.out.println(input);
                strLine = strLine + input + fieldSeparator;
                recordCount = recordCount + 1;
                //Once header information is read, come out of while loop
                if(recordCount > 1)
                {
                    break;
                }
            }

            //Close the input stream
            in.close();
            inputFileTokens = strLine.split(fieldSeparator);


            String foundTokens="";

            for(int i=0; i<  configTokens.length; i++)
            {
                columnFound = false;
                for(int j=0; j<inputFileTokens.length; j++)
                {
                    if(inputFileTokens[j].contentEquals(configTokens[i]))
                    {
                        indexOfTokens[i] = j;
                        foundTokens = foundTokens + configTokens[i] + fieldSeparator;
                        columnFound = true;
                       
                    }
                }
                if(!columnFound)
                {
                    System.out.println("Attribute "+configTokens[i]+ " not available in input file");
                    return;
                }
            }
            foundTokens = chop(foundTokens);
            foundConfigTokens = foundTokens.split(fieldSeparator);

        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

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
     *     A function to gather input file information; that is to find out the indexes of columns in input file.
     */
    public void readInputFile(String fn) {
        //Total records in input file
        int recordCount = 0;
        records = new ArrayList();
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fStream = new FileInputStream(fn);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String input = "";
            String splitInput [];

            //Read File Line By Line
            while ((input = br.readLine()) != null) {
                //System.out.println(input);
                splitInput = input.split(fieldSeparator);
                for(int i=0; i< indexOfTokens.length; i++)
                {
                    if(i!=indexOfTokens.length-1)
                        tempRecord = tempRecord + splitInput[indexOfTokens[i]] + fieldSeparator;
                    else
                        tempRecord = tempRecord + splitInput[indexOfTokens[i]] + "\n";

                }
                records.add(tempRecord);
                tempRecord = "";
                recordCount = recordCount + 1;
            }

            //Close the input stream
            in.close();

        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }

        /**
         * Write the parsed input file
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




}
