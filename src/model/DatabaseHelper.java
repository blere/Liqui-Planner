package model;

import java.sql.*;

/**
 * Verwaltet die Datenbankverbindung und -operationen für Einnahmen und Ausgaben.
 * Diese Klasse stellt Methoden zur Verfügung, um Tabellen zu erstellen,
 * Einträge hinzuzufügen, Einträge abzurufen und zu löschen.
 */
public class DatabaseHelper {
    // Datenbankkonfiguration (Ersetze Benutzername und Passwort mit deinen eigenen Werten)
    private static final String DB_URL = "jdbc:mysql://localhost:3307/LiquiPlanner";
    private static final String DB_USER = "deinBenutzername"; // Dein MySQL-Benutzername
    private static final String DB_PASSWORD = "deinPasswort"; // Dein MySQL-Passwort

    /**
     * Konstruktor: Erstellt die benötigten Tabellen und fügt Standardmonate hinzu.
     */
    public DatabaseHelper() {
        createTables();
        insertDefaultMonths();
    }

    /**
     * Erstellt die Tabellen "Monate", "Einträge", "Kategorien" und "Transaktionen".
     */
    private void createTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            // Tabelle für Monate
            String sqlMonate = "CREATE TABLE IF NOT EXISTS Monate ("
                    + "monatKey INT AUTO_INCREMENT PRIMARY KEY, "
                    + "monatName VARCHAR(20) UNIQUE NOT NULL"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            stmt.execute(sqlMonate);

            // Tabelle für Einträge
            String sqlEintraege = "CREATE TABLE IF NOT EXISTS Einträge ("
                    + "eintragKey INT AUTO_INCREMENT PRIMARY KEY, "
                    + "eintragTitel VARCHAR(255) NOT NULL, "
                    + "eintragBetrag DECIMAL(15,2) NOT NULL, "
                    + "eintragTyp ENUM('Einnahme','Ausgabe') NOT NULL, "
                    + "eintragMonat INT, "
                    + "erstellt_am TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (eintragMonat) REFERENCES Monate(monatKey) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            stmt.execute(sqlEintraege);

            // Tabelle für Kategorien (Spalten: katKey, katName)
            String sqlKategorien = "CREATE TABLE IF NOT EXISTS Kategorien ("
                    + "katKey INT AUTO_INCREMENT PRIMARY KEY, "
                    + "katName VARCHAR(100) NOT NULL UNIQUE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            stmt.execute(sqlKategorien);

            // Tabelle für Transaktionen (Spalten: transEintragId, transKatId)
            String sqlTransaktionen = "CREATE TABLE IF NOT EXISTS Transaktionen ("
                    + "transEintragId INT NOT NULL, "
                    + "transKatId INT NOT NULL, "
                    + "PRIMARY KEY (transEintragId, transKatId), "
                    + "FOREIGN KEY (transEintragId) REFERENCES Einträge(eintragKey) ON DELETE CASCADE ON UPDATE CASCADE, "
                    + "FOREIGN KEY (transKatId) REFERENCES Kategorien(katKey) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            stmt.execute(sqlTransaktionen);

