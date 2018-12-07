/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	public String ID(String table) throws SQLException{
		Random ran = new Random(55);
	
		//generate random number
		int newID = ran.nextInt() & Integer.MAX_VALUE;				
	
		//create SQL statement to seach if new id exists in table
		String query = "SELECT * FROM " + table + " WHERE ";

		if(table == "Owns"){
			query += "ownership_";
		} else if (table == "Service_Request") {
			query += "r";
		} else if (table == "Closed_Request") {
			query += "w";
		}

		String query2 = "id = ";	

		//if new id exists, re-generate random number

		String execute = query + query2 + Integer.toString(newID);	
		int results = executeQuery(execute);	

		while(results == 1){
			newID = ran.nextInt() & Integer.MAX_VALUE;	
			execute = query + query2 + Integer.toString(newID);
			results = executeQuery(execute);
		}				

		return Integer.toString(newID);
	}//end ID

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddCustomer(MechanicShop esql){//1
		try{
			String query = "INSERT INTO Customer(id, fname, lname, phone, address) VALUES (";
			System.out.print("=======================================");		
			System.out.print("ADDING NEW CUSTOMER");
			System.out.print("=======================================");
		
			String id = esql.ID("Customer"); 

			System.out.print("Please enter first name: ");
			String fname = in.readLine();		

			System.out.print("Please enter last name: ");
			String lname = in.readLine();
		
			System.out.print("Please enter phone number: ");
			String ph = in.readLine();

			System.out.print("Please enter address: ");
			String ad = in.readLine();

			query = query + id + ", " + fname + ", " + lname + ", " + ph + ", " + ad + ")";

			esql.executeUpdate(query);
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public static void AddMechanic(MechanicShop esql){//2
		/*String query = "Mechanic(fname, lname, experience) VALUES (";
		System.out.print("=======================================");		
		System.out.print("ADDING NEW MECHANIC");
		System.out.print("=======================================");		


		System.out.print("Please enter first name: ");
		String fname = in.readLine();

		System.out.print("Please enter last name: ");
		String lname = in.readLine();
		
		System.out.print("Please enter years of experience: ");
		String exp = in.readLine();

		query = query + fname + ", " + lname + ", " + exp + ")";

		esql.executeUpdate(query);*/
	}
	
	public static void AddCar(MechanicShop esql){//3
		/*String query = "INSERT INTO Car(vin, make, model, year) VALUES (";
		System.out.print("=======================================");		
		System.out.print("ADDING NEW CAR");
		System.out.print("=======================================");


		System.out.print("Please enter VIN: ");
		String vin = in.readLine();
		
		System.out.print("Please enter make of car: ");
		String make = in.readLine();
		
		System.out.print("Please enter model of car: ");
		String model = in.readLine();
		
		System.out.print("Please enter car year: ");
		String carYear = in.readLine();
		
		query = query + vin + "," + make + "," + model + "," + carYear + ")";
		esql.executeUpdate(query);*/
	}
	
	/*This function will allow you to add a service request for a customer into the database.
	Given a last name, the function should search the database of existing customers. 
	* If many customers match, a menu option should appear listing all customers with the given last
	name asking the user to choose which customer has initiated the service request.
	Otherwise, the client application should provide the option of adding a new customer. 
	* If an existing customer is chosen, the client application should list all cars associated with
	that client providing the option to initiate the service request for one of the listed cars,
	otherwise a new car should be added along with the service request information for it.*/
	public static void InsertServiceRequest(MechanicShop esql){//4
		/*
		//search database of existing customers given lname
		String query = "SELECT lname, id FROM Customer WHERE lname = '";
		System.out.print("=======================================");		
		System.out.print("CREATING NEW SERVICE REQUEST");
		System.out.print("=======================================");
		
		System.out.print("Please enter customer last name ");
		String last = in.readLine();
		query = query + last + "'";

		// store number of results with given last name
		int numResults = executeQuery(query); 
		int customerID = 0;

		
		if(numResults == 0) { // No customers with given last name
			System.out.print("Customer not found. Would you like to add a customer? (Y/N)");
			char choice = in.readLine();
			

			if(choice != 'y' || choice != 'n' || choice != 'Y' || choice != 'N') { // if input isn't y or n, keep looping until it is
					do {
						System.out.print("Error: Unrecognized input. Enter (Y/N)");
						choice = in.readLine();
					} while (choice != 'y' || choice != 'n' || choice != 'Y' || choice != 'N');
			}	
			
			if(choice == 'y' || choice == 'Y') { // if y, add car
				AddCar(esql);
			} else if (choice == 'n' || choice == 'N') {
				System.out.print("Service request cancelled.");
			}

		} else { 

			if (numResults == 1) { // If only one customer, save their customer ID
				List<List<String>> results = executeQueryAndReturnResult(query);
				customerID = results.get(0).get(1);
			} else if (numResults > 1) { // If multiple customers
				String newQuery = "SELECT fname, phone, id FROM Customer WHERE lname = '" + last + "'";
				List<List<String>> results = executeQueryAndReturnResult(newQuery);
				
				System.out.print("Select which customer initiated the service request");
				System.out.print("Customers with last name \"%s\"", last);
				
				for (int i = 0; i < results.size(); i++) {
					String fname = results.get(i).get(0);
					String phone = results.get(i).get(1);
					int currentName = i+1;
					System.out.print("%s) %s\t%s", currentName, fname, phone);
				}
				
				int choice = in.readLine();
				customerID = results.get(choice-1).get(2);
			}
			
			//get car info from Car table if customer_id 
			String listCars = "SELECT vin, make, model FROM Car WHERE car_vin 
		
		}*/
	}
	
	/*This function will allow you to complete an existing service request. Given a service
	request number and an employee id, the client application should verify the information
	provided and attempt to create a closing request record. You should make sure to check
	for the validity of the provided inputs (i.e. does the mechanic exist, does the request exist,
	valid closing date after request date, etc.)*/
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		
	}
	
	/*.List the customers that have paid less than 100 dollars for repairs based on their
	previous service requests.*/
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		
	}
	
	/*Find how many cars each customer has counting from the ownership relation and
	discover who has more than 20 cars.*/
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		
	}
	
	/*Get the odometer from the service_requests and find all cars before 1995 having less
	than 50000 miles in the odometer*/
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		
	}
	
	/*Find for all cars in the database the number of service requests. Return the make,
	model and number of service requests for the cars having the k highest number of
	service requests. The k value should be positive and larger than 0. The user should
	provide this value. Focus on the open service requests.*/
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
		
	}
	
	/*For all service requests find the aggregate cost per customer and order customers
	according to that cost. List their first, last name and total bill.*/
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//9
		//
		
	}
	
}
