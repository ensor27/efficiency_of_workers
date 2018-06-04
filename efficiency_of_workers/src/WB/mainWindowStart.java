package WB;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import PDF.PDF_Godziny_wgGodzin;
import PDF.Efficiency;

public class mainWindowStart extends JFrame {

	private JPanel contentPane;
	private JTextField dataRozpoczecia;
	private JTextField dataZakonczenia;
	private JLabel lblWprowadDzieRozpoczcia;
	private JLabel lblWprowadDzieZakoczenia;
	private JLabel lblWprowadOczekiwanLiczb;

	
	/**
	 * Launch the application.
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					mainWindowStart frame = new mainWindowStart();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	

	/**
	 * Create the frame.
	 */
	public mainWindowStart() {
		setResizable(false);
		setTitle("Efficiency of Workers");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 420, 420);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		dataRozpoczecia = new JTextField();
		dataRozpoczecia.setBounds(132, 48, 138, 38);
		contentPane.add(dataRozpoczecia);
		dataRozpoczecia.setColumns(10);
		
		dataZakonczenia = new JTextField();
		dataZakonczenia.setBounds(132, 146, 138, 38);
		contentPane.add(dataZakonczenia);
		dataZakonczenia.setColumns(10);
		
		lblWprowadDzieRozpoczcia = new JLabel("Wprowad\u017A dzie\u0144 rozpocz\u0119cia analizy, format: rrrr-MM-dd");
		lblWprowadDzieRozpoczcia.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblWprowadDzieRozpoczcia.setBounds(24, 0, 369, 57);
		contentPane.add(lblWprowadDzieRozpoczcia);
		
		lblWprowadDzieZakoczenia = new JLabel("Wprowad\u017A dzie\u0144 zako\u0144czenia analizy, format: rrrr-MM-dd");
		lblWprowadDzieZakoczenia.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblWprowadDzieZakoczenia.setBounds(24, 97, 369, 38);
		contentPane.add(lblWprowadDzieZakoczenia);
		
		JButton btnStartEfficiencyWorkers = new JButton("Start Analyze Efficiency Workers");
		btnStartEfficiencyWorkers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					System.out.println("start efficiency analyze");
					Efficiency.createRaport();
					System.exit(0);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		btnStartEfficiencyWorkers.setBounds(89, 231, 232, 45);
		contentPane.add(btnStartEfficiencyWorkers);
		
		
	}
	
	private static boolean checkDatePattern(String data) {
	    try {
	        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	        format.parse(data);
	        return true;
	    } catch (ParseException e) {
	        return false;
	    }
	}
}