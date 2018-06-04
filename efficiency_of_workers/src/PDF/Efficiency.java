package PDF;

import java.io.File ;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.itextpdf.io.util.DecimalFormatUtil;
import com.itextpdf.layout.border.Border;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Section;

public class Efficiency {
static Connection connection= WB.Connection2DB.dbConnector();
	
	private static Font catFont ;
	private static Font smallFont;
	private static Font smallFont2 = new Font(Font.FontFamily.TIMES_ROMAN, 6);
	private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,Font.BOLD);
	

	public static void createRaport() throws SQLException, FileNotFoundException, DocumentException, ParseException{

		FontFactory.register(Efficiency.class.getClassLoader().getResource("times.ttf").toString(), "times");
		catFont = FontFactory.getFont("times", BaseFont.CP1250, BaseFont.EMBEDDED, 18);
		smallFont = FontFactory.getFont("times", BaseFont.CP1250, BaseFont.EMBEDDED, 12);
		Document doc = new Document();
		
		// configure and get actual date
		SimpleDateFormat doNazwy = new SimpleDateFormat("yyyy.MM.dd");
		SimpleDateFormat godz = new SimpleDateFormat("HH;mm");
	
		Calendar calendar = Calendar.getInstance();
		
		String path = Parameters.getPathToSaveHours()+"/"+doNazwy.format(calendar.getTime())+"/";
		//int actualactualyear       	= calendar.get(Calendar.YEAR);
		//int actualmonth      		= calendar.get(Calendar.DAY_OF_MONTH);
		int actualday		   		= calendar.get(Calendar.DAY_OF_MONTH);

	
			
		//intialize begining of last actualmonth
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		String sdate = "";
		String edate = "";
		
		
		Date startdate;
		Date enddate;

		
		// take yesterday as the last day for analyze
		calendar.add(calendar.DAY_OF_MONTH, -1);
		edate = date.format(calendar.getTime());
		enddate = calendar.getTime();
	
		calendar.add(calendar.DAY_OF_MONTH, +1);
		
		
		//System.out.println("actual actualmonth : " + Integer.toString(i) );
		calendar.add(calendar.DAY_OF_MONTH, -actualday+1);
		calendar.add(calendar.MONTH, -1);
		sdate = date.format(calendar.getTime());  // string format
		startdate = calendar.getTime();           // date format
	
		System.out.println("start date for analyze : " + sdate );
    	System.out.println("end date for analyze   : " + edate );
		
		long numberofdays = (enddate.getTime()-startdate.getTime())/(1000 * 60 * 60 * 24);
		System.out.println("number of days to be analyzed: "+String.valueOf(numberofdays));
		
		// test area
		
		/**
		 * index[0] = incoming time in milliseconds 
		 * index[1] = outgoing time in milliseconds
		 */
		ArrayList<Long> bramki = new ArrayList<Long>();
		
		/**
		 * index[0] = start time in milliseconds as Long
		 * index[1] = stop time in milliseconds 
		 * index[2] = worknote index as long
		 * index[3] = sequence number as long
		 * index[4] = status
		 * index[5] = theoretical time
		 * index[6] = spare rest of theoretical time
		 */
		ArrayList<Long> worknotes = new ArrayList<Long>();

	    
	    //end test area
	    
		
		//create the directory
		File theDir = new File(path);
		if (!theDir.exists()) {
		    try{
		        theDir.mkdir();
		    } 
		    catch(SecurityException se){
		        //handle it
		    }
		}
		//create the pdf file
		String name = "Efficiency_workers.pdf";
		File f = new File(path+name);
		if(f.exists() && !f.isDirectory())
			name = godz.format(calendar.getTime())+" "+name;
		PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(path+name));
		doc.open();
		writer.setPageEvent(new PDF_MyFooter());
		//end creation of file
		//create the dump file
		String name2 = "error_dump_efficiency_workers.txt";
		File fdump = new File(path+name2);
		if(fdump.exists() && !fdump.isDirectory())
			name2 = godz.format(calendar.getTime())+" "+name2;
		FileOutputStream fos = new FileOutputStream(path+name2);
		PrintStream ps = new PrintStream(fos);
		PrintStream console = new PrintStream(new FileOutputStream(FileDescriptor.out));
		System.setOut(ps); 
		System.setOut(console);
		
		//end creation error file
			
	    //create header table
       	String [] headers = new String [7];
		int ileKolumn = headers.length;
		headers[0] = "naam";
		headers[1] = "haconumber";
		headers[2] = "id number of card";
		headers[3] = "string of entry times for a period";
		headers[4] = " ";
		headers[5] = " ";
		headers[6] = " ";
	
		//end creation header table
		
		//header
		Paragraph preface = new Paragraph();
        preface.add("\n");
        preface.add(new Paragraph("Efficiency of our workers", catFont));
        preface.add("\n");
        doc.add(preface);
              
        // check how many workers to be analyzed
        int howManyWorkers =1;
        int counter =0;
		String sql1 = "select count(*) from fatdb.pracownicy where WERKREGIME=2";
		Statement st1 = connection.createStatement();
		ResultSet rs1 = st1.executeQuery(sql1);
		while(rs1.next()){
			howManyWorkers= rs1.getInt(1);
		}
		System.setOut(console);
		System.out.println("How many workers to be analyzed :"+Integer.toString(howManyWorkers));
		st1.close();
		rs1.close();
        
		//create data table
		String [][] tab = new String [ileKolumn][howManyWorkers];
		// end creation data tabel
		
		//create second data table
		String[][] dailytab = new String [ileKolumn][50];
		
		
	
		
		
		//creat PDF table
        PdfPTable tabPDF = new PdfPTable(ileKolumn);
        float widths[] = new float[] { 5, 2, 2, 5, 2, 14, 5}; //must be 30 total
        float headerwidths[] = new float[] {30};
        //end creation PDF table
		
		
		//fill table with name and hacosoft nummer
        	//tab[0] kolomn 0 :name of worker
        	//tab[1] kolomn 1 : hacosoft number of worker
        System.out.println("L213 fill tab table with data");
        System.setOut(ps);  //write to file
        
		String sql2 = "SELECT NAAM , Werknemer FROM fatdb.pracownicy WHERE WERKREGIME=2";
		Statement st2 = connection.createStatement();
		ResultSet rs2 = st2.executeQuery(sql2);
		counter = 0;
		while(rs2.next()){
				tab[0][counter] = rs2.getString(1);
				tab[1][counter] = rs2.getString(2);
				counter++;
		}
		st2.close();
		rs2.close();
		
		// COLLECT  ALL DATA FROM HACOSOFT
				for(int k = 0; k<howManyWorkers; k++){
					
					
						//fill table with name and hacosoft nummer
		        		//tab[0] kolomn 0 :name of worker
		        		//tab[1] kolomn 1 : hacosoft number of worker
						// ==> tab[2] kolomn 2 : ID cardnumber for gate
						System.out.println("236 collect id card number according hacosoft data");
						String haconumber = tab[1][k];
						String sql3 = "select id_karty from fat.cards_name_surname_nrhacosoft where HacoSoftnumber = " + haconumber;
						Statement st3 = connection.createStatement();
						ResultSet rs3 = st3.executeQuery(sql3);
						while(rs3.next()){
								tab[2][k] = rs3.getString(1)   ;
						}
						st3.close();
						rs3.close();
				}
		// END OF BLOCK 
		
		// COLLECT DAILY DATA FOR EVERY WORKER
			for(int k = 22; k<(howManyWorkers-5); k++){
				//reset allways to first initial date
				calendar = Calendar.getInstance();
				calendar.add(calendar.DAY_OF_MONTH, -actualday+1);
				//calendar.add(calendar.MONTH, -1);
				sdate = date.format(calendar.getTime());  // string format
				calendar.add(calendar.DAY_OF_MONTH, 1);
				edate = date.format(calendar.getTime());  // string format

				startdate = calendar.getTime();           // date format
				bramki.clear();
				worknotes.clear();
				
				
				for (int l=0; l<=numberofdays; l++){
  
					
					
					//////////////////////////////////////////////
					////// fill entrance data from bramka ////////
					//////////////////////////////////////////////
		
					
					// new I is new present incoming date
					// new O is new present outgoing date
					// incom  the last previous date was an incoming date
					// outcom the last previous date was an outgoing date
					
					
					// new I  new O   incom  outcom
					//  0      0        0       0	nothing to do
					//  0      0        0       1	nothing to do
					//  0      0        1       0	nothing to do
					//  0      0        1       1	nothing to do
					//----------------------------
					//  0      1        0       0	proc I   --> ERROR missing input
					//  0      1        0       1	proc II  --> ERROR missing input or check is diff new income < 5 min
					//  0      1        1       0	proc III --> calculate time block and reset incom set outcom
					//  0      1        1       1	proc IV  not possible
					//----------------------------
					//  1      0        0       0	proc V    --> set income
					//  1      0        0       1	proc VI   --> set income and reset outcome
					//  1      0        1       0	proc VII  --> ERROR missing one output OR check if diff is small, then skip the value
					//  1      0        1       1	proc VIII not possible
					//----------------------------
					//  1      1        0       0	not possible
					//  1      1        0       1	not possible
					//  1      1        1       0	not possible
					//  1      1        1       1	not possible
					//----------------------------			
			
					String idcard = tab[2][k];
					String sql4 = "select akcja , `data` from fat.access where id_karty = " + idcard + " and    `data`  between '" 
									+ sdate + " 00:00' and '" + sdate + " 23:59' order by `data` ";
					Statement st4 = connection.createStatement();
					ResultSet rs4 = st4.executeQuery(sql4);
					System.setOut(console);  //write to console
					System.out.println("NEW DATE: " + tab[0][k] + " on date: " + sdate);
					System.out.println("  ");
					
					String oldincomingdate = null;
					String oldoutgoingdate = null;

					while(rs4.next()){
						System.out.println( rs4.getString(1) + " " + rs4.getString(2));
						// proc V <=> VIII
						if (rs4.getString(1).equals("wejscie")){
							
							// PROC V:  take new data as valid and store in oldincomingdate and set outgoingdate == null
							if (oldincomingdate == null && oldoutgoingdate == null){
								oldincomingdate = rs4.getString(2); 
								oldoutgoingdate = null ;
								bramki.add(getLongFromStringDate(rs4.getString(2)));
							}
						
							// PROC VI: oldoutgoingdate available, so add a new date to arraylist and reset oldoutgoingdate
							if ((oldincomingdate == null)&&(oldoutgoingdate != null)){
								oldincomingdate = rs4.getString(2); 
								oldoutgoingdate = null ;
								bramki.add(getLongFromStringDate(rs4.getString(2)));
							}
							
							// Proc VII A: date was registerd twice in a small time
							if ((oldincomingdate != null)&&(presenttime(oldincomingdate,rs4.getString(2))<5)){
								bramki.remove(bramki.size()-1);
								oldincomingdate = rs4.getString(2); 
								oldoutgoingdate = null ;
								bramki.add(getLongFromStringDate(rs4.getString(2)));
							}
							
							// Proc VII B: date was registerd twice in a long time
							if ((oldincomingdate != null)&&(presenttime(oldincomingdate,rs4.getString(2))>5)){
								bramki.add(-1L);                   // fill negative value for the missing incoming date
								oldincomingdate = rs4.getString(2); 
								oldoutgoingdate = null ;
								bramki.add(getLongFromStringDate(rs4.getString(2)));
							}
	
						} // END IF
						
						
						if (rs4.getString(1).equals("wyjscie")){	
							
							
							// Proc I: error missing incomingdate
							if (oldincomingdate == null && oldoutgoingdate == null){
								bramki.add(-1L);    // fill negative value for the missing incoming date
								oldincomingdate = null; 
								oldoutgoingdate = rs4.getString(2);
								bramki.add(getLongFromStringDate(rs4.getString(2)));
								bramki.add(0L);
								bramki.add(0L);
							}
							
							// Proc II A: date was registerd twice in a small time
							if (oldincomingdate == null && oldoutgoingdate != null && (presenttime(oldoutgoingdate,rs4.getString(2))<5)){
								bramki.remove(bramki.size()-1);    //remove last value
								oldincomingdate = null; 
								oldoutgoingdate = rs4.getString(2); // update last outgoingdate
								bramki.add(getLongFromStringDate(rs4.getString(2)));
								bramki.add(0L);
								bramki.add(0L);
							}
							
							// Proc II B: date was registerd twice in a long time
							if (oldincomingdate == null && oldoutgoingdate != null && (presenttime(oldoutgoingdate,rs4.getString(2))>5)){
								bramki.add(-1L);    // fill negative value for the missing incoming date
								oldincomingdate = null; 
								oldoutgoingdate = rs4.getString(2);
								bramki.add(getLongFromStringDate(rs4.getString(2)));
								bramki.add(0L);
								bramki.add(0L);
							}
							
							// Proc III : normal situation
							if (oldincomingdate != null && oldoutgoingdate == null ){
								oldincomingdate = null; 
								oldoutgoingdate = rs4.getString(2);
								bramki.add(getLongFromStringDate(rs4.getString(2)));
								bramki.add(0L);
								bramki.add(0L);
							}
							
						} // END IF
					} // END WHILE
					
					// we have to check if last entry was an output, ifnot we have to add a zeroline
					if (oldincomingdate != null){
						 bramki.add(-1L);
						 bramki.add(0L);
						 bramki.add(0L);
					}
					
					
					
					st4.close();
					rs4.close();
					
						
					//////////////////////////////////////////////
					//////         WORKNOTES        //////////////
					//////////////////////////////////////////////
				
					// STEP 1 check the number of jobs done that day
					
					String werknemer = tab[1][k];
					String sql5 = "SELECT COUNT(*) from rejestracja where WERKNEMER='"+ werknemer +"' and DATUM between '"+
										sdate + "' and '" + sdate + " 23:59' ";
					
					
					
					// System.out.println(sql5); for testreasons
					Statement st5 = connection.createStatement();
					ResultSet rs5 = st5.executeQuery(sql5);
					
					int numberOfJobs = 0 ;
					while(rs5.next()){
						numberOfJobs= rs5.getInt(1);
					}
					
					System.setOut(console);
					System.out.println("number of jobs : " + String.valueOf(numberOfJobs));
					rs5.close();
					rs5.close();
						
					// step 2 get the jobs out of database
					
					
					
					long totalPresentTime2 = 0;
					long theoreticalTime = 0;
					long realTime = 0;
					
					String sql6 = "SELECT rejestracja.DATUM, rejestracja.WERKBON, rejestracja.CFARTIKELCODE, rejestracja.STATUS, rejestracja.BEGINUUR,"
							+ "rejestracja.BEGINMIN, rejestracja.EINDUUR, rejestracja.EINDMIN, rejestracja.TIJD, rejestracja.WERKPOST,"
							+ " werkbon.INSTELMINUTEN, werkbon.WERKMINUTEN , werkbon.HOEVEELHEID  FROM rejestracja  "
							+ "left join werkbon ON rejestracja.WERKBON = werkbon.WERKBONNUMMER  "
							+ "WHERE rejestracja.WERKNEMER='" + werknemer + "' AND (rejestracja.DATUM BETWEEN '" + sdate + "' and '" + sdate + " 23:59') "
							+ "order by rejestracja.DATUM ASC";
					Statement st6 = connection.createStatement();
					ResultSet rs6 = st6.executeQuery(sql6);
//					String [][] temptab = new String [8][numberOfJobs]; 
//					int row2 = 0 ;
//					while(rs6.next()){
//						temptab[0][row2] = rs6.getString(1); //datum
//						temptab[1][row2] = rs6.getString(2); //werkbon
//						temptab[7][row2] = rs6.getString(3); //artiklecode
//						temptab[3][row2] = rs6.getString(4); //status
//						temptab[4][row2] = rs6.getString(5) + ":" + rs6.getString(6) ; //starttime
//						temptab[5][row2] = rs6.getString(7) + ":" + rs6.getString(8) ; //endtime
//						temptab[6][row2] = rs6.getString(9) + "@" + Long.toString(rs6.getInt(11) + rs6.getInt(12) * rs6.getInt(13)); // reel time@theo time
//						temptab[2][row2] = rs6.getString(10);
//						row2++;	
//						incomingdate2 = rs6.getString(1) +" "+String.format("%02d",rs6.getInt(5))+ ":" + String.format("%02d",rs6.getInt(6))+":00";
//						outgoingdate2 = rs6.getString(1) +" "+String.format("%02d",rs6.getInt(7))+ ":" + String.format("%02d",rs6.getInt(8))+":00";
//						totalPresentTime2 += presenttime(incomingdate2,outgoingdate2);
//						realTime += rs6.getInt(9);
//						if (rs6.getInt(4)==90){
//							theoreticalTime += rs6.getInt(11) + rs6.getInt(12) * rs6.getInt(13);
//						}
//						
//					}
//					
//					tab[5][k] = "";
//					for (int j = 0;j<numberOfJobs; j++ ){
//					tab[5][k]= tab[5][k] + temptab[0][j]+" "+temptab[1][j]+" "+temptab[2][j]+ " "+ temptab[3][j] +" "
//							+temptab[4][j]+" "+temptab[5][j]+" "+temptab[6][j]+ " "+temptab[7][j]+ "\n";
//					}
//					tab[5][k] += "total scanned worktime" + convertPresetTimeToString(totalPresentTime2)+"\n"+
//									"total theo worktime" + convertPresetTimeToString(theoreticalTime) +"\n"+
//									" real time" + convertPresetTimeToString(realTime);
					System.setOut(console);  
					while(rs6.next()){
						
						
						Long 	werkbon 		= rs6.getLong(2); //werkbon
						String 	articlecode 	= rs6.getString(3); //artiklecode
						Long 	status         	= rs6.getLong(4); // get status of worknote
						Long   	realtime		= rs6.getLong(9); // real time
						Long   	theotime		= rs6.getLong(11) + rs6.getLong(12) * rs6.getLong(13); // theoretical time
						String 	starttime 		= rs6.getString(1) +" "+String.format("%02d",rs6.getInt(5))+ ":" + String.format("%02d",rs6.getInt(6))+":00";
						String 	endtime 		= rs6.getString(1) +" "+String.format("%02d",rs6.getInt(7))+ ":" + String.format("%02d",rs6.getInt(8))+":00";
						
						
						System.out.println(getLongFromStringDate(starttime));
						
						// still to be developed the total used working time for this worknote
						if (rs6.getInt(4)==90){
							
							
							
							theoreticalTime += rs6.getInt(11) + rs6.getInt(12) * rs6.getInt(13);
						}
						/**
						 * index[0] = start time in milliseconds as Long
						 * index[1] = stop time in milliseconds 
						 * index[2] = worknote index as long
						 * index[3] = if covers present time
						 * index[4] = status
						 * index[5] = theoretical time
						 * index[6] = spare rest of theoretical time
						 */
						worknotes.add(getLongFromStringDate(starttime));	//index[0]
						worknotes.add(getLongFromStringDate(endtime));		//index[1]
						worknotes.add(werkbon);								//index[2]
						worknotes.add(0L);									//index[3]
						worknotes.add(status);								//index[4]
						worknotes.add(theotime);							//index[5]
						worknotes.add(0L);									//index[6]
												
						
					}
				
				
				
				
				
				
				
				
				
					//increase the day with one day
					sdate = date.format(calendar.getTime());  // string format
					calendar.add(calendar.DAY_OF_MONTH, +1);
					edate = date.format(calendar.getTime());
					
				
					// END COLLECT DAILY DATA FOR EVERY WORKER	
				} //END IF LOOP for (int l=0; l<=numberofdays; l++)
				System.out.println(bramki);
				System.out.print("number of items in the bramki arraylist: ");
				System.out.println(bramki.size());
				System.out.println(worknotes);
				System.out.print("number of items in worknote arraylist");
				System.out.println(worknotes.size());
				
				// procedure to calculate present time in FAT
				long ttime = 0;
				int badvalues = 0;
				for (int h= 0; h < bramki.size(); h +=4){
					if (bramki.get(h) > 0L && bramki.get(h+1) > 0L){
						ttime += bramki.get(h+1) - bramki.get(h);
					}
					if (bramki.get(h) < 0L || bramki.get(h+1) < 0L){
						badvalues++;
					}
				}
				System.out.println("this worker was present in FAT " + convertPresetTimeFromMilliToString(ttime));	
				System.out.println("number of bad samples : " + String.valueOf(badvalues));
				
				//procedure to check if worknotes are within the worktime
					
					
				
				
				
				
				
			}
		
		
		
		
