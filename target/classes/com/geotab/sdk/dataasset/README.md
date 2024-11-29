# Data Asset

The Data Asset is an application that allows a third party to easily receive all the telematics data from your
devices. The application can be run interactively or in the background. The application will produce easy to consume CSV
files containing the key telematics data sets with updates every few seconds. Furthermore, the application can easily be
updated and customized when further integration is required for example, pushing the data into a Web service, writing to
a database, etc..

Data Assets method is an executable designed to generate an initial dump since the last 7 days and, subsequently, to add 
the new events in a recursive call which is executed each minute. For this example I used the statusData registers. However, 
this could be used for any type because we would use the LogRecordEntity to recover the coordinate data and the TachographDataFileSearch 
to recover the tachograph data (this last statement was not tested yet).

The status data was chosen, but the LogRecord could be used as a trigger for the new event generation too due to the fact 
that with both methods we can retrieve the required data for the test. 

- For the first step I used a method from the API named allGet for the StatusDataEntity since the last week, and for each registry we look 
for the last record through the LogRecordEntity, saving it into a map which acts like a "fake" cache. Using this, we don't need to call the 
API for each registry, calling it once for each VIN (in this case, the device associated with a specific VIN), recovering it each time we need 
it without calling the API. In this first step there also is an utility to send the existing files to a folder named "history", adding the date 
and timestamp to be saved as an execution historic file.

- For the second step, we used the callGetFeed method from StatusData to get advantage from the fromVersion utility, retrieving only the data 
from the last call. After calling the first step, the second one is called to retrieve the version and to be able to use that version after 1 minute.

Improvements:

- A utility could be implemented that, using the fromVersion of a specific Status through the command line, doesn't execute the first step, executing only the second one and adding the registers since the last version of the same file.
- To implement the "fake" cache in a more elegant way in the caches utility. However, this implementation looked a little bit tricky for me, due to fact that we should mix different utilities and I was not sure if it was very correct or useful.



## Prerequisites

The sample application requires:

- JDK 11 or higher
- Maven 3.6.*

The Geotab Data Feed application connects to the MyGeotab cloud hosting services, please ensure that devices have been
registered and added to the database. The following information is required:

- Server (my.geotab.com)
- Username
- Password
- Database (customer)
- The folder to save any output files to, if applicable. Defaults to the current directory.
- Run the feed continuously. Defaults to false.

The application will bring up the following console:

```shell
> java -cp sdk-java-samples.jar com.geotab.sdk.datafeed.DataFeedApp --s 'server' --d 'database' --u 'user' --p 'password' --exp 'csv' --f 'file path' --c
--s  The Server
--d  The Database
--u  The User
--p  The Password
--f  The folder to save any output files to, if applicable. Defaults to the current directory.
--c  Run the feed continuously. Defaults to false.
```

Example usage:

```shell
java -cp my-geotab-proyect.jar com.geotab.sdk.dataasset.DataAssetApp --s "mypreview.geotab.com" --d "demo_candidates_net" --u "username" --p "pass" --exp "csv" --f  "C:\csv" --c true 60000
```

The options above are the inputs that the feed example can take. A server, database, user and password must be supplied
in order for the feed to run. Finally the feed can be instructed to run continuously or only one time.

By default, the Vehicles Datas files will output its results to a CSV files for each vehicle in the location specified by the -f flag above. If no
location is provided the CSV file will be placed in the same directory where the app is located.

#### Vehicle data

| **#** | **Field Name** | **Description** | **Example** |
| --- | --- | --- | --- |
| 1 | StatusDAta Id | The unique Id for the device. |  |
| 2 | Date | The date and time in UTC for the GPS position. | 12/12/21 09:43:01 |
| 3 | Vehicle Name | The vehicle name/description as displayed to users in Checkmate | Truck 123 |
| 4 | Vehicle Serial Number | The unique serial number printed on the GO device. | GT8010000001 |
| 5 | VIN | The Vehicle Identification Number of the vehicle | 1FUBCYCS111111111 |
| 6 | Longitude | The coordinate longitude in decimal degrees. | -80.6860275268555 |
| 7 | Latitude | The coordinate latitude in decimal degrees. | 37.0907897949219 |
| 8 | Odometer | The Odometer of vehicle in km/h. | 103 |