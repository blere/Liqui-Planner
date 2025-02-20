package app;

import controller.MainController;
import view.MainView;

/**
 * Startet die Liquid-Planner Anwendung.
 * 
 * Diese Klasse erstellt ein Exemplar der MainView und instanziiert
 * anschliessend den MainController, welcher die Interaktionen zwischen der View
 * und der Datenbank (über DatabaseHelper) steuert. Zum Schluss wird die GUI
 * sichtbar gemacht.
 */
public class Main {
	public static void main(String[] args) {
		// Erstelle die Benutzeroberflaeche (View)
		MainView view = new MainView();
		// Erstelle den Controller, der die Interaktion zwischen View und Model
		// (Datenbank) übernimmt
		new MainController(view);
		// Zeige die GUI an
		view.setVisible(true);
	}
}
