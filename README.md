# Java Examples

The following examples show common usages of the SDK using Java. We recommend that you study the examples to learn
everything necessary to build your own custom applications.

## How to run the examples?

In order to run these examples, you first need to install:

- JDK 11 or higher
- Maven 3.6.*

Then build the samples jar and execute the main class you need:

```shell
> git clone https://github.com/Geotab/sdk-java-samples.git
> cd sdk-java-samples
> mvn clean verify
```

You could run it from your IDE or with the following command line:

```shell
> java -cp target/sdk-java-samples.jar;target/lib/* <mainClassWithPackage>
```
In Unix-based systems like Linux or macOS, you would use a colon (`:`) instead of a semicolon (`;`) as the path separator:

```shell
> java -cp "target/sdk-java-samples.jar:target/lib/*" <mainClassWithPackage>
```

## Examples list

### Get Logs

An example that obtains the logs for a given vehicle between a range of dates.

### Get Count

An example that obtains the count of entities. Enter the entity name (e.g. User, Zone, Device, etc) to get the count-of result.

### Text Message

An example that sends text messages to and from a GO device.

### Import Groups

A console example that is also a group import tool. It enables a one time import of groups to a database from a CSV
file.

### Import Devices

Another console example that imports devices from a CSV file.

### Import Users

Another console example that imports users from a CSV file.

### Data Feed

An example of retrieving GPS, Status and Fault data as a feed and exporting to a CSV file.


Notas personales 

s=[ option: s  [ARG] :: [required] The Server :: class java.lang.String ], 
d=[ option: d  [ARG] :: [required] The Database :: class java.lang.String ], 
u=[ option: u  [ARG] :: [required] The User :: class java.lang.String ], 
p=[ option: p  [ARG] :: [required] The Password :: class java.lang.String ], 
gt=[ option: gt  [ARG] :: [optional] The last known gps data token :: class java.lang.String ], 
st=[ option: st  [ARG] :: [optional] The last known status data token :: class java.lang.String ], 
ft=[ option: ft  [ARG] :: [optional] The last known fault data token :: class java.lang.String ], 
tt=[ option: tt  [ARG] :: [optional] The last known trip token :: class java.lang.String ], 
et=[ option: et  [ARG] :: [optional] The last known exception token :: class java.lang.String ], 
exp=[ option: exp  [ARG] :: [optional] The export type: console, csv. Defaults to console. :: class java.lang.String ], 
f=[ option: f  [ARG] :: [optional] The folder to save any output files to, if applicable. Defaults to the current directory. :: class java.lang.String ], 
c=[ option: c  [ARG] :: [optional] Run the feed continuously. Defaults to false. :: class java.lang.String ]} ] [ long {} ]

s server d database u user p password gt nnn st nnn ft nnn tt nnn -t nnn exp csv f file path

s my.geotab.com d customer u aalex.dbv@gmail.com p 387$Alex

s  [ARG] :: [required] The Server :: my.geotab.com  
