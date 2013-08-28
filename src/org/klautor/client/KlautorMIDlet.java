// Klautor - An MIDP-MIDlet for downloading and organizing dates of exams
// Copyright (C) 2004, 2008  Johannes Brakensiek <johannes @quackes.de>
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
package org.klautor.client;

import org.klautor.client.LoadedChooser;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStoreException;

/**
 * Die Klasse KlautorMIDlet erweitert die Klasse MIDlet und stellt
 * somit die zentrale Klasse des Projekts dar. Hier befinden sich die
 * Methodenaufrufe fuer Start und Beendigung des Programmes, ausserdem
 * werden von hier aus alle UserInterface-Elemente und somit (fast) alle
 * weiteren Funktionen aufgerufen und verwaltet.
 * Die Eigenschaften dieser Klasse sind zum groesstenteil Menueklassen/
 * -objekte, die so strukturiert sind, dass sie nach oder bei Erledigung
 * ihrer Aufgabe wiederum diese (KlautorMIDlet-) Klasse aufrufen, 
 * welche dann alle weiteren Vorgaenge einleitet (sog. Callback-Methode).
 * Eine weitere Aufgabe dieser Klasse, neben der Regelung der Funktionsablaeufe,
 * ist die Speicherverwaltung. Alle Objekte sollen, sobald sie nicht
 * mehr benoetigt werden, vom GarbageCollector eingesammelt werden koennen,
 * weshalb ihre jeweilige Referenz so schnell wie moeglich auf "null"
 * gesetzt wird, um so Speicher freizugeben.
 * Desweiteren wird bei der Erzeugung einiger Objekte ueberprueft, ob
 * dafuer noch genug Speicher vorhanden ist. Wenn nicht, wird der Garbage-
 * Collector losgeschickt, der Speicher freigeben soll, damit eine Fehler-
 * meldung angezeigt werden kann und das Programm nicht in sehr uneleganter
 * Weise abstuerzt.
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 *
 */
