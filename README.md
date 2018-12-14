# Mechanic Shop Database Application

## Summary
This is a simple Java client application that uses the Java embedded SQL library to communicate with and update a SQL database running on a PostgreSQL server. It has simple functionalities such as adding a customer, adding a mechanic, adding a car, adding a service request, and closing a service request. It also includes 5 options to output more complicated SQL queries.
## Usage
After cloning the repository, change to the `postgresql` directory located in `MechanicShopDB/code/postgresql`. Then run the 2 shell scripts in this order:

```~/MechanicShopDB/phase3/code/postgresql
user@localmachine $ source ./startPostgreSql.sh 
.
.
.
user@localmachine $ source ./createPostgreSql.sh 
.
.
.
```

If done correctly, the postgreSQL environment should be running. By default the server runs on port 9998. This can be changed by editting the `createPostgreSql.sh` file.
After, change directories to `java` located in `MechanicShopDB/code/java`. Then run  `source ./compile.sh && source ./run.sh $USER_DB 9998 $USER` and the Java client should launch:

```~/MechanicShopDB/phase3/code/java
user@localmachine $ source ./compile.sh && source ./run.sh $USER_DB 9998 $USER
(1)
(2)
Connecting to database...Connection URL: jdbc:postgresql://localhost:9998/kluu008_DB

Done
  __  __           _                 _         _____ _                 
 |  \/  |         | |               (_)       / ____| |                
 | \  / | ___  ___| |__   __ _ _ __  _  ___  | (___ | |__   ___  _ __  
 | |\/| |/ _ \/ __| '_ \ / _` | '_ \| |/ __|  \___ \| '_ \ / _ \| '_ \ 
 | |  | |  __/ (__| | | | (_| | | | | | (__   ____) | | | | (_) | |_) |
 |_|  |_|\___|\___|_| |_|\__,_|_| |_|_|\___| |_____/|_| |_|\___/| .__/ 
                                                                | |    
                                                                |_|    
MAIN MENU
---------
1. AddCustomer
2. AddMechanic
3. AddCar
4. InsertServiceRequest
5. CloseServiceRequest
6. ListCustomersWithBillLessThan100
7. ListCustomersWithMoreThan20Cars
8. ListCarsBefore1995With50000Milles
9. ListKCarsWithTheMostServices
10. ListCustomersInDescendingOrderOfTheirTotalBill
11. < EXIT
Please make your choice: 
```
Once the mechanic shop is launched, enter any number from 1 to 10 to interact with the PostgreSQL server. Press 11 to exit.

## Known Bugs/Oversights
* In the insert service request function (4) when listing customer’s cars, sometimes the choice for the car terminates the function early. This is probably due to some String or List access error.
* In the insert service request function (4) when the service request is on a customer not yet in the database, a prompt to add the customer then add a car will pop up, but the user must initiate function 4 again after adding the customer’s car to continue initiating the service request.
* When listing all cars attached to a customer in the initiate service request function(4), there isn’t an option to add a new car even though the prompt implies that this is an option.