//			Document document = new Document();
//	        PdfWriter writerr = PdfWriter.getInstance(document, new FileOutputStream(path +"draw_lines.pdf"));
//	        document.open();
//	        PdfContentByte canvas = writerr.getDirectContent();
//	        CMYKColor magentaColor = new CMYKColor(0.f, 1.f, 0.f, 0.f);
//	        canvas.setColorStroke(magentaColor);
//	        canvas.moveTo(36, 36);
//	        canvas.lineTo(36, 806);
//	        canvas.lineTo(559, 36);
//	        canvas.lineTo(559, 806);
//	        canvas.rectangle(50, 50, 100, 100);
//	        canvas.closePathStroke();
//	        
//	        document.close();
//		
		
		
			
		
		
		
		for(int k = 0; k<howManyWorkers; k++){	
			//fill table with name and hacosoft nummer
        	//tab[0] kolomn 0 :name of worker
        	//tab[1] kolomn 1 : hacosoft number of worker
			//tab[2] kolomn 2 : ID cardnumber for gate
			// ==> tab[3] kolomn 3 : string of in - out movements at the gate
			///System.out.println("280 entry times from Bramka");
				String entrydates = "";
				String incomingdate = null;
				String outgoingdate = null;
				String summaryOfPresence = "";
				int row = 0;
				long totalPresentTime = 0;
							
				String idcard = tab[2][k];
				String sql4 = "select data ,akcja from fat.access where id_karty ="+ idcard +"   and  data between '"+sdate+"' and '"+edate+" 23:59:00' order by `data`";
				Statement st4 = connection.createStatement();
				ResultSet rs4 = st4.executeQuery(sql4);
				System.setOut(ps);  //write to file
				System.out.println(tab[0][k]);
				System.out.println("  ");
				
				
				
				
				
				
				
				while(rs4.next()){
					
					if (rs4.getString(2).equals("wejscie")){
						if ( outgoingdate != null || (incomingdate == null && outgoingdate == null)){
							entrydates += "proc I  ";
							entrydates += "I//"+ rs4.getString(1) + "\n";
							incomingdate = rs4.getString(1);
							System.out.println("IN:  " + incomingdate + " Proc I " + dayOfTheYear(incomingdate));
							//summaryOfPresence += "IN:  " + incomingdate + " Proc I " + dayOfTheYear(incomingdate);
							
							outgoingdate=null;
						}else{
							// something is wrong, we have two entrances in a line
							String secondentrance = rs4.getString(1);
							if (presenttime(incomingdate,secondentrance)>60){
								entrydates += "proc II  ";
								System.out.println("missing entrance data on: "+secondentrance+ " Proc II");
								entrydates += "ERROR I no outgoing mov on:  "+incomingdate.substring(1, 10)+"\n";
							}
							if (presenttime(incomingdate,secondentrance)<5){
								entrydates += "proc III  ";
								System.out.println("deleted entrance data cos difference too small "+secondentrance+ " Proc III");
							}
						}
						
					}else{
						if (outgoingdate == null && incomingdate != null){
							// normal procedure, entrance is availabe
							entrydates += "proc IV  ";
							entrydates = entrydates +"O//"+ rs4.getString(1) + "\n";
							outgoingdate = rs4.getString(1);
							System.out.println("OUT: "+ outgoingdate + " Proc IV");
							totalPresentTime += presenttime(incomingdate,outgoingdate);
							summaryOfPresence += "OUT: "+ dayOfTheYear(incomingdate) + "/" + incomingdate.substring(11, 16) + "/" + presenttime(incomingdate,outgoingdate);
							
							
							
							incomingdate=null;
						}else if (outgoingdate != null){ 
							//there are two times an outgoing without ingoing, if smaller filter out otherwise message
							String secondoutgoingdate = rs4.getString(1);
							//System.out.println(outgoingdate + " " + newdate);
							//System.out.println(convertPresetTimeToString(presenttime(outgoingdate, newdate)));
							if (presenttime(outgoingdate, secondoutgoingdate)<5 ){
								entrydates += "proc V  ";
								System.out.println("deleted outgoing data cos difference too small "+secondoutgoingdate + " Proc V");
							}else{
								entrydates += "proc VI  ";
								System.out.println("ingoingdate missingggggg :  Proc VI ");	
								System.out.println("OUT: " + secondoutgoingdate + " Proc VI");
								entrydates += entrydates + "O//" + rs4.getString(1) + "\n";
							}
						}else if (incomingdate == null){ //no ingoing movement detected
								String secondoutgoingdate = rs4.getString(1);
								entrydates += "proc VII  ";
								System.out.println("ERROR O no incoming aktion on:" + secondoutgoingdate.substring(1, 10) + " Proc VII");
								entrydates +=  "ERROR OUT no incoming\n";
								entrydates += "O//" + rs4.getString(1) + "\n";
								System.out.println("outgoingdate :" + outgoingdate + " Proc VII");
								outgoingdate = secondoutgoingdate;
						}
					}
					row++;
					}
				
//				System.out.println(secondentrance);
//				
//				System.out.println("it s meeeeee"+ String.format ("%d", diffdays));
//				diffdays= diffdays /360;
//				System.out.println("it s meeeeee"+ String.format ("%d", diffdays));
//				diffdays += 1;
//				System.out.println("it s meeeeee"+ String.format ("%d", diffdays));
//				diffdays = diffdays / 4;
//				
//				System.out.println("it s meeeeee"+ String.format ("%d", diffdays));
				
				
					entrydates += convertPresetTimeToString(totalPresentTime)  + "\n";
					tab[3][k] = entrydates;
					
					
					//test
					 	
					System.out.println(summaryOfPresence);
				    String[] result = summaryOfPresence.split("OUT:");
				    int qtyOfRows = result.length;
				    int[] timeline = new int [288]; //create empty timeline 1 unit is 5 min
				    String timestring = "";			//printout of timeline array
				    
				   					
					String [][] analyzetab = new String [ileKolumn][qtyOfRows];
					for (int j = 1;j<qtyOfRows; j++ ){
						String testt = result[j]; 
						//System.out.println(testt);
						String[] separate = testt.split("\\/");
						//System.out.println(" de split heeft gewerkt " + Integer.toString(separate.length)  );
						
						analyzetab[0][j] = separate[0]; // write day number
						System.out.println(separate[0]);
						analyzetab[1][j] = separate[1]; // write start hour
						System.setOut(console);
						int startpos = converTimeToPos(separate[1]);
						int block = Integer.parseInt(separate[2]);
						int endpos = (int) (startpos + block/5);
						//System.out.println(String.valueOf(endpos));
						//System.out.println("testpositions" + String.valueOf(startpos) + " " + String.valueOf(endpos) );
						if (endpos> 288) {
							endpos = 278;
						}
						for(int g = startpos;g<endpos;g++){
							timeline[g] = 1;
						}
						converTimeToPos(separate[1]);
						System.setOut(ps);
						System.out.println("incomingdate print " +  separate[1]);
						System.out.println(separate[1]);
						analyzetab[2][j] = separate[2]; // write total present time
						System.out.println("separate 2 :" + separate[2]);
						timestring = "";
						for(int g = 0;g<288;g++){
							timestring += Integer.toString( timeline[g]);
						}
						System.out.println(timestring);
						for(int g = 0; g<288;g++){
							timeline[g] = 0;
						}
						}
//						analyzetab[3][j] = 
//						analyzetab[4][j] = 
//						
										
					
					
					//System.out.println("änalyzetab :" + analyzetab[0][1]);
//
//					    for (String s : result) {
//					        System.out.println(">" + s + "<");
//					    }
//					
					
					
					//test
					
					
					
					st4.close();
					rs4.close();
					// END OF BLOCK 
		
				
					System.setOut(console);
		
				//fill table 
		        //tab[0] kolomn 0 :name of worker
		        //tab[1] kolomn 1 : hacosoft number of worker
				//tab[2] kolomn 2 : ID cardnumber for gate
				//tab[3] kolomn 3 : string of in - out movements at the gate
				// ==> tab[4] kolomn 4 : count the number of jobs
				//System.out.println("count the number of jobs done in the certain time");
		
				String werknemer = tab[1][k];
				String sql5 = "SELECT COUNT(*) from rejestracja where WERKNEMER='"+ werknemer +"' and DATUM between '"+sdate+"' and '"+edate+"' order by datum asc";
				Statement st5 = connection.createStatement();
				ResultSet rs5 = st5.executeQuery(sql5);
				while(rs5.next()){
					tab[4][k]= String.valueOf(rs5.getInt(1));
				}
				rs5.close();
				rs5.close();
				// END OF BLOCK 
		
		
			
				//fill table 
		        //tab[0] kolomn 0 :name of worker
		        //tab[1] kolomn 1 : hacosoft number of worker
				//tab[2] kolomn 2 : ID cardnumber for gate
				//tab[3] kolomn 3 : string of in - out movements at the gate
				//tab[4] kolomn 4 : count the number of jobs
				//==> tab[5] kolomn 5 : create string for all theoretical and real times for every job
				//System.out.println("select all jobs theoritical and reel times");
	
				int numberofjobs = Integer.valueOf(tab[4][k]);  
				String incomingdate2 = null;
				String outgoingdate2 = null;
				long totalPresentTime2 = 0;
				long theoreticalTime = 0;
				long realTime = 0;
				//for (int j = 0;j<numberofjobs; j++ ){
				String workerid = tab[1][k];
				String sql6 = "SELECT rejestracja.DATUM, rejestracja.WERKBON, rejestracja.CFARTIKELCODE, rejestracja.STATUS, rejestracja.BEGINUUR,"
						+ "rejestracja.BEGINMIN, rejestracja.EINDUUR, rejestracja.EINDMIN, rejestracja.TIJD, rejestracja.WERKPOST,"
						+ " werkbon.INSTELMINUTEN, werkbon.WERKMINUTEN , werkbon.HOEVEELHEID  FROM rejestracja  "
						+ "left join werkbon ON rejestracja.WERKBON = werkbon.WERKBONNUMMER  "
						+ "WHERE rejestracja.WERKNEMER='"+workerid+"' AND (rejestracja.DATUM BETWEEN '"+sdate+"' AND '"+edate+"') "
						+ "order by rejestracja.DATUM ASC";
				Statement st6 = connection.createStatement();
				ResultSet rs6 = st6.executeQuery(sql6);
				String [][] temptab = new String [8][numberofjobs]; // 13 koloms i # of jobs
				int row2 = 0 ;
				while(rs6.next()){
					temptab[0][row2] = rs6.getString(1); //datum
					temptab[1][row2] = rs6.getString(2); //werkbon
					temptab[7][row2] = rs6.getString(3); //artiklecode
					temptab[3][row2] = rs6.getString(4); //status
					temptab[4][row2] = rs6.getString(5) + ":" + rs6.getString(6) ; //starttime
					temptab[5][row2] = rs6.getString(7) + ":" + rs6.getString(8) ; //endtime
					temptab[6][row2] = rs6.getString(9) + "@" + Long.toString(rs6.getInt(11) + rs6.getInt(12) * rs6.getInt(13)); // reel time@theo time
					temptab[2][row2] = rs6.getString(10);
					row2++;	
					incomingdate2 = rs6.getString(1) +" "+String.format("%02d",rs6.getInt(5))+ ":" + String.format("%02d",rs6.getInt(6))+":00";
					outgoingdate2 = rs6.getString(1) +" "+String.format("%02d",rs6.getInt(7))+ ":" + String.format("%02d",rs6.getInt(8))+":00";
					totalPresentTime2 += presenttime(incomingdate2,outgoingdate2);
					realTime += rs6.getInt(9);
					if (rs6.getInt(4)==90){
						theoreticalTime += rs6.getInt(11) + rs6.getInt(12) * rs6.getInt(13);
					}
				}
				tab[5][k] = "";
				for (int j = 0;j<numberofjobs; j++ ){
				tab[5][k]= tab[5][k] + temptab[0][j]+" "+temptab[1][j]+" "+temptab[2][j]+ " "+ temptab[3][j] +" "
						+temptab[4][j]+" "+temptab[5][j]+" "+temptab[6][j]+ " "+temptab[7][j]+ "\n";
				}
				tab[5][k] += "total scanned worktime" + convertPresetTimeToString(totalPresentTime2)+"\n"+
								"total theo worktime" + convertPresetTimeToString(theoreticalTime) +"\n"+
								" real time" + convertPresetTimeToString(realTime);
				
			}
			
		
		   

        
		//copy data table to pdf tabel
		//print next table
		System.out.println("copy table to pdf table");
		for(int k = 0; k<howManyWorkers; k++){
			for(int j = 0; j<ileKolumn; j++){
				String zawartosc = tab[j][k];
				PdfPCell c2 = new PdfPCell(new Phrase(zawartosc, smallFont2));
				c2.setMinimumHeight(10);
				c2.setRowspan(1);
				c2.setHorizontalAlignment(Element.ALIGN_LEFT);
				c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
				c2.setBorder(c2.NO_BORDER);
				tabPDF.addCell(c2);
			}
		}
		tabPDF.setWidths(widths);
		tabPDF.setWidthPercentage(100);
		tabPDF.setHorizontalAlignment(Element.ALIGN_CENTER);
		tabPDF.setHorizontalAlignment(Element.ALIGN_CENTER);
	    doc.add(tabPDF);
        
        
        
    	doc.close();
		
		return;
	}

	
	public int daysBetween(Date d1, Date d2){
	    return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	}
	
	public static long presenttime(String from, String till) throws ParseException { 
		// working and tested 10/05/2018
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Date dfrom = null;
		Date dtill = null;
	   
	    dfrom = sdf.parse(from);
	    dtill = sdf.parse(till);
	    
	    long diff = dtill.getTime() - dfrom.getTime();
	    long diffhours = diff / ( 60 * 1000);
	    
	    return diffhours;
	}
	
	public static int converTimeToPos(String datum) throws ParseException { 
		// working and tested 21/05/2018  format datum HH:MM
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT + 1"));
		Date date = null;
		date = sdf.parse(datum);
		
		int hours = (int) ((date.getTime()/(1000 * 60 * 60)) % 24);
	    int min   = (int) ((date.getTime()/(1000 * 60 )) % 60);
	    	   
	    int pos = (int) (hours * 12 + (min /5));
	    //System.out.println( "convert time to position hours " + String.valueOf(hours) +  " min  " + String.valueOf(min) +" " + datum + " " + String.valueOf(pos));
	    
	    return pos;
	}
	
	
	
	public static String convertPresetTimeFromMilliToString (Long time) {
			// working and tested 30/10/2018
			String result = String.format("%02d:%02d", 
					TimeUnit.MILLISECONDS.toHours(time),	
					TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)));
		return result;
	}
	
	
	
	
	
	public static String convertPresetTimeToString (Long time) {
		// working and tested 10/05/2018
		String timestring = null;
		long showhours =  time / 60;
		long showminutes = time % 60 ;
		
		timestring = String.format("%02d",showhours)+":"+ String.format("%02d",showminutes);
		 	
		return timestring;
	}
	
	public static String dayOfTheYear (String day) throws ParseException{
			String dayoftheyear = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
			Date date = sdf.parse(day);
	
		    Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
		    
			dayoftheyear = String.valueOf(calendar.get(calendar.DAY_OF_YEAR));
		    	    
		return dayoftheyear;
	}
	
	public static Long getLongFromStringDate(String datum) throws ParseException { 
			// working and tested 21/05/2018  format datum HH:MM
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT + 1"));
			
			Date date = sdf.parse(datum);
			
			Long result = date.getTime();
	    return result;
	}
	
	public static String getDate(long milliSeconds) {
		    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    String dateString = formatter.format(new Date(milliSeconds));
		return dateString;
	}
	
	
	
	