            System.out.println("Tabellen überprüft oder erstellt.");
        } catch (SQLException e) {
            System.err.println("Fehler beim Erstellen der Tabellen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fügt Standardmonate in die Tabelle "Monate" ein, falls sie nicht existieren.
     */
    private void insertDefaultMonths() {
        String[] monate = { "Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September",
                "Oktober", "November", "Dezember" };
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO Monate (monatName) VALUES (?)")) {
            for (String monat : monate) {
                pstmt.setString(1, monat);
                pstmt.executeUpdate();
            }
            System.out.println("Standardmonate überprüft oder hinzugefügt.");
        } catch (SQLException e) {
            System.err.println("Fehler beim Einfügen der Standardmonate: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ruft alle Einträge aus der Tabelle "Einträge" ab.
     *
     * @return ResultSet mit allen Einträgen.
     */
    public ResultSet fetchAllEntries() {
        String sql = "SELECT eintragKey AS id, eintragTitel AS title, eintragBetrag AS amount, eintragTyp AS type, "
                + "(SELECT monatName FROM Monate WHERE monatKey = Einträge.eintragMonat) AS month "
                + "FROM Einträge";

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.err.println("Fehler beim Abrufen der Einträge: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Ruft alle Einträge für einen bestimmten Monat ab.
     *
     * @param monat Der Monatsname (z. B. "Februar").
     * @return ResultSet mit den Einträgen des angegebenen Monats.
     */
    public ResultSet fetchEntriesByMonth(String monat) {
        String sql = "SELECT eintragKey AS id, eintragTitel AS title, eintragBetrag AS amount, eintragTyp AS type, "
                + "(SELECT monatName FROM Monate WHERE monatKey = Einträge.eintragMonat) AS month "
                + "FROM Einträge WHERE eintragMonat = (SELECT monatKey FROM Monate WHERE monatName = ? LIMIT 1)";

        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            pstmt.setString(1, monat);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Fehler beim Abrufen der Monatsdaten: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Löscht einen Eintrag anhand seiner ID.
     *
     * @param id Die ID des zu löschenden Eintrags.
     */
    public void deleteEntryById(int id) {
        String sql = "DELETE FROM Einträge WHERE eintragKey = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Eintrag mit ID " + id + " wurde gelöscht.");
            } else {
                System.err.println("Kein Eintrag mit ID " + id + " gefunden.");
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Löschen des Eintrags: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Löscht alle Einträge in der Tabelle "Einträge".
     */
    public void deleteAllEntries() {
        String sql = "DELETE FROM Einträge";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            int rowsAffected = stmt.executeUpdate(sql);
            System.out.println("Alle " + rowsAffected + " Einträge wurden gelöscht.");
        } catch (SQLException e) {
            System.err.println("Fehler beim Löschen aller Einträge: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fügt einen neuen Eintrag in die Tabellen "Einträge", "Kategorien" und "Transaktionen" ein.
     *
     * @param titel  Titel des Eintrags (z. B. "Lohn", "Miete").
     * @param betrag Betrag der Einnahme oder Ausgabe.
     * @param typ    "Einnahme" oder "Ausgabe".
     * @param monat  Monat als Text (z. B. "Februar").
     */
    public void insertEntry(String titel, double betrag, String typ, String monat) {
        Connection conn = null;
        PreparedStatement pstmtMonat = null;
        PreparedStatement pstmtEintrag = null;
        PreparedStatement pstmtKategorie = null;
        PreparedStatement pstmtTransaktion = null;

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            conn.setAutoCommit(false); // Beginne Transaktion

            // 1. Monatsschlüssel (monatKey) aus der Tabelle "Monate" abrufen
            String monatQuery = "SELECT monatKey FROM Monate WHERE monatName = ? LIMIT 1";
            pstmtMonat = conn.prepareStatement(monatQuery);
            pstmtMonat.setString(1, monat);
            ResultSet rs = pstmtMonat.executeQuery();

            int monatId = -1;
            if (rs.next()) {
                monatId = rs.getInt("monatKey"); // Monat existiert, speichere die ID
            } else {
                System.err.println("Fehler: Monat wurde nicht gefunden!");
                return; // Falls der Monat nicht existiert, beenden
            }

            // 2. Eintrag in "Einträge" speichern
            String sqlEintrag = "INSERT INTO Einträge (eintragTitel, eintragBetrag, eintragTyp, eintragMonat) "
                    + "VALUES (?, ?, ?, ?)";
            pstmtEintrag = conn.prepareStatement(sqlEintrag, Statement.RETURN_GENERATED_KEYS);
            pstmtEintrag.setString(1, titel);
            pstmtEintrag.setDouble(2, betrag);
            pstmtEintrag.setString(3, typ);
            pstmtEintrag.setInt(4, monatId);
            pstmtEintrag.executeUpdate();

            // 3. Automatisch generierte ID des Eintrags abrufen
            rs = pstmtEintrag.getGeneratedKeys();
            int eintragId = -1;
            if (rs.next()) {
                eintragId = rs.getInt(1);
            }

            // 4. Eintrag in "Kategorien" speichern (falls nicht vorhanden)
            String sqlKategorie = "INSERT IGNORE INTO Kategorien (katName) VALUES (?)";
            pstmtKategorie = conn.prepareStatement(sqlKategorie);
            pstmtKategorie.setString(1, titel);
            pstmtKategorie.executeUpdate();

            // 5. Eintrag in "Transaktionen" speichern
            String sqlTransaktion = "INSERT INTO Transaktionen (transEintragId, transKatId) "
                    + "VALUES (?, (SELECT katKey FROM Kategorien WHERE katName = ? LIMIT 1))";
            pstmtTransaktion = conn.prepareStatement(sqlTransaktion);
            pstmtTransaktion.setInt(1, eintragId);
            pstmtTransaktion.setString(2, titel);
            pstmtTransaktion.executeUpdate();

            // 6. Transaktion abschliessen
            conn.commit();
            System.out.println("Eintrag erfolgreich gespeichert: " + titel);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Bei Fehler: Änderungen zurücksetzen
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Fehler beim Speichern des Eintrags: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Ressourcen schliessen
            try {
                if (pstmtMonat != null)
                    pstmtMonat.close();
                if (pstmtEintrag != null)
                    pstmtEintrag.close();
                if (pstmtKategorie != null)
                    pstmtKategorie.close();
                if (pstmtTransaktion != null)
                    pstmtTransaktion.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
