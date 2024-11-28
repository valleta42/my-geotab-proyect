# Data Asset

The Data Asset is an application that allows a third party to easily receive all the telematics data from your
devices. The application can be run interactively or in the background. The application will produce easy to consume CSV
files containing the key telematics data sets with updates every few seconds. Furthermore, the application can easily be
updated and customized when further integration is required for example, pushing the data into a Web service, writing to
a database, etc..

El metodo data Assets es un ejecutable diseñado para generar un primer volcado de datos de los ultimos 7 dias y 
posteriormente ir añadiendo los nuevos eventos en una llamada recursiva que se ejecuta cada minuto. He usado para este 
ejemplo los registros de los statusData pero se podrian extrapolar a todos los demas tipos ya que podriamos usar los métodos 
LogRecordEntity para recuperar los  datos de coordenadas y el TachographDataFileSearch para recuperar los datos del 
tacometro (Este ultimo no esta probado)

se eligio el StatusData pero se podria haber elegido el LogRecord como trigger de la generación de un nuevo evento ya 
que con los datos de estos dos obetnemos todos los datos necesarios que se exigen en la prueba.

callGet(StatusDataEntity, StatusDataSearch.builder().fromDate(fromDate).toDate(toDate).build());

- Para el primer step he usado un método de la api llamado allGet para los StatusDataEntity de la ultima semana y por 
cada uno de los registros buscamos su ultima traza a traves del LogRecordEntity guardandolo en un map que hace de 'fakecache' 
para no tener que llamar a la API una por cada registro si no una vez por cada VIN (en este caso device asociado a un VIN) y 
recuperarlo cada vez que nos haga falta sin llamar a la API. Este primer estep tambien tiene una utilidad para enviar los 
archivos existentes a una carpeta History añadiendolos una fecha y timestamp para pdoer guardar histórico de las ejecuciones.

- Para el segundo step hemos ultilizado el callGetFeed de los statusData para poder aprovechar la utilidad de fromVersion y solo 
recuperar los datos desde la ultima llamada. Segun se llama al primer step llamamos al segundo para recuperar una version y poder 
usar esa versión en la siguiente llamada pasado 1 minuto.

Puntos de mejora:

- Se podria haber implementado una utilidad que si le pasas un fromVersion de un Status data por linea de comandos  no ejecute el primer step y solo 
ejecutar el segundo añadiendo los registros desde la ultima version en el mismo archivo.

- Realizar la fakeCache de una forma un poco mas elegante dentro de la utilidad de las caches, pero me aprecio algo enrevesado ya que tendriamos que mezclar
 churras con merinas.



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
java -cp my-geotab-proyect.jar com.geotab.sdk.dataasset.DataAssetApp --s "mypreview.geotab.com" --d "demo_candidates_net" --u "aalex.dbv@gmail.com" --p "387$Alex" --exp "csv" --f  "C:\csv" --c true 60000
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