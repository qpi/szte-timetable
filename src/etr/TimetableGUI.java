package etr;

import java.awt.Button;
import java.awt.Choice;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;

public class TimetableGUI extends Frame {
	
	private static final long serialVersionUID = -1960589676396460627L;
	
	private final Choice choice = new Choice();
	private final TextField userText = new TextField();
	private final TextField passwodText = new TextField();
	private final TextArea console = new TextArea();

	public TimetableGUI() {

		super("SZTE ETR órarend exportáló");
		setSize(500, 350);
		setResizable(false);

		Panel panel = new Panel();
		panel.setLayout(new FlowLayout());

		Panel dataPanel = new Panel();
		dataPanel.setLayout(new GridLayout(3, 2, 10, 10));
		dataPanel.setSize(200, 300);

		Label semesterLabel = new Label("Szemeszter:");
		semesterLabel.setAlignment(Label.RIGHT);
		dataPanel.add(semesterLabel);
		
		int year = Calendar.getInstance().get(Calendar.YEAR);
		choice.add(new StringBuffer().append(year - 1).append('-').append(year)
				.append("-2").toString());
		choice.add(new StringBuffer().append(year).append('-').append(year + 1)
				.append("-1").toString());
		choice.add(new StringBuffer().append(year).append('-').append(year + 1)
				.append("-2").toString());
		dataPanel.add(choice);

		Label userLabel = new Label("ETR felhasználói név:");
		userLabel.setAlignment(Label.RIGHT);
		dataPanel.add(userLabel);
		
		dataPanel.add(userText);

		Label passwordLabel = new Label("ETR jelszó:");
		passwordLabel.setAlignment(Label.RIGHT);
		dataPanel.add(passwordLabel);
		
		passwodText.setEchoChar('*');
		dataPanel.add(passwodText);

		panel.add(dataPanel);

		
		Panel consolePanel = new Panel();
		consolePanel.setSize(200, 300);
		consolePanel.add(console);
		panel.add(consolePanel);

		panel.add(new Panel());

		Button sendButton = new Button("Órarend letöltése");

		sendButton.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Timetable timetable = new Timetable(userText.getText(),
						passwodText.getText(), choice.getSelectedItem());
				console.setText(timetable.getConsole());
			}
		});

		panel.add(sendButton);

		add(panel);
	}
	
	public static void main(String[] args) {
		TimetableGUI frame = new TimetableGUI();

		// show the frame
		frame.setVisible(true);

		// add the event for the exit button
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});
	}
}