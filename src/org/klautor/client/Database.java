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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.lcdui.AlertType;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 * Diese Klasse repraesentiert den (persistenten) Speicher des Mobiltelephones.
 * Mit ihr koennen Termine und Einstellungen gespeichert und geladen werden.
 * Voraussetzung dafuer ist, dass die Datenbank zuvor geoeffnet/geschlossen
 * wurde.
 *
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class Database {

    /**
     * Repraesentiert den persistenten Telefonspeicher
     */
    private RecordStore recordStore;
    /**
     * Die "entparsten", also zu einem Text formatierten Termine
     */
    private String termine = null;
    /**
     * Information ueber die Einstellung der Jahrgangsstufe
     */
    private String stufe = null;
    /**
     * Information ueber die Einstellung der Quelldatei, aus der die
     * Termine beim "Neu Laden" und "Aktualisieren" geladen werden.
     */
    private String quelldatei = null;
    /**
     * Information ueber die letzte Aktualisierung der Daten
     */
    private long datum = 0;
    /**
     * Wird gesetzt, wenn Daten geaendert wurden.
     */
    private boolean changedSettings,  changedTermine;
    /**
     * kann extern abgefragt werden, um zu erfahren, ob schon einmal Daten
     * gespeichert werden.
     * @see dataExists()
     */
    private boolean dataExists = false;
    /**
     * Instanz dieser Klasse
     */
    private static Database database = null;

    /**
     * Singleton-Technik: Alle Objekte koennen die Instanz dieser Klasse
     * abrufen, dabei wird garantiert, dass alle die Referenz zu dem
     * gleichen Objekt erhalten.
     * 
     * @return DIE Instanz dieser Klasse
     */
    public static Database getDatabase() {
        if (database == null) {
            database = new Database();
        }
        return database;
    }

    /**
     * Gibt die durch die Singleton-Methode erzeugte Instanz wieder frei
     * @see #getDatabase()
     */
    public static void freeMem() {
        if (database != null) {
            database = null;
        }
    }

    /**
     * Oeffnet den persistenten Speicher
     * 
     * @throws RecordStoreFullException	Wird geworfen, wenn der Speicher voll ist
     * @throws RecordStoreNotFoundException	Wird geworfen, wenn der RecordStore nicht gefunden wurde
     * @throws RecordStoreException	Wird geworfen, wenn irgendetwas anderes nicht stimmt
     */
    public void open() throws RecordStoreFullException,
            RecordStoreNotFoundException,
            RecordStoreException {

        //RecordStore.deleteRecordStore("Klautor");
        recordStore = RecordStore.openRecordStore("Klautor", true);
        changedSettings = false;
        changedTermine = false;
    }

    /**
     * Ueberprueft, ob die gespeicherten Daten kompatibel sind mit der aktuellen
     * Version.
     * Laedt die Daten aus dem Speicher in den Termine-Vektor bzw.
     * die jeweiligen Datentypen und legt Records an, falls noch keine
     * existieren.
     * mainly (c) 2005  Juho Vähä-Herttua (jmirc-project). Thanks a lot!
     */
    public void load() {
        DataInputStream din;

        try {
            String version;

            try {
                byte[] temp = recordStore.getRecord(1);
                version = (new DataInputStream(new ByteArrayInputStream(temp))).readUTF();
            } catch (Exception e) {
                version = "";
            }

            if (!version.equals(KlautorMIDlet.VERSION) || recordStore.getNumRecords() == 0) {
                recordStore.closeRecordStore();
                try {
                    RecordStore.deleteRecordStore("Klautor");
                } catch (Exception e) {
                }

                recordStore = RecordStore.openRecordStore("Klautor", true);

                byte[] temp;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                dos.writeUTF(KlautorMIDlet.VERSION);
                temp = baos.toByteArray();
                dos.close();
                baos.close();

                recordStore.addRecord(temp, 0, temp.length);
                temp = new String("n/a").getBytes();
                recordStore.addRecord(temp, 0, temp.length);
                recordStore.addRecord(temp, 0, temp.length);
            } else {
                // Daten nur laden, wenn sie existieren
                // merken, dass Daten existieren
                dataExists = true;

                // Termine laden und in den Termine-Vektor parsen lassen
                termine = new String(recordStore.getRecord(2));
                if (!termine.equals("n/a")) {
                    Parser.parse(termine);
                }
                termine = null;


                din = new DataInputStream(new ByteArrayInputStream(recordStore.getRecord(3)));
                quelldatei = din.readUTF();
                stufe = din.readUTF();
                datum = din.readLong();
                din.close();
            } // Wenn der RecordStore nicht offen ist, versuchen ihn zu oeffnen,
            // ansonsten Fehlermeldung ausgeben
        } catch  (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Speichert die Daten
     */
    public void save() {
        if (changedTermine) {
            saveTermine();
            changedTermine = false;
        }

        if (changedSettings) {
            saveSettings();
            changedSettings = false;
        }
    }

    public void explicitSave() {
        changedTermine = true;
        save();
    }

    /**
     * Speichert alle uebrigen Daten
     * im Moment:
     * - Quelldatei
     * - Stufe
     * - Datum
     */
    private void saveSettings() {
        if (quelldatei == null) {
            quelldatei = "n/a";
        }
        if (stufe == null) {
            stufe = "n/a";
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            byte[] byteout;

            dos.writeUTF(quelldatei);
            dos.writeUTF(stufe);
            dos.writeLong(datum);

            byteout = baos.toByteArray();
            dos.close();
            baos.close();

            recordStore.setRecord(3, byteout, 0, byteout.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Speichert die Termine
     */
    private void saveTermine() {
        // Termine als formatierten Text zum Speichern abholen
        termine = Parser.deparse();

        // Wenn es keine Termine gibt, speichern wir stattdessen
        // einfach einen Platzhalter (damit beim Laden die RecordID
        // stimmt)
        if (termine == null) {
            termine = "n/a";
        }

        try {
            recordStore.setRecord(2, termine.getBytes(), 0, termine.length());
        } // Fehler abfangen...
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * RecordStore wieder schliessen
     */
    public void close() {
        try {
            recordStore.closeRecordStore();
        } catch (RecordStoreNotOpenException e) {
        // Was schon zu ist, muss nicht mehr geschlossen werden
        } // Sollte es Probleme geben: Meldung ausgeben
        catch (RecordStoreException e) {
            Message.show("Datensicherungsfehler! Beim Schliessen der Datenbank trat" +
                    "ein Fehler auf. Eventuell sind die Daten selbst hiervon nicht betroffen" +
                    "und unversehrt.", AlertType.ERROR);

        }

    }

    /**
     * @return Information darueber, ob im RMS schon einmal Daten gespeichert
     * wurden -> erster Start des MIDlets?
     */
    public boolean dataExists() {
        return dataExists;
    }

// Folgende "setter" und "getter" werden vom PrefsMenu oder dem
// KlautorMIDlet oder vom TerminLoader genutzt.
    /**
     * @return	Die zuvor gesetzten oder geladenen Information
     * ueber die Quelldatei-Einstellung
     */
    public String getQuelldatei() {
        return quelldatei;
    }

    /**
     * @return Die zuvor gesetzten oder geladenen Information
     * ueber die Stufen-Einstellung
     */
    public String getStufe() {
        return stufe;
    }

    /**
     * @param string Die Quelldatei-Einstellung
     */
    public void setQuelldatei(String string) {
        quelldatei = string;
        changedSettings = true;
    }

    /**
     * @param string Die Stufen-Einstellung
     */
    public void setStufe(String string) {
        stufe = string;
        changedSettings = true;
    }

    /**
     * Das Versions-Datum der geladenen Daten setzen
     * @param datum
     */
    public void setDatum(long datum) {
        this.datum = datum;
        changedSettings = true;
    }

    /**
     * @return das Versions-Datum der geladenen Daten
     */
    public long getDatum() {
        return datum;
    }
}
