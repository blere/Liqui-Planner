package model;

/**
 * Modelliert eine Tabellenzeile mit drei Spalten.
 * 
 * Diese Klasse kann verwendet werden, um einzelne Zeilen zu kapseln, falls ein
 * benutzerdefiniertes TableModel benötigt wird.
 */
public class TableEntry {
	private String column1;
	private String column2;
	private String column3;

	// Konstruktor.

	// @param column1 Wert der ersten Spalte.
	// @param column2 Wert der zweiten Spalte.
	// @param column3 Wert der dritten Spalte.

	public TableEntry(String column1, String column2, String column3) {
		this.column1 = column1;
		this.column2 = column2;
		this.column3 = column3;
	}

	// Gibt den Wert der ersten Spalte zurück.
	// @return Der Wert der ersten Spalte.
	public String getColumn1() {
		return column1;
	}

	// Setzt den Wert der ersten Spalte.
	// @param column1 Der neue Wert.
	public void setColumn1(String column1) {
		this.column1 = column1;
	}

	// Gibt den Wert der zweiten Spalte zurück.
	// @return Der Wert der zweiten Spalte.
	public String getColumn2() {
		return column2;
	}

	// Setzt den Wert der zweiten Spalte.
	// @param column2 Der neue Wert.
	public void setColumn2(String column2) {
		this.column2 = column2;
	}

	// Gibt den Wert der dritten Spalte zurück.
	// @return Der Wert der dritten Spalte.
	public String getColumn3() {
		return column3;
	}

	// Setzt den Wert der dritten Spalte.
	// @param column3 Der neue Wert.
	public void setColumn3(String column3) {
		this.column3 = column3;
	}
}