//	//qty of openstanding projekts 4
//  int qtyProject4 = 0;
//	String e = "select count(*) from calendar where (Zakonczone = 0 AND (NrMaszyny like '4/%'))";
//	Statement e1 = connection.createStatement();
//	ResultSet rse = e1.executeQuery(e);
//	while(rse.next()){
//		qtyProject4= rse.getInt(1);
//	}
//	e1.close();
//	rse.close();
//	
//	  
//	//qty of openstanding projekts 14
//  int qtyProject14 = 0;
//	String d = "select count(*) from calendar where (Zakonczone = 0 AND (NrMaszyny like '14/%'))";
//	Statement d1 = connection.createStatement();
//	ResultSet rsd = d1.executeQuery(d);
//	while(rsd.next()){
//		qtyProject14= rsd.getInt(1);
//	}
//	d1.close();
//	rsd.close();
//	
//	// print results on PDF
//	preface.add(new Paragraph("opstanding projekts 2/6/0 \t : "+ qtyProject2and6, smallBold));
//  preface.add(new Paragraph("opstanding projekts 4 \t\t : "+ qtyProject4, smallBold));
//  preface.add(new Paragraph("opstanding projekts 14 \t\t : "+ qtyProject14, smallBold));
//  preface.add("\n");
 
  
  

  //------------------------------------------------------------
  //------------------loop for every actualmonth----------------------
  //------------------------------------------------------------