public class KlautorMIDlet
        extends MIDlet {

    /**
     * Die Referenz zum Objekt, welches das Display repraesentiert.
     */
    private Display display;
    /**
     * Die Momentan auf dem Display angezeigte Oberflaeche (UserInterface)
     */
    private Displayable current;
    /**
     * Referenz auf die "Laufzeitumgebung" dieses MIDlets
     */
    private Runtime runtime;
    /**
     * Das Hauptmenue
     */
    private MainMenu mm = null;
    /**
     * Das Einstellungen-Menue
     */
    private PrefsMenu pm = null;
    /**
     * Bildschirm zum Betrachten und Organisieren der Termine.
     */
    private Browser browser = null;
    /**
     * Bildchirm zum Betrachten eines einzelnen Termins
     */
    private DateViewer dv = null;
    /**
     * Bildschirm zur Bestaetigung aus Ausfuehrung des Loeschvorgangs
     * eines Termins
     */
    private Deleter deleter = null;
    /**
     * Bildschirm aendern der Notiz-Information eines Termins
     */
    private Editor editor = null;
    /**
     * Bildschirm zum Sortierenlassen der Termine
     */
    private Sorter sorter = null;
    /**
     * Bildschirm zur Abfrage, ob Update (aktualisieren der Termindaten)
     * durchgefuehrt werden soll.
     */
    private UpdateScreen us = null;
    /**
     * Bildschirm, der den Fortschritt des "Neu Laden"-Vorganges anzeigt
     * (und diesen auch durchfuehrt).
     */
    private LoadScreen loadScr = null;
    /**
     * Bildschirm, um aus den heruntergeladenen Terminen, die fuer den
     * Benutzer relevanten auszuwaehlen (und nicht relevante zu loeschen).
     */
    private LoadedChooser loadCh = null;
    /**
     * Konstante mit Speicher-Fehler-Meldung fuer die Fehler-Benachrichtigung
     */
    private final String MEMORY_ERROR = "Nicht mehr genug freier Arbeitsspeicher. Vorgang wurde abgebrochen!";
    /**
     * Statische Konstante, die massgeblich ist fuer den Versionsstand des
     * permanenten Speichers (RMS)
     */
    public static final String VERSION = "0.9.26";
    
    /**
     * Setzt beim Erzeugen des Programms ein paar wichtige Variablen
     * und bereitet die "Message" vor.
     */
    public KlautorMIDlet() {
        display = Display.getDisplay(this);
        Message.init(display);
        current = display.getCurrent();
        runtime = Runtime.getRuntime();
    }

    /**
     * Wird beim Aufrufen des MIDlets ausgefuehrt.
     * Wenn das Programm nicht zuvor beim Ausfuehren unterbrochen wurde
     * wird der Willkommen-Bildschirm und anschliessend das Hauptmenue
     * angezeigt.
     */
    public void startApp() {
        // Datenbank oeffnen und Werte einlesen lassen
        try {
            Database.getDatabase().open();
        } catch (RecordStoreException e) {
            Message.show("Datensicherungsfehler! Deine Termine und Einstellungen" +
                    "k\u00f6nnen leider nicht gespeichert werden.", AlertType.ERROR);
        }

        Database.getDatabase().load();
        
        // Werte aus dem .jad-File an die Datenbank uebergeben, sofern
        // noch nicht geschehen
        if (Database.getDatabase().getQuelldatei() == null) {
            Database.getDatabase().setQuelldatei(getAppProperty("Quelldatei"));
        }
        if (Database.getDatabase().getStufe() == null) {
            Database.getDatabase().setStufe(getAppProperty("Stufe"));
        }

        //SplashScreen und das Hauptmenue anzeigen
        if (current == null) {
            mm = new MainMenu(this);
            display.setCurrent(mm);
            // SplashScreen nur beim ersten Programmstart anzeigen
            if(!Database.getDatabase().dataExists()) {
                Message.splashScreen(getAppProperty("MIDlet-Version"));
            }
        }
    }

    /**
     * Wird von dem Hauptmenue aufgerufen, wenn der Benutzer den Befehl
     * gegeben hat, das Programm zu beenden.
     */
    public void quitRequested() {
        try {
            destroyApp(true);
        } catch (MIDletStateChangeException e) {
        // Nicht erreichbarer catch-Block, da Parameter immer true (s.o.)
        }
    }

    /**
     * Beim Pausieren des MIDlets geschieht so ziemlich das Gleiche
     * wie beim Beenden:
     * Die wichtigen Daten werden gespeichert, alle uebrigen freigegeben.
     */
    public void pauseApp() {
        freeAll();
        Database.getDatabase().save();
        Database.getDatabase().close();
        Termine.freeMem();
        Database.freeMem();
        Message.freeMem();
        notifyPaused();
    }

    /**
     * Wenn ein Beenden des MIDlets unbedingt noetig ist, werden die
     * wichtigen Daten gespeichert und alle Resourcen freigegeben,
     * wenn nicht wird eine MIDletStateChangeException geworfen, die
     * angibt, dass das MIDlet noch weiterlaufen will.
     */
    public void destroyApp(boolean unconditional)
            throws MIDletStateChangeException {
        if (unconditional) {
            freeAll();
            Database.getDatabase().save();
            Database.getDatabase().close();
            Termine.freeMem();
            Database.freeMem();
            Message.freeMem();
            notifyDestroyed();
        } else {
            throw new MIDletStateChangeException();
        }
    }

    /**
     * Gibt den Speicher des Willkommens-Bildschirm und eventueller
     * SubMenues frei und zeigt anschliessen das Hauptmenue an.
     */
    public void mainMenu() {
        freeSubMenus();
        freeBrowser();
        runtime.gc();
        if (mm == null) {
            mm = new MainMenu(this);
            display.setCurrent(mm);
        }
    }

    /**
     * Gibt nicht mehr benoetigten Speicher frei und zeigt das
     * Einstellungen-Menue an.
     */
    public void showPrefs() {
        freeMainMenu();
        try {
            pm = new PrefsMenu(this);
            display.setCurrent(pm);
        } catch (OutOfMemoryError e) {
            pm = null;
            runtime.gc();
            Message.show(MEMORY_ERROR, AlertType.ERROR);
        }
    }

    /**
     * Gibt nicht mehr benoetigten Speicher frei und zeigt die 
     * "Aktualisieren"-Abfrage an.
     */
    public void showUpdate() {
        freeMainMenu();
        try {
            us = new UpdateScreen(this);
            display.setCurrent(us);
        } catch (OutOfMemoryError e) {
            us = null;
            runtime.gc();
            Message.show(MEMORY_ERROR, AlertType.ERROR);
        }
    }

    /**
     * Gibt nicht mehr benoetigten Speicher frei und zeigt die
     * "Neu Laden"-Abfrage an.
     */
    public void showLoadScreen() {
        freeMainMenu();
        try {
            loadScr = new LoadScreen(this);
            display.setCurrent(loadScr);
        } catch (OutOfMemoryError e) {
            loadScr = null;
            runtime.gc();
            Message.show(MEMORY_ERROR, AlertType.ERROR);
        }
    }

    /**
     * Wird vom LoadScreen-Objekt aufgerufen, wenn der Download- und
     * Parsevorgang abgeschlossen ist. Gibt dessen Speicher frei und
     * ruft die Methode zur Anzeige des Auswahl der relevaten Termine
     * auf.
     * @param wasError Ob ein Fehler waehrend des Ladens stattgefunden hat
     */
    public void loadFinished(boolean wasError) {
        if (wasError) {
            mainMenu();
            return;
        }
        Database.getDatabase().explicitSave();
        // Daten geladen
        if (loadScr != null) {
            loadScr = null;
            showLoadedChooser();
        // Daten aktualisiert
        } else {
            us = null;
            showBrowser(true);
        }
    }

    /**
     * Zeigt das Menue zur Auswahl der relevanten Termine.
     */
    public void showLoadedChooser() {
        try {
            loadCh = new LoadedChooser(this);
            display.setCurrent(loadCh);
        } catch (OutOfMemoryError e) {
            loadCh = null;
            runtime.gc();
            Message.show(MEMORY_ERROR, AlertType.ERROR);
        }
    }

    /**
     * Wird von LoadedChooser aufgerufen, wenn die Auswahl (durch
     * expliziten Benutzer-Befehl) beendet wurde.
     * Gibt dessen Speicher frei und laesst das Hauptmenue anzeigen.
     */
    public void chooseLoadedFinished() {
        loadCh = null;
        Database.getDatabase().explicitSave();
        mainMenu();
    }

    /**
     * Laesst einen Message-Bildschirm mit Hilfe- und Creditstext
     * anzeigen.
     */
    public void showHelp() {
        Message.show("- Hilfe - \nUnter \"Einstellungen\" kannst du angeben " +
                "woher die Daten geladen werden sollen und in welcher Jgstf. du bist. " +
                "Mit \"Neu laden\" kannst du dann die Termine herunterladen, alle eventuell " +
                "bereits existierenden Termine werden hierbei gel\u00f6scht. Mit \"Aktualisieren\" k\u00f6nnen bereits " +
                "gespeicherte Termine auf den neusten Stand gebracht werden, unter \"Liste\" " +
                "k\u00f6nnen die gespeicherten Termine eingesehen/bearbeitet/gel\u00f6scht und sortiert " +
                "werden.\n - Support - \nMehr Infos unter http://klautor.quackes.de" +
                "\n - Credits - \nProgramm(code) (c) by Johannes Brakensiek, Grafiken (c) " +
                "by Tobias Wingerter. \n - Lizenz - \nDieses Programm ist freie Software und " +
                "untersteht den Bedingungen der \"GNU General Public License\".", AlertType.INFO);
    }

    /**
     * Laesst den Speicher des Hauptmenues freigeben und zeigt den
     * Browser zum Sichten und Organisieren der (gespeicherten) Termine
     * an.
     * @param update Soll die Liste aktualisiert werden - noetig nach Aenderungen
     */
    public void showBrowser(boolean update) {
        freeMainMenu();
        freeDateViewer();
        freeDeleter();
        freeEditor();
        freeSorter();
        freeSubMenus();
        runtime.gc();
        try {
            if(browser == null) {
                browser = new Browser(this);
                update = false;
            }
            if(update) {
                browser.update();
            }
            display.setCurrent(browser);
        } catch (OutOfMemoryError e) {
            browser = null;
            runtime.gc();
            Message.show(MEMORY_ERROR, AlertType.ERROR);
        }
    }

    /**
     * Laesst den Speicher der SubMenues/des Browser freigeben und zeigt
     * den Termin-Anzeiger an.
     * 
     * @param dateNum Nummer des Termins im Termine-Vektor
     */
    public void showDateViewer(int dateNum) {
        freeSubMenus();
        try {
            dv = new DateViewer(dateNum, this);
            display.setCurrent(dv);
        } catch (OutOfMemoryError e) {
            dv = null;
            runtime.gc();
            Message.show(MEMORY_ERROR, AlertType.ERROR);
        }
    }

    /**
     * Gibt den Speicher des "Termin-Anzeigers" (DateViewer) frei,
     * soweit dieser existiert.
     */
    private void freeDateViewer() {
        if (dv != null) {
            dv = null;
        }
    }

    /**
     * Laesst den Speicher der SubMenues/des Browser freigeben und
     * aktiviert den Termin-Loescher
     * 
     * @param dateNum Nummer des Termins im Termine-Vektor
     */
    public void activateDeleter(int dateNum) {
        freeSubMenus();
        freeDateViewer();
        try {
            deleter = new Deleter(dateNum, this);
            display.setCurrent(deleter);
        } catch (OutOfMemoryError e) {
            deleter = null;
            runtime.gc();
            Message.show(MEMORY_ERROR, AlertType.ERROR);
        }
    }

    /**
     * Gibt den Speicher des Termin-Loeschers frei, sofern dieser existiert.
     */
    private void freeDeleter() {
        if (deleter != null) {
            deleter = null;
        }
    }

    /**
     * Laesst den Speicher der SubMenues/des Browser freigeben und
     * zeigt den Termin-Editor
     * 
     * @param dateNum Nummer des Termins im Termine-Vektor
     */
    public void showEditor(int dateNum) {
        freeSubMenus();
        freeDateViewer();
        try {
            editor = new Editor(dateNum, this);
            display.setCurrent(editor);
        } catch (OutOfMemoryError e) {
            editor = null;
            runtime.gc();
            Message.show(MEMORY_ERROR, AlertType.ERROR);
        }
    }

    /**
     * Gibt den Speicher des Termin-Editors frei, sofern dieser existiert.
     */
    private void freeEditor() {
        if (editor != null) {
            editor = null;
        }
    }

    /**
     * Laesst den Speicher der SubMenues/des Browser freigeben und
     * zeigt den Sortier-Bildschirm.
     */
    public void showSorter() {
        freeSubMenus();
        try {
            sorter = new Sorter(this);
            display.setCurrent(sorter);
        } catch (OutOfMemoryError e) {
            sorter = null;
            runtime.gc();
            Message.show(MEMORY_ERROR, AlertType.ERROR);
        }
    }

    /**
     * Gibt den Speicher des Sorter-Bildschirms frei, sofern dieser existiert.
     */
    private void freeSorter() {
        if (sorter != null) {
            sorter = null;
        }
    }

    /**
     * Untersucht, ob irgendwelche Objekte der SubMenues existieren
     * und gibt den Speicher dieser dann zur "GarbageCollection" frei.
     */
    private void freeSubMenus() {
        if (pm != null) {
            pm = null;
        }
        if (us != null) {
            us = null;
        }
        if (loadScr != null) {
            loadScr = null;
        }
    }
    
    /**
     * Gibt den Speicher des Browser/Listen-Bildschirms frei, 
     * sofern dieser existiert.
     */
    private void freeBrowser() {
        if (browser != null) {
            browser = null;
        }
    }

    /**
     * Gibt den Speicher des Hauptmenues frei, sofern dieses instantiiert
     * wurde.
     */
    private void freeMainMenu() {
        if (mm != null) {
            mm = null;
            runtime.gc();
        }
    }

    /**
     * Gibt den Speicher aller Attribute frei, sofern diese instantiiert
     * wurden.
     */
    private void freeAll() {
        display = null;
        current = null;
        mm = null;
        pm = null;
        browser = null;
        dv = null;
        deleter = null;
        editor = null;
        sorter = null;
        us = null;
        loadScr = null;
        loadCh = null;
    }
}
