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
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.Ticker;

/**
 * Diese Klasse ist dafuer zustaendig, dass die (kurz zuvor) heruntergeladenen
 * Termininformationen nacheinander angezeigt werden. Der Benutzer hat
 * dann die Moeglichkeit zu entscheiden, ob der angezeigte Termin fuer
 * ihn relevant ist oder nicht. Im ersten Fall passiert nichts, im zweiten
 * wird der entsprechende Termin geloescht.
 * Ist der Vorgang beendet, wird die Kontrolle (nach Befehl des Benutzers)
 * wieder der MIDlet-Klasse uebergeben.
 * 
 * NOTE: Enthaelt einige Workarounds, da die Klasse Form nicht immer das
 * gewuenschte Verhalten zeigte. Ueber den eigenartigen Code also bitte nicht
 * wundern - Verbesserungsvorschlaege wuerde ich aber gerne umsetzen..
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class LoadedChooser
        extends Form
        implements CommandListener {

    /**
     * Die MIDlet-Klasse (fuer Callbacks)
     */
    private KlautorMIDlet midlet;
    /**
     * Befehle zur Einflussnahme des Vorgangs
     */
    private Command rel, nonRel, finished, cancel, del, save;
    /**
     * Der momentan angezeigte Termin
     */
    private Termin termin;
    /**
     * Die Nummer des Termins in der Datenbank (im Vektor)
     */
    private int terminNum;
    /**
     * Container fuer die Termin-Informationen
     */
    private StringItem fach, lehrer, datum, raum, thema;
    /**
     * Gibt an, ob dem Form schon einmal Fach-Items hinzugefuegt wurden
     * oder nicht, damit beim erneuern der Informationen keine Informationen
     * geloescht werden, die nicht existieren (=> Fehler).
     */
    private boolean itemsExist = false;
    /**
     * Wird am Ende des Vorgangs hochgezaehlt.
     */
    private int finishCount = 0;

    /**
     * Baut den Bildschirm "zusammen", welcher aus einem Ticker, vier
     * Befehlen (s.o.), den Fachinformationen, sowie einer Ueberschrift
     * besteht.
     * 
     * @param midlet Referenz zum MIDlet fuer die/den Callback(s)
     */
    public LoadedChooser(KlautorMIDlet midlet) {
        super("Auswahl");
        terminNum = 0;

        Ticker ticker = new Ticker("W\u00e4hle deine Termine aus");
        setTicker(ticker);
        this.midlet = midlet;

        rel = new Command("Relevant", Command.SCREEN, 1);
        nonRel = new Command("Irrelevant", Command.SCREEN, 1);
        finished = new Command("Fertig", Command.OK, 1);
        cancel = new Command("Abbrechen", Command.CANCEL, 2);
        addCommand(rel);
        addCommand(nonRel);
        addCommand(cancel);
        setCommandListener(this);

        show();
    }

    /**
     * Definiert, welche Methode nach Betaetigung eines bestimmten
     * Befehls aufgerufen werden soll.
     */
    public void commandAction(Command c, Displayable d) {
        if (c == rel) {
            relevant();
        } else if (c == nonRel) {
            nonRel();
        } else if (c == finished) {
            midlet.chooseLoadedFinished();
            return;
        } else if (c == cancel) {
            canceled();
            return;
        } else if(c == save) {
            midlet.chooseLoadedFinished();
            return;
        } else if(c == del) {
            Termine.getTermine().removeAllElements();
            midlet.chooseLoadedFinished();
            return;
        }

        //Ueberpruefen, wann der Benutzer alle Termine "berwertet" hat
        //und den Vorgang abschliessen
        if (Termine.getTermine().size() == terminNum + 1 || Termine.getTermine().size() == terminNum) {
            finishCount++;
        }

        if (finishCount == 2 || Termine.getTermine().isEmpty()) {
            finished();
        }
    }
    

    /**
     * Setzt die Nummer des Termins in der Datenbank, auf den zugegriffen
     * werden soll um eins hoeher und laesst ihn anschliessen anzeigen.	 
     */
    private void relevant() {
        if (Termine.getTermine().size() > terminNum + 1) {
            terminNum++;
        }
        show();
    }

    /**
     * Loescht, wenn der Vektor noch nicht leer ist, den aktuellen
     * Termin und laesst den naechsten anzeigen.
     */
    private void nonRel() {
        if (!Termine.getTermine().isEmpty()) {
            Termine.getTermine().removeElementAt(terminNum);
        }
        show();
    }

    /**
     * Zeigt den momentan zu bearbeitenden Termin an.
     */
    private void show() {
        // Nur anzeigen, wenn es noch Termine gibt
        if (!Termine.getTermine().isEmpty()) {
            // Und sie nicht schon bewertet wurden
            if (Termine.getTermine().size() > terminNum) {
                termin = Termine.getTermine().terminAt(terminNum);
                fach = new StringItem("Fach: ", termin.getFach() + " " + termin.getKurstyp());
                lehrer = new StringItem("Lehrer: ", termin.getLehrer());
                datum = new StringItem("Datum: ", termin.getDatum());
                raum = new StringItem("Raum: ", termin.getRaum());
                thema = new StringItem("Thema: ", termin.getThema());

                if (itemsExist) {
                    set(0, fach);
                    set(1, lehrer);
                    set(2, datum);
                    set(3, raum);
                    set(4, thema);
                } else {
                    insert(0, fach);
                    insert(1, lehrer);
                    insert(2, datum);
                    insert(3, raum);
                    insert(4, thema);
                    itemsExist = true;
                }
            }
        }
    }

    /**
     * Loescht die alten Befehle und fuegt einen neuen hinzu, damit
     * der Benutzer weiss, wann alle Termine bewertet sind und er
     * nun weitergeleitet werden kann, wenn der Befehl ausgefuehrt wird.
     */
    private void finished() {
        removeCommand(rel);
        removeCommand(nonRel);
        removeCommand(cancel);
        addCommand(finished);
    }
    
    /**
     * Wenn der Benutzer den Auswahlvorgang abbricht, soll er noch entscheiden,
     * was mit den geladnenen Terminen im Termin-Vektor geschehen soll
     * -> loeschen, oder
     * -> unbewertet speichern
     * 
     * NOTE: einige Workarounds
     */
    private void canceled() {
            removeCommand(rel);
            removeCommand(nonRel);
            removeCommand(cancel);

            setTicker(null);

            for (int i = 3; i < size(); i++) {
                set(i, new StringItem(" ", " "));
            }

            save = new Command("Ja", Command.OK, 1);
            del = new Command("Nein", Command.CANCEL, 1);

            try {
                set(0, new ImageItem("", Image.createImage(Image.createImage("/warnung.png")), ImageItem.LAYOUT_LEFT, "Warning"));
            } catch (IOException ex) {
            }
            set(1, new StringItem("Achtung: ", "Vorgang abgebrochen!"));
            set(2, new StringItem("", "Geladene Termine trotzdem speichern?"));

            addCommand(del);
            addCommand(save);
    }
}
