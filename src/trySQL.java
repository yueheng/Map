import java.sql.*;

class testMysqlConnection {
	public static void main(String[] args) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
		    Connection conn =
		       DriverManager.getConnection("jdbc:mysql://localhost/mydata1?" +
		                                   "user=root&password=root");
		   
		} catch (SQLException ex) {
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
	}


	
}
