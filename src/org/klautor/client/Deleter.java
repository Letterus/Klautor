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

import java.io.IOException;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;

/**
 * Der Deleter hat die simple Aufgabe, einen oder alle Termine
 * nach Bestaetigung des Benutzers zu loeschen.
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class Deleter
        extends Form
        implements CommandListener {

    /**
     * Referenz zum KlautorMIDlet fuer Callbacks
     */
    private KlautorMIDlet midlet;
    /**
     * Nummer des zu loeschenden Termins im Termine-Vektor
     * (-1, wenn alle Termine geloescht werden sollen)
     */
    private int dateNum;
    /**
     * Befehl, um den Loeschvorgang zu bestaetigen
     */
    private Command yes;
    /**
     * Befehl, um den Loeschvorgang zu negieren
     */
    private Command no;

    /**
     * Beim Erzeugen eines Objekts dieser Klasse wird das UserInterface
     * vorbereitet
     * 
     * @param dateNum	Die Nummer des zu loeschenden Termins im Vektor.
     * -1, wenn alle Termine geloescht werden sollen.
     * 
     * @param midlet	Referenz zum KlautorMIDlet
     */
    public Deleter(int dateNum, KlautorMIDlet midlet) {
        super("L\u00f6schen");
        this.dateNum = dateNum;
        this.midlet = midlet;
        try {
            append(new ImageItem("", Image.createImage(Image.createImage("/warnung.png")), ImageItem.LAYOUT_LEFT, "Warning"));
        } catch (IOException ex) {
        }
        if (dateNum != -1) {
            append("Soll der Termin wirklich gel\u00f6scht werden?");
        } else {
            append("Sollen alle Termine wirklich gel\u00f6scht werden?");
        }
        yes = new Command("Ja", Command.OK, 0);
        no = new Command("Nein", Command.CANCEL, 0);
        addCommand(yes);
        addCommand(no);
        setCommandListener(this);
    }

    /**
     * Reagiert auf Befehle des Benutzers
     */
    public void commandAction(Command c, Displayable d) {
        if (c == yes) {
            // Alle Termine loeschen
            if (dateNum != -1) {
                Termine.getTermine().removeElementAt(dateNum);
                Database.getDatabase().explicitSave();
                Message.show("Termin gel\u00f6scht!", AlertType.CONFIRMATION);
            } // Oder nur einen (angegebenen)
            else {
                Termine.getTermine().removeAllElements();
                Database.getDatabase().explicitSave();
                Message.show("Alle Termine gel\u00f6scht!", AlertType.CONFIRMATION);
            }
            midlet.showBrowser(true);
        } else if (c == no) {
            midlet.showBrowser(false);
        }
    }
}
