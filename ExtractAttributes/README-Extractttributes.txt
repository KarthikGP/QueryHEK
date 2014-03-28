A ExtractAttributes takes a path to tab delimited input file (including file name) or a directory that will be recursively searched for files, path to input configuration file (including file name), path to output folder, and a optional flag R to search recursively
and generates a tab delimited file with columns specified in the configuration file. Config file should be a text file with column names to be extracted in each line. Output is 
a tab delimited file in the output folder ending with name(s) "Extracted-Attributes.txt".

Example of config file attributes.txt:
event_type
event_starttime
event_endtime
hpc_coord
hpc_bbox
boundbox_c1ll
boundbox_c2ll
boundbox_c1ur
boundbox_c2ur

Example to run the program:
java ExtractAttributes /home/usr/sg_event_startdate\=2011-10-11T00-00-00event_enddate\=2012-01-30T23-59-59.txt /home/usr/attributes.txt /home/usr/extractedop/


java ExtractAttributes /home/usr/inputdir /home/usr/attributes.txt /home/usr/extractedop/ R

