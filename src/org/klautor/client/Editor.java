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

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;

/**
 * Mit dem Editor-Bildschirm kann der Benutzer das Notiz-Attribut
 * eines im Browser ausgewaehlten Termins bearbeiten
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class Editor
        extends Form
        implements CommandListener {

    /**
     * Referenz zum KlautorMIDlet fuer Callbacks
     */
    private KlautorMIDlet midlet;
    /**
     * Nummer des zu bearbeitenden Termins im Termine-Vektor
     */
    private int dateNum;
    /**
     * Aus dem Vektor geladener Termin
     */
    private Termin termin;
    /**
     * Textfeld zur Anzeige und Bearbeitung des Notiz-Attributs
     */
    private TextField tf;
    /**
     * Befehl, um zu signalisieren, dass fertig bearbeitet wurde
     */
    private Command ok;
    /**
     * Befehl, um abzubrechen und Aenderungen rueckgaengig zu machen
     */
    private Command cancel;

    /**
     * Beim Erzeugen eines Objekts dieser Klasse, wird das UserInterface
     * vorbereitet.
     * 
     * @param dateNum	Nummer des zu bearbeitenden Termins im Termine-Vektor
     * @param midlet	Referenz zum KlautorMIDlet
     */
    public Editor(int dateNum, KlautorMIDlet midlet) {
        super("Termin bearbeiten");
        this.dateNum = dateNum;
        this.midlet = midlet;

        termin = Termine.getTermine().terminAt(dateNum);
        Ticker ticker = new Ticker(termin.getFach() + " " + termin.getKurstyp() + " am " + termin.getDatum());
        setTicker(ticker);

        ok = new Command("Fertig", Command.OK, 2);
        cancel = new Command("Abbrechen", Command.CANCEL, 2);
        addCommand(ok);
        addCommand(cancel);
        setCommandListener(this);

        String notiz = termin.getNotiz();
        if(notiz.equals("n/a")) {
            notiz = "";
        }
        tf = new TextField("Notiz zu dem Termin", notiz, 255, TextField.ANY);
        append(tf);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == ok) {
            // Aenderung speichern und zurueckkehren
            termin.setNotiz(tf.getString());
            Database.getDatabase().explicitSave();
            Message.show("Notiz gespeichert!", AlertType.CONFIRMATION);
            midlet.showBrowser(true);
        } else if (c == cancel) {
            // Anderung verwerfen und zurueckkehren
            midlet.showBrowser(false);
        }
    }
}
