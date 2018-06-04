package WB;
import java.sql.*;

import javax.swing.*;
public class Connection2DB {
	Connection conn=null;
	public static Connection dbConnector()
	{
		try {
			//Class.forName("org.firebirdsql.jdbc.FBDriver");
			//Connection conn=DriverManager.getConnection("jdbc:firebirdsql://192.168.90.203/Logistyka/Tosia/Projekty JAVA/HSDATAFAT.FDB", "SYSDBA", "masterkey");
			

			/*Class.forName("org.sqlite.JDBC");
			Connection conn=DriverManager.getConnection("jdbc:sqlite:"+PDF.Parameters.getPathToDB()+"/HacoSoftDB.sqlite");*/
			
			Class.forName("org.mariadb.jdbc.Driver");
			Connection conn=DriverManager.getConnection("jdbc:mariadb://192.168.90.123/fatdb","RaportGodzin","1234");
			
			//JOptionPane.showMessageDialog(null, "Connection Successful");
			return conn;
		}catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, e);
			return null;
		}
	}
}
