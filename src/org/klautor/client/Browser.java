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
import javax.microedition.lcdui.List;

/**
 * Im Browser werden alle Termine mit den wichtigsten Daten aufgelistet.
 * der Benutzer hat die Moeglichkeit ueber verschiedene Befehle mit
 * diesen Daten zu arbeiten.
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class Browser
        extends List
        implements CommandListener {

    /**
     * Referenz zur MIDlet-Klasse fuer Callbacks
     */
    private KlautorMIDlet midlet;
    /**
     * Befehl zum Anzeigenlassen aller Informationen eines Termins
     */
    private Command view;
    /**
     * Befehl zum Sortierenlassen der Liste
     */
    private Command sort;
    /**
     * Befehl zum Bearbeiten der Notiz eines Termins
     */
    private Command edit;
    /**
     * Befehl, um zum Hauptmenue zurueckzukehren
     */
    private Command back;
    /**
     * Befehl zum Loeschen eines Termins
     */
    private Command delete;
    /**
     * Befehl zum Loeschen aller Termine
     */
    private Command deleteAll;
    /**
     * Bestimmt, ob das Auswaehlen (=Anzeigenlassen) der Termins
     * durchgefuehrt werden darf (nicht gestattet, wenn keine Termine
     * in der Liste sind.
     */
    private boolean acceptSelect;   
    /**
     * Terminevektor
     */
    private Termine termine;
        
    /**
     * Beim Erzeugen eines Objekts dieser Klasse wird das UserInterface
     * vorbereitet.
     * 
     * @param midlet	Referenz zur KlautorMIDlet-Klasse bzw. dessen Objekt
     */
    public Browser(KlautorMIDlet midlet) {
        // Ueberschrift und Listentyp setzen
        super("Terminliste", List.IMPLICIT);

        this.midlet = midlet;

        // Befehle erzeugen
        view = new Command("Ansehen", Command.ITEM, 0);
        sort = new Command("Sortieren", Command.SCREEN, 0);
        edit = new Command("Bearbeiten", Command.ITEM, 1);
        back = new Command("Zur\u00fcck", Command.BACK, 0);
        delete = new Command("L\u00f6schen", Command.ITEM, 2);
        deleteAll = new Command("Alle l\u00f6schen", Command.SCREEN, 1);

        addCommand(back);
        setCommandListener(this);

        /* 
         * Termininformationen aus dem Termine-Vektor laden und
         * anzeigen lassen.
         */
        termine = Termine.getTermine();      
        showData();
    }
    
    /**
     * Wendet Aenderungen an
     */
    public void update() {
        int selected = getSelectedIndex();
        this.deleteAll();
        showData();
        
        if(selected < size() && selected > -1) {
            this.setSelectedIndex(selected, true);
        }
    }
    
    /**
     * Zeigt die (vorhandenen) Termine an
     */
    private void showData() {
     if (termine.isEmpty()) {
            append("- Leer -", null);
            acceptSelect = false;
            removeCommand(view);
            removeCommand(sort);
            removeCommand(edit);
            removeCommand(delete);
            removeCommand(deleteAll);
        } else {
            addCommand(view);
            addCommand(sort);
            addCommand(edit);
            addCommand(delete);
            addCommand(deleteAll);
            acceptSelect = true;
            for (int i = 0; i < termine.size(); i++) {
                Termin termin = termine.terminAt(i);
                append(termin.getFach() + " " + termin.getKurstyp() + " " + termin.getDatum(), null);
            }
        }   
    }

    /**
     * Reagiert auf die Befehle des Benutzes
     */
    public void commandAction(Command c, Displayable d) {
        // Zurueck zum Hauptmenue
        if (c == back) {
            midlet.mainMenu();
        }

        // Hier Abbrechen, wenn keine Termine in der Liste sind und
        // kein Auswaehlen eines Termins moeglisch sein soll.
        if (!acceptSelect) {
            return;
        }
        
        // Termin anzeigen
        if (c == view | c == List.SELECT_COMMAND) {
            if (getSelectedIndex() != -1) {
                midlet.showDateViewer(getSelectedIndex());
            }
        } // Termin loeschen
        else if (c == delete) {
            if (getSelectedIndex() != -1) {
                midlet.activateDeleter(getSelectedIndex());
            }
        } // Alle Termine loeschen
        else if (c == deleteAll) {
            midlet.activateDeleter(-1); // -1 -> alle loeschen
        } // Notiz eines Termins bearbeiten
        else if (c == edit) {
            if (getSelectedIndex() != -1) {
                midlet.showEditor(getSelectedIndex());
            }
        } // Liste sortieren
        else if (c == sort) {
            midlet.showSorter();
        }
    }
}
