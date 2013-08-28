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
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;

/**
 * TODO: Doc ueberarbeiten - ueberholt!
 *
 * Von dieser abstrakten Klasse werden (momentan) LoadScreen und
 * UpdateScreen abgeleitet.
 * Die Klasse fuehrt durch den Lade- und Parsevorgang. Dabei werden zwei 
 * neue Threads erstellt (insgesamt wird also mit drei Threads gearbeitet).
 * Die Threads synchronisieren sich am Objekt "sync".
 * Anm.: Die Threads muessen durch die ableitenden Klassen erstellt werden,
 * damit der jeweilige Thread weiss, an wen er die Daten wieder zurueck-
 * geben muss.
 * 
 * Der Main-Thread ist fuer das UserInterface zustaendig, wahrend der
 * TerminLoader die Daten aus dem Internet laedt und dabei den Ladebalken
 * aktualisiert. Hat der TerminLoader-Thread alle Daten geladen, beginnt
 * der ParseManager Thread mit dem Parse-Vorgang wahrend der TerminLoader-
 * Thread die Verbindung schliesst etc.
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public abstract class AbstractLoadScreen
        extends Form
        implements CommandListener {

    /**
     * Der Fortschrittsbalken
     */
    protected Gauge gauge;
    /**
     * Befehl zur Bestaetigung des Ladevorgangs
     */
    protected Command yes;
    /**
     * Befehl zur Negation des Ladevorgangs
     */
    protected Command no;
    /**
     * Referenz zum MIDlet fuer Callbacks nach Befehlseingabe
     */
    protected KlautorMIDlet midlet;
    /**
     * Befehl zum Schliessen des Bildschirms nach Beendigung der
     * Vorgaenge
     */
    protected Command ok;
    /**
     * Befehl zum Abbrechen des Lade-Vorgangs
     */
    protected Command cancel;
    /**
     * Unter dieser ID wird die Frage an den Benutzer gespeichert, nach
     * Bestaetigung fuer den Ladevorgang kann hiermit der Text wieder vom
     * Bildschirm entfernt werden, damit genug Platz fuer den Ladebalken
     * ist.
     */
    protected int textID;
    /**
     * Das Gleiche mit dem Bild
     * @see textID
     */
    protected int imageID = -1;
    /**
     * Wird vom ParseManager gesetzt, wenn beim Laden oder Parsen
     * ein Fehler aufgetreten ist.
     */
    protected boolean wasError = false;
    /**
     * Der Thread fuer den Ladevorgang
     */
    protected TerminLoader loader = null;

    /**
     * Beim Erzeugen eines Objekts dieser Klasse wird das UserInterface
     * vorbereitet.
     * 
     * @param midlet Referenz zum MIDlet
     */
    public AbstractLoadScreen(KlautorMIDlet midlet) {
        super(null);
        this.midlet = midlet;

        try {
            imageID = append(new ImageItem("", Image.createImage(Image.createImage("/warnung.png")), ImageItem.LAYOUT_LEFT, "Warning"));
        } catch (IOException ex) {
        }
        // Nur aktualisieren, wenn bereits Termine existieren/gebraucht werden
        if (needTermine() && Termine.getTermine().isEmpty()) {
            append("Es existieren keine Termine zum Aktualisieren!\n" +
                    "Termine k\u00f6nnen über \"Neu Laden\" geladen werden.");
            wasError = true;
            ok = new Command("OK", Command.OK, 1);
            addCommand(ok);
            setCommandListener(this);
            return;
        }
        
        setTitle(heading());
        textID = append(text());

        yes = new Command("Ja", Command.OK, 0);
        no = new Command("Nein", Command.CANCEL, 0);
        addCommand(yes);
        addCommand(no);
        setCommandListener(this);
    }

    /**
     * Hier soll die ableitende Klasse die Uberschrift fuer den
     * Bildschirm zurueckgeben.
     * 
     * @return Ueberschrift des Bildschirms
     */
    public abstract String heading();

    /**
     * Hier soll die ableitende Klasse den (Frage-)String zurueckgeben, der
     * dem Benutzer angezeigt werden soll
     * 
     * @return Frage nach Bestaetigung
     */
    public abstract String text();

    /**
     * Hier soll die abgeleitete Klasse zurueckgeben, ob bereits Termine
     * vorhanden sein muessen, damit der Ladevorgang sinnvoll ist.
     * 
     * @return Muessen Termine vorhanden sein?
     */
    public abstract boolean needTermine();

    /**
     * Reagiert auf Benutzeraktionen
     */
    public void commandAction(Command c, Displayable d) {
        // Es soll nichts geladen werden
        if (c == no) {
            midlet.mainMenu();
        }
        // Es soll geladen werden
        if (c == yes) {
            loadNow();
        }
        // Es ist fertig geladen und kann zum LoadedChooser oder Browser
        // oder im Fehlerfall zum Hauptmenue zurueckgehen.
        if (c == ok) {
            midlet.loadFinished(wasError);
        }
        // Abbruch waehrend des Lade-Vorgangs
        if (c == cancel) {
            loader.abort();
        }
    }

    /**
     * Wird ausgefuehrt, wenn vom Benutzer die Bestaetigung ueber den
     * Lade-Vorgang gegeben wurde.
     */
    protected void loadNow() {
        // Frage-String entfernen
        delete(textID);
        if (imageID != -1) {
            delete(imageID);
        }


        setTitle("Bitte warten...");
        // Ladebalken vorbereiten
        gauge = new Gauge("Lade...", false, 10, 0);
        append(gauge);

        // Entsprechend neue Befehle zur Verfuegung stellen/entfernen
        cancel = new Command("Abbrechen", Command.SCREEN, 0);
        ok = new Command("OK", Command.OK, 1);
        removeCommand(yes);
        removeCommand(no);

        addCommand(cancel);
        // Threads werden gestartet, es kann losgehen..
        startThreads();
    }

    /**
     * Hier muessen die ableitenden Klassen die beiden Threads
     * "TerminLoader" und "ParseManager" starten und dabei die
     * Referenzen uebergeben, die die Threads benoetigen.
     * @see TerminLoader
     * @see ParseManager
     */
    protected abstract void startThreads();

    /**
     * Wird vom TerminLoader-Thread aufgerufen, um den Ladebalken
     * synchron zum Ladevorgang zu aktualisieren.
     * 
     * @param cur	Momentan geladene Bytes
     * @param max	Maximale Bytes = Dateigroesse
     */
    public void setGauge(int cur, int max) {
        if (max != gauge.getMaxValue()) {
            gauge.setMaxValue(max);
        }
        gauge.setValue(cur);
    }
    
    /**
     * Gibt den aktuellen Wert der Gauge wieder. Wird vom
     * TerminLoader-Thread aufgerufen.
     * @return  Aktueller Wert der Gauge
     */
    public int getGaugeValue() {
        return gauge.getValue();
    }

    /**
     * Wird vom ParseManager aufgerufen, wenn ein Fehler aufgetreten
     * ist.
     */
    public void wasError() {
        wasError = true;
    }
}
