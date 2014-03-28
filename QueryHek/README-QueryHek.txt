READ ME
Program tested in java version "1.6.0_20"
To  compile: javac QueryHEK.java
To run: java QueryHEK opfolder inputfilename XML or java QueryHEK opfolder inputfilename JSON
where opfolder is the folder where results will be stored, inputfilename has query search information and XML or JSON are options for download formats.

Format of input file.
1.It has six mandatory “Parameter-Value” pairs followed by optional “Parameter- Operator, Value” triplet.

2.The first six parameters are Start Date, Start Time, End Date, End Time, Event Type, Spatial Region. Format for each is “Parameter-Value” and a newline at the end. Currently, we have the following Event Types, available from HEK, Sigmoid (sg), Filaments (fi), Flares (fl), ActiveRegion (ar), CoronalHole (ch), EmergingFlux (ef). For Spatial Region parameter currently helioprojective (from Earth's perspective, in arcseconds from disk center), Stonyhurst (longitude measured from central meridian and latitude in degrees) and Carrington (longitude and latitude in degrees) are supported. Let (x1,y1) and (x2,y2) be the lower-left and upper-right coordinates of a bounding box respectively. Format of Spatial Region value is “region, x1, x2, y1, y2” (note: Here region should be helioprojective or stonyhurst or carrington). An example of input (file) is given below.
Start Date : 2011-01-01
Start Time : 00-00-00
End Date : 2011-01-30
End Time : 23-59-59
Event Type : sg
Spatial Region :  helioprojective, -5000,5000,-5000,5000
(Note: The parameter values and operators of input file will be automatically URL encoded (with encoding scheme “UTF-8”) by the program).

3.Optional filter conditions, can be used to further filter search results. Format for each is “Parameter – Operator, Value”and a newline at the end. At present the following operators are supported by HEK API, =, !=, >, <, >=, <= and like.  For more information about filter parameters, please use this link, “http://vso.stanford.edu/hekwiki/ApplicationProgrammingInterface?action=print#head-4505d658c207b707054a1c76a53a9d8ca3778d5d”. Also for a complete list of all parameter, descriptions and the corresponding event types, please use this following link, “http://www.lmsal.com/hek/VOEvent_Spec.html”. An example of input (file) is given below, 
Start Date : 2011-01-01
Start Time : 00-00-00
End Date : 2011-01-30
End Time : 23-59-59
Event Type : sg
Spatial Region :  helioprojective, -5000,5000,-5000,5000
FRM_Contact :=, Nour.Eddine.Raouafi@jhuapl.edu;manolis.georgoulis@academyofathens.gr
FRM_Name : =, Sigmoid Sniffer
OBS_ChannelID : =, 94_THIN

Example to run program :
java QueryHEK /home/karthik/NetBeansProjects/QueryHEK/Result/ /home/karthik/NetBeansProjects/QueryHEK/input.txt XML
java QueryHEK /home/karthik/NetBeansProjects/QueryHEK/Result/ /home/karthik/NetBeansProjects/QueryHEK/input.txt JSON




