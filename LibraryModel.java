/*
 * LibraryModel.java
 * Author: tranhai 300224467
 * Created on: 09/10/14
 */



import javax.swing.*;
import java.sql.*;

public class LibraryModel {

	// For use in creating dialogs and making them modal
	private JFrame dialogParent;
	private Connection connect = null;

	public LibraryModel(JFrame parent, String userid, String password) {
		dialogParent = parent;
		//Register a PostgreSQL Driver
		try{
			Class.forName("org.postgresql.Driver");
			System.out.println("Successfully registered a Postgresql driver.");
		}
		catch(ClassNotFoundException cnfe){
			System.out.println("Can not find"+
					"the driver class: "+
					"\nEither I have not installed it"+
					"properly or \n postgresql.jar "+
					" file is not in my CLASSPATH");
		}
		//Establish a Connection
		//Use this url at university.
		//String url = "jdbc:postgresql:" + "//db.ecs.vuw.ac.nz/" + userid + "_jdbc";
		//Use this url at home.
		String url = "jdbc:postgresql:" + "//localhost:5432/postgres";
		try{
			connect = DriverManager.getConnection(url, userid, password);
			System.out.println("Successfully connected to the Database. ");
		}
		catch(SQLException sqlex){
			System.out.println("Can not connect to the database. ");
			System.out.println(sqlex.getMessage());
		}
	}

	public String bookLookup(int isbn) {
		return "Lookup Book Stub";
	}

	public String showCatalogue() {
		return "Show Catalogue Stub";
	}

	public String showLoanedBooks() {
		return "Show Loaned Books Stub";
	}

	public String showAuthor(int authorID) {
		return "Show Author Stub";
	}

	public String showAllAuthors() {
		return "Show All Authors Stub";
	}

	public String showCustomer(int customerID) {
		return "Show Customer Stub";
	}

	public String showAllCustomers() {
		return "Show All Customers Stub";
	}

	public String borrowBook(int isbn, int customerID,
			int day, int month, int year) {
		return "Borrow Book Stub";
	}

	public String returnBook(int isbn, int customerid) {
		return "Return Book Stub";
	}

	public void closeDBConnection() {
	}

	public String deleteCus(int customerID) {
		return "Delete Customer";
	}

	public String deleteAuthor(int authorID) {
		return "Delete Author";
	}

	public String deleteBook(int isbn) {
		return "Delete Book";
	}
}