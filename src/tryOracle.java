import java.sql.*;
public class tryOracle {
	
	public static void main(String arg[]){
		try {
			Connection conn = DriverManager.getConnection( 
					"jdbc:oracle:thin:@localhost:1521:XE", "SYSTEM", "root");
			Statement stmt = conn.createStatement();
			String query = "SELECT * FROM AA_HASHTAG";
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				int tweetId = rs.getInt("TWEET_ID");
				String tag = rs.getString("TAG");
				System.out.println(tweetId + " " + tag);
			}
			stmt.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
