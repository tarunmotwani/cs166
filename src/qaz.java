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

//Added libraries
import org.apache.commons.lang.StringUtils;
import java.util.regex.*;
import java.util.Date;
import java.util.*;
import java.text.SimpleDateFormat;

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
			++rowCount;
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
				System.out.println("");
				System.out.println("GUI");
				System.out.println("MAIN MENU");
				System.out.println("------------------------------------------------------------------------");
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
				System.out.println("------------------------------------------------------------------------");
				
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
		int id;
		String fname;
		String lname;
		String phone;
		String address;
		
		try {
			//fname
			System.out.println("Enter Customer First Name");
			String temp = in.readLine();
			while (temp.isEmpty()) {
				temp = in.readLine();
			}
			if (temp.length() > 32) {
				System.out.println("NOTE: Name too large, will be shortened!");
				temp = temp.substring(0,32);
			}
			temp = Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
			fname = temp;
			
			//lname
			System.out.println("Enter Customer Last Name");
			temp = in.readLine();
			while (temp.isEmpty()) {
				temp = in.readLine();
			}
			if (temp.length() > 32) {
				System.out.println("NOTE: Name too large, will be shortened!");
				temp = temp.substring(0,32);
			}
			temp = Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
			lname = temp;
			
			//phone
			System.out.println("Enter Customer Phone Number (Excluding Symbols)");
			temp = in.readLine();
			while (temp.isEmpty()) {
				temp = in.readLine();
			}
			while (!StringUtils.isNumeric(temp) || temp.length() < 10 || temp.length() > 13) {
				System.out.println("Invalid Phone #, Please Re-Enter");
				temp = in.readLine();
			}
			if (temp.length() == 10) {
				temp = "(" + temp.substring(0,3) + ")" + temp.substring(3,6) + "-" + temp.substring(6);
			}
			phone = temp;
			
			//address
			System.out.println("Enter Customer Address (Will be capped to 256 Characters)");
			temp = in.readLine();
			while (temp.isEmpty()) {
				temp = in.readLine();
			}
			/*while (Character.isDigit(temp.charAt(0)) || Character.isDigit(temp.charAt(temp.length() -1))) {
				System.out.println("Invalid Address, Please Re-Enter");
				temp = in.readLine();
			}*/
			if (temp.length() > 256) {
				temp = temp.substring(0, 256);
			}
			address = temp;
			
			//id
			temp = "SELECT * FROM Customer";
			int curr_id = esql.executeQuery(temp);
			id = curr_id;
			
			//add user info into DB
			temp = String.format("INSERT INTO Customer(\"id\",\"fname\",\"lname\",\"phone\",\"address\") VALUES(%d,'%s','%s','%s','%s');",id,fname,lname,phone,address); 
			esql.executeUpdate(temp);
			
			/*//output updated table
			temp = "SELECT * FROM Customer";
			curr_id = esql.executeQueryAndPrintResult(temp);
			id = curr_id;*/
		}
		catch (java.sql.SQLException e) {
			System.out.println("Database Error!");
			System.out.println(e.getMessage());
		}
		catch (java.io.IOException e) {
			System.out.println("Failed to read input!");
		}
	}
	
	public static void AddMechanic(MechanicShop esql){//2
		int id;
		String fname;
		String lname;
		int experience;
		
		try {
			//fname
			System.out.println("Enter Mechanic First Name");
			String temp = in.readLine();
			while (temp.isEmpty()) {
				temp = in.readLine();
			}
			if (temp.length() > 32) {
				System.out.println("NOTE: Name too large, will be shortened!");
				temp = temp.substring(0,32);
			}
			temp = Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
			fname = temp;
			
			//lname
			System.out.println("Enter Mechanic Last Name");
			temp = in.readLine();
			while (temp.isEmpty()) {
				temp = in.readLine();
			}
			if (temp.length() > 32) {
				System.out.println("NOTE: Name too large, will be shortened!");
				temp = temp.substring(0,32);
			}
			temp = Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
			lname = temp;
			
			//experience
			int val = -1;
			System.out.println("Enter Years of Experience");
			temp = in.readLine();
			while (val < 0 || val >= 100) {
				while (temp.isEmpty()) {
					System.out.println("Invalid Years");
					temp = in.readLine();
				}
				if (!StringUtils.isNumeric(temp)){	
					System.out.println("Invalid Years");
					temp = in.readLine();
					continue;
				}	
				if (Integer.parseInt(temp) < 0 || Integer.parseInt(temp) >= 100) {
					System.out.println("Invalid Years");
					temp = in.readLine();
					continue;
				}
				val = Integer.parseInt(temp);
			}
			experience = val;
			
			//id
			temp = "SELECT * FROM Mechanic";
			int curr_id = esql.executeQuery(temp);
			id = curr_id;
			
			//add user info into DB
			temp = String.format("INSERT INTO Mechanic(\"id\",\"fname\",\"lname\",\"experience\") VALUES(%d,'%s','%s','%s');",id,fname,lname,experience); 
			esql.executeUpdate(temp);
			
			/*//output updated table
			temp = "SELECT * FROM Mechanic";
			curr_id = esql.executeQueryAndPrintResult(temp);
			id = curr_id;*/
		}
		catch (java.sql.SQLException e) {
			System.out.println("Database Error!");
			System.out.println(e.getMessage());
		}
		catch (java.io.IOException e) {
			System.out.println("Failed to read input!");
		}
	}
	
	public static void AddCar(MechanicShop esql){//3
		String vin;
		String make;
		String model;
		int year;
		
		try {
			//vin
			//Vin Validation
			String VIN_PATTERN = "^[A-Z0-9]{16}$";
			Pattern vin_pat = Pattern.compile(VIN_PATTERN);
			Matcher matcher;
			
			System.out.println("Enter VIN");
			String temp = in.readLine();
			while (temp.isEmpty()) {
				temp = in.readLine();
			}
			temp = temp.toUpperCase();
			matcher = vin_pat.matcher(temp);
			if (!matcher.matches()) {
				System.out.println("Invalid VIN");
				temp = in.readLine();
				temp = temp.toUpperCase();
				matcher = vin_pat.matcher(temp);
			}
			vin = temp;
			
			//make
			System.out.println("Enter Make");
			temp = in.readLine();
			while (temp.isEmpty()) {
				temp = in.readLine();
			}
			if (temp.length() > 32) {
				System.out.println("NOTE: Make too large, will be shortened!");
				temp = temp.substring(0,32);
			}
			temp = Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
			make = temp;
			
			//model
			System.out.println("Enter Model");
			temp = in.readLine();
			while (temp.isEmpty()) {
				temp = in.readLine();
			}
			if (temp.length() > 32) {
				System.out.println("NOTE: Model too large, will be shortened!");
				temp = temp.substring(0,32);
			}
			temp = Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
			model = temp;			

			//year
			int val = -1;
			System.out.println("Enter Year");
			temp = in.readLine();
			while (val < 1970 || val > 9999) {
				while (temp.isEmpty()) {
					System.out.println("Invalid Year");
					temp = in.readLine();
				}
				if (!StringUtils.isNumeric(temp)){	
					System.out.println("Invalid Year");
					temp = in.readLine();
					continue;
				}
				if (Integer.parseInt(temp) < 1970 || Integer.parseInt(temp) > 9999) {
					System.out.println("Invalid Years");
					temp = in.readLine();
					continue;
				}	
				val = Integer.parseInt(temp);
			}
			year = val;
			
			//add user info into DB
			temp = String.format("INSERT INTO Car(\"vin\",\"make\",\"model\",\"year\") VALUES('%s','%s','%s','%s');",vin,make,model,year); 
			esql.executeUpdate(temp);
			
			/*//output updated table
			temp = "SELECT * FROM Car";
			int rows = esql.executeQueryAndPrintResult(temp);*/
		}
		catch (java.sql.SQLException e) {
			System.out.println("Database Error!");
			System.out.println(e.getMessage());
		}
		catch (java.io.IOException e) {
			System.out.println("Failed to read input!");
		}
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4
		String temp = "";
		
		try {
			System.out.println("Enter lname of Customer");
			while (temp.isEmpty()) {
				temp = in.readLine();
			}
			temp = Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
			//query to grab customers with that lname
			String query = String.format("SELECT fname, lname FROM Customer WHERE lname = '%s';", temp);
			List<List<String>> out = esql.executeQueryAndReturnResult(query);
			//If no results, give user option to add the customer
			if (out.size() < 1) {
				System.out.println("Customer not in DB, Would you like to add Customer? (y/n)");
				temp = in.readLine();
				if (temp.equals("y")) {
					AddCustomer(esql);
				}
			}
			else {
				//lets user select which customer they want, or add a new customer
				System.out.println("Select Customer");
				for (int i =0; i < out.size(); ++i) {
					temp = String.format("%d : %s", i, out.get(i));
					System.out.println(temp);
				}
				temp = "Add new Customer";
				temp = String.format("%d : %s", out.size(), temp);
				System.out.println(temp);
				int val = -1;
				//If user entered a value thats not one of the options
				temp = in.readLine();
				while (val < 0 || val > out.size()) {
					while (temp.isEmpty()) {
						System.out.println("Retry");
						temp = in.readLine();
					}
					if (!StringUtils.isNumeric(temp)){	
						System.out.println("Retry");
						temp = in.readLine();
						continue;
					}
					if (Integer.parseInt(temp) < 0 || Integer.parseInt(temp) > out.size()) {
						System.out.println("Invalid Years");
						temp = in.readLine();
						continue;
					}				
					val = Integer.parseInt(temp);
				}
				//if user chose to add a new customer
				if (val == out.size()) {
					AddCustomer(esql);
				}
				//if user chose a customer
				else {
					String fname = out.get(val).get(0);
					String lname = out.get(val).get(1);
					//get selected customer's unique id
					query = String.format("SELECT Customer.id FROM Customer WHERE fname = '%s' AND lname = '%s';", fname, lname); 
					out = esql.executeQueryAndReturnResult(query);
					int cust_id = Integer.parseInt(out.get(0).get(0));	
					//use id to find the cars that belong to customer
					query = String.format("SELECT Car.vin, Car.make, Car.model, Car.year FROM Customer, Owns, Car WHERE %d = Customer.id AND Customer.id = Owns.customer_id AND Owns.car_vin = Car.vin;", cust_id);
					out = esql.executeQueryAndReturnResult(query);					
										
					//lets user select which car they want, or add a new car
					System.out.println("Select Car");
					for (int i =0; i < out.size(); ++i) {
						temp = String.format("%d : %s", i, out.get(i));
						System.out.println(temp);
					}
					temp = "Add new Car";
					temp = String.format("%d : %s", out.size(), temp);
					System.out.println(temp);
					val = -1;
					//If user entered a value thats not one of the options
					temp = in.readLine();
					while (val < 0 || val > out.size()) {
						while (temp.isEmpty()) {
							System.out.println("Retry");
							temp = in.readLine();
						}
						if (!StringUtils.isNumeric(temp)){	
							System.out.println("Retry");
							temp = in.readLine();
							continue;
						}	
						if (Integer.parseInt(temp) < 0 || Integer.parseInt(temp) > out.size()) {
							System.out.println("Invalid Years");
							temp = in.readLine();
							continue;
						}
						val = Integer.parseInt(temp);
					}
					//if user chose to add a new car
					if (val == out.size()) {
						AddCar(esql);
						query = String.format("SELECT * FROM Car");
						//System.out.println("Before doing query");
						out = esql.executeQueryAndReturnResult(query);
						//System.out.println("After doing query");
						val = out.size() - 1;
						//add car to owns table
						int own_id = esql.executeQuery("SELECT * FROM Owns;");
						temp = String.format("INSERT INTO Owns(\"ownership_id\",\"customer_id\",\"car_vin\") VALUES(%d,%d,'%s');", own_id, cust_id, out.get(val).get(0));
						esql.executeUpdate(temp);
					}
					//adds the service request into the DB					
					String vin = out.get(val).get(0);
					String date = new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance().getTime());
					String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
					date = date + " " + timeStamp;
					System.out.println("Enter Odometer Value");
					int odometer = -1;
					String temp_int = "";
					temp_int = in.readLine();
					while (odometer < 0) {
						while (temp_int.isEmpty()) {
							System.out.println("Retry");
							temp_int = in.readLine();
						}
						if (!StringUtils.isNumeric(temp_int)){	
							System.out.println("Retry");
							temp_int = in.readLine();
							continue;
						}	
						if (Integer.parseInt(temp_int) < 0) {
							System.out.println("Invalid Years");
							temp = in.readLine();
							continue;
						}	
						odometer = Integer.parseInt(temp_int);
					}

					System.out.println("Enter Your Complaint (Don't Include Quotes)");
					String complain = in.readLine();
					query = String.format("SELECT * FROM Service_Request;");
					int rid = esql.executeQuery(query);
					
					//add the created service request to the DB
					query = String.format("INSERT INTO Service_Request(\"rid\",\"customer_id\",\"car_vin\",\"date\",\"odometer\",\"complain\") VALUES(%d,%d,'%s','%s',%d,'%s');",rid,cust_id,vin,date,odometer,complain); 
					esql.executeUpdate(query);
					
					/*//print new table
					query = String.format("SELECT * FROM Service_Request;");
					esql.executeQueryAndPrintResult(query);*/
						
				}
			}
		}
		catch (java.sql.SQLException e) {
			System.out.println("Database Error!");
			System.out.println(e.getMessage());
		}
		catch (java.io.IOException e) {
			System.out.println("Failed to read input!");
		}
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		int mid = -1;
		int rid = -1;
		String date = new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance().getTime());
		String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
		date = date + " " + timeStamp;
		String query;
		List<List<String>> out;
		
		System.out.println("Enter Employee ID");
		String temp = in.readLine();
		while (mid < 0) {
			while (temp.isEmpty()) {
				System.out.println("Retry");
				temp = in.readLine();
			}
			if (!StringUtils.isNumeric(temp)){	
				System.out.println("Retry");
				temp = in.readLine();
				continue;
			}
			if (Integer.parseInt(temp) < 0) {
				System.out.println("Invalid Years");
				temp = in.readLine();
				continue;
			}
			mid = Integer.parseInt(temp);
			//check if entered id is valid
			query = String.format("SELECT * FROM Mechanic WHERE %d = id;", mid);
			out = esql.executeQueryAndReturnResult(query);
			//if mechanic doesn't exist continue loop
			if (out.size() < 1) {
				System.out.println("Mechanic With that ID doesn't Exist");
				temp = "";
				mid = -1;
			}
		}
		System.out.println("Enter Service Request ID");
		temp = in.readLine();
		while (rid < 0) {
			while (temp.isEmpty()) {
				System.out.println("Retry");
				temp = in.readLine();
			}
			if (!StringUtils.isNumeric(temp)){	
				System.out.println("Retry");
				temp = in.readLine();
				continue;
			}
			if (Integer.parseInt(temp) < 0) {
				System.out.println("Invalid Years");
				temp = in.readLine();
				continue;
			}
			rid = Integer.parseInt(temp);
			//check if entered id is valid
			query = String.format("SELECT * FROM Service_Request WHERE %d = rid;", rid);
			out = esql.executeQueryAndReturnResult(query);
			//if request doesn't exist continue loop
			if (out.size() < 1) {
				System.out.println("Service_Request With that ID doesn't Exist");
				temp = "";
				rid = -1;
			}
			//check if the service request hasnt been closed already
			query = String.format("SELECT * FROM Closed_Request WHERE %d = rid;", rid);
			out = esql.executeQueryAndReturnResult(query);
			if (out.size() > 0) {
				System.out.println("Service_Request With that ID has already been closed!");
				temp = "";
				rid = -1;
			}
		}
		System.out.println("Enter Any Comments");
		String comment = in.readLine();
		System.out.println("Enter Bill");
		int bill = -1;
		temp = in.readLine();
		while (bill < 0) {
			while (temp.isEmpty()) {
				System.out.println("Invalid Bill Amount");
				temp = in.readLine();
			}
			if (!StringUtils.isNumeric(temp)){	
				System.out.println("Invalid Bill Amount");
				temp = in.readLine();
				continue;
			}
			if (Integer.parseInt(temp) < 0) {
				System.out.println("Invalid Years");
				temp = in.readLine();
				continue;
			}
			bill = Integer.parseInt(temp);
		}
		//find next wid
		query = String.format("SELECT wid FROM Closed_Request ORDER BY wid DESC LIMIT 1;");
		out = esql.executeQueryAndReturnResult(query);
		int wid = Integer.parseInt(out.get(0).get(0)) + 1;
		
		query = String.format("INSERT INTO Closed_Request(\"wid\",\"rid\",\"mid\",\"date\",\"comment\",\"bill\") VALUES(%d,%d,%d,'%s','%s',%d);", wid, rid, mid, date, comment, bill);
		esql.executeUpdate(query);
		
		/*//output table
		query = String.format("SELECT * FROM Closed_Request;");
		esql.executeQueryAndPrintResult(query);*/
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		try {
			String query = String.format("SELECT Customer.fname, Customer.lname, Closed_Request.date, Closed_Request.comment, Closed_Request.bill FROM Customer, Service_Request, Closed_Request WHERE Customer.id = customer_id AND Service_Request.rid = Closed_Request.rid AND bill < 100 ORDER BY date;");
			List<List<String>> out = esql.executeQueryAndReturnResult(query);
			String res = "";
			for (int i = 0; i < out.size(); ++i) {
				res += "\nName: " + out.get(i).get(0) + out.get(i).get(1);
				res += "\nDate Closed: " + out.get(i).get(2);
				res += "\nComment: " + out.get(i).get(3);
				res += "\nBill: $" + out.get(i).get(4);
				res += "\n";
			}
			System.out.println(res);
			int total = out.size();
			String output = String.format("Total Results: %d", total);
			System.out.println(output);
		}
		catch (java.sql.SQLException e) {
			System.out.println("Database Error!");
			System.out.println(e.getMessage());
		}
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		try {
			String query = String.format("SELECT fname, lname, COUNT(*) FROM Customer, Owns, Car WHERE id = customer_id AND car_vin = vin GROUP BY id HAVING Count(*) > 20;");
			List<List<String>> out = esql.executeQueryAndReturnResult(query);
			String res = "";
			for (int i = 0; i < out.size(); ++i) {
				res += "\nName: " + out.get(i).get(0) + out.get(i).get(1);
				res += "\n";
			}
			System.out.println(res);
			int total = out.size();
			String output = String.format("Total Results: %d", total);
			System.out.println(output);
		}
		catch (java.sql.SQLException e) {
			System.out.println("Database Error!");
			System.out.println(e.getMessage());
		}
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		try {
			String query = String.format("SELECT DISTINCT make, model, year FROM Car, Service_Request WHERE vin = car_vin AND year < 1995 AND odometer < 50000 ORDER BY year;");
			List<List<String>> out = esql.executeQueryAndReturnResult(query);
			String res = "";
			for (int i = 0; i < out.size(); ++i) {
				res += "\nMake: " + out.get(i).get(0);
				res += "\nModel: " + out.get(i).get(1);
				res += "\nYear: " + out.get(i).get(2);
				res += "\n";
			}
			System.out.println(res);
			int total = out.size();
			String output = String.format("Total Results: %d", total);
			System.out.println(output);
		}
		catch (java.sql.SQLException e) {
			System.out.println("Database Error!");
			System.out.println(e.getMessage());
		}
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
		try {
			int k = -1;
			System.out.println("Enter K (K>0)");
			String temp = in.readLine();
			while (k < 1) {
				while (temp.isEmpty()) {
					System.out.println("Invalid K");
					temp = in.readLine();
				}
				if (!StringUtils.isNumeric(temp)){	
					System.out.println("Invalid K");
					temp = in.readLine();
					continue;
				}
				k = Integer.parseInt(temp);
			}	
			
			String query = String.format("SELECT make,model,COUNT(*) FROM Car, Service_Request WHERE vin = car_vin GROUP BY vin ORDER BY COUNT(*) DESC LIMIT %d;", k);
			List<List<String>> out = esql.executeQueryAndReturnResult(query);
			String res = "";
			for (int i = 0; i < out.size(); ++i) {
				res += "\nMake: " + out.get(i).get(0);
				res += "\nModel: " + out.get(i).get(1);
				res += "\nNumberOfSR's: " + out.get(i).get(2);
				res += "\n";
			}
			System.out.println(res);
			int total = out.size();
			String output = String.format("Total Results: %d", total);
			System.out.println(output);
		}
		catch (java.sql.SQLException e) {
			System.out.println("Database Error!");
			System.out.println(e.getMessage());
		}
		catch (java.io.IOException e) {
			System.out.println("Failed to read input!");
		}
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//10
		//
		try {
			String query = String.format("SELECT fname, lname, SUM(bill) FROM Customer, Service_Request, Closed_Request WHERE id = customer_id AND Service_Request.rid = Closed_Request.rid GROUP BY id ORDER BY SUM(bill) DESC;");
			List<List<String>> out = esql.executeQueryAndReturnResult(query);
			String res = "";
			for (int i = 0; i < out.size(); ++i) {
				res += "\nFName: " + out.get(i).get(0);
				res += "\nLName: " + out.get(i).get(1);
				res += "\nSpent: " + out.get(i).get(2);
				res += "\n";
			}
			System.out.println(res);
			int total = out.size();
			String output = String.format("Total Results: %d", total);
			System.out.println(output);
		}
		catch (java.sql.SQLException e) {
			System.out.println("Database Error!");
			System.out.println(e.getMessage());
		}		
	}
}
