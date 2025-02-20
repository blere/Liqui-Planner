package controller;

import model.DatabaseHelper;
import model.ListItem;
import view.MainView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Der MainController verwaltet die Interaktionen zwischen der View und der
 * Datenbank. Er übernimmt das Laden der Daten, das Anwenden des Monatsfilters,
 * die Bilanzberechnung, das Hinzufügen neuer Einträge sowie das Löschen
 * ausgewählter Datensätze.
 */
public class MainController {
	private final MainView view;
	private final DatabaseHelper dbHelper;

	/**
	 * Konstruktor: Initialisiert den Controller, richtet ActionListener ein und
	 * lädt die Daten.
	 *
	 * @param view die zugehörige Benutzeroberfläche.
	 */
	public MainController(MainView view) {
		this.view = view;
		this.dbHelper = new DatabaseHelper();
		initialize();
		loadEntriesFromDatabase();
	}

	/**
	 * Initialisiert die ActionListener für die GUI-Elemente.
	 */
	private void initialize() {
		view.getAddButton().addActionListener(e -> addEntry());
		view.getDeleteButton().addActionListener(e -> deleteSelectedEntry());
		view.getFilterMonthCombo().addActionListener(e -> applyMonthFilter());

		if (view.getSaveMenuItem() != null) {
			view.getSaveMenuItem().addActionListener(e -> view.saveToPDF());
		}
		if (view.getPrintMenuItem() != null) {
			view.getPrintMenuItem().addActionListener(e -> view.printTable());
		}
		if (view.getExitMenuItem() != null) {
			view.getExitMenuItem().addActionListener(e -> System.exit(0));
		}
	}

