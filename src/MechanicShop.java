
/*
 * vallate JAVA User Interface
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
import java.util.Scanner;
//added functions
import java.util.regex.*;
import java.util.Date;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop {
	// reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try {
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println("Connection URL: " + url + "\n");

			// obtain a physical connection
			this._connection = DriverManager.getConnection(url, user, passwd);
			System.out.println("Done");
		} catch (Exception e) {
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
			System.out.println("Make sure you started postgres on this machine");
			System.exit(-1);
		}
	}

	/**
	 * Method to execute an update SQL statement. Update SQL instructions includes
	 * CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 */
	public void executeUpdate(String sql) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement();

		// issues the update instruction
		stmt.executeUpdate(sql);

		// close the instruction
		stmt.close();
	}// end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT). This method
	 * issues the query to the DBMS and outputs the results to standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult(String query) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery(query);

		/*
		 * obtains the metadata object for the returned result set. The metadata
		 * contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData();
		int numCol = rsmd.getColumnCount();
		int rowCount = 0;

		// iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()) {
			if (outputHeader) {
				for (int i = 1; i <= numCol; i++) {
					System.out.print(rsmd.getColumnName(i) + "\t");
				}
				System.out.println();
				outputHeader = false;
			}
			for (int i = 1; i <= numCol; ++i)
				System.out.print(rs.getString(i) + "\t");
			System.out.println();
			++rowCount;
		} // end while
		stmt.close();
		return rowCount;
	}

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT). This method
	 * issues the query to the DBMS and returns the results as a list of records.
	 * Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery(query);

		/*
		 * obtains the metadata object for the returned result set. The metadata
		 * contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData();
		int numCol = rsmd.getColumnCount();
		int rowCount = 0;

		// iterates through the result set and saves the data returned by the query.
		boolean outputHeader = false;
		List<List<String>> result = new ArrayList<List<String>>();
		while (rs.next()) {
			List<String> record = new ArrayList<String>();
			for (int i = 1; i <= numCol; ++i)
				record.add(rs.getString(i));
			result.add(record);
		} // end while
		stmt.close();
		return result;
	}// end executeQueryAndReturnResult

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT). This method
	 * issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery(String query) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery(query);

		int rowCount = 0;

		// iterates through the result set and count nuber of results.
		if (rs.next()) {
			rowCount++;
		} // end while
		stmt.close();
		return rowCount;
	}

	/**
	 * Method to fetch the last value from sequence. This method issues the query to
	 * the DBMS and returns the current value of sequence used for autogenerated
	 * keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */

	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement();

		ResultSet rs = stmt.executeQuery(String.format("Select currval('%s')", sequence));
		if (rs.next())
			return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup() {
		try {
			if (this._connection != null) {
				this._connection.close();
			} // end if
		} catch (SQLException e) {
			// ignored.
		} // end try
	}// end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login
	 *             file>
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName()
					+ " <dbname> <port> <user>");
			return;
		} // end if

		MechanicShop esql = null;

		try {
			System.out.println("(1)");

			try {
				Class.forName("org.postgresql.Driver");
			} catch (Exception e) {

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}

			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];

			esql = new MechanicShop(dbname, dbport, user, "");

			boolean keepon = true;
			while (keepon) {
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
				switch (readChoice()) {
				case 1:
					AddCustomer(esql);
					break;
				case 2:
					AddMechanic(esql);
					break;
				case 3:
					AddCar(esql);
					break;
				case 4:
					InsertServiceRequest(esql);
					break;
				case 5:
					CloseServiceRequest(esql);
					break;
				case 6:
					ListCustomersWithBillLessThan100(esql);
					break;
				case 7:
					ListCustomersWithMoreThan20Cars(esql);
					break;
				case 8:
					ListCarsBefore1995With50000Milles(esql);
					break;
				case 9:
					ListKCarsWithTheMostServices(esql);
					break;
				case 10:
					ListCustomersInDescendingOrderOfTheirTotalBill(esql);
					break;
				case 11:
					keepon = false;
					break;
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			try {
				if (esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup();
					System.out.println("Done\n\nBye !");
				} // end if
			} catch (Exception e) {
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
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		return input;
	}// end readChoice

	public static void AddCustomer(MechanicShop esql) {// 1
		int id;
		String val;
		int customerID = 0;
		try {
			String ry = "SELECT count(*) FROM Customer";
			List<List<String>> new_id = esql.executeQueryAndReturnResult(ry);
			customerID = Integer.valueOf(new_id.get(0).get(0)) + 1;
			String query = "Insert INTO Customer (id, fname, lname, phone, address) VALUES (";
			Scanner s = new Scanner(System.in);
		
			System.out.print("\tEnter firstname: ");
			String firstname = in.readLine();
			while(firstname.isEmpty()) { firstname = in.readLine();}
			if (firstname.length() > 32){ System.out.print("\n Too many characters! Will be shortened\n");firstname = firstname.substring(0,31);}
			
			System.out.print("\tEnter lastname: ");
			String lastname = in.readLine();
			while(lastname.isEmpty()) {lastname = in.readLine();}
			if( lastname.length() > 32){ System.out.print("\n Too many characters! Will be shortened\n");lastname = lastname.substring(0,31);}

			System.out.print("\tEnter Phone Number: ");
			String phone = in.readLine();
			while(phone.isEmpty()){phone = in.readLine();}
			while(phone.length() < 10 || phone.length() > 13){ System.out.print("\n Invalid Phone # please re-enter: "); phone = in.readLine();}
			
			System.out.print("\tEnter Address: ");
			String address = in.readLine();
			while(address.isEmpty()){address = in.readLine();}
			if( address.length() > 32){ System.out.print("\n Too many characters! Will be shortened\n");address = address.substring(0,31);}
			
			query += customerID + ", '" + firstname + "', '" + lastname + "', '" + phone + "', '" + address + "')";
			esql.executeUpdate(query);

		} 
		catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	public static void AddMechanic(MechanicShop esql) {// 2
		int id = 0;
		int customerID = 0;
		String val;
		try {
			String ry = "SELECT Count(*) FROM Mechanic";
			List<List<String>> new_id = esql.executeQueryAndReturnResult(ry);
			customerID = Integer.valueOf(new_id.get(0).get(0)) + 1;

			String query = "Insert INTO Mechanic (id,fname, lname, experience) VALUES (";
			Scanner s = new Scanner(System.in);
			
			System.out.print("\tEnter first name: ");
			String firstname = in.readLine();
			while(firstname.isEmpty()) { firstname = in.readLine();}
			if (firstname.length() > 32){ System.out.print("\n Too many characters! Will be shortened\n");firstname = firstname.substring(0,31);}
			
			System.out.print("\tEnter last name: ");
			String lastname = in.readLine();
			while(lastname.isEmpty()) {lastname = in.readLine();}
			if( lastname.length() > 32){ System.out.print("\n Too many characters! Will be shortened\n");lastname = lastname.substring(0,31);}

			System.out.print("\tEnter experience in years: ");
			String experience = in.readLine();
			while(experience.isEmpty()){experience = in.readLine();}
			while( experience.length() > 2){ System.out.print("\n Invalid Experience! Please RE enter:");experience = in.readLine();}
			
			query += customerID + ", '"+ firstname + "', '" + lastname + "', '" + experience + "')";
			esql.executeUpdate(query);

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void AddCar(MechanicShop esql) {// 3
		int id = 0;
		int customerID = 0;
		String val;
		try {
			
			String query = "Insert INTO Car (vin, make, model, year) VALUES (";
			Scanner s = new Scanner(System.in);
		

			System.out.print("\tEnter vin number: ");
			String vin = in.readLine();
			while(vin.isEmpty()){vin = in.readLine();}
			while( vin.length() > 18 ) { System.out.print("\n Invalid VIN # please re-enter: "); vin = in.readLine();}
			
			
			System.out.print("\tEnter make: ");
			String make = in.readLine();
			while(make.isEmpty()) {make = in.readLine();}
			if( make.length() > 32){ System.out.print("\n Too many characters! Will be shortened\n");make = make.substring(0,31);}

			System.out.print("\tEnter model: ");
			String model = in.readLine();
			while(model.isEmpty()) { vin = in.readLine();}
			if (model.length() >32){ System.out.print("\n Too many characters! Will be shortened\n");model = model.substring(0,31);}
			
			
			System.out.print("\tEnter year: ");
			String year = in.readLine();
			while(year.isEmpty()){year = in.readLine();}
			while( year.length() != 4 ) { System.out.print("\n Invalid Year Entry, please re-enter: "); year = in.readLine();}
			
			query += vin + ", '"+ make + "', '" + model + "', " + year + ")";
			esql.executeUpdate(query);

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void InsertServiceRequest(MechanicShop esql) {// 4
			String query = "INSERT INTO Service_Request (rid,customer_id,car_vin,date,odometer,comment) VALUES (";
			Integer rid = 0;
			Integer customer_id = 0;
			Integer id = 0;
			Integer odo = 0;
			Integer carinput = 0; 
			Integer current = 0;
			List<List<String>> id_array; String val; Integer carNum = 0;
			List<List<String>> rid_array; String comment; String lastname; String input;
			String carQuery; String ownership; String vin; String queryID; String cid;
			List<List<String>> a; //a is the sql query result for customer
			List<List<String>> c; //c is the sql query result for car
			Integer i = 0, j = 0, maxVal = 32;
			String date = new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance().getTime());
			String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
		try {
			//search customer by entering last name
			System.out.print("\nSearch customer by entering last name: ");
			lastname = in.readLine();
			//error checking lastname for less than 32 and empty
			if (lastname.isEmpty() ) {if(lastname.length() > maxVal){while (lastname.isEmpty() || (lastname.length() > maxVal)) {System.out.println("Error: Invalid Input!");System.out.print("\nSearch customer by entering last name: ");lastname = in.readLine();}}}
			//query to get current rid - finding the highest rid + 1
			id_array = esql.executeQueryAndReturnResult("SELECT rid FROM Service_Request ORDER BY rid DESC LIMIT 1;");
			rid = Integer.parseInt(id_array.get(0).get(0)); rid += 1;
			//query to search lastname
			String query_lname = "SELECT * FROM Customer WHERE lname = '" + lastname + "';";
			a = esql.executeQueryAndReturnResult(query_lname);
			//find if there is data in the db
			if (a.size() == 0) {
				System.out.println("Not Found!");
				current = 0;
			} 
			//multiple display case
			if (a.size() > 1) {
				while (i.equals(a.size()) == false) {
					display(esql,a, i);
					++i;
				}
			} 
			//one display case
			else {
				// System.out.print("woohoo");
				display(esql,a, 0);
				current = 0;
			}
			//rego prompt
			System.out.print("\n Would you like to register (y/n):");
			input = in.readLine();
			input = input.toLowerCase();
			//check input
			// System.out.print(!input.equals("y"));
			while (!input.equals("y") && !input.equals("n")) {
				System.out.println("Error: invalid input\n");
				System.out.print("\n Would you like to register (y/n) ");
				input = in.readLine();
			}
			//new registration case
			if (input.equals("y")) {
				//adding new customer manually
				AddCustomer(esql);
				//next, we check if the customer was added successfully
				System.out.print("\nSearch customer by entering last name: ");
				lastname = in.readLine();
				//value checking if empty or too long
				while (lastname.length() == 0 || (lastname.length() > maxVal)) {
					System.out.println("Error: Invalid Input!");
					System.out.print("\nSearch customer by entering last name: ");
					lastname = in.readLine();
				}//querying the lastname to check success
				query_lname = "SELECT * FROM Customer WHERE lname = '"+ lastname + "';";
				a = esql.executeQueryAndReturnResult(query_lname);
				///////////////////////////////////////////////////////////////////////////////////
				if (a.size() == 0) {
				System.out.println("Not Found!");
				current = 0;
			} 
			//multiple display case
			if (a.size() > 1) {
				while (i.equals(a.size()) == false) {
					display(esql,a, i);
					++i;
				}
			} 
			//one display case
			else {
				// System.out.print("woohoo");
				display(esql,a, 0);
				current = 0;
			}
				
			} 
				if(a.size() >= 1) {
					System.out.print("Choose Customer (1..): ");
					val = in.readLine();
					current = Integer.parseInt(val) - 1;
					if(current > a.size() || val.isEmpty()) {
						System.out.println("invalid input!");
						}
				}
			//this query runs to return the details on the owner's car
			carQuery = "SELECT C.vin, C.make, C.model, C.year FROM Car C, Customer Cust, Owns O WHERE Cust.id = O.customer_id AND C.vin = O.car_vin AND Cust.id = " + a.get(current).get(0) + ";";
			c = esql.executeQueryAndReturnResult(carQuery);
			if (c.size() > 0) {
				while (j < c.size()) {
					displayCar(esql, c, j);
					++j;
				}
			} 
			else { 		System.out.println("\nNo cars found...\n\n");			} ////if no query, then we say no cars found
			System.out.print("Add a new car? (y/n): ");
			input = (in.readLine()).toLowerCase();//parse input
			System.out.print("\n");		
			//add new car option
			while (!input.equals("y") && !input.equals("n")) {	//if input is not 1 length
				System.out.println("\nError invalid input\n");
				System.out.print("Add a new car? (y/n): ");
				input = in.readLine();
				}
			
			if (input.equals("y")) {			//add new car case
				i = 0;
				AddCar(esql);
				queryID = "SELECT Owns.ownership_id FROM Owns ORDER BY Owns.ownership_id DESC LIMIT 1";
				//selecting owners database and finding the most current ownership id
				id_array = esql.executeQueryAndReturnResult(queryID);
				id = Integer.parseInt(id_array.get(0).get(0)) + 1;
				cid = a.get(current).get(0);
				System.out.print("Enter VIN again:");
				vin = in.readLine();
				///inserting new car into ownership table using customer id ownership id and car vin
				ownership = "INSERT INTO Owns (ownership_id, customer_id, car_vin) VALUES (" + id + ","+ cid +", "+ vin + ");"; //+ ",'" + vin + "');";
				esql.executeUpdate(ownership);
				System.out.println("\n Updated Customer's Car to Owner's Table.");
				//completed ownership update
				c = esql.executeQueryAndReturnResult(carQuery);
				//query the owned cars based on vin
				if(c.size() == 0) {		System.out.println("\nCustomer has no cars registered");		}//check if there are cars registered
				else{
					while (i != c.size()) {
						displayCar(esql, c, i);	///display function
						++i;
					}
				} 
				System.out.print("Choose the vehicle to add Service Request to): ");
				val = in.readLine();
				//taking input for the service request
				if(val.length() != 1){System.out.println("Error invalid input\n");}
				carinput = Integer.parseInt(val) - 1;
				
			} 
			else if(input.equals("n")) { //no case
				System.out.print("Choose the vehicle to add Service Request to): ");
				val = in.readLine();				//taking the value of the vehicle
				if(val.length() != 1){System.out.println("Error invalid input\n");}
				carinput = Integer.parseInt(val) - 1;//save it into val of carinput
			}
			//imported date and timestamp
			vin = c.get(carinput).get(0);
			
			date = date + " " + timeStamp;
			//concatenating date and timestamp
		
			System.out.print("Enter current milage of your car: ");				///taking the odometer reading
			odo = Integer.parseInt(in.readLine());
			while (odo <= 0){											//checking odometer reading for positivity
				System.out.println("\nError invalid input\n");
				//reprompting the user for the new odometer reading
				System.out.print("\nEnter odometer reading: ");
				odo = Integer.parseInt(in.readLine());
			}
			
			System.out.print("\nEnter customer comment: ");				//taking the customer comment
				comment = in.readLine();			
			while (comment.isEmpty()){									//checking the comment section for text
				comment = in.readLine();
			};
			//last insert query in order to submit a service request
			query = "INSERT INTO Service_Request (rid, customer_id, car_vin, date, odometer, complain) VALUES (" + rid + "," + a.get(current).get(0) + ",'" + vin + "','" + date + "'," + odo + ",'" + comment + "');";
			esql.executeUpdate(query);
			//success
			System.out.println("Finished Insertion of Request!\n\n\n We'll call you back shortly to schedule your appointment!\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		} 
		catch (Exception e) {
			System.err.println(e.getMessage());
			System.out.println("DB error!");

		}

	}


	public static void display(MechanicShop esql, List<List<String>> output, Integer i) {
		try {
			System.out.println("Customer:"+ (i+1)+"\nFirst Name: " + output.get(i).get(1)+"\nLast Name: " + output.get(i).get(2)+"\nPhone: " + output.get(i).get(3)+"\nAddress: " + output.get(i).get(4)+"\n");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.out.println("DB error!");

		}
	}
	
	public static void displayCar(MechanicShop esql, List<List<String>> output, Integer i) {
		try {
			System.out.println("Customer\n"+(i+1)+"\nVin:"+ output.get(i).get(0)+"\nMake: " + output.get(i).get(1)+"\nModel: " + output.get(i).get(2)+ "\nYear: " + output.get(i).get(3) +"\n");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.out.println("DB error!");

		}
	}

	public static void CloseServiceRequest(MechanicShop esql) throws Exception {// 5
		int id = -1, rid = -1,wid = -1, bill = -1, n = 0;
		String query;
		List<List<String>> output;
		String date = new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance().getTime());
		String timeStamp = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());

		try{
			System.out.println("Enter Mechanic ID: ");
			String val = in.readLine();
			//checkign if the id of the mechanic is a positive number
			while (id < 0) {
				id = Integer.parseInt(val);	
				query = String.format("SELECT * FROM Mechanic WHERE %d = id;", id); // where id is not equal to 
				output = esql.executeQueryAndReturnResult(query); 
			}
			System.out.println("Enter Service Request ID: ");
			val = in.readLine();
			rid = Integer.parseInt(val);//update service request id
				
			//prompt mechanic to make comments about the service
			System.out.println("Enter Comments");
			String comment = in.readLine();
			//prompt mechanic to add a bill of service amount to the closing request
			System.out.println("Enter Amount Bill of Service: ");
			val = in.readLine();
			bill = Integer.parseInt(val);
			//parse string to integer
			query = String.format("SELECT wid FROM Closed_Request ORDER BY wid DESC LIMIT 1;");
			output = esql.executeQueryAndReturnResult(query);
			//find the maximum wid
			n = Integer.parseInt(output.get(0).get(0));
			wid = n + 1;
			date = date + " " + timeStamp;
			//import date of the timestamp piece
			//wid+1 to find the next value
			query = String.format("INSERT INTO Closed_Request(\"wid\",\"rid\",\"mid\",\"date\",\"comment\",\"bill\") VALUES(%d,%d,%d,'%s','%s',%d);", wid, rid, id, date, comment, bill);
			esql.executeUpdate(query); ///update the closed request
			//success
			System.out.println("\n\n\n\n Thank You For Your Business. We have successfully closed your request for repairs to your vehicle. Come back again!\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		}
		catch(Exception e) {
			System.err.println(e.getMessage());
			System.out.println("DB error");

		}
	}

	public static void ListCustomersWithBillLessThan100(MechanicShop esql) {// 6
		try {
			String query = "SELECT fname, lname, Closed_Request.date, comment, bill FROM Closed_Request, Customer C, Service_Request S WHERE S.customer_id = C.id AND Closed_Request.rid = S.rid AND bill < 100";
			List<List<String>> answer = esql.executeQueryAndReturnResult(query);
			for (int i = 0; i < answer.size(); ++i) {
				System.out.println(answer.get(i));
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql) {// 7
		try {
			String query = "SELECT C.fname, O.customer_id, COUNT(DISTINCT O.car_vin) as vin_count FROM Owns O, Customer C WHERE O.customer_id = C.id GROUP BY O.customer_id, C.fname HAVING COUNT(DISTINCT O.car_vin) > 20";
			List<List<String>> answer = esql.executeQueryAndReturnResult(query);
			for (int i = 0; i < answer.size(); ++i) {
				System.out.println(answer.get(i));
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ListCarsBefore1995With50000Milles(MechanicShop esql) {// 8
		try {
			String query = "SELECT C.make, C.model, C.year, S.odometer FROM Service_Request S, Car C WHERE C.vin = S.car_vin AND C.year < 1995 and S.odometer > 50000";
			List<List<String>> answer = esql.executeQueryAndReturnResult(query);
			for (int i = 0; i < answer.size(); ++i) {
				System.out.println(answer.get(i));
			}
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ListKCarsWithTheMostServices(MechanicShop esql) {// 9
		//
		try {
			int k;
			System.out.println("Select number of Cars you wanna output");
			k = Integer.parseInt(in.readLine());

			String query = "SELECT C.make, C.model, COUNT(*) FROM Service_Request S, Car C WHERE C.vin = S.car_vin GROUP BY S.car_vin, C.make, C.model ORDER BY COUNT(*) DESC";
			List<List<String>> answer = esql.executeQueryAndReturnResult(query);
			for (int i = 0; i < k; ++i) {
				System.out.println(answer.get(i));
			}
			System.out.println("The resulting query is of length"+answer.size());
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql) {// 10
		//
		try {
			String query = "SELECT C.fname, C.lname, sum(R.bill) FROM Closed_Request R, Customer C, Service_Request S WHERE C.id = S.customer_id AND R.rid = S.rid GROUP BY S.customer_id, C.fname, C.lname ORDER BY sum(R.bill) DESC";
			List<List<String>> answer = esql.executeQueryAndReturnResult(query);
			for (int i = 0; i < answer.size(); ++i) {
				System.out.println(answer.get(i));
			}
			System.out.println("The resulting query is of length  "+answer.size());
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}