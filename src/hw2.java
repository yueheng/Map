import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import oracle.sdoapi.OraSpatialManager;
import oracle.sdoapi.adapter.GeometryAdapter;
import oracle.sdoapi.geom.CoordPoint;
import oracle.sdoapi.geom.CurveString;
import oracle.sdoapi.geom.Geometry;
import oracle.sql.STRUCT;

public class hw2 {

	private static final int WIDTH = 1100;
	private static final int HEIGHT = 700;
	private static final int MAPWIDTH = 820;
	private static final int MAPHEIGHT = 580;

	private static JFrame frame;
	private static JLabel mouseCoordinate = new JLabel();;
	private static JPanel panel1;
	private static JCheckBox type_AS;
	private static JCheckBox type_Building;
	private static JCheckBox type_Students;
	private static JRadioButton whole;
	private static JRadioButton point;
	private static JRadioButton range;
	private static JRadioButton surrounding;
	private static JRadioButton emergency;
	
	private static JButton submit;
	private static ButtonGroup queryGroup;
	private static JTextArea queryText;

	private final int POINTRADIUS = 50;
	private int pointX;
	private int pointY;
	ArrayList<Integer> rangeX = new ArrayList<Integer>();
	ArrayList<Integer> rangeY = new ArrayList<Integer>();
	private String rangeXY = "";
	private int seX;
	private int seY;
	private int centerX;
	private int centerY;

	private static Connection conn = null;
	private static Statement stmt = null;

	private int queryCount = 0;

	public static void main(String[] args) {
		hw2 frame = new hw2();
		hw2.ConnectToDB();
		frame.LaunchFrame();
	}

