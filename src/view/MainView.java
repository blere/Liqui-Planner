package view;

import model.ListItem;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Stellt die Benutzeroberflaeche für den Liquid-Planner bereit.
 * 
 * Diese Klasse erstellt und konfiguriert alle GUI-Komponenten, wie
 * Eingabefelder, Buttons, Tabellen, Listen und Menüs. Dabei wird die ID-Spalte
 * im zugrunde liegenden TableModel beibehalten, aber aus der Ansicht entfernt,
 * so dass der Endbenutzer sie nicht sieht. Zudem werden die ScrollPanes für die
 * Listen so eingestellt, dass horizontale Scrollbalken angezeigt werden (falls
 * notwendig), und die feste Zellbreite der Listen ist auf 400 Pixel gesetzt, um
 * den angezeigten Text etwas zu begrenzen.
 */
public class MainView extends JFrame {

	// Diese Serialnummer hilft, beim Laden zu prüfen, ob die Klasse unverändert ist.
	// Ohne sie erscheint eine Warnung, da sonst keine serialVersionUID definiert ist.
	private static final long serialVersionUID = 1L;

	// Eingabefelder und ComboBoxen
	private JTextField titleField;
	private JTextField amountField;
	private JComboBox<String> typeCombo;
	private JComboBox<String> monthCombo;
	private JButton addButton;

	// Tabelle für die Datensätze
	private JTable table;

	// Labels für die Bilanzübersicht
	private JLabel incomeLabel;
	private JLabel expenseLabel;
	private JLabel balanceLabel;

	// Buttons und Filter-Elemente
	private JButton deleteButton;
	private JComboBox<String> filterMonthCombo;
	private JList<ListItem> incomeList;
	private JList<ListItem> expenseList;

	// Menüelemente
	private JMenuItem saveMenuItem;
	private JMenuItem printMenuItem;
	private JMenuItem exitMenuItem;

