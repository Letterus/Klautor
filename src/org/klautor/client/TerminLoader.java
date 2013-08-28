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
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.ContentConnection;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;

/**
 * Dieser Thread ist fuer das Laden der Daten aus dem Internet und
 * das Aktualisieren des Fortschrittsbalkens vom LoadScreen/UpdateScreen
 * zustaendig.
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class TerminLoader
        extends Thread {

    /**
     * Buffer, in den die Daten geladen werden
     */
    private StringBuffer buf;
    /**
     * Wird vom UpdateScreen/LoadScreen gesetzt, wenn abgebrochen
     * werden soll (volatile, damit es nicht zu Komplikationen
     * beim gleichzeitigen Lesen und Schreiben kommt).
     */
    private volatile boolean abort = false;
    /**
     * Wird gesetzt, wenn ein Fehler auftritt (wird dann spaeter
     * weitergeleitet an den TerminListener).
     */
    private boolean wasError = false;
    /**
     * Der LoadScreen
     */
    private LoadScreen lScreen = null;
    /**
     * Der UpdateScreen
     */
    private UpdateScreen uScreen = null;
    /**
     * Befehl, der gesetzt wird, wenn der Parse-Vorgang abgeschlossen ist
     */
    private Command ok;
    /**
     * Befehl zum Abbrechen des Lade-Vorgangs vom TerminLoader
     */
    private Command cancel;

    /**
     * Variablen setzen und Thread starten. Wird vom LoadScreen
     * aufgerufen.
     * 
     * @param screen	Der LoadScreen
     * @param ok
     * @param cancel 
     */
    public TerminLoader(LoadScreen screen, Command ok, Command cancel) {
        lScreen = screen;
        this.ok = ok;
        this.cancel = cancel;
        start();
    }

    /**
     * Variablen setzen und Thread starten. Wird vom UpdateScreen
     * aufgerufen.
     * 
     * @param screen	Der LoadScreen
     * @param ok
     * @param cancel 
     */
    public TerminLoader(UpdateScreen screen, Command ok, Command cancel) {
        uScreen = screen;
        this.ok = ok;
        this.cancel = cancel;
        start();
    }

    /**
     * Wird vom UpdateScreen oder LoadScreen (Main-Thread) aufgerufen,
     * wenn der Benutzer einen Abbruch des Lade-Vorgangs wuenscht.
     */
    public void abort() {
        abort = true;
    }

    /**
     * Laden und Verarbeitung der der Daten.
     * Beim Updaten checken, ob neue Daten verfuegbar sind, sonst abbrechen
     */
    public void run() {
        if (uScreen != null) {
            if (!update() && !wasError) {
                uScreen.append("Die Daten sind aktuell.");
                uScreen.removeCommand(cancel);
                uScreen.addCommand(ok);
                return;
            }
        }
        download();
        startParsing();
    }
    
    /**
     * Stellt beim Updaten eine Verbindung zum Server her und schaut in den
     * Header-Informationen, ob das Datum der Server-Datei juenger ist,
     * als das im Speicher.
     * @return true, wenn die Daten auf dem Server neuer sind als die lokalen,
     * sonst false.
     */
    private boolean update() {
        HttpConnection c = null;
        String url = Database.getDatabase().getQuelldatei() + "/" + Database.getDatabase().getStufe();
        long date = 0;
        int rc;
        boolean newData = false;
         
        try {
            c = (HttpConnection) Connector.open(url);

            // Getting the response code will open the connection,
            // send the request, and read the HTTP response headers.
            // The headers are stored until requested.
            rc = c.getResponseCode();
            if (rc != HttpConnection.HTTP_OK) {
                throw new IOException("HTTP response code: " + rc);
            }

            date = c.getHeaderFieldDate("last-modified", 0);
            
        } catch (Exception e) {
            wasError = true;
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException ex) {
                }
            }
        }
        
        if(date >= Database.getDatabase().getDatum()) {
            newData = true;
        }
        
        return newData;
    }

    /**
     * Diese Methode wird nach dem Starten des Threads aufgerufen
     * und laed daraufhin die Datei mit den Terminen herunter und 
     * bestimmt dabei das Verhalten des Fortschrittbalkens.
     * Nach dem Download werden dem TerminListener die Daten uebergeben,
     * sodass dieser mit der Auswertung der Daten beginnen kann,
     * waehrend dieser Thread die Verbindung wieder schliesst.<br>
     * Der Quelltext zum Download der Datei wurde groesstenteils aus
     * zwei Beispiel-Quellen der Firmen Nokia und Sun uebernommen.
     * 
     * Der Ladevorgang funktioniert folgendermassen:<br>
     * Die drei benoetigen Objekte sind ein ContentConnection, ein
     * InputStream und ein StringBuffer. ContentConnction repraesentiert
     * die (Internet-)Verbindung, die von dem Connector hergestellt
     * wird. Aus dieser Verbindung "liest" dann der InputStream, aus
     * welchem dann in den Buffer geschrieben werden kann.
     * Das Laden der Daten geschieht dabei successive byte-weise
     * (als Integer), waehrenddessen wird proportional der 
     * Fortschrittsbalken aktualisiert.
     * 
     * Quellen:
     * 	- Nokia Corporation (18.11.2002): "Brief Introduction to Networked MIDlets". Seite 17
     * 			URL: http://www.forum.nokia.com/main/1,6566,21_10,00.html#java [Stand: 30.1.2004]
     *  - Sun Microsystems, Inc. (2000): "Mobile Information Device Profile (MIDP)� Specification".
     * 		Datei: html/javax/microedition/io/HttpConnection.html, 
     * 		URL: http://jcp.org/aboutJava/communityprocess/final/jsr037/index.html
     * 			[Stand: 26.1.2004]
     */
    public void download() {
        ContentConnection c = null;
        InputStream is = null;

        try {
            // URL - pruefen (momentan sehr simpel)
            String url = Database.getDatabase().getQuelldatei() + "/" + Database.getDatabase().getStufe();
            if (!url.startsWith("http://") || url.equals("") || (url.indexOf(" ") != -1)) {
                throw new IOException("Invalid URL");
            }

            // Verbindung vom Typ herstellen und in den Typ "ContentConnction" umwandeln.
            c = (ContentConnection) Connector.open(url);
            // Einen InputString aus dieserVerbindung oeffnen
            is = c.openInputStream();
            // Dateilaenge abfragen
            int len = (int) c.getLength();
            
            // Fuer die Gauge: Wer etwas intelligenteres weiss, darf sich melden
            // Enthaelt den aktuellen Wert, der der Gauge uebergeben werden soll
            int b = 0;
            // Schrittanzahl der Gauge
            int stepCount = 8;
            // Errechnete Schrittgroesse der Gauge
            int stepSize = 0;
            // Wenn Daten existieren...
            if (len > 0) {
                // Neuen Buffer mit Dateilaenge anlegen
                buf = new StringBuffer(len);
                
                stepSize = len / stepCount;
                
                //Die Daten Byteweise einlesen, dabei den Ladebalken
                //"fuellen"; abbrechen bei Benutzerwunsch. 
                for (int i = 0; i < len; ++i) {
                    if (abort) {
                        break;
                    }
                    int ch = is.read();
                    if (ch == -1) {
                        break;
                    }
                    buf.append((char) ch);
                    
                    b = i / stepSize;
                    if (uScreen != null) {
                        if(b != uScreen.getGaugeValue())
                            uScreen.setGauge(b, stepCount);
                    } else {
                        if(b != lScreen.getGaugeValue())
                            lScreen.setGauge(b, stepCount);
                    }
                }
                Database.getDatabase().setDatum(System.currentTimeMillis());
            } // Keine Daten => Fehler
            else {
                wasError = true;
            }
        } // Falls der Speicher nicht reicht, Meldung ausgeben.
        catch (OutOfMemoryError e) {
            buf = null;
            Runtime.getRuntime().gc();
            Message.show("Nicht mehr genug freier Arbeitsspeicher. Vorgang wurde abgebrochen!", AlertType.ERROR);
        } // Wenns einen Fehler mit der Verbindung gab, Flag setzen.
        catch (IOException e) {
            wasError = true;
        } // Zum Schluss alles schliessen.
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (c != null) {
                try {
                    c.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Wird nach starten des Threads unabhaengig vom Main-Thread ausgefuehrt
     * Beeinflusst das Benutzerinterface, fuehrt den Parse-Vorgang druch
     * und gibt bei Bedarf eine Fehlermeldung aus.
     */
    private void startParsing() {
        // Wir arbeiten entweder mit dem LoadScreen...
        if (lScreen != null) {
            lScreen.removeCommand(cancel);
            // Wenn ein Fehler aufgetreten ist, Meldung ausgeben
            if (wasError) {
                Message.show("Fehler beim Laden der Daten! " +
                        "Es konnte keine Verbindung aufgebaut werden und/oder " +
                        "die angegebene Adresse ist falsch!", AlertType.ERROR);
                lScreen.wasError();
                lScreen.addCommand(ok);
                return;
            }

            if (abort) {
                lScreen.wasError();
                lScreen.append("Laden der Daten abgebrochen!");
                lScreen.addCommand(ok);
                return;
            }

            // Ansonsten Daten parsen
            lScreen.append("Daten werden ausgewertet...");

            Parser.parse(buf.toString());
            lScreen.append("Fertig.");
            lScreen.addCommand(ok);
        } // ... oder dem UpdateScreen
        else {
            uScreen.removeCommand(cancel);

            //Wenn ein Fehler aufgetreten ist, Meldung ausgeben
            if (wasError) {
                Message.show("Fehler beim Laden der Daten! " +
                        "Es konnte keine Verbindung aufgebaut werden und/oder " +
                        "die angegebene Adresse ist falsch!", AlertType.ERROR);
                uScreen.wasError();
                uScreen.addCommand(ok);
                return;
            }

            if (abort) {
                uScreen.wasError();
                uScreen.append("Laden der Daten abgebrochen!");
                uScreen.addCommand(ok);
                return;
            }

            //Ansonsten Update durchfuehren
            uScreen.append("Daten werden ausgewertet...");

            int counts = Parser.parseUpdates(buf.toString());
            uScreen.append("Fertig.");
            //Meldung ueber Anzahl der aktualisierten Termine ausgeben
            Message.show(counts + " Termin(e) aktualisiert.", AlertType.INFO);
            uScreen.addCommand(ok);
        }
    }
}