	public static void ConnectToDB() {
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

	public void LaunchFrame() {
		frame = new JFrame("Name: Yueheng Li      USCID: 9461789319");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(0, 0);
		frame.setSize(WIDTH, HEIGHT);
		frame.setVisible(true);
		frame.setResizable(true);
		MouseMonitor myMouseMonitor = new MouseMonitor();
		frame.getContentPane().addMouseListener(myMouseMonitor);
		frame.getContentPane().addMouseMotionListener(myMouseMonitor);
		
		// add map
		ImageIcon image = new ImageIcon("map.jpg"); 
		JLabel imageLabel = new JLabel(image);
		frame.add(imageLabel, BorderLayout.WEST);

		// add right panel
		panel1 = new JPanel();
		frame.add(panel1, BorderLayout.EAST);
		panel1.setLayout(new BorderLayout());

		JPanel panel11 = new JPanel();
		JPanel panel12 = new JPanel();
		JPanel panel13 = new JPanel();
		panel1.add(panel11, BorderLayout.NORTH);
		panel1.add(panel12, BorderLayout.CENTER);
		panel1.add(panel13, BorderLayout.SOUTH);
		panel11.setLayout(new GridLayout(3, 1));
		panel12.setLayout(new GridLayout(2, 1));

		//add label for mouse mouseCoordinate
		mouseCoordinate.setText("Hello");
		panel11.add(mouseCoordinate);
		JLabel typeLabel = new JLabel("Active Feature Type");
		panel11.add(typeLabel);

		JPanel subPanel1 = new JPanel();
		panel11.add(subPanel1);
		subPanel1.setLayout(new GridLayout(2, 2));
		type_AS = new JCheckBox("AS");
		type_AS.setSelected(true);
		type_Building = new JCheckBox("Building");
		type_Building.setSelected(true);
		type_Students = new JCheckBox("Students");
		type_Students.setSelected(true);
		subPanel1.add(type_AS);
		subPanel1.add(type_Building);
		subPanel1.add(type_Students);

		// add middle right panel
		JLabel queryLabel = new JLabel("Query");
		panel12.add(queryLabel);

		JPanel subPanel2 = new JPanel();
		panel12.add(subPanel2);
		subPanel2.setLayout(new GridLayout(5, 1));
		whole = new JRadioButton("Whole Region");
		whole.setSelected(true);
		point = new JRadioButton("Point Query");
		range = new JRadioButton("Range Query");
		surrounding = new JRadioButton("Surrounding Student");
		emergency = new JRadioButton("Emergency Query");
		RadioButtonListener myRadioButtonListener = new RadioButtonListener();
		whole.addActionListener(myRadioButtonListener);
		point.addActionListener(myRadioButtonListener);
		range.addActionListener(myRadioButtonListener);
		surrounding.addActionListener(myRadioButtonListener);
		emergency.addActionListener(myRadioButtonListener);
		queryGroup = new ButtonGroup();
		queryGroup.add(whole);
		queryGroup.add(point);
		queryGroup.add(range);
		queryGroup.add(surrounding);
		queryGroup.add(emergency);
		subPanel2.add(whole);
		subPanel2.add(point);
		subPanel2.add(range);
		subPanel2.add(surrounding);
		subPanel2.add(emergency);

		// add bottom right panel
		submit = new JButton("Submit Query");
		submit.addActionListener(new SubmitMonitor());
		panel13.add(submit, BorderLayout.SOUTH);

		// add scrool bar

		queryText = new JTextArea();
		queryText.setEditable(false);
		queryText.setLineWrap(true);
		queryText.setRows(1);
		// queryText.setWrapStyleWord(true);
		JScrollPane scroll = new JScrollPane(queryText);
		// scroll.setAutoscrolls(true);
		frame.add(scroll, BorderLayout.SOUTH);
		frame.pack();		
	}

	private class SubmitMonitor implements ActionListener {
		Container contentPane = frame.getContentPane();
		Graphics g = contentPane.getGraphics();

		public void actionPerformed(ActionEvent e) {
			try {
				if (type_Students.isSelected() && whole.isSelected())	StudentsWhole();
				if (type_Building.isSelected() && whole.isSelected())	BuildingsWhole();
				if (type_AS.isSelected() && whole.isSelected())			ASWhole();
				if (type_Students.isSelected() && point.isSelected())	StudentsPoint();
				if (type_Building.isSelected() && point.isSelected())	BuildingsPoint();
				if (type_AS.isSelected() && point.isSelected())			ASPoint();
				if (type_Students.isSelected() && range.isSelected())	StudentsRange();
				if (type_Building.isSelected() && range.isSelected())	BuildingsRange();
				if (type_AS.isSelected() && range.isSelected())			ASRange();
				if (surrounding.isSelected())							Surrounding();
				if (emergency.isSelected())								Emergency();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		private void StudentsWhole() throws Exception {
			String query = "SELECT * FROM AA_2_STUDENTS";
			Draw(query, Color.GREEN, 2);
		}

		private void BuildingsWhole() throws Exception {
			String query = "SELECT * FROM AA_2_BUILDINGS";
			Draw(query, Color.YELLOW, 4);
		}

		private void ASWhole() throws Exception {
			String query = "SELECT * FROM AA_2_AS";
			Draw(query, Color.RED, 5);
		}

		private void StudentsPoint() throws Exception {
			String createCircle = "SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1,1003,4),"
					+ "SDO_ORDINATE_ARRAY("	+ pointX+ ","+ (pointY + POINTRADIUS)+ ", "+ (pointX + POINTRADIUS)
					+ ","+ pointY+ ", "	+ pointX+ "," + (pointY - POINTRADIUS) + "))";
			String createPoint = "SDO_GEOMETRY (2001, NULL, SDO_POINT_TYPE("
					+ pointX + ", " + pointY + ", NULL),NULL,NULL)";
			String intersect = "SELECT S.PLOCATION " 
					+ "FROM AA_2_STUDENTS S "
					+ "WHERE SDO_ANYINTERACT (S.PLOCATION," + createCircle	+ ") = 'TRUE'";
			String nearest = "SELECT /*+ INDEX(AA_2_STUDENTS AA_STU_IDX) */ S.PLOCATION "
					+ "FROM AA_2_STUDENTS S "
					+ "WHERE SDO_NN(S.PLOCATION, " + createPoint + ", 'sdo_num_res=1') = 'TRUE' "
					+ "AND SDO_ANYINTERACT (S.PLOCATION," + createCircle + ") = 'TRUE'";
			Draw(intersect, Color.GREEN, 1);
			Draw(nearest, Color.YELLOW, 1);
		}

		private void BuildingsPoint() throws Exception {
			String createCircle = "SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1,1003,4),"
					+ "SDO_ORDINATE_ARRAY(" + pointX + "," + (pointY + POINTRADIUS)
					+ ", " + (pointX + POINTRADIUS) + "," + pointY + ", " + pointX + "," + (pointY - POINTRADIUS) + "))";
			String createPoint = "SDO_GEOMETRY (2001, NULL, SDO_POINT_TYPE("
					+ pointX + ", " + pointY + ", NULL),NULL,NULL)";
			String intersect = "SELECT B.BCOORDINATE "
					+ "FROM AA_2_BUILDINGS B "
					+ "WHERE SDO_ANYINTERACT (B.BCOORDINATE," + createCircle + ") = 'TRUE'";
			String nearest = "SELECT /*+ INDEX(AA_2_BUILDINGS AA_BLD_IDX) */ B.BCOORDINATE "
					+ "FROM AA_2_BUILDINGS B "
					+ "WHERE SDO_NN(B.BCOORDINATE, " + createPoint + ", 'sdo_num_res=1') = 'TRUE' "
					+ "AND SDO_ANYINTERACT (B.BCOORDINATE," + createCircle + ") = 'TRUE'";
			Draw(intersect, Color.GREEN, 1);
			Draw(nearest, Color.YELLOW, 1);
		}

		private void ASPoint() throws Exception {
			String createCircle = "SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1,1003,4),"
					+ "SDO_ORDINATE_ARRAY("	+ pointX + "," + (pointY + POINTRADIUS) + ", " + (pointX + POINTRADIUS)
					+ "," + pointY + ", " + pointX + "," + (pointY - POINTRADIUS) + "))";
			String createPoint = "SDO_GEOMETRY (2001, NULL, SDO_POINT_TYPE("
					+ pointX + ", " + pointY + ", NULL),NULL,NULL)";
			String intersect = "SELECT A.CIRCLE " + "FROM AA_2_AS A "
					+ "WHERE SDO_ANYINTERACT (A.CIRCLE," + createCircle	+ ") = 'TRUE'";
			String nearest = "SELECT /*+ INDEX(AA_2_AS AA_AS_IDX) */ A.CIRCLE "
					+ "FROM AA_2_AS A " 
					+ "WHERE SDO_NN(A.CIRCLE, "	+ createPoint + ", 'sdo_num_res=1') = 'TRUE' "
					+ "AND SDO_ANYINTERACT (A.CIRCLE," + createCircle + ") = 'TRUE'";
			Draw(intersect, Color.GREEN, 1);
			Draw(nearest, Color.YELLOW, 1);
		}

		
		private void StudentsRange() throws Exception {
			if (rangeXY.equals(""))	return;
			String createPolygon = "SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1,1003,1), SDO_ORDINATE_ARRAY("
					+ rangeXY + "))";
			String selectGeo1 = "SELECT S.PLOCATION " + "FROM AA_2_STUDENTS S "
					+ "WHERE SDO_ANYINTERACT (S.PLOCATION," + createPolygon	+ ") = 'TRUE'";
			Draw(selectGeo1, Color.GREEN, 1);
		}

		private void BuildingsRange() throws Exception {
			if (rangeXY.equals(""))	return;
			String createPolygon = "SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1,1003,1), SDO_ORDINATE_ARRAY("
					+ rangeXY + "))";
			String selectGeo1 = "SELECT B.BCOORDINATE "
					+ "FROM AA_2_BUILDINGS B "
					+ "WHERE SDO_ANYINTERACT (B.BCOORDINATE," + createPolygon + ") = 'TRUE'";
			Draw(selectGeo1, Color.YELLOW, 1);
		}

		private void ASRange() throws Exception {
			if (rangeXY.equals(""))
				return;
			String createPolygon = "SDO_GEOMETRY(2003, NULL, NULL, SDO_ELEM_INFO_ARRAY(1,1003,1), SDO_ORDINATE_ARRAY("
					+ rangeXY + "))";
			String selectGeo1 = "SELECT A.CIRCLE " + "FROM AA_2_AS A "
					+ "WHERE SDO_ANYINTERACT (A.CIRCLE," + createPolygon + ") = 'TRUE'";
			Draw(selectGeo1, Color.RED, 1);
		}

		private void Surrounding() throws Exception {
			String createPoint = "SDO_GEOMETRY (2001, NULL, SDO_POINT_TYPE("
					+ seX + ", " + seY + ", NULL),NULL,NULL)";
			String findStu = "SELECT  S.PLOCATION "
					+ "FROM AA_2_STUDENTS S, AA_2_AS A "
					+ "WHERE SDO_NN(A.CENTER, " + createPoint + ", 'sdo_num_res=1') = 'TRUE' "
					+ "AND SDO_ANYINTERACT(S.PLOCATION, A.CIRCLE) = 'TRUE' ";
			Draw(findStu, Color.GREEN, 1);
		}

		private void Emergency() throws Exception {
			String createPoint = "SDO_GEOMETRY (2001, NULL, SDO_POINT_TYPE("
					+ seX + ", " + seY + ", NULL),NULL,NULL)";
			String find2AS = "SELECT  S.PLOCATION, A2.CIRCLE, A2.COLOR "
					+ "FROM AA_2_STUDENTS S, AA_2_AS A1, AA_2_AS A2 "
					+ "WHERE SDO_NN(A1.CENTER, "+ createPoint + ", 'sdo_num_res=1') = 'TRUE' "
					+ "AND SDO_ANYINTERACT(S.PLOCATION, A1.CIRCLE) = 'TRUE' "
					+ "AND SDO_NN(A2.CIRCLE, S.PLOCATION, 'sdo_num_res=2') = 'TRUE' "
					+ "AND A2.AID <> A1.AID";
			System.out.println(find2AS);
			int color;
			STRUCT student = null; // Structure to handle Geometry Objects
			STRUCT as = null;
			
			Geometry geom1, geom2, lastGeom1 = null; // Structure to handle Geometry Objects
			ResultSet resultSet = stmt.executeQuery(find2AS);
			queryCount++;
			queryText.append("\nQuery " + queryCount + ": " + find2AS);
			GeometryAdapter sdoAdapter = OraSpatialManager.getGeometryAdapter("SDO", "9", STRUCT.class, null, null, conn);
			while (resultSet.next()) {
				student = (STRUCT) resultSet.getObject(1);
				geom1 = sdoAdapter.importGeometry(student);
				if(geom1.equals(lastGeom1)) continue;
				lastGeom1 = geom1;
				as = (STRUCT) resultSet.getObject(2);
				geom2 = sdoAdapter.importGeometry(as);
				color = resultSet.getInt(3);

				if (geom2 instanceof oracle.sdoapi.geom.CurvePolygon) {
					oracle.sdoapi.geom.CurvePolygon circle = (oracle.sdoapi.geom.CurvePolygon) geom2;
					CurveString exteriorRing = circle.getExteriorRing();
					CoordPoint[] points = exteriorRing.getPointArray();
					int numpoints = exteriorRing.getNumPoints();
					int[] X = new int[numpoints];
					int[] Y = new int[numpoints];
					for (int i = 0; i < points.length; i++) {
						X[i] = (int) points[i].getX();
						Y[i] = (int) points[i].getY();
					}
					centerX = X[0];
					centerY = Y[1];
					int radius = Y[1] - Y[0];
					
					if(color%3 == 0) g.setColor(new Color(255, 25*color,255 - 25*color));
					else if(color%3 == 1) g.setColor(new Color(25*color, 255,255 - 25*color));
					else g.setColor(new Color(25*color,255 - 25*color,255));
					
					g.fillRect(centerX - 7, centerY - 7, 15, 15);
					g.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
				}


				if (geom1 instanceof oracle.sdoapi.geom.Point) {
					oracle.sdoapi.geom.Point point0 = (oracle.sdoapi.geom.Point) geom1;
					int X = (int) point0.getX();
					int Y = (int) point0.getY();
					if(color%3 == 0) g.setColor(new Color(255, 25*color,255 - 25*color));
					else if(color%3 == 1) g.setColor(new Color(25*color, 255,255 - 25*color));
					else g.setColor(new Color(25*color,255 - 25*color,255));
					g.fillRect(X - 5, Y - 5, 10, 10);
				}
			}
		}

		private void Draw(String query, Color color, int objectPosition) throws Exception {
			g.setColor(color);
			STRUCT location = null; // Structure to handle Geometry Objects
			Geometry geom; // Structure to handle Geometry Objects
			ResultSet resultSet = stmt.executeQuery(query);
			queryCount++;
			queryText.append("\nQuery " + queryCount +  ": " + query);
			GeometryAdapter sdoAdapter = OraSpatialManager.getGeometryAdapter("SDO", "9", STRUCT.class, null, null, conn);
			while (resultSet.next()) {
				location = (STRUCT) resultSet.getObject(objectPosition);
				geom = sdoAdapter.importGeometry(location);
				if (geom instanceof oracle.sdoapi.geom.Point) {
					oracle.sdoapi.geom.Point point0 = (oracle.sdoapi.geom.Point) geom;
					int X = (int) point0.getX();
					int Y = (int) point0.getY();
					g.fillRect(X - 5, Y - 5, 10, 10);
				}

				else if (geom instanceof oracle.sdoapi.geom.Polygon) {
					oracle.sdoapi.geom.Polygon polygon = (oracle.sdoapi.geom.Polygon) geom;
					CurveString exteriorRing = polygon.getExteriorRing();
					CoordPoint[] points = exteriorRing.getPointArray();
					int numpoints = exteriorRing.getNumPoints();
					int[] X = new int[numpoints];
					int[] Y = new int[numpoints];
					for (int i = 0; i < points.length; i++) {
						X[i] = (int) points[i].getX();
						Y[i] = (int) points[i].getY();
					}
					g.drawPolygon(X, Y, numpoints);
				}

				else if (geom instanceof oracle.sdoapi.geom.CurvePolygon) {
					oracle.sdoapi.geom.CurvePolygon circle = (oracle.sdoapi.geom.CurvePolygon) geom;
					CurveString exteriorRing = circle.getExteriorRing();
					CoordPoint[] points = exteriorRing.getPointArray();
					int numpoints = exteriorRing.getNumPoints();
					int[] X = new int[numpoints];
					int[] Y = new int[numpoints];
					for (int i = 0; i < points.length; i++) {
						X[i] = (int) points[i].getX();
						Y[i] = (int) points[i].getY();
					}
					centerX = X[0];
					centerY = Y[1];
					int radius = Y[1] - Y[0];
					g.fillRect(centerX - 7, centerY - 7, 15, 15);
					g.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
				}

				else {
					System.out.println(geom.getGeometryType());
				}
			}			
		}
	}

	private class MouseMonitor extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			Container contentPane = frame.getContentPane();
			Graphics g = contentPane.getGraphics();
			g.setClip(0, 0, MAPWIDTH, MAPHEIGHT);
			g.setColor(Color.RED);
			if (point.isSelected()) {
				pointX = e.getX();
				pointY = e.getY();
				g.fillRect(pointX, pointY, 5, 5);
				g.drawOval(pointX - POINTRADIUS, pointY - POINTRADIUS, POINTRADIUS * 2,POINTRADIUS * 2);
			}

			if (range.isSelected()) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					rangeX.add(e.getX());
					rangeY.add(e.getY());
					g.fillRect(e.getX(), e.getY(), 5, 5);
				}
				if (SwingUtilities.isRightMouseButton(e)) {
					rangeXY = "";
					int length = rangeX.size();
					if(length == 0) return;
					int[] rangeXArray = new int[length];
					int[] rangeYArray = new int[length];
					for (int i = 0; i < length; i++) {
						rangeXArray[i] = rangeX.get(i);
						rangeYArray[i] = rangeY.get(i);
						rangeXY += rangeX.get(i) + ", " + rangeY.get(i) + ", ";
					}
					rangeXY += rangeX.get(0) + ", " + rangeY.get(0);
					g.drawPolygon(rangeXArray, rangeYArray, length);
					rangeX.clear();
					rangeY.clear();
				}
			}

			if (surrounding.isSelected() || emergency.isSelected()) {
				seX = e.getX();
				seY = e.getY();
				g.fillRect(seX, seY, 5, 5);
				String createPoint = "SDO_GEOMETRY (2001, NULL, SDO_POINT_TYPE("
						+ seX + ", " + seY + ", NULL),NULL,NULL)";
				String findAS = "SELECT /*+ INDEX(AA_2_AS AA_AS_IDX) */ A.CIRCLE "
						+ "FROM AA_2_AS A "
						+ "WHERE SDO_NN(A.CIRCLE, "
						+ createPoint + ", 'sdo_num_res=1') = 'TRUE' ";
				try {
					new SubmitMonitor().Draw(findAS, Color.RED, 1);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	
		public void mouseMoved(MouseEvent e) {			
			int currentMouseX = e.getX();
			int currentMouseY = e.getY();
			mouseCoordinate.setText("X: " + currentMouseX + "     Y: " + currentMouseY);
			
		}
	}
	
	private class RadioButtonListener implements ActionListener {
		private Boolean wholeValue = true;
		private Boolean pointValue = false;
		private Boolean rangeValue = false;
		private Boolean surroundingValue = false;
		private Boolean emergencyValue = false;
		public void actionPerformed(ActionEvent e) {
			if(whole.isSelected()){
				if(wholeValue == true) return;
				else {
					wholeValue = true;
					pointValue = false;
					rangeValue = false;
					surroundingValue = false;
					emergencyValue = false;
				}
			} 
			
			else if(point.isSelected()){
				if(pointValue == true) return;
				else {
					wholeValue = false;
					pointValue = true;
					rangeValue = false;
					surroundingValue = false;
					emergencyValue = false;
				}
			}
			
			else if(range.isSelected()) {
				if (rangeValue == true) return;
				else {
					wholeValue = false;
					pointValue = false;
					rangeValue = true;
					surroundingValue = false;
					emergencyValue = false;
				}
			} 
			else if(surrounding.isSelected()) {
				if (surroundingValue == true) return;
				else {
					wholeValue = false;
					pointValue = false;
					rangeValue = false;
					surroundingValue = true;
					emergencyValue = false;
				}
			} 
			else if(emergency.isSelected()) {
				if (emergencyValue == true) return;
				else {
					wholeValue = false;
					pointValue = false;
					rangeValue = false;
					surroundingValue = false;
					emergencyValue = true;
				}
			} 
			Container contentPane = frame.getContentPane();			
			contentPane.repaint();
		}
	}	
}