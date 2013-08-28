//Klautor - An MIDP-MIDlet for downloading and organizing dates of exams
//Copyright (C) 2004, 2008  Johannes Brakensiek <johannes @quackes.de>
// 
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
// 
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
package org.klautor.client;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 * Diese Klasse praesentiert ein Formular, in dem Einstellungen
 * zur Quelldatei, aus welcher die Termin-Daten geladen werden sollen 
 * und zur Jahrgangsstufe, in der sich der Benutzer befindet, gemacht
 * werden koennen.
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class PrefsMenu
        extends Form
        implements CommandListener {

    /**
     * Referenz zum KlautorMIDlet
     */
    private KlautorMIDlet midlet;
    /**
     * Eingabe-Feld fuer die Quelldatei-URL
     */
    private TextField quelld;
    /**
     * Eingabe-Feld fuer die Jahrgangsstufe
     */
    private TextField stufe;
    /**
     * Befehl zum Speichern und Beenden
     */
    private Command ok;
    /**
     * Befehl zum Abbrechen
     */
    private Command cancel;
    /**
     * Referenz zur Datenbank, in der die Einstellungen gespeichert
     * werden.
     */
    private Database db;

    public PrefsMenu(KlautorMIDlet midlet) {
        super("Einstellungen");
        this.midlet = midlet;

        //Referenz zur Instanz der Datenbank-Klasse aholen
        db = Database.getDatabase();

        String dbquelld = db.getQuelldatei();
        if(dbquelld == null || dbquelld.equals("")) {
            dbquelld = "http://";
        }
        quelld = new TextField("Wo kommen die Daten her?", dbquelld, 255, TextField.ANY);       
        stufe = new TextField("In welcher Stufe bist du?", db.getStufe(), 2, TextField.NUMERIC);
        dbquelld = null;
        
        append(quelld);
        append(stufe);

        ok = new Command("Speichern", Command.OK, 0);
        cancel = new Command("Abbrechen", Command.BACK, 0);
        addCommand(ok);
        addCommand(cancel);
        setCommandListener(this);
    }

    /**
     * Reagiert auf Aktionen des Benutzers
     */
    public void commandAction(Command c, Displayable d) {
        // Abbrechen...
        if (c == cancel) {
            midlet.mainMenu();
        } // Speichern und zurueckkehren
        else if (c == ok) {
            db.setQuelldatei(quelld.getString().trim());
            db.setStufe(stufe.getString().trim());
            midlet.mainMenu();
        }
    }
}