//  for(int i=0;i<23;i++){
//	
//  	//print to PDF header for every actualmonth:
//  	 	PdfPCell cellheader = new PdfPCell(new Phrase(nameofactualmonth.format(calendar.getTime())+ " "+ nameofactualyear.format(calendar.getTime() )));
// 	        cellheader.setMinimumHeight(30);
// 	        cellheader.setHorizontalAlignment(Element.ALIGN_CENTER);
// 	        cellheader.setVerticalAlignment(Element.ALIGN_MIDDLE);
// 	        cellheader.setBackgroundColor(BaseColor.ORANGE);
// 	        cellheader.setColspan(7);
// 	        tabPDF.addCell(cellheader);
//	
//  	
//  	// count the number of open projects in this particalar actualmonth
// 	        String a = "select count(*) from calendar where  DataKoniecMontazu between  '"+ sdate +"'  and  '"+ edate +"' ";
// 	        Statement a1 = connection.createStatement();
// 	        ResultSet rs2 = a1.executeQuery(a);
// 	        while(rs2.next()){
// 	        	ileOtwartyProjektow= rs2.getInt(1);
// 	        }
// 	        a1.close();
// 	        rs2.close();
//		
// 	    //debug line
// 	        System.out.println("number of open project in actualmonth \t :  "+ ileOtwartyProjektow);
// 	        System.out.println(sdate);
// 	        System.out.println(edate);
// 	        
//		//prepare table data for every actualmonth:
// 	        String sql1 = "select NrMaszyny, Opis, klient, Cena, Waluta, DataKoniecMontazu from calendar where DataKoniecMontazu between  '"+ sdate +"'  and  '"+ edate +"' order by DataKoniecMontazu ";
// 	        String [][] tab = new String [7][ileOtwartyProjektow];
// 	        Statement st1 = connection.createStatement();
// 	        ResultSet rs1 = st1.executeQuery(sql1);
// 	        int counter = -1;
// 	        while(rs1.next()){
// 	        	counter++;
// 	        	String nummer = String.valueOf(counter+1);
// 	        	String projectnr = rs1.getString(1);
//				String description = rs1.getString(2);
//				String client = rs1.getString(3);
//				String price = rs1.getString(4);
//				String currency = rs1.getString(5);
//				String shipmentdate = rs1.getString(6);
//				
//				// check if 0/ or 6/ is not doubled, if yes, then we eliminate this line
//				if (projectnr.startsWith("0/") || projectnr.startsWith("6/") ) {
//					int counter1 = 0;
//				    System.out.println("detected 0 project hihihi   " + projectnr);
//				    String sql4 = "SELECT COUNT(*) FROM calendar WHERE NrMaszyny Like'2/" + projectnr.substring(2) +"' OR NrMaszyny Like '6/"+ projectnr.substring(2)+"'";
//				    Statement st4 = connection.createStatement();
//					ResultSet rs4 = st4.executeQuery(sql4);
//					while(rs4.next()){
//						counter1= rs4.getInt(1);
//					}
//					rs4.close();
//					rs4.close();
//				    if (counter1==0) {
//					   // detect project 0: only project 0 is available
//					    tab[0][counter] = nummer;
//						tab[1][counter] = projectnr;
//						tab[2][counter] = description;
//						tab[3][counter] = client;
//						tab[4][counter] = price;
//						tab[5][counter] = currency;
//						tab[6][counter] = shipmentdate;
//				    } else if (counter1==1) {
//				    	// detected project 0: or project 2 exist or project 6 exist => do NOT add to table
//				    	// detected project 6: only project 6 is available => add to table
//				    	if (projectnr.startsWith("0/")) {
//				    		ileOtwartyProjektow--;
//						   	counter--;
//				    	} else {
//						    tab[0][counter] = nummer;
//							tab[1][counter] = projectnr;
//							tab[2][counter] = description;
//							tab[3][counter] = client;
//							tab[4][counter] = price;
//							tab[5][counter] = currency;
//							tab[6][counter] = shipmentdate;
//				    	}
//				    } else if (counter1==2) {
//				    	// detected project 0: project 2 and project 6 exist => do NOT add to table
//				    	// detected project 6: project 2 and project 6 exist => do NOT add to table
//				    	ileOtwartyProjektow--;
//					   	counter--;
//				   }
//				   
//				} else { 
//						// standard projects
//						if (((projectnr.length()==8 && (projectnr.startsWith("6/") || projectnr.startsWith("2/"))||( (projectnr.startsWith("4/") || projectnr.startsWith("14/")     )))) ){
//					    tab[0][counter] = nummer;
//						tab[1][counter] = projectnr;
//						tab[2][counter] = description;
//						tab[3][counter] = client;
//						tab[4][counter] = price;
//						tab[5][counter] = currency;
//						tab[6][counter] = shipmentdate;
//						} else {
//							ileOtwartyProjektow--;
//						   	counter--;
//						}
//				}
//			}
//		
//		// make summaries
// 	        System.out.println(Integer.toString(ileOtwartyProjektow));
// 	        Float totaleuro = 0f;
// 	        Float totalpln = 0f;
// 	        for(int m =0; m<ileOtwartyProjektow; m++){
// 	        	if (tab[5][m].equals("EUR")) {	
// 	        		totaleuro = totaleuro +  Float.parseFloat(tab[4][m]);
// 	        		}
// 	        	if (tab[5][m].equals("PLN")) {	
// 	        		totalpln = totalpln +  Float.parseFloat(tab[4][m]);
// 	        		}
// 	        }
// 	        System.out.println(String.valueOf(totaleuro));
// 	       System.out.println(String.valueOf(totalpln));		
//		
//		//print next table
//		for(int k = 0; k<ileOtwartyProjektow; k++){
//			for(int j = 0; j<ileKolumn; j++){
//				String zawartosc = tab[j][k];
//				PdfPCell c2 = new PdfPCell(new Phrase(zawartosc, smallFont2));
//				c2.setMinimumHeight(10);
//				c2.setRowspan(1);
//				c2.setHorizontalAlignment(Element.ALIGN_CENTER);
//				c2.setVerticalAlignment(Element.ALIGN_MIDDLE);
//				c2.setBorder(c2.NO_BORDER);
//				tabPDF.addCell(c2);
//			}
//		}
//		tabPDF.setWidths(widths);
//		tabPDF.setWidthPercentage(100);
//		tabPDF.setHorizontalAlignment(Element.ALIGN_CENTER);
//		tabPDF.setHorizontalAlignment(Element.ALIGN_CENTER);
//		
//		//ADD ONE SPACE LINES
//		PdfPCell cellspace = new PdfPCell(new Phrase(""));
//		cellspace.setMinimumHeight(10);
//		cellspace.setHorizontalAlignment(Element.ALIGN_CENTER);
//		cellspace.setVerticalAlignment(Element.ALIGN_MIDDLE);
//		cellspace.setColspan(7);
//		cellspace.setRowspan(1);
//		cellspace.setBorder(cellspace.NO_BORDER);
//	        tabPDF.addCell(cellspace);
//		
//		
//		//ADD summary
//	        Phrase abc = new Phrase(" 1EURO = 4.2PLN");
//	      	PdfPCell cellsum = new PdfPCell(new Phrase("SUM"));
//			cellsum.setMinimumHeight(10);
//			cellsum.setHorizontalAlignment(Element.ALIGN_CENTER);
//			cellsum.setVerticalAlignment(Element.ALIGN_MIDDLE);
//			cellsum.setColspan(4);
//			cellsum.setBorder(cellsum.NO_BORDER);
//		tabPDF.addCell(cellsum);
//			cellsum.setColspan(3);
//			cellsum.setPhrase(abc);
//		tabPDF.addCell(cellsum);
//		// new line 7 cells
//		abc=new Phrase(String.valueOf(totalpln));
//			cellsum.setColspan(3);
//			cellsum.setPhrase(abc);
//		tabPDF.addCell(cellsum); 
//		abc=new Phrase("PLN");
//			cellsum.setColspan(1);
//			cellsum.setPhrase(abc);
//		tabPDF.addCell(cellsum); 
//			cellsum.setColspan(3);
//			abc=new Phrase(String.format("%.1f", totalpln / 4.2) +" EUR ");	
//			cellsum.setPhrase(abc);
//		tabPDF.addCell(cellsum); 
//		// new line 7 cells
//			cellsum.setColspan(3);
//			abc=new Phrase(String.format("%.1f",totaleuro));
//			cellsum.setPhrase(abc);
//		tabPDF.addCell(cellsum);
//			abc=new Phrase("EUR");
//			cellsum.setColspan(1);
//			cellsum.setPhrase(abc);
//		tabPDF.addCell(cellsum);	
//			cellsum.setColspan(3);
//			abc=new Phrase(" ");
//			cellsum.setPhrase(abc);
//		tabPDF.addCell(cellsum); 
//		// new line 7 cells
//			cellsum.setColspan(3);
//			cellsum.setBorder(cellsum.TOP);
//			abc=new Phrase(String.format("%.1f",(totaleuro+(totalpln/4.2))));
//			cellsum.setPhrase(abc);
//		tabPDF.addCell(cellsum);
//			abc=new Phrase("EUR");
//			cellsum.setColspan(1);
//			cellsum.setPhrase(abc);
//		tabPDF.addCell(cellsum);
//			cellsum.setColspan(3);
//			abc=new Phrase(" ");	
//			cellsum.setPhrase(abc);
//		tabPDF.addCell(cellsum); 
//	
//					
//		//ADD ONE SPACE LINES
//		cellspace = new PdfPCell(new Phrase(""));
//		cellspace.setMinimumHeight(30);
//		cellspace.setHorizontalAlignment(Element.ALIGN_CENTER);
//		cellspace.setVerticalAlignment(Element.ALIGN_MIDDLE);
//		cellspace.setColspan(7);
//		cellspace.setRowspan(1);
//		cellspace.setBorder(cellspace.NO_BORDER);
//	        tabPDF.addCell(cellspace);
//		
//		
//		
//		//prepare next dates for next loop
//		calendar.add(Calendar.DAY_OF_MONTH, 1);	
//		sdate = date.format(calendar.getTime());
//		System.out.println("startactualday "+i+" Date : " +sdate );
//		calendar.add(calendar.DAY_OF_MONTH, 1);
//		calendar.add(Calendar.DAY_OF_MONTH, -1);
//		edate = date.format(calendar.getTime());
//		System.out.println("enddate "+i+" Date : " + edate );
//		
//	}
	
	
}
