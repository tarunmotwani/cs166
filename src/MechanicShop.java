
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
import java.util.Scanner;

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
		String temp;
		int customerID = 0;
		try {
			String ry = "SELECT count(*) FROM Customer";
			List<List<String>> new_id = esql.executeQueryAndReturnResult(ry);
			customerID = Integer.valueOf(new_id.get(0).get(0)) + 1;
			String query = "Insert INTO Customer (id, fname, lname, phone, address) VALUES (";
			Scanner s = new Scanner(System.in);
		
			System.out.print("\tEnter fname: ");
			String fname = in.readLine();
			while(fname.isEmpty()) { fname = in.readLine();}
			if (fname.length() > 32){ System.out.print("\n Too many characters! Will be shortened\n");fname = fname.substring(0,31);}
			
			System.out.print("\tEnter lname: ");
			String lname = in.readLine();
			while(lname.isEmpty()) {lname = in.readLine();}
			if( lname.length() > 32){ System.out.print("\n Too many characters! Will be shortened\n");lname = lname.substring(0,31);}

			System.out.print("\tEnter Phone Number: ");
			String phone = in.readLine();
			while(phone.isEmpty()){phone = in.readLine();}
			while(phone.length() < 10 || phone.length() > 13){ System.out.print("\n Invalid Phone # please re-enter: "); phone = in.readLine();}
			
			System.out.print("\tEnter Address: ");
			String address = in.readLine();
			while(address.isEmpty()){address = in.readLine();}
			if( address.length() > 32){ System.out.print("\n Too many characters! Will be shortened\n");address = address.substring(0,31);}
			
			query += customerID + ", '" + fname + "', '" + lname + "', '" + phone + "', '" + address + "')";
			esql.executeUpdate(query);

		} 
		catch (Exception e) {
			System.err.println(e.getMessage());
		}

	}

	public static void AddMechanic(MechanicShop esql) {// 2
		int id = 0;
		int customerID = 0;
		String temp;
		try {
			String ry = "SELECT Count(*) FROM Mechanic";
			List<List<String>> new_id = esql.executeQueryAndReturnResult(ry);
			customerID = Integer.valueOf(new_id.get(0).get(0)) + 1;

			String query = "Insert INTO Mechanic (id,fname, lname, experience) VALUES (";
			Scanner s = new Scanner(System.in);
			
			System.out.print("\tEnter first name: ");
			String fname = in.readLine();
			while(fname.isEmpty()) { fname = in.readLine();}
			if (fname.length() > 32){ System.out.print("\n Too many characters! Will be shortened\n");fname = fname.substring(0,31);}
			
			System.out.print("\tEnter last name: ");
			String lname = in.readLine();
			while(lname.isEmpty()) {lname = in.readLine();}
			if( lname.length() > 32){ System.out.print("\n Too many characters! Will be shortened\n");lname = lname.substring(0,31);}

			System.out.print("\tEnter experience in years: ");
			String experience = in.readLine();
			while(experience.isEmpty()){experience = in.readLine();}
			while( experience.length() > 2){ System.out.print("\n Invalid Experience! Please RE enter:");experience = in.readLine();}
			
			query += customerID + ", '"+ fname + "', '" + lname + "', '" + experience + "')";
			esql.executeUpdate(query);

		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public static void AddCar(MechanicShop esql) 
	{// 3
		int id = 0;
		int customerID = 0;
		String temp;
		try {
			
			String query = "Insert INTO Car (vin, make, model, year) VALUES (";
			Scanner s = new Scanner(System.in);
		

			System.out.print("\tEnter vin number: ");
			String vin = in.readLine();
			while(vin.isEmpty()){vin = in.readLine();}
			while( vin.length() != 17 ) { System.out.print("\n Invalid VIN # please re-enter: "); vin = in.readLine();}
			
			
			System.out.print("\tEnter make: ");
			String make = in.readLine();
			while(make.isEmpty()) {make = in.readLine();}
			if( make.length() > 32){ System.out.print("\n Too many characters! Will be shortened\n");make = make.substring(0,31);}

			System.out.print("\tEnter model: ");
			String model = in.readLine();
			while(model.isEmpty()) { vin = in.readLine();}
			if (model.length() == 17){ System.out.print("\n Too many characters! Will be shortened\n");model = model.substring(0,31);}
			
			
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

	public static void InsertServiceRequest(MechanicShop esql) 
	{// 4
		
	}

	public static void CloseServiceRequest(MechanicShop esql) throws Exception {// 5

	}

	public static void ListCustomersWithBillLessThan100(MechanicShop esql) {// 6
		try {
			String query = "SELECT date, comment, bill FROM Closed_Request WHERE bill < 100";
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
		}
		catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
}