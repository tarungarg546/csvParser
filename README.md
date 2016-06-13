 
## csvParser

This is a project made by Team mangoPeople[ Me with few other guys] for EMC2 ECD techathon.

Basically problem statement was to creat an API using Java REST services that will have a CSV file uploaded and depending upon contents of CSV file[like number of column and othe data], It will dynamically create a table schema in our database[mySql used here] and insert the data into the table.

Our API should be smart enough to recognize what is data type of particular column is .

We used RegEx to determine dataType. BTW you could also change dataType if you think this is not the dataType of column should be.

## Backend

It was stated in requirements that we have to use Java for4 building REST services and MySQL for database. So we used above technology.

When user submits a file to our UI. We give him an option to provide a tableName he would like to give the table if he provides that we take that else fileName is by default taken as tableName.

Then "POST" request is sent to server with file provided and tableName provided. 
	- We check on on our database if the table with particular tableName alread exists or not. 
	  If it does we show him a toast that the table with this tableNam already exist. 
	  Now user have two options either to drop previous  table  or create table with some other name.
	  To drop existing table he can head over to "view Table" option in our UI and enter the tablename he
	  wants to have a look at and then delete that table by clicking drop this table button provide beneath.

	- Else we just show the user what our system can interpret from CSV data like column names and  
	  data type of each column. User can change it or not its his choice. User can also delete a 
	  particular column of his choice is he would likes to. Now, suppose csv file has million records so its not advisable to send this much amount of data back & forth. So, we are just sending some rows of data from server to client at this step. Rest of data can be lazy loaded.
	  Then user can click on create table button table would be created on our backend and then user can also look at table created by "view table" option.

	- Now if csv contains millions of records so it is not advisable to do database i/o for each row 
	  to be inserted in table so we created an batch of configurable size and then using batch 
	  processing for inserting data into table.


## Frontend

Frontend was built with AngularJS and materialize css. We have also used "service-worker" API to
leverage caching and optimizing rendering performance when offline / poor connection. This type of 
approach is referred as offline-first.

## Set up

You have to change username/password in `context.xml` to specify your databases needs.