	/**
	 * Lädt alle Einträge aus der Datenbank in das TableModel der GUI.
	 * 
	 * Hier werden die Spaltenwerte formatiert: - Bei Einnahmen wird in der Spalte
	 * "Betrag (CHF)" ein Pluszeichen vorangestellt. - Bei Ausgaben wird der Betrag
	 * als negativer Wert angezeigt.
	 */
	private void loadEntriesFromDatabase() {
		DefaultTableModel tableModel = (DefaultTableModel) view.getTable().getModel();
		tableModel.setRowCount(0);
		try {
			ResultSet rs = dbHelper.fetchAllEntries();
			while (rs.next()) {
				int id = rs.getInt("id");
				String title = rs.getString("title");
				double amount = rs.getDouble("amount");
				String type = rs.getString("type");
				String month = rs.getString("month");
				String formattedAmount;
				if ("Einnahme".equals(type)) {
					formattedAmount = String.format("+%.2f CHF", amount);
				} else {
					formattedAmount = String.format("-%.2f CHF", Math.abs(amount));
				}
				tableModel.addRow(new Object[] { id, title, formattedAmount, type, month });
			}
			applyMonthFilter();
			updateBalance();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Wendet den Monatsfilter an und aktualisiert das Tabellenmodel sowie die
	 * Listen.
	 * 
	 * In der Listenanzeige: - Bei Einnahmen wird der Text im Format "Titel -
	 * +Betrag CHF (Monat)" angezeigt. - Bei Ausgaben wird der Text im Format "Titel
	 * - -Betrag CHF" angezeigt, wobei nur ein Minus erscheint.
	 */
	private void applyMonthFilter() {
		String selectedMonth = (String) view.getFilterMonthCombo().getSelectedItem();
		DefaultTableModel filteredModel = new DefaultTableModel(
				new String[] { "ID", "Titel", "Betrag (CHF)", "Typ", "Monat" }, 0);
		DefaultListModel<ListItem> incomeModel = new DefaultListModel<>();
		DefaultListModel<ListItem> expenseModel = new DefaultListModel<>();

		try {
			ResultSet rs;
			if ("Alle Monate".equals(selectedMonth)) {
				rs = dbHelper.fetchAllEntries();
			} else {
				rs = dbHelper.fetchEntriesByMonth(selectedMonth);
			}
			boolean hasIncome = false;
			boolean hasExpense = false;
			while (rs.next()) {
				int id = rs.getInt("id");
				String title = rs.getString("title");
				double amount = rs.getDouble("amount");
				String type = rs.getString("type");
				String month = rs.getString("month");
				String formattedAmount;
				if ("Einnahme".equals(type)) {
					formattedAmount = String.format("+%.2f CHF", amount);
					incomeModel.addElement(new ListItem(title + "  " + formattedAmount + " (" + month + ")"));
					hasIncome = true;
				} else {
					formattedAmount = String.format("-%.2f CHF", Math.abs(amount));
					expenseModel.addElement(new ListItem(title + "  " + formattedAmount + " (" + month + ")"));
					hasExpense = true;
				}
				filteredModel.addRow(new Object[] { id, title, formattedAmount, type, month });
			}
			if (!hasIncome) {
				incomeModel.addElement(new ListItem("Keine Einnahmen für " + selectedMonth));
			}
			if (!hasExpense) {
				expenseModel.addElement(new ListItem("Keine Ausgaben für " + selectedMonth));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		view.setTableModel(filteredModel);
		view.getIncomeList().setModel(incomeModel);
		view.getExpenseList().setModel(expenseModel);
		updateBalance();
	}

	/**
	 * Aktualisiert die Bilanzanzeige (Einnahmen, Ausgaben, Gesamtsaldo).
	 * 
	 * Die Bilanz wird als Differenz berechnet: Summe der Einnahmen minus Summe der
	 * Ausgaben. Dabei werden nur die Einträge berücksichtigt, die dem aktuell
	 * gewählten Monatsfilter entsprechen. In der Anzeige: - Das Einnahmen-Label
	 * erscheint mit einem vorangestellten "+" in dunkelgrün. - Das Ausgaben-Label
	 * erscheint mit einem vorangestellten "-" in rot. - Das Bilanz-Label zeigt die
	 * Differenz; ist sie >= 0, erscheint sie in dunkelgruen (mit "+"), andernfalls
	 * in rot.
	 */
	private void updateBalance() {
		double sumEinnahmen = 0.0;
		double sumAusgaben = 0.0;
		// Hole den aktuell gewählten Monat aus dem Filter
		String selectedMonth = (String) view.getFilterMonthCombo().getSelectedItem();
		try {
			ResultSet rs;
			if ("Alle Monate".equals(selectedMonth)) {
				rs = dbHelper.fetchAllEntries();
			} else {
				rs = dbHelper.fetchEntriesByMonth(selectedMonth);
			}
			while (rs.next()) {
				double amount = rs.getDouble("amount");
				String type = rs.getString("type");
				if ("Einnahme".equals(type)) {
					sumEinnahmen += amount;
				} else if ("Ausgabe".equals(type)) {
					sumAusgaben += Math.abs(amount);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		double bilanz = sumEinnahmen - sumAusgaben;
		view.getIncomeLabel().setText(String.format("Einnahmen: %+.2f CHF", sumEinnahmen));
		view.getExpenseLabel().setText(String.format("Ausgaben: -%.2f CHF", sumAusgaben));
		view.getBalanceLabel().setText(String.format("Bilanz: %+.2f CHF", bilanz));
		view.getIncomeLabel().setForeground(new Color(0, 100, 0)); // dunkelgrün für Einnahmen
		view.getExpenseLabel().setForeground(Color.RED); // rot für Ausgaben
		if (bilanz >= 0) {
			view.getBalanceLabel().setForeground(new Color(0, 100, 0)); // dunkelgrün, wenn Bilanz positiv oder 0
		} else {
			view.getBalanceLabel().setForeground(Color.RED); // rot, wenn Bilanz negativ
		}
	}

	/**
	 * Fügt einen neuen Eintrag zur Datenbank hinzu.
	 * 
	 * Liest die Eingabefelder aus der View aus, überprüft diese und fügt dann
	 * den Eintrag über DatabaseHelper in die Datenbank ein. Falls der Typ
	 * "Ausgabe" ist, wird der eingegebene Betrag in einen negativen Wert
	 * umgewandelt.
	 */
	private void addEntry() {
		String title = view.getTitleField().getText();
		String amountText = view.getAmountField().getText();
		String type = (String) view.getTypeCombo().getSelectedItem();
		String month = (String) view.getMonthCombo().getSelectedItem();

		if (title.isEmpty() || amountText.isEmpty()) {
			JOptionPane.showMessageDialog(view, "Bitte alle Felder ausfüllen!", "Fehler", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			double amount = Double.parseDouble(amountText);
			if ("Ausgabe".equals(type)) {
				amount = -Math.abs(amount);
			} else {
				amount = Math.abs(amount);
			}
			dbHelper.insertEntry(title, amount, type, month);
			loadEntriesFromDatabase();
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(view, "Betrag muss eine Zahl sein!", "Fehler", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Löscht den aktuell ausgewählten Eintrag.
	 * 
	 * Ermittelt die ID des ausgewählten Eintrags (aus dem zugrunde liegenden
	 * TableModel) und löscht diesen über DatabaseHelper. Anschliessend werden die
	 * Daten neu geladen.
	 */
	private void deleteSelectedEntry() {
		int selectedRow = view.getTable().getSelectedRow();
		if (selectedRow != -1) {
			Object idObj = ((DefaultTableModel) view.getTable().getModel()).getValueAt(selectedRow, 0);
			try {
				int id = Integer.parseInt(idObj.toString());
				dbHelper.deleteEntryById(id);
				loadEntriesFromDatabase();
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(view, "Fehler: Ungültige ID.", "Fehler", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(view, "Bitte eine Zeile zum Löschen auswählen!", "Fehler",
					JOptionPane.WARNING_MESSAGE);
		}
	}
}
