READ ME
Program tested in java version "1.6.0_20"
To  compile: javac JsonParser.java
To run: java JsonParser infolder outfolder or java JsonParser infilepath outfolder or java JsonParser infolder R outfolder
where infolder is the folder where JSON files are available, infilepath is the complete path of JSON file (including file name), R is a flag to search recursively, and outfolder is the folder in which tab delimited .txt file(s) will be saved.


Examples to run program:
java JsonParser /home/karthik/NetBeansProjects/QueryHEK/Result/ /home/karthik/NetBeansProjects/QueryHEK/formattedop/

java JsonParser /home/karthik/NetBeansProjects/QueryHEK/Result/ R /home/karthik/NetBeansProjects/QueryHEK/formattedop/

java JsonParser /home/karthik/NetBeansProjects/QueryHEK/Result/Data.json /home/karthik/NetBeansProjects/QueryHEK/formattedop/




