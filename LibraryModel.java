/*
 * LibraryModel.java
 * Author: tranhai 300224467
 * Created on: 09/10/14
 */



import javax.swing.*;

import java.sql.*;
import java.text.Format;
import java.text.SimpleDateFormat;

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
		String url = "jdbc:postgresql:" + "//db.ecs.vuw.ac.nz/" + userid + "_jdbc";
		//Use this url at home.
		//String url = "jdbc:postgresql:" + "//localhost:5432/postgres";

		try {
			connect = DriverManager.getConnection(url, userid, password);
			System.out.println("Successfully connected to the Database. ");
		}
		catch (SQLException sqlex){
			System.out.println("Can not connect to the database. ");
			System.out.println(sqlex.getMessage());
		}
	}

	public String bookLookup(int isbn) {
		//Book result
		String bookResult = "Book Lookup:\n\tNo such ISBN: " + isbn;

		try {
			// Create a Statement object
			Statement s = connect.createStatement();
			// Execute the Statement object
			ResultSet rs = s.executeQuery(
					"SELECT * FROM (SELECT STRING_AGG(surname, ', ' ORDER BY AuthorSeqNo ASC) AS authorNames "
							+ "FROM Book_Author NATURAL JOIN Author WHERE ISBN = " + isbn
							+ ") AS authorTable , Book WHERE ISBN = " + isbn + ";"
					);

			// Handle query answer in ResultSet object
			while (rs.next()){
				//Format the answer
				bookResult = String.format("Book Lookup:\n"
						+ "\t%d: %s \n"
						+ "\tEdition: %d - Number of copies: %d - Copies left: %d\n"
						+ "\tAuthors: %s", rs.getInt("isbn"),rs.getString("title"),rs.getInt("Edition_No"),
						rs.getInt("numOfCop"),rs.getInt("numLeft"),
						//Check if there are any authors - prints appropriate message
						rs.getString("authorNames") == null? "(No authors)": rs.getString("authorNames"));
			}
			s.close();
			// End of the try block
		} catch (SQLException sqlex){
			System.out.println(sqlex.getMessage());
		}

		return bookResult;
	}

	public String showCatalogue() {
		//Show all the books
		String allBooks = "Show Catalogue: \n\n";

		try {
			// Create a Statement object
			Statement s = connect.createStatement();
			// Execute the Statement object
			ResultSet rs = s.executeQuery(
					"SELECT *, authorNames FROM Book LEFT JOIN "
							+ "(SELECT STRING_AGG(surname,', ' ORDER BY AuthorSeqNo ASC) AS authorNames, isbn "
							+ "FROM Book_Author NATURAL JOIN Author "
							+ "GROUP BY isbn) as authorTable "
							+ "ON Book.isbn = authorTable.isbn "
							+ "ORDER BY Book.isbn;"
					);
			// Result is empty
			if (!rs.isBeforeFirst()){
				return "Show Catalogue:\n(No Books)";
			}
			// Handle query answer in ResultSet object
			while (rs.next()){
				//Format the answer
				allBooks += String.format("%d: %s \n"
						+ "\tEdition: %d - Number of copies: %d - Copies left: %d\n"
						+ "\tAuthors: %s\n", rs.getInt("isbn"),rs.getString("title"),rs.getInt("Edition_No"),
						rs.getInt("numOfCop"),rs.getInt("numLeft"),
						//Check if there are any authors - prints appropriate message
						rs.getString("authorNames") == null? "(No authors)": rs.getString("authorNames"));
			}
			s.close();
			// End of the try block
		} catch (SQLException sqlex){
			System.out.println(sqlex.getMessage());
		}

		return allBooks;
	}

	public String showLoanedBooks() {
		//Show loaned the books
		String loanedBooks = "Show Loaned Books: \n\n";

		try {
			// Create a Statement object
			Statement s = connect.createStatement();
			// Execute the Statement object
			ResultSet rs = s.executeQuery(
					" SELECT DISTINCT loanedBooks.isbn, title,edition_no,authorNames, numofcop, numLeft, borrowers  "
							+	" FROM Cust_Book NATURAL JOIN Book AS loanedBooks NATURAL JOIN customer LEFT JOIN "
							+	" (SELECT STRING_AGG(surname,', ' ORDER BY AuthorSeqNo ASC) AS authorNames, isbn "
							+	 " FROM Book_Author NATURAL JOIN Author GROUP BY isbn) as authorTable "
							+	" ON authorTable.isbn = loanedBooks.isbn "
							+	" LEFT JOIN (SELECT STRING_AGG(customerid || ': ' || l_name || ', ' || f_name || ' - ' || city,'\n\t\t'"
							+	" ORDER BY customerid ASC) AS borrowers, isbn "
							+	" FROM Cust_Book NATURAL JOIN Customer GROUP BY isbn) as borrowersTable"
							+	" ON borrowersTable.isbn = loanedBooks.isbn "
							+	" ORDER BY loanedBooks.isbn ASC;"
					);
			// Result is empty
			if (!rs.isBeforeFirst()){
				return "Show Loaned Books:\n(No Loaned Books)";
			}
			// Handle query answer in ResultSet object
			while (rs.next()){
				//Format the answer
				loanedBooks += String.format("%d: %s \n"
						+ "\tEdition: %d - Number of copies: %d - Copies left: %d\n"
						+ "\tAuthors: %s\n"
						+ "\tBorrowers:\n"
						+ "\t\t%s\n", rs.getInt("isbn"),rs.getString("title"),rs.getInt("Edition_No"),
						rs.getInt("numOfCop"),rs.getInt("numLeft"),
						//Check if there are any authors - prints appropriate message
						rs.getString("authorNames") == null? "(No authors)": rs.getString("authorNames")
								,rs.getString("borrowers"));
			}
			s.close();
			// End of the try block
		} catch (SQLException sqlex){
			System.out.println(sqlex.getMessage());
		}

		return loanedBooks;
	}

	public String showAuthor(int authorID) {
		String author = "Show Author:";

		try {
			// Create a Statement object
			Statement s = connect.createStatement();
			// Execute the Statement object
			ResultSet rs = s.executeQuery(
					"SELECT * FROM "
							+"(SELECT STRING_AGG(isbn ||' - ' ||title, '\n\t\t' ORDER BY isbn ASC) AS allTitles "
							+"FROM Book_Author NATURAL JOIN Book WHERE authorid ="+ authorID + ") AS allBooks NATURAL JOIN Author "
							+"WHERE authorid =" + authorID + ";"
					);
			if (!rs.isBeforeFirst()){
				return author + "\n\tNo such author ID: " + authorID;
			}
			// Handle query answer in ResultSet object
			while (rs.next()){
				//Format the answer
				author += String.format("\n\t%d: %s %s\n\tBooks written:\n\t\t%s",
						rs.getInt("authorId"), rs.getString("name").trim(), rs.getString("surname").trim(),
						//Check if author has written any books - prints appropriate message
						rs.getString("allTitles") == null? "(no books written)" : rs.getString("allTitles"));
			}
			s.close();
			// End of the try block
		} catch (SQLException sqlex){
			System.out.println(sqlex.getMessage());
		}

		return author;
	}

	public String showAllAuthors() {
		String authors = "Show All Authors:";

		try {
			// Create a Statement object
			Statement s = connect.createStatement();
			// Execute the Statement object
			ResultSet rs = s.executeQuery(
					"SELECT * FROM author ORDER BY authorid ASC;"
					);
			// Result is empty
			if (!rs.isBeforeFirst()){
				return authors + "\n\t(No Authors)";
			}
			// Handle query answer in ResultSet object
			while (rs.next()){
				//Format the answer
				authors += String.format("\n\t%d: %s, %s ", rs.getInt("authorId"), rs.getString("surname").trim(), rs.getString("name").trim());
			}
			s.close();
			// End of the try block
		} catch (SQLException sqlex){
			System.out.println(sqlex.getMessage());
		}

		return authors;
	}

	public String showCustomer(int customerID) {
		String customer = "Show Customer:";

		try {
			// Create a Statement object
			Statement s = connect.createStatement();
			// Execute the Statement object
			ResultSet rs = s.executeQuery(
					"SELECT * FROM "
							+ "(SELECT STRING_AGG(isbn ||' - ' ||title, '\n\t\t' ORDER BY isbn ASC) AS booksBorrowed "
							+ "FROM Cust_Book NATURAL JOIN Book WHERE customerid =" + customerID + ") AS booksBorrowedTable NATURAL JOIN Customer "
							+ "WHERE customerid =" + customerID + ";"
					);
			if (!rs.isBeforeFirst()){
				return customer + "\n\tNo such customer ID: " + customerID;
			}
			// Handle query answer in ResultSet object
			while (rs.next()){
				//Format the answer
				customer += String.format("\n\t%d: %s, %s - %s\n\tBooks Borrowed:\n\t\t%s",
						rs.getInt("customerid"), rs.getString("L_Name").trim(), rs.getString("F_Name").trim(),
						//Check if city exists - prints appropriate message
						rs.getString("city") == null? "(no city)" : rs.getString("city"),
								//Check if customer has borrowed any books - prints appropriate message
								rs.getString("booksBorrowed") == null? "(No books borrowed)" : rs.getString("booksBorrowed"));
			}
			s.close();
			// End of the try block
		} catch (SQLException sqlex){
			System.out.println(sqlex.getMessage());
		}

		return customer;
	}

	public String showAllCustomers() {
		String customers = "Show All Customers:";

		try {
			// Create a Statement object
			Statement s = connect.createStatement();
			// Execute the Statement object
			ResultSet rs = s.executeQuery(
					"SELECT * FROM customer ORDER BY customerid ASC;"
					);
			// Result is empty
			if (!rs.isBeforeFirst()){
				return customers + "\n\t(No Customers)";
			}
			// Handle query answer in ResultSet object
			while (rs.next()){
				//Format the answer
				customers += String.format("\n\t%d: %s, %s - %s", rs.getInt("customerId"), rs.getString("L_Name").trim(),
						rs.getString("F_Name").trim(),
						//Check if city exists - prints appropriate message
						rs.getString("city") == null? "(no city)" : rs.getString("city"));
			}
			s.close();
			// End of the try block
		} catch (SQLException sqlex){
			System.out.println(sqlex.getMessage());
		}

		return customers;
	}

	public String borrowBook(int isbn, int customerID,
			int day, int month, int year) {
		String book = "Borrow Book:";

		try {
			//BEGIN;
			connect.setAutoCommit(false);

			// Create a Statement object
			Statement s = connect.createStatement();

			// Check whether the customer exists (and lock him/her as if the delete option were available
			ResultSet rCustomer = s.executeQuery("SELECT * FROM Customer WHERE customerid = "+customerID+" FOR UPDATE;");
			// No customer exists
			if (!rCustomer.isBeforeFirst()){
				connect.rollback();
				return book + "\n\tNo such customer ID: " + customerID;
			}

			// Lock the book if it exists
			ResultSet rBook = s.executeQuery("SELECT * FROM Book WHERE isbn = "+ isbn +" FOR UPDATE;");

			// No book exists
			if (!rBook.isBeforeFirst()){
				connect.rollback();
				return book + "\n\tNo such ISBN: " + isbn;
			}
			// No copies of the book are left
			while (rBook.next()){
				if (rBook.getInt("numLeft") <= 0){
					connect.rollback();
					return book + "\n\tNot enough copies of book " + isbn + " left";
				}
			}

			// Insert appropriate tuple in the Cust_Book table
			String insert = "INSERT INTO Cust_Book VALUES(" + isbn + "," + String.format("to_date('%d-%d-%d', 'YYYY-MM-DD')", year,month,day) + "," + customerID + ");";
			s.executeUpdate(insert);

			// Dialog box - to stall the processing of the program
			JOptionPane.showMessageDialog(dialogParent, "Locked tuple(s), ready to update. Click OK to continue");

			// Update the Book table
			String update = "UPDATE Book SET NumLeft = NumLeft-1 WHERE isbn =" + isbn + ";";
			s.executeUpdate(update);

			// Commit the transaction (if actions were all successful, otherwise rollback)
			connect.commit();
			connect.setAutoCommit(true);

			// Return the message with correct format
			ResultSet rMessage = s.executeQuery("SELECT * FROM Cust_Book NATURAL JOIN Customer NATURAL JOIN Book WHERE customerID = " + customerID + " AND isbn ="+  isbn + ";");
			while (rMessage.next()){
				book += String.format("\n\tBook: %d (%s)\n\tLoaned to: %d (%s %s)\n\tDue Date: %s",
						rMessage.getInt("isbn"),rMessage.getString("title").trim(), rMessage.getInt("customerID"),
						rMessage.getString("f_name").trim(), rMessage.getString("l_name").trim(), rMessage.getString("duedate").toString());
			}
			s.close();

			return book;
			// End of the try block
		} catch (SQLException sqlex){
			System.out.println(sqlex.getMessage());
		}

		try {
			// If actions were not all successful, rollback
			connect.rollback();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Only path left is that customer is already borrowing the book
		return book + "\n\tCustomer "+customerID + " already has book " + isbn + " on loan";
	}

	public String returnBook(int isbn, int customerID) {
		String book = "Return Book:";

		try {
			//BEGIN;
			connect.setAutoCommit(false);

			// Create a Statement object
			Statement s = connect.createStatement();

			// Check whether the customer exists (and lock him/her as if the delete option were available
			ResultSet rCustomer = s.executeQuery("SELECT * FROM Customer WHERE customerid = "+customerID+" FOR UPDATE;");
			// No customer exists
			if (!rCustomer.isBeforeFirst()){
				connect.rollback();
				return book + "\n\tNo such customer ID: " + customerID;
			}
			// Check if the book exists
			ResultSet rBook = s.executeQuery("SELECT * FROM Book WHERE isbn = "+ isbn +";");
			// No book exists
			if (!rBook.isBeforeFirst()){
				connect.rollback();
				return book + "\n\tNo such ISBN: " + isbn;
			}
			// Lock if customer has borrowed the book
			ResultSet rBorrow = s.executeQuery("SELECT * FROM Cust_Book WHERE isbn = "+ isbn +"AND customerID = " + customerID +" FOR UPDATE;");
			// Customer hasn't borrowed the book
			if (!rBorrow.isBeforeFirst()){
				connect.rollback();
				return book + "\n\tThis customer has not borrowed the book: " + isbn;
			}

			// Insert appropriate tuple in the Cust_Book table
			String delete = "DELETE FROM Cust_Book WHERE isbn = " + isbn + " AND customerID = " + customerID + ";";
			s.executeUpdate(delete);
			// Dialog box - to stall the processing of the program
			JOptionPane.showMessageDialog(dialogParent, "Book Returned.");
			// Update the Book table
			String update = "UPDATE Book SET NumLeft = NumLeft + 1 WHERE isbn =" + isbn + ";";
			s.executeUpdate(update);
			// Commit the transaction (if actions were all successful, otherwise rollback)
			connect.commit();
			connect.setAutoCommit(true);

			return book + "\n\tYou have returned the book. ";
			// End of the try block
		} catch (SQLException sqlex){
			System.out.println(sqlex.getMessage());
		}

		try {
			// If actions were not all successful, rollback
			connect.rollback();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Only path left is that customer is already borrowing the book
		return book;
	}

	public void closeDBConnection() {
		//Close all connections
		try {
			if (connect != null){
				System.out.println("Successfully disconnected from the Database.");
				connect.close();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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