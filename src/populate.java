import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class populate {	
	private static Connection conn = null;
    private static Statement stmt = null;
    
	public static void main(String[] args) {
		ConnectToDB();
		ToPopulate(args[0], 0);
		ToPopulate(args[1], 1);
		ToPopulate(args[2], 2);
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void ConnectToDB () {
		try {
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
	    	String URL = "jdbc:oracle:thin:@localhost:1521:XE";
	    	String userName = "SYSTEM";
	    	String password = "root";
	    	conn = DriverManager.getConnection(URL, userName, password);
			stmt = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void ToPopulate(String file, int num) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String cl;
			int toInsertNum = 0;
			if(num == 0) stmt.executeUpdate("DELETE FROM AA_2_BUILDINGS");
			else if (num == 1) stmt.executeUpdate("DELETE FROM AA_2_STUDENTS");
			else stmt.executeUpdate("DELETE FROM AA_2_AS");
			
			while((cl = reader.readLine()) != null) {
				//System.out.println(cl);
				String delims = "[,]+";
				String[] toInsert = cl.split(delims);
				for(int i = 0; i< toInsert.length; i++) {
					toInsert[i] = toInsert[i].trim();
				}
				String query;
				if(num == 0) query = BuildingQuery(toInsert);
				else if (num == 1) query = StudentsQuery(toInsert);
				else {
					toInsertNum++;
					query = ASQuery(toInsert, toInsertNum);	
				} 			
				stmt.executeUpdate(query);
				
			}
			reader.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String BuildingQuery(String[] toInsert) {
		String subString = "";
		int i = 3;
		for(; i < toInsert.length; i++) {
			subString += toInsert[i] + ",";
		}
		subString += toInsert[3] + "," + toInsert[4];  //go back to the first node
		String query = 	"INSERT INTO AA_2_BUILDINGS (BID,BNAME,VNUM,BCOORDINATE) VALUES ('"+ 
						toInsert[0] +"'," + "'" + toInsert[1] + "'," + toInsert[2] + ","
						+ "SDO_GEOMETRY (2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1,1003,1), SDO_ORDINATE_ARRAY("+subString+")))";	
		return query;
	}
	
	public static String StudentsQuery(String[] toInsert) {
		String query = 	"INSERT INTO AA_2_STUDENTS VALUES ('"+ toInsert[0] +"',"
				+ "SDO_GEOMETRY (2001, NULL, SDO_POINT_TYPE(" +toInsert[1] +", "+toInsert[2]+", NULL),NULL,NULL))";	
		return query;
	}
	
	public static String ASQuery(String[] toInsert, int toInsertNum) {
		int toInsert1 = Integer.parseInt(toInsert[1]);
		int toInsert2 = Integer.parseInt(toInsert[2]);
		int toInsert3 = Integer.parseInt(toInsert[3]);
		String query = "INSERT INTO AA_2_AS VALUES ('"+ toInsert[0] +"'," + toInsertNum + ","+ toInsert[3] + ","
				+ "SDO_GEOMETRY (2001, NULL, SDO_POINT_TYPE(" +toInsert[1] +", "+toInsert[2]+", NULL),NULL,NULL), "  //center
				+ "SDO_GEOMETRY(2003, NULL, NULL,SDO_ELEM_INFO_ARRAY(1,1003,4), SDO_ORDINATE_ARRAY("                //circle object
				+ toInsert1+ "," + (toInsert2 + toInsert3) + "," + (toInsert1 + toInsert3) + "," + toInsert2 
				+"," + toInsert1 + "," + (toInsert2 - toInsert3) + ")))";	
		return query;
	}	
}
