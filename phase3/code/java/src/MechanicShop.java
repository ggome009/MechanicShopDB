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
import java.util.Scanner;
import java.text.DateFormat;  
import java.text.SimpleDateFormat;  
import java.util.Date;  
import java.util.Calendar;  
import java.text.ParseException;

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
			
			System.out.println("  __  __           _                 _         _____ _                 ");
			System.out.println(" |  \\/  |         | |               (_)       / ____| |                ");
			System.out.println(" | \\  / | ___  ___| |__   __ _ _ __  _  ___  | (___ | |__   ___  _ __  ");
			System.out.println(" | |\\/| |/ _ \\/ __| '_ \\ / _` | '_ \\| |/ __|  \\___ \\| '_ \\ / _ \\| '_ \\ ");
			System.out.println(" | |  | |  __/ (__| | | | (_| | | | | | (__   ____) | | | | (_) | |_) |");
			System.out.println(" |_|  |_|\\___|\\___|_| |_|\\__,_|_| |_|_|\\___| |_____/|_| |_|\\___/| .__/ ");
			System.out.println("                                                                | |    ");
			System.out.println("                                                                |_|    ");

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
					
					System.out.println("  _    _                                                 _       _             _  ");
					System.out.println(" | |  | |                                               | |     | |           | | ");
					System.out.println(" | |__| | __ ___   _____    __ _    __ _  ___   ___   __| |   __| | __ _ _   _| | ");
					System.out.println(" |  __  |/ _` \\ \\ / / _ \\  / _` |  / _` |/ _ \\ / _ \\ / _` |  / _` |/ _` | | | | | ");
					System.out.println(" | |  | | (_| |\\ V /  __/ | (_| | | (_| | (_) | (_) | (_| | | (_| | (_| | |_| |_| ");
					System.out.println(" |_|  |_|\\__,_| \\_/ \\___|  \\__,_|  \\__, |\\___/ \\___/ \\__,_|  \\__,_|\\__,_|\\__, (_) ");
					System.out.println("                                    __/ |                                 __/ |  	");
					System.out.println("                                   |___/                                 |___/    \n");

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

	public static boolean validateName(String name){
		name = name.trim();		
		if(name.length() > 32) {
			System.out.println("ERROR: name must be 32 characters or less");			
			return false;
		}
	
		if(name.isEmpty()) {
			System.out.println("ERROR: name can not be empty");
			return false;
		} 
		
		char[] chars = name.toCharArray();
		for (char c : chars) {
			if( (!Character.isLetter(c) && c != '-') &&  (c != '\'' &&  c != ' ') ) {
				System.out.println("ERROR: name must be composed of only"); 
				System.out.println(" * alphabetical characters (A-Z,a-z),"); 
				System.out.println(" * hyphens (-),");
				System.out.println(" * and/or apostrophes(')");				
				return false;
			}
		}

		return true;
	}

	public static boolean validatePhone(String phone){
		if(phone.isEmpty()) {
			System.out.println("ERROR: phone number must not empty");
			return false;
		}

		if((phone.length() != 13 || phone.charAt(0) != '(') || (phone.charAt(4) != ')' || phone.charAt(8) != '-')) {
			System.out.println("ERROR: phone number must be of the form (###)###-####");	
			return false;
		}		

		char[] chars = phone.toCharArray();
		int numCount = 0;

		for (char c: chars) {
			if(Character.isDigit(c)) {
				numCount++;
			}
			
			if(!Character.isDigit(c) && c != '-' && c != '(' && c != ')') {
				System.out.println("ERROR: phone number must be of the form (###)###-####");
				return false;
			}
		}

		if(numCount != 10) {
			System.out.println("ERROR: phone number must be of the form (###)###-####");
			return false;
		}

		return true;
	}

	public static boolean validateAddress(String address, int mode){
		address = address.trim();		
		if(address.isEmpty()) {
			System.out.println("ERROR: input line must not empty");
			return false;
		}		

		if (mode == 1) { // street address
			char[] chars = address.toCharArray();
			for (char c : chars) {
				if( !Character.isDigit(c) && ((!Character.isLetter(c) && c != '.') &&  (c != '-' &&  c != ' ')) ) {
					System.out.println("ERROR: street address must be composed of only"); 
					System.out.println(" * alphabetical characters (A-Z,a-z),"); 
					System.out.println(" * numerical characters (0-9),");
					System.out.println(" * spaces ( ),");
					System.out.println(" * hyphens (-),");
					System.out.println(" * and/or periods (.),");			
					return false;
				}
			}
		} else if (mode == 2) { // city
			char[] chars = address.toCharArray();
			for (char c : chars) {
				if( (!Character.isDigit(c)) && ((!Character.isLetter(c) && c != '.') &&  (c != '-' &&  c != ' ')) ) {
					System.out.println("ERROR: City must be composed of only"); 
					System.out.println(" * alphabetical characters (A-Z,a-z),"); 
					System.out.println(" * numerical characters (0-9),");
					System.out.println(" * spaces ( ),");
					System.out.println(" * hyphens (-),");
					System.out.println(" * and/or periods (.),");			
					return false;
				}
			}
		} else if (mode == 3) { // state
			if (address.length() != 2) {
				System.out.println("ERROR: State code must be two capital letters (ex: CA, NY)");
				return false;
			}

			char[] chars = address.toCharArray();
			for (char c : chars) {
				if(!Character.isUpperCase(c)) {		
					System.out.println("ERROR: State code must be two capital letters (ex: CA, NY)");
					return false;
				}
			}
		} else if (mode == 4) { // zip
			if (address.length() != 5) {
				System.out.println("ERROR: Zip code must be 5 digits long");
				return false;
			}

			char[] chars = address.toCharArray();
			for (char c : chars) {
				if(!Character.isDigit(c)) {		
					System.out.println("ERROR: Zip code must be only composed of digits");
					return false;
				}
			}
		}	

		return true;
	}

	public static boolean validateYears(String years){
		char[] chars = years.toCharArray();
		for (char c : chars) {
			if(!Character.isDigit(c)) {		
				System.out.println("ERROR: years of experience must be a number from 0 to 99 inclusive");
				return false;
			}
		}		

		int experience = Integer.parseInt(years);

		if(experience < 0 || experience >= 100) {
			System.out.println("ERROR: years of experience must be a number from 0 to 99 inclusive");		
			return false;
		}

		return true;
	}
	
	public static boolean validateCar(String input, int mode) {
		input = input.trim();
		if (mode == 1) { // VIN
			Boolean alpha = false;
			Boolean numeric = false;
			if (input.length() != 16) {
					System.out.println("ERROR: VIN must be 16 characters long");
					return false;
			}
			char[] chars = input.toCharArray();
			for (char c : chars) {
				if (!Character.isLetterOrDigit(c)) {
					System.out.println("ERROR: VIN must be only composed of digits and letters");
					return false;
				}
				else {
					if (Character.isLetter(c)) {
						alpha = true;
					}
					if (Character.isDigit(c)) {
						numeric = true;
					}
			
				}
			}
		
			if (!(alpha && numeric)) {
				System.out.println("ERROR: VIN must contain both digits and letters");
				return false;
			}
			
			return true;
		} else if (mode == 2) { // MAKE
			if(input.length() > 32) {
				System.out.println("ERROR: make must be 32 characters or less");			
				return false;
			}
			char[] chars = input.toCharArray();
			for (char c : chars) {
				if(!Character.isLetterOrDigit(c) && ((c != '&') &&  (c != '-' &&  c != ' ')) ) {
					System.out.println("ERROR: make must be composed of only"); 
					System.out.println(" * alphabetical characters (A-Z,a-z),"); 
					System.out.println(" * numerical characters (0-9),");
					System.out.println(" * spaces ( ),");
					System.out.println(" * hyphens (-),");
					System.out.println(" * and/or ampersands (&),");			
					return false;
				}
			}
			return true;
		} else if (mode == 3) { // MODEL
			if(input.length() > 32) {
					System.out.println("ERROR: model must be 32 characters or less");			
					return false;
			}
			char[] chars = input.toCharArray();
			for (char c : chars) {
				if(!Character.isLetterOrDigit(c) && (c != '-' &&  c != ' ') ) {
					System.out.println("ERROR: model must be composed of only"); 
					System.out.println(" * alphabetical characters (A-Z,a-z),"); 
					System.out.println(" * numerical characters (0-9),");
					System.out.println(" * spaces ( ),");
					System.out.println(" * and/ or hyphens (-),");			
					return false;
				}
			}
			return true;
		} else if (mode == 4) { // YEAR
			if(input.length() != 4) {
					System.out.println("ERROR: year must be 4 digits");			
					return false;
			} else {
				char[] chars = input.toCharArray();
				for (char c : chars) {
					if(!Character.isDigit(c)) {
						System.out.println("ERROR: year must be composed of only 4 digits"); 		
						return false;
					}
				}
			}
			return true;
		}
		return true;
	}
	
	public static boolean validateServiceRequest(String srnum, MechanicShop esql){
		if(srnum.isEmpty()) {
			System.out.println("ERROR: Service Request Number must not be empty");		
			return false;
		}	

		char[] chars = srnum.toCharArray();
		for (char c : chars) {
			if(!Character.isDigit(c)) {		
				System.out.println("ERROR: Service Request Number must be only composed of digits");
				return false;
			}
		}	

		try{
			if(0 == esql.executeQuery("SELECT * FROM Service_Request WHERE rid="+srnum)) {
				System.out.println("ERROR: Service Request Number " + srnum + " not found");
				return false;
			}
		} catch (Exception e) {
			
		}
		return true;
	}

	public static boolean validateMechanic(String emnum, MechanicShop esql){
		if(emnum.isEmpty()) {
			System.out.println("ERROR: Employee ID Number must not be empty");		
			return false;
		}	

		char[] chars = emnum.toCharArray();
		for (char c : chars) {
			if(!Character.isDigit(c)) {		
				System.out.println("ERROR: Employee ID Number must be only composed of digits");
				return false;
			}
		}	

		try{
			if(0 == esql.executeQuery("SELECT * FROM Mechanic WHERE id="+emnum)) {
				System.out.println("ERROR: Employee ID Number " + emnum + " not found");
				return false;
			}
		} catch (Exception e) {
			
		}
		return true;
	}	

	public static boolean validateBill(String bill){
		if(bill.isEmpty()) {
			System.out.println("ERROR: total bill must not empty");
			return false;
		}		

		char[] chars = bill.toCharArray();
		for (char c : chars) {
			if(!Character.isDigit(c)) {		
				System.out.println("ERROR: bill must be composed of numbers only");
				return false;
			}
		}		

		return true;
	}

	/*
		Add a new customer into the database. You should provide an interface that takes as
		input the information of a new customer (i.e. first, last name, phone, address) and checks
		if the provided information are valid based on the constraints of the database schema.
	*/		
	public static void AddCustomer(MechanicShop esql){//1
		try{
			String query = "INSERT INTO Customer(id, fname, lname, phone, address) VALUES ('";
			System.out.print("===================================================\n");
			System.out.print(" (1) ADDING NEW CUSTOMER\n");
			System.out.print("===================================================\n");;

			String id = esql.ID("Customer"); 
			String fname;
			String lname;		
			String ph;	
			String ad;
			String temp;

			do {
				System.out.print("Please enter first name: ");
				fname = in.readLine();	
				fname = fname.trim();	
			} while(!validateName(fname));

			do {
				System.out.print("Please enter last name: ");
				lname = in.readLine();
				lname = lname.trim();
			} while(!validateName(lname));


			do {
				System.out.print("Please enter phone number [ex: (###)###-####]: ");
				ph = in.readLine();
			} while(!validatePhone(ph));

			System.out.print("Please enter address:\n");

			do {
				System.out.print(" - Street Address: ");
				temp = in.readLine();
				temp = temp.trim();
			} while(!validateAddress(temp, 1));	
			
			ad = temp;
			
			do {
				System.out.print(" - City: ");
				temp = in.readLine();
				temp = temp.trim();
			} while(!validateAddress(temp, 2));

			ad = ad + ", " + temp;

			do {
				System.out.print(" - State code [ex: CA, NY, AZ]: ");
				temp = in.readLine();
				temp = temp.trim();
			} while(!validateAddress(temp, 3));			

			ad = ad + " " + temp;

			do {
				System.out.print(" - Zip code: ");
				temp = in.readLine();
				temp = temp.trim();
			} while(!validateAddress(temp, 4));
			
			ad = ad + " " + temp;
			
			query = query + id + "', '" + fname + "', '" + lname + "', '" + ph + "', '" + ad + "')";
			//System.out.println(query);			

			System.out.print("---------\n");
			System.out.print("Creating Customer #"+ id +"\nName: "+ fname + " "+ lname + "\nPhone: " + ph + "\nAddress: "+ ad + "\n");

			esql.executeUpdate(query);
			System.out.print("---------\n");
			System.out.print("SUCCESS\n");
			System.out.print("===================================================\n");
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	/*
		Add a new mechanic into the database. You should provide an interface that takes as
		input the information of a new mechanic (i.e. first, last name, specialty, experience) and
		checks if the provided information is valid based on the constraints of the database
		schema.
	*/
	public static void AddMechanic(MechanicShop esql){//2
		try {
			String query = "INSERT INTO Mechanic(id, fname, lname, experience) VALUES (";
			System.out.print("===================================================\n");
			System.out.print(" (2) ADDING NEW MECHANIC\n");
			System.out.print("===================================================\n");	

			String id = esql.ID("Mechanic");
			String fname;
			String lname;
			String exp;

			do {
				System.out.print("Please enter first name: ");
				fname = in.readLine();		
				fname = fname.trim();
			} while(!validateName(fname));

			do {
				System.out.print("Please enter last name: ");
				lname = in.readLine();
				lname = lname.trim();
			} while(!validateName(lname));
		
			do {
				System.out.print("Please enter years of experience: ");
				exp = in.readLine();
				exp = exp.trim();
			} while(!validateYears(exp));

			query = query + "'" + id +  "', '" + fname + "', '"+ lname + "', '" + exp + "')";
			//System.out.print(query + "\n");


			System.out.print("---------\n");
			System.out.print("Creating Mechanic #"+ id +"\nName: "+ fname + " "+ lname + "\nExperience: " + exp + " years\n");

			esql.executeUpdate(query);
			System.out.print("---------\n");
			System.out.print("SUCCESS\n");
			System.out.print("===================================================\n");
		} catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	/*
		This function should allow for adding a new car into the database. You should
		provide an interface that takes as input the information of a new car (i.e. vin, make,
		model, year) checking if the provided information are valid based on the constrains of the
		database schema.
	*/
	public static void AddCar(MechanicShop esql){//3
		try {
			String query = "INSERT INTO Car(vin, make, model, year) VALUES ('";
			System.out.print("===================================================\n");		
			System.out.print(" (3) ADDING NEW CAR\n");
			System.out.print("===================================================\n");

			String newVIN;
			String newMake;
			String newModel;
			String newYear;
			Boolean getVin = true;
			String customerID = "";
			
			
			System.out.print("Does this new car belong to an existing customer? Enter (Y/N): ");
			String input1 = in.readLine();
			input1 = input1.trim();
			if(!input1.equalsIgnoreCase("y") && !input1.equalsIgnoreCase("n")) { 
				do {
					System.out.print("Error: Unrecognized input. Enter (Y/N): ");
					input1 = in.readLine();
					input1 = input1.trim();
				} while (!input1.equalsIgnoreCase("y") && !input1.equalsIgnoreCase("n"));
			}
			if (input1.equalsIgnoreCase("y")) {
				System.out.print("Please enter the customer's last name: ");
				String lastName = in.readLine();
				lastName = lastName.trim();
				
				String query2 = "SELECT lname, id FROM Customer WHERE lname = '";

				query2 = query2 + lastName + "'";

				// store number of results with given last name
				int numResults = esql.executeQuery(query2); 

				// No customers with given last name
				if(numResults == 0) { 
					System.out.println("No customers with last name " + lastName + " found. Request to add car cancelled. No customer to attach.");
					System.out.println("===================================================");					
					return;
				}	
				
				
				String newQuery = "SELECT fname, phone, id FROM Customer WHERE lname = '" + lastName + "'";
				List<List<String>> results = esql.executeQueryAndReturnResult(newQuery);

				int choice = 0;
				do {
					System.out.println("Select which customer initiated the service request");
					System.out.println("Customers with last name \"" + lastName + "\"");
					for (int i = 0; i < results.size(); i++) {
						String fname = results.get(i).get(0);
						String phone = results.get(i).get(1);
						int currentName = i+1;
						System.out.println(currentName + ") " + fname + "\t" + phone);
					}
					System.out.println("\n" + Integer.toString(results.size()+1) + ") Customer not listed");
					choice = Integer.parseInt(in.readLine());
					if(choice < 1 || choice > results.size()+1) {
						System.out.println("ERROR: invalid choice");
					}
					if(choice == results.size()+1) {
						System.out.println("No customers match. ADD CAR CANCELLED.");
						System.out.println("===================================================");						
						return;
					}
				}	while (choice < 1 || choice > results.size()+1);
				customerID = (results.get(choice-1).get(2));
			}
			else {
				System.out.print("Do you want to add a new customer? Enter (Y/N): ");
				String input2 = in.readLine();
				input2 = input2.trim();
				if(!input2.equalsIgnoreCase("y") && !input2.equalsIgnoreCase("n")) { 
					do {
						System.out.print("Error: Unrecognized input. Enter (Y/N): ");
						input2 = in.readLine();
						input2 = input2.trim();
					} while (!input2.equalsIgnoreCase("y") && !input2.equalsIgnoreCase("n"));
				}
				if (input2.equalsIgnoreCase("y")) {
					customerID = addCustomerReturnID(esql);
				}
				else {
					System.out.println("Request to add car cancelled. No customer to attach.");
					System.out.println("===================================================");
					return;
				}
			}		
			
			do {
				//ask for vin
				do {
					System.out.print("Please enter VIN (VIN must be 16 alphanumeric characters): ");
					newVIN = in.readLine();	
					newVIN = newVIN.trim();	
				} while(!validateCar(newVIN, 1));
				
				newVIN = newVIN.toUpperCase();
				//run sql query to check if VIN exists in database
				String checkExistingCar = "SELECT * FROM Car WHERE vin = '" + newVIN + "'";
				int carExists = esql.executeQuery(checkExistingCar);
				
				// CAR ALREADY EXISTS
				if (carExists == 1) {
					System.out.println("ERROR: Car \"" + newVIN +"\" already exists in database");
					// TRY AGAIN?
					System.out.print("Do you want to try again? (Y/N): ");
					String tryAgain = in.readLine();
					tryAgain = tryAgain.trim();
			
					// CHECK IF Y OR N
					if(!tryAgain.equalsIgnoreCase("y") && !tryAgain.equalsIgnoreCase("n")) { 
							do {
								System.out.print("Error: Unrecognized input. Enter (Y/N): ");
								tryAgain = in.readLine();
								tryAgain = tryAgain.trim();
							} while (!tryAgain.equalsIgnoreCase("y") && !tryAgain.equalsIgnoreCase("n"));
					}
					if (tryAgain.equalsIgnoreCase("n")) {
						getVin = false;
						System.out.println("CANCELLED ADDING CAR");
						System.out.println("===================================================");
						return;
					}
					else {getVin = true;}
				} 
				else { getVin = false;}

			} while (getVin);
			
			do {
				System.out.print("Please enter make of car: ");
				newMake = in.readLine();
				newMake = newMake.trim();		
			} while(!validateCar(newMake, 2)); 
				
			do {
				System.out.print("Please enter model of car: ");
				newModel = in.readLine();
				newModel = newModel.trim();
			} while (!validateCar(newModel, 3));
	
			do {
				System.out.print("Please enter car year: ");
				newYear = in.readLine();
				newYear = newYear.trim();
			} while (!validateCar(newYear, 4));

			if (Integer.parseInt(newYear) < 1970) {
				System.out.println("ERROR: Car is too old to add to Mechanic Shop Database");
				System.out.println("CANCELLED ADDING CAR");
				System.out.println("===================================================");
				return;
			}

			if (Integer.parseInt(newYear) > 2019) {
				System.out.println("ERROR: Mechanic shop does not have the tools to work on a car from the future");
				System.out.println("CANCELLED ADDING CAR");
				System.out.println("===================================================");
				return;
			}

			System.out.print("---------\n");
			System.out.print("Adding Car "+ newVIN + "\nMake: " + newMake + "\nModel: "+ newModel + "\nYear: " + newYear + "\n");

			query = query + newVIN + "', '" + newMake + "', '" + newModel + "', '" + newYear + "')";

			esql.executeUpdate(query);
			esql.executeUpdate("INSERT INTO OWNS(ownership_id, customer_id, car_vin) VALUES ('" + esql.ID("Owns") + "', '" + customerID + "', '" + newVIN + "')");
			
			System.out.print("---------\n");
			System.out.print("SUCCESS\n");
			System.out.print("===================================================\n");

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	// HELPER FUNCTIONS FOR INSERT SERVICE REQUEST
	public static String addCarReturnVIN(MechanicShop esql, String customerID){
		String newVIN = "";
		try {
			String query = "INSERT INTO Car(vin, make, model, year) VALUES ('";
			System.out.print("===================================================\n");		
			System.out.print(" (3) ADDING NEW CAR\n");
			System.out.print("===================================================\n");
			String newMake;
			String newModel;
			String newYear;
			Boolean getVin = true;
			
			do {
				//ask for vin
				do {
					System.out.println("Please enter VIN: ");
					newVIN = in.readLine();	
					newVIN = newVIN.trim();	
				} while(!validateCar(newVIN, 1));
				
				newVIN = newVIN.toUpperCase();
				//run sql query to check if VIN exists in database
				String checkExistingCar = "SELECT * FROM Car WHERE vin = '" + newVIN + "'";
				int carExists = esql.executeQuery(checkExistingCar);
				
				// CAR ALREADY EXISTS
				if (carExists == 1) {
					System.out.println("ERROR: Car \"" + newVIN +"\" already exists in database");
					// TRY AGAIN?
					System.out.println("Do you want to try again? (Y/N)");
					String tryAgain = in.readLine();
					tryAgain = tryAgain.trim();
					// CHECK IF Y OR N
					if(!tryAgain.equalsIgnoreCase("y") && !tryAgain.equalsIgnoreCase("n")) { 
							do {
								System.out.println("Error: Unrecognized input. Enter (Y/N)");
								tryAgain = in.readLine();
								tryAgain = tryAgain.trim();
							} while (!tryAgain.equalsIgnoreCase("y") && !tryAgain.equalsIgnoreCase("n"));
					}
					if (tryAgain.equalsIgnoreCase("n")) {
						getVin = false;
						System.out.println("CANCELLED ADDING CAR");
						System.out.println("===================================================");
						return "cancelled";
					}
					else {getVin = true;}
				} 
				else { getVin = false;}

			} while (getVin);
			
			do {
				System.out.println("Please enter make of car: ");
				newMake = in.readLine();
				newMake = newMake.trim();		
			} while(!validateCar(newMake, 2)); 
				
			do {
				System.out.println("Please enter model of car: ");
				newModel = in.readLine();
				newModel = newModel.trim();
			} while (!validateCar(newModel, 3));
	
			do {
				System.out.println("Please enter car year: ");
				newYear = in.readLine();
				newYear = newYear.trim();
			} while (!validateCar(newYear, 4));

			if (Integer.parseInt(newYear) < 1970) {
				System.out.println("ERROR: Car is too old to add to Mechanic Shop Database");
				System.out.println("CANCELLED ADDING CAR");
				System.out.println("===================================================");
				return "cancelled";
			}

			System.out.print("---------\n");
			System.out.print("Adding Car "+ newVIN + "\nMake: " + newMake + "\nModel: "+ newModel + "\nYear: " + newYear + "\n");

			query = query + newVIN + "', '" + newMake + "', '" + newModel + "', '" + newYear + "')";

			esql.executeUpdate(query);
			esql.executeUpdate("INSERT INTO OWNS(ownership_id, customer_id, car_vin) VALUES ('" + esql.ID("Owns") + "', '" + customerID + "', '" + newVIN + "')");
			return newVIN;
					
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return newVIN;
		
	}
	
	public static String addCustomerReturnID(MechanicShop esql) {
		String id = "";
		try{
			String query = "INSERT INTO Customer(id, fname, lname, phone, address) VALUES ('";
			System.out.print("===================================================\n");
			System.out.print(" (1) ADDING NEW CUSTOMER\n");
			System.out.print("===================================================\n");;

			id = esql.ID("Customer"); 
			String fname;
			String lname;		
			String ph;	
			String ad;
			String temp;

			do {
				System.out.print("Please enter first name: ");
				fname = in.readLine();	
				fname = fname.trim();	
			} while(!validateName(fname));

			do {
				System.out.print("Please enter last name: ");
				lname = in.readLine();
				lname = lname.trim();
			} while(!validateName(lname));


			do {
				System.out.print("Please enter phone number [ex: (###)###-####]: ");
				ph = in.readLine();
			} while(!validatePhone(ph));

			System.out.print("Please enter address:\n");

			do {
				System.out.print(" - Street Address: ");
				temp = in.readLine();
				temp = temp.trim();
			} while(!validateAddress(temp, 1));	
			
			ad = temp;
			
			do {
				System.out.print(" - City: ");
				temp = in.readLine();
				temp = temp.trim();
			} while(!validateAddress(temp, 2));

			ad = ad + ", " + temp;

			do {
				System.out.print(" - State code [ex: CA, NY, AZ]: ");
				temp = in.readLine();
				temp = temp.trim();
			} while(!validateAddress(temp, 3));			

			ad = ad + " " + temp;

			do {
				System.out.print(" - Zip code: ");
				temp = in.readLine();
				temp = temp.trim();
			} while(!validateAddress(temp, 4));
			
			ad = ad + " " + temp;
			
			query = query + id + "', '" + fname + "', '" + lname + "', '" + ph + "', '" + ad + "')";
			//System.out.println(query);			

			System.out.print("---------\n");
			System.out.print("Creating Customer #"+ id +"\nName: "+ fname + " "+ lname + "\nPhone: " + ph + "\nAddress: "+ ad + "\n");
			
			esql.executeUpdate(query);
			System.out.print("---------\n");
			System.out.print("SUCCESS\n");
			System.out.print("===================================================\n");
			return id;
		}  catch(Exception e){
			System.out.println(e.getMessage());
		}
		return id;
		
	}
	
	/*This function will allow you to add a service request for a customer into the database.
	Given a last name, the function should search the database of existing customers. 
	* If many customers match, a menu option should appear listing all customers with the given last
	name asking the user to choose which customer has initiated the service request. (DONE)
	Otherwise, the client application should provide the option of adding a new customer. (DONE)
	* If an existing customer is chosen, the client application should list all cars associated with
	that client providing the option to initiate the service request for one of the listed cars,
	otherwise a new car should be added along with the service request information for it.*/
	public static void InsertServiceRequest(MechanicShop esql){//4
		try {	
			//search database of existing customers given lname
			System.out.print("===================================================\n");
			System.out.print(" (4) CREATING NEW SERVICE REQUEST\n");
			System.out.print("===================================================\n");

			String query = "SELECT lname, id FROM Customer WHERE lname = '";
			System.out.println("Please enter customer last name ");
			String last = in.readLine();
			last = last.trim();
			query = query + last + "'";

			// store number of results with given last name
			int numResults = esql.executeQuery(query); 
			String customerID = "";
			String carVIN = "";

			// No customers with given last name
			if(numResults == 0) { 
				System.out.print("Customer not found. Would you like to add a customer? (Y/N): ");
				String choice = in.readLine();
				choice = choice.trim();
				// if input isn't y or n, keep looping until it is
				if(!choice.equalsIgnoreCase("y") && !choice.equalsIgnoreCase("n")) { 
						do {
							System.out.print("Error: Unrecognized input. Enter (Y/N): ");
							choice = in.readLine();
							choice = choice.trim();
						} while (!choice.equalsIgnoreCase("y") && !choice.equalsIgnoreCase("n"));
				}	
				// if y, add car
				if(choice.equalsIgnoreCase("y")) { 
					customerID = addCustomerReturnID(esql); //add customer and get ID
					
					carVIN = addCarReturnVIN(esql, customerID); //adds to car and to owns
					if (carVIN.equals("cancelled")) {
						System.out.println("Cancelled adding car - Insert service request cancelled");
						return;
					}
				} else {
					System.out.println("Service request cancelled.");
				}
			//found customer(s) with given last name
			} else { 
				String newQuery = "SELECT fname, phone, id FROM Customer WHERE lname = '" + last + "'";
				List<List<String>> results = esql.executeQueryAndReturnResult(newQuery);


				System.out.println("Select which customer initiated the service request");
				System.out.println("Customers with last name \"" + last + "\"");
				for (int i = 0; i < results.size(); i++) {
					String fname = results.get(i).get(0);
					String phone = results.get(i).get(1);
					int currentName = i+1;
					System.out.println(currentName + ") " + fname + "\t" + phone);
				}
				int choice = Integer.parseInt(in.readLine());
				customerID = (results.get(choice-1).get(2));
			
				// list customer's cars
				String listCars = "SELECT * FROM Car C WHERE C.vin IN (SELECT car_vin FROM Owns	WHERE customer_id = '"; 
				listCars = listCars + customerID + "')";
				
				List<List<String>> customerCars = esql.executeQueryAndReturnResult(listCars);
				if (customerCars.size() == 0) {
					System.out.println("ERROR: Customer does not have any cars in the database.\nCancelling insert service request.");
					System.out.print("===================================================\n");					
					return;
				} else {
					System.out.println("Select which car to initiate service request for or add a new car");

					for (int i = 0; i < customerCars.size(); i++) {
						String carVin = customerCars.get(i).get(0);
						String carMake = customerCars.get(i).get(1);
						String carModel = customerCars.get(i).get(2);
						String carYear = customerCars.get(i).get(3);
						int currentCar = i+1;
						System.out.println(currentCar + ") " + carVin + "\t" + carMake + "\t" + carModel + "\t" + carYear);
					}
					int carChoice = Integer.parseInt(in.readLine());
					String srVin = "0";
					//add new car if user selects last option				
					if (carChoice == customerCars.size() + 1) {
				
						//new car tuple in CAR and OWNS
						srVin = addCarReturnVIN(esql, customerID);
					}

					else { //get selected car
						srVin = customerCars.get(carChoice).get(0);	
					}
				
					//initiate service request
					String srID = esql.ID("Service_Request"); //make new RID
					//customerID found earlier
					// srVin set earlier
				
					String srOdometer;
					String srComplain;
		

					Boolean isValidOdometer = false;
					do {
						System.out.print("Please enter odometer reading: ");
						srOdometer = in.readLine();	
						srOdometer = srOdometer.trim();
						char[] odometerNum = srOdometer.toCharArray();
						int length = odometerNum.length;
						for (char c : odometerNum) {
							if(!Character.isDigit(c)) {
								isValidOdometer = false;
								System.out.println("ERROR: Odometer value must be composed of numerical characters (0-9)");
							}
							else if (c == length-1) {
								isValidOdometer = true;
							}
						}
						isValidOdometer = true;
					} while(!isValidOdometer);

					System.out.print("Please enter a complaint: ");
					srComplain = in.readLine();
					srComplain = srComplain.trim();
					
					String srQuery = "INSERT INTO Service_request(rid, customer_id, car_vin, date, odometer, complain) VALUES ('";
					srQuery = srQuery + srID + "', '" + customerID + "', '" + srVin + "', '" + "CURRENT_DATE"   + "', '" + srOdometer + "', '" + srComplain + "')";
				}
			 }
		} catch (Exception e) {
			System.out.println(e.getMessage());			
		}

	}
	
	/*This function will allow you to complete an existing service request. Given a service
	request number and an employee id, the client application should verify the information
	provided and attempt to create a closing request record. You should make sure to check
	for the validity of the provided inputs (i.e. does the mechanic exist, does the request exist,
	valid closing date after request date, etc.)*/
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		try {
			System.out.print("===================================================\n");
			System.out.print(" (5) CLOSING A SERVICE REQUEST\n");
			System.out.print("===================================================\n");

//YYYY-MM-DD
			String rid;
			do {
				System.out.print("Please enter service request number: ");
				rid = in.readLine();
				rid = rid.trim();
			} while(!validateServiceRequest(rid, esql));

			String mid;
			do {
				System.out.print("Please enter employee id number: ");
				mid = in.readLine();
				mid = mid.trim();		
			} while(!validateMechanic(mid,esql));

			Date d = Calendar.getInstance().getTime();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String closureDate = dateFormat.format(d);

			String validationQuery = "SELECT date FROM Service_Request WHERE rid='" + rid + "'";
			List<List<String>> myTuple = esql.executeQueryAndReturnResult(validationQuery);
			
			String ServiceRequestDate = myTuple.get(0).get(0);
			
			Date ServiceDate = dateFormat.parse(ServiceRequestDate);
			Date ClosureDate = dateFormat.parse(closureDate);

			if (ServiceDate.compareTo(ClosureDate) > 0) {
				// System.out.println("ServiceDate is after ClosureDate");
				System.out.println("ERROR: Current date (" + closureDate + ") is before date of service (" + ServiceRequestDate + ")\nCannot close service request " + rid + " because no service has been done");
			} else {
				String wid = esql.ID("Closed_Request");
				
				String bill;
				do {
					System.out.print("Please enter billing amount: $");
					bill = in.readLine();
					bill = bill.trim();
				} while(!validateBill(bill));

				String comment;
				
				System.out.print("Please enter comment(s) about service [press Enter when done]: ");
				comment = in.readLine();
				comment = comment.trim();
				


				String closeString = "INSERT INTO Closed_Request(wid,rid,mid,date,comment,bill) VALUES ('";
				closeString = closeString + wid + "', '" + rid + "', '" + mid + "', '" + closureDate + "', '" + comment + "', '" + bill + "')";

				System.out.print("---------");
				String receipt = "\nReceipt number: " + wid + "\n\nService request #"+ rid +"\nClosed by Employee #" + mid + "\nDate of Closure: " + closureDate + "\nComments: "+ comment + "\nAmount Paid: $" + bill + "\n";
				System.out.print(receipt);

				esql.executeUpdate(closeString);
				System.out.print("---------\n");
				System.out.print("SUCCESS\n");
				System.out.print("===================================================\n");
			}

			
				
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/*.List the customers that have paid less than 100 dollars for repairs based on their
	previous service requests.*/

	// TO DO: input validation, beautification

	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		try {		
			String query = "SELECT date,bill,comment FROM Closed_Request WHERE bill < 100";
			System.out.print("===================================================\n");
			System.out.print(" (6) LISTING CUSTOMERS WITH BILL < 100\n");
			System.out.print("===================================================\n");
			esql.executeQueryAndPrintResult(query);
			System.out.print("===================================================\n");
		} catch (Exception e) {
			System.out.println(e.getMessage());		
		}
		
	}
	
	/*Find how many cars each customer has counting from the ownership relation and
	discover who has more than 20 cars.*/

	// TO DO: input validation, beautification

	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		try {		
			String query = "SELECT fname, lname, O.car_num FROM Customer,( SELECT customer_id,COUNT(customer_id) as car_num FROM Owns GROUP BY customer_id HAVING COUNT(customer_id) > 20) AS O WHERE O.customer_id = id;";
			
			System.out.print("===================================================\n");
			System.out.print(" (7) LISTING CUSTOMERS WITH CARS > 20\n");
			System.out.print("===================================================\n");
			esql.executeQueryAndPrintResult(query);
			System.out.print("===================================================\n");
		} catch (Exception e) {
			System.out.println(e.getMessage());		
		}
	}
	
	/*Get the odometer from the service_requests and find all cars before 1995 having less
	than 50000 miles in the odometer*/
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		try {		
			String query = "SELECT DISTINCT make,model, year FROM Car AS C, Service_Request AS S WHERE year < 1995 and S.car_vin = C.vin and S.odometer < 50000";
			
			System.out.print("===================================================\n");	
			System.out.print(" (8) LISTING CARS BEFORE 1995 WITH < 50,000 MILES\n");
			System.out.print("===================================================\n");
						
			esql.executeQueryAndPrintResult(query);
			System.out.print("===================================================\n");
		} catch (Exception e) {
			System.out.println(e.getMessage());		
		}
	}
	
	/*Find for all cars in the database the number of service requests. Return the make,
	model and number of service requests for the cars having the k highest number of
	service requests. The k value should be positive and larger than 0. The user should
	provide this value. Focus on the open service requests.*/

	// TO DO: input validation, beautification

	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		try {		
			String query = "SELECT make, model, R.creq FROM Car AS C, ( SELECT car_vin, COUNT(rid) AS creq FROM Service_Request GROUP BY car_vin ) AS R WHERE R.car_vin = C.vin ORDER BY R.creq DESC LIMIT ";

			System.out.print("===================================================\n");	
			System.out.print(" (9) LISTING CARS WITH MOST SERVICE REQUESTS\n");
			System.out.print("===================================================\n");
			
			String kstring;		
			int k;	
			
			do {
				System.out.print("Please enter limit value, k: ");
				kstring = in.readLine();
				k = Integer.parseInt(kstring);
			} while (false);
			
			query = query + kstring;
			esql.executeQueryAndPrintResult(query);

			System.out.print("===================================================\n");
		} catch (Exception e) {
			System.out.println(e.getMessage());		
		}
	}
	
	/*For all service requests find the aggregate cost per customer and order customers
	according to that cost. List their first, last name and total bill.*/

	// TO DO: beautification

	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//10
		try {		
			String query = "SELECT C.fname , C.lname, Total FROM Customer AS C, (SELECT sr.customer_id, SUM(CR.bill) AS Total FROM Closed_Request AS CR, Service_Request AS SR WHERE CR.rid = SR.rid GROUP BY SR.customer_id) AS A WHERE C.id=A.customer_id ORDER BY A.Total DESC";

			System.out.print("===================================================\n");	
			System.out.print(" (10) LISTING CARS WITH MOST SERVICE REQUESTS\n");
			System.out.print("===================================================\n");

			esql.executeQueryAndPrintResult(query);
			System.out.print("===================================================\n");
		} catch (Exception e) {
			System.out.println(e.getMessage());		
		}
	}
	
}