	/**
	 * Konstruktor für die MainView.
	 * 
	 * Initialisiert und ordnet alle GUI-Komponenten an, richtet die Menüleiste,
	 * Panels und Listener ein.
	 */
	public MainView() {
		setTitle("Liquid-Planner");
		setSize(1200, 700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		// Menüleiste einrichten
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("Datei");
		saveMenuItem = new JMenuItem("Speichern als PDF");
		printMenuItem = new JMenuItem("Drucken");
		exitMenuItem = new JMenuItem("Beenden");
		fileMenu.add(saveMenuItem);
		fileMenu.add(printMenuItem);
		fileMenu.add(exitMenuItem);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);

		// Eingabepanel erstellen
		// Das Eingabepanel wird in diesem Beispiel weiterhin im West-Bereich platziert.
		JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
		inputPanel.setBorder(new TitledBorder("Eingabe und Listen"));
		inputPanel.setPreferredSize(new Dimension(500, 0));

		// Formular-Panel für Titel, Betrag, Typ und Monat
		JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
		titleField = new JTextField();
		amountField = new JTextField();
		typeCombo = new JComboBox<>(new String[] { "Einnahme", "Ausgabe" });
		monthCombo = new JComboBox<>(new String[] { "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli",
				"August", "September", "Oktober", "November", "Dezember" });
		addButton = new JButton("Hinzufügen");

		formPanel.add(new JLabel("Titel:"));
		formPanel.add(titleField);
		formPanel.add(new JLabel("Betrag (CHF):"));
		formPanel.add(amountField);
		formPanel.add(new JLabel("Typ:"));
		formPanel.add(typeCombo);
		formPanel.add(new JLabel("Monat:"));
		formPanel.add(monthCombo);
		formPanel.add(new JLabel()); // Platzhalter für Layout
		formPanel.add(addButton);

		inputPanel.add(formPanel, BorderLayout.NORTH);

		// Listen für Einnahmen und Ausgaben 
		JPanel listsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
		incomeList = new JList<>(new DefaultListModel<>());
		expenseList = new JList<>(new DefaultListModel<>());

		// Custom CellRenderer: Einnahmen in dunkelgrün
		incomeList.setCellRenderer(new DefaultListCellRenderer() {

			// Diese Serialnummer verhindert, dass eine Warnung erscheint, weil die Klasse
			// keinen serialVersionUID hat.
			private static final long serialVersionUID = 1L;
			private final Color darkGreen = new Color(0, 100, 0);

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				c.setForeground(darkGreen);
				return c;
			}
		});
		// Custom CellRenderer: Ausgaben in rot
		expenseList.setCellRenderer(new DefaultListCellRenderer() {

			// Diese Serialnummer verhindert, dass eine Warnung erscheint, da ansonsten
			// keine serialVersionUID definiert ist.
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				c.setForeground(Color.RED);
				return c;
			}
		});
		// Feste Zellbreite auf 200 Pixel – falls der Text länger ist, erscheint ein
		// horizontaler Scrollbalken.
		incomeList.setFixedCellWidth(200);
		expenseList.setFixedCellWidth(200);

		// Erstelle ScrollPanes für die Listen und setze den horizontalen Scrollbalken
		// auf "AS_NEEDED"
		JScrollPane incomeScrollPane = new JScrollPane(incomeList);
		incomeScrollPane.setBorder(new TitledBorder("Einnahmen"));
		incomeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JScrollPane expenseScrollPane = new JScrollPane(expenseList);
		expenseScrollPane.setBorder(new TitledBorder("Ausgaben"));
		expenseScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		listsPanel.add(incomeScrollPane);
		listsPanel.add(expenseScrollPane);

		inputPanel.add(listsPanel, BorderLayout.CENTER);

		// Filter-Panel für Monat und Löschen
		JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		filterPanel.setBorder(new TitledBorder("Monatsfilter"));
		filterMonthCombo = new JComboBox<>(new String[] { "Alle Monate", "Januar", "Februar", "März", "April", "Mai",
				"Juni", "Juli", "August", "September", "Oktober", "November", "Dezember" });
		deleteButton = new JButton("Auswahl löschen");
		filterPanel.add(new JLabel("Monat:"));
		filterPanel.add(filterMonthCombo);
		filterPanel.add(deleteButton);

		// Tabelle einrichten 
		DefaultTableModel tableModel = new DefaultTableModel(new Object[][] {},
				new String[] { "ID", "Titel", "Betrag (CHF)", "Typ", "Monat" });
		table = new JTable(tableModel);
		table.setRowHeight(25);
		table.setFillsViewportHeight(true);
		// Die ID-Spalte wird später über die Methode setTableModel(...) aus der
		// Ansicht entfernt.
		JScrollPane tableScrollPane = new JScrollPane(table);

		// Bilanz-Panel
		JPanel balancePanel = new JPanel(new GridLayout(1, 3, 10, 10));
		balancePanel.setBorder(new TitledBorder("Bilanz"));
		incomeLabel = new JLabel("Einnahmen: 0.00 CHF");
		expenseLabel = new JLabel("Ausgaben: 0.00 CHF");
		balanceLabel = new JLabel("Bilanz: 0.00 CHF");
		balancePanel.add(incomeLabel);
		balancePanel.add(expenseLabel);
		balancePanel.add(balanceLabel);

		// Komponenten in das Hauptfenster einfügen
		add(filterPanel, BorderLayout.NORTH);
		add(inputPanel, BorderLayout.WEST);
		add(tableScrollPane, BorderLayout.CENTER);
		add(balancePanel, BorderLayout.SOUTH);

		// Menü-ActionListener 
		saveMenuItem.addActionListener(e -> saveToPDF());
		printMenuItem.addActionListener(e -> printTable());
		exitMenuItem.addActionListener(e -> System.exit(0));
	}

	/**
	 * Setzt das TableModel der JTable und entfernt anschliessend die ID-Spalte aus
	 * der Ansicht.
	 * 
	 * Die ID-Spalte bleibt im zugrunde liegenden Model erhalten, wird aber aus der
	 * GUI entfernt, so dass der Endbenutzer sie nicht sieht.
	 *
	 * @param model das zu setzende DefaultTableModel.
	 */
	public void setTableModel(DefaultTableModel model) {
		table.setModel(model);
		if (table.getColumnCount() > 0 && "ID".equals(table.getColumnName(0))) {
			table.removeColumn(table.getColumnModel().getColumn(0));
		}
	}

	/**
	 * Exportiert die Inhalte der Tabelle als PDF.
	 * 
	 * Der PDF-Export berücksichtigt nur die in der GUI sichtbaren Spalten (ohne die
	 * ID-Spalte). Wird der Dialog abgebrochen oder geschlossen, wird die Methode
	 * ohne weitere Aktion beendet.
	 */
	public void saveToPDF() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Speicherort wählen");
		fileChooser.setSelectedFile(new File("LiquiPlanner_Raport.pdf"));

		int userSelection = fileChooser.showSaveDialog(this);
		if (userSelection != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File fileToSave = fileChooser.getSelectedFile();
		if (fileToSave == null) {
			return;
		}
		try {
			Document document = new Document();
			PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
			document.open();
			document.add(new Paragraph("Liqui-Planner Bericht\n\n"));
			PdfPTable pdfTable = new PdfPTable(4);
			pdfTable.addCell("Titel");
			pdfTable.addCell("Betrag (CHF)");
			pdfTable.addCell("Typ");
			pdfTable.addCell("Monat");
			for (int i = 0; i < table.getRowCount(); i++) {
				for (int j = 0; j < table.getColumnCount(); j++) {
					pdfTable.addCell(table.getValueAt(i, j).toString());
				}
			}
			document.add(pdfTable);
			document.close();
			JOptionPane.showMessageDialog(this, "PDF gespeichert unter: " + fileToSave.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Druckt die Inhalte der Tabelle.
	 */
	public void printTable() {
		try {
			table.print();
		} catch (PrinterException e) {
			e.printStackTrace();
		}
	}

	// Getter-Methoden für die GUI-Komponenten 

	public JTable getTable() {
		return table;
	}

	public JComboBox<String> getFilterMonthCombo() {
		return filterMonthCombo;
	}

	public JLabel getIncomeLabel() {
		return incomeLabel;
	}

	public JLabel getExpenseLabel() {
		return expenseLabel;
	}

	public JLabel getBalanceLabel() {
		return balanceLabel;
	}

	public JMenuItem getSaveMenuItem() {
		return saveMenuItem;
	}

	public JMenuItem getPrintMenuItem() {
		return printMenuItem;
	}

	public JMenuItem getExitMenuItem() {
		return exitMenuItem;
	}

	public JTextField getTitleField() {
		return titleField;
	}

	public JTextField getAmountField() {
		return amountField;
	}

	public JComboBox<String> getTypeCombo() {
		return typeCombo;
	}

	public JComboBox<String> getMonthCombo() {
		return monthCombo;
	}

	public JButton getAddButton() {
		return addButton;
	}

	public JButton getDeleteButton() {
		return deleteButton;
	}

	public JList<ListItem> getIncomeList() {
		return incomeList;
	}

	public JList<ListItem> getExpenseList() {
		return expenseList;
	}
}
