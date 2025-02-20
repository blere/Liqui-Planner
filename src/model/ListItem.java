package model;

/**
 * Modelliert einen einzelnen Listeneintrag.
 * 
 * Jeder ListItem enthält einen Namen (String), der in den JLists der GUI
 * angezeigt wird.
 */
public class ListItem {
	private String name;

	// Konstruktor.
	// @param name der anzuzeigende Name des Listeneintrags.
	public ListItem(String name) {
		this.name = name;
	}

	// Gibt den Namen des Eintrags zurück.
	// @return Der Name.
	public String getName() {
		return name;
	}

	// Setzt den Namen des Eintrags.
	// @param name Der neue Name.
	public void setName(String name) {
		this.name = name;
	}

	// Überschreibt die toString()-Methode, sodass der Name in den Listen angezeigt
	// wird.
	// @return Der Name des Listeneintrags.
	@Override
	public String toString() {
		return name;
	}
}
