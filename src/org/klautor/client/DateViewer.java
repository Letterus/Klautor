//Klautor - An MIDP-MIDlet for downloading and organizing dates of exams
//Copyright (C) 2004  Johannes Brakensiek <johannes @quackes.de>
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
import javax.microedition.lcdui.StringItem;

/**
 * Diese Klasse stellt eine Benutzeroeberflaeche dar, mit dessen Hilfe
 * der Benutzer alle Informationen/Eigenschaften eines Termins (bei grossen
 * Displays) mit einem Blick ansehen kann.
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class DateViewer
        extends Form
        implements CommandListener {

    /**
     * Referenz zum MIDlet
     */
    private KlautorMIDlet midlet;
    /**
     * Nummer des Termins in dem Termine-Vektor
     */
    private int dateNum;
    /**
     * Container fuer die Fach-Informationen
     */
    private StringItem fach;
    /** 
     * Container fuer die Lehrer-Informationen
     */
    private StringItem lehrer;
    /**
     * Container fuer die Datums-Informationen
     */
    private StringItem datum;
    /**
     * Container fuer die Raum-Informationen
     */
    private StringItem raum;
    /**
     * Container fuer Informationen ueber das Thema der Klausur
     */
    private StringItem thema;
    /**
     * Container fuer Notiz-Informationen
     */
    private StringItem notiz;
    /**
     * Befehl, um zum Browser zurueckzugelangen
     */
    private Command back;
    /**
     * Befehl, um das Loeschen des Termins zu veranlassen
     */
    private Command delete;
    /**
     * Befehl, um das Bearbeiten der Terminnotiz zu veranlassen
     */
    private Command edit;

    /**
     * Legt die Befehle an und fuegt sie der Liste hinzu, fragt die
     * Termininformationen ab und zeigt sie an.
     * 
     * @param dateNum Die Nummer des Termins im Termin-Vektor
     */
    public DateViewer(int dateNum, KlautorMIDlet midlet) {
        super("Terminansicht");
        this.midlet = midlet;
        this.dateNum = dateNum;

        back = new Command("Zur\u00fcck", Command.BACK, 2);
        delete = new Command("L\u00f6schen", Command.SCREEN, 1);
        edit = new Command("Bearb.", Command.SCREEN, 0);
        addCommand(back);
        addCommand(delete);
        addCommand(edit);
        setCommandListener(this);

        Termin termin = Termine.getTermine().terminAt(dateNum);
        fach = new StringItem("Fach: ", termin.getFach() + " " + termin.getKurstyp());
        lehrer = new StringItem("Lehrer: ", termin.getLehrer());
        datum = new StringItem("Datum: ", termin.getDatum());
        raum = new StringItem("Raum: ", termin.getRaum());
        thema = new StringItem("Thema: ", termin.getThema());
        notiz = new StringItem("Notiz: ", termin.getNotiz());

        append(fach);
        append(lehrer);
        append(datum);
        append(raum);
        append(thema);
        append(notiz);
    }

    /**
     * Reagiert auf Aktionen des Benutzers
     */
    public void commandAction(Command c, Displayable d) {
        // Zurueck zum Browser
        if (c == back) {
            midlet.showBrowser(false);
        } // Angezeigten Termin loeschen
        else if (c == delete) {
            midlet.activateDeleter(dateNum);
        } // Termin (Notiz) editieren
        else if (c == edit) {
            midlet.showEditor(dateNum);
        }
    }
}
