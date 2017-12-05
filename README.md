# Transaction Statistics Service

This application provides statistics for the registered transactions in the last minute.
Transactions are registered via a REST endpoint and statistics are retrieved the same way.

##Specs

##Api

* POST /transactions

Every Time a new transaction happened, this endpoint will be called.
```
Body:
{
    "amount": 12.3,
    "timestamp": 1478192204000
}
```
Where:
* amount - transaction amount
* timestamp - transaction time in epoch in millis in UTC time zone (this is not current
timestamp)

Returns: Empty body with either 201 or 204.
* 201 - in case of success
* 204 - if transaction is older than 60 seconds
* 204 - if transaction from the future
* 204 - if transaction is negative


Where:
* amount is a double specifying the amount
* time is a long specifying unix time format in milliseconds

* GET​ ​/statistics

Returns:
```
{
    "sum": 1000,
    "avg": 100,
    "max": 200,
    "min": 50,
    "count": 10
}
```
Where:
* sum is a double specifying the total sum of transaction value in the last 60 seconds
* avg is a double specifying the average amount of transaction value in the last 60
seconds
* max is a double specifying single highest transaction value in the last 60 seconds
* min is a double specifying single lowest transaction value in the last 60 seconds
* count is a long specifying the total number of transactions happened in the last 60
seconds

## Requirements

* The API have to be threadsafe with concurrent requests
* The API have to function properly, with proper result
* The project should be buildable, and tests should also complete successfully. e.g. If
  maven is used, then mvn clean install should complete successfully.
* The API should be able to deal with time discrepancy, which means, at any point of time,
  we could receive a transaction which have a timestamp of the past
* Make sure to send the case in memory solution without database (including in-memory
 database)
* Endpoints have to execute in constant time and memory (O(1))

## Implementation

```
* The API have to function properly, with proper result
* The project should be buildable, and tests should also complete successfully. e.g. If
  maven is used, then mvn clean install should complete successfully.
```
For setting up the web-server I choose a spring boot starter core + web setup. Ref [here](https://spring.io/blog/2014/03/07/deploying-spring-boot-applications) 
```
* The API have to be threadsafe with concurrent requests
``` 
* Using spring injection all the implementation are created by default as singletons.
* The [repository implementation](./src/main/java/org/mblascoespar/transactionservice/repository/StatisticsRepositoryV2.java) 
contains a ConcurrentHashMap that will hold the statistics. This implementation of Map includes locking for the operations used in the repo.
* The repository also provided the data in the form of defensive copy -> The general approach was to use always immutable data.
* Used ConcurrentHashMap that provides thread safety for the shallow copy and put operations.
```
 Make sure to send the case in memory solution without database (including in-memory database)
```
Check [repository implementation](./src/main/java/org/mblascoespar/transactionservice/repository/StatisticsRepositoryV2.java)
```
Endpoints have to execute in constant time and memory (O(1))
```
For achieving this I have modeled a time window approach:
* keywords:
    * Window: Fixed interval of time.
    * Time Bucket: Represents a slot in time of the window.
* The core idea is that we do not need to store each single transaction and recalculate the statistics on insertion and on get.
Instead we will be updating statistics following the following steps:
    * check timestamp of the statistics
    * If is before now - window then we can restart the statistics
    * Update the statistics with the data of the new transaction (also updates the timestamp of the statistic)
Ideally using only one statistic object should be enough but this would work poorly with concurrent request.

For tackling this the idea is to have an map with a fixed number of elements. This map would represent Time Buckets of the window where each index represents a time bucket.
Whenever a new transaction comes in the application depending on the time modulo Window will be assigned to a specific slot,
With this the requests are split into positions of the array depending on the timestamp (Time Bucket).
Then the application will recalculate the statistics,  using the new transaction, of the time bucket as stated above.

For retrieving the statistics, with reducing the statistics that are before (request time - Window) you can get the statistics with an time error of ()Time Bucket)/2
The get operation is O(1) because the map is fixed. Also because the map gets filled on transaction time the GET statistics time will not be constant until map is filled (but will be theoretically faster)

This approach might have problems when dealing with burst of POSTs with a fixed frequency that could lead to a delay of stats being inserted into the Time Bucket.
Also, for the more precision you define, the higher the memory will be used for the map, and it will take longer for the statistics to be calculated. 

I also attach a Jmeter profile that I have been using to test the behaviour of the application.
Using this application in combination of jVisualVM will give us the ability to spot unwanted behaviour
like memory leaks, not achieving the expected O(1) specifications.
Regretfully i did not have the time to make a thoughtful profile of the application worth of presenting.
    
I also kept in the code my previous implementation which was based on keeping an store of transactions and when the statistics
would be requested the application will calculate the stats from the transaction within the time frame and clean up the rest.
If for whatever reason you want to use it you will have to change the Statistics interface used in the controllers.

##Configuration

* logging.level.org.mblascoespar.transactionservice : Log level of the application
* statistics.window.size.in.millis: Size of the time window for the statistics
* statistics.precision: Defines the precision of the statistics the time error of the statistics will be on avg windowSize/(2*precision).
The higher the precision the higher the memory consumption and the fixed time for the statistics to be calculated.

##Dependencies

* Maven 
* Spring Boot 1.5.8
  * spring-boot-starter-web
  * spring-boot-devtools
  * spring-boot-starter-test
* Java 1.8 

## Running
```
mvn spring-boot:run
```
OR

```
java -jar transaction-service-0.0.1-SNAPSHOT.jar
```

This will boot a tomcat server at 8080 (Default). 
For other port specify the property server.port in application.properties or in command line.

Log levels are set up on INFO, reduce or increase the logging level in application.properties.

## Testing

```
mvn test
```

Log levels are set up on the logback-test.xml file.

For checking the behaviour with concurrent users I set up a [jmeter profile](./JMETER%20Profile.jmx).
It set up a thread group of users that will post transactions with a random amount and current time of the request as timestamp.
A second thread group of users will request the statistics.
Included a couple of graph listeners to represent the response time.

## NEXT STEPS

* Test behaviour when precision > windowsize in millis (timeBucket < 1ms)
* Use JMeter + VisualVM to make a profile 
* Concurrency Unit Testing
* Swagger API endpoint + specs
* Add coverage
* Clean up old version (Kept for evaluation purposes)
* All of the above may vary on development cycle considerations