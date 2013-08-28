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

import javax.microedition.lcdui.AlertType;

/**
 * Der Parser enthaelt nur drei statische Methoden: 
 * parse(String string), parseUpdates(String string) und deparse().
 * Erstere wird genutzt, um die Termininformationen aus der Datei
 * (bzw. eigentlich dem String) aus dem Internet zu filtern und 
 * in den Termine-Vektor einzufuegen, letztere fuegt alle Termininformationen
 * aus dem Vektor wieder zu einem Strng zusammen, damit sie im
 * permanten Speicher des Mobiltelephones unkompliziert gesichert
 * werden koennen.
 * "parseUpdates" tut fast das selbe wie "parse", jedoch mit dem 
 * Unterschied, dass Daten im Termine-Vektor ueberschrieben werden,
 * wenn die uebergebenen anders/aktueller sind als die im Vektor.  
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class Parser {

    private Termine termine;

    /**
     * Diese Methode wertet die Daten aus dem Internet aus.
     * Der Algorithmus dazu ist relativ einfach und funktioniert
     * folgenermassen:<br>
     * Der String wird zuerst nach Zeilenumbruechen abgesucht. Wird
     * einer gefunden, wird Zeile von den restlichen Daten abgesplittet.
     * In ihr befinden sich nun die Informationen fuer exakt einen Termin.
     * Diese Zeile wird nun wiederrum der Reihe nach nach Doppelpunkten
     * abgesucht. Vor jedem dieser Doppelpunkte befindet sich eine
     * Termininformation. Diese werden nun der Reihe nach an das zuvor
     * erzeugte Objekt "Termin" uebergeben. Ist die Terminzeile dann
     * vollstaendig ausgewertet, kann der "gefuellte" Termin dem
     * Terminvektor hinzugefuegt werden und die naechste Terminzeile
     * wird interpretiert.
     * Dieser Vorgang geschieht so lange, bis sich im urspruenglichen
     * Daten-String keine Daten mehr befinden -> alles ist fertig
     * ausgewertet.<br>
     * Dieser Algorithmus hat den extremen Nachteil, dass durch die vielen
     * Stringoperationen sehr viel Speicher benoetigt wird, da ein
     * String nicht veraenderbar ist, bei jeder "Pseudo-Veraenderung"
     * eins Strings also nur eine Kopie des urspruenglichen angefertigt
     * wird.
     * Normalerweise haette man diesen Algorithmus auf eine sehr viel
     * einfachere Art mit der Klasse StringTokenizer/der Methode String.split()
     * realisiert, diese ist jedoch (leider) nicht Bestandteil des MID-Profiles.
     * 
     * @param string Enthaelt die aus dem Internet geladenen Daten
     */
    public static void parse(String string) {
        try {
            int lineOcc, charOcc;
            // Alle alten Termine loeschen
            Termine.getTermine().removeAllElements();
            // Solange noch Zeilenumbrueche im String sind...
            while ((lineOcc = string.indexOf('\n')) != -1) {
                // Eine Zeile absplitten
                String line = string.substring(0, lineOcc);
                string = string.substring(lineOcc + 1);
                // Einen Termin anlegen
                Termin termin = new Termin();
                //Solange noch Doppelpunkte in der Zeile sind...
                while ((charOcc = line.indexOf(':')) != -1) {
                    // Den Wert vorm jeweiligen Doppelpunkt zum Termin hinzufuegen
                    termin.addValue(line.substring(0, charOcc));
                    // Und den gerade hinzugefuegten Wert von den restlichen
                    // Daten "abschneiden"
                    line = line.substring(charOcc + 1);
                }
                // Der Termin ist fertig und kann dem Vekor hinzugefuegt werden
                Termine.getTermine().addElement(termin);
            }
        } // Fehlermeldung ausgeben, wenn der Speicher nicht mehr reicht
        catch (OutOfMemoryError e) {
            string = null;
            Runtime.getRuntime().gc();
            Message.show("Nicht mehr genug freier Arbeitsspeicher. Vorgang wurde abgebrochen!", AlertType.ERROR);
        }
    }

    /**
     * Hier gilt dasselbe wie fuer parse(String string), jedoch mit dem
     * Unterschied, dass der ausgewertete Termin einen Termin im Vektor
     * uebschreibt, sofern sich diese in einigen Punkten unterscheiden.
     * Die Anzahl der aktualisierten Termine wird dabei mitgezaehlt und
     * am Schluss zurueckgegeben.
     * 
     * @see #parse(String string)
     * @param string Die aus dem Internet geladenen Update-Informationen
     * @return Anzahl der aktualisierten Termine
     */
    public static int parseUpdates(String string) {
        // Anzahl der aktualisierten Termine
        int counter = 0;
        try {
            int lineOcc, charOcc;
            Termine termine = Termine.getTermine();
            while ((lineOcc = string.indexOf('\n')) != -1) {
                String line = string.substring(0, lineOcc);
                string = string.substring(lineOcc + 1);
                Termin termin = new Termin();
                while ((charOcc = line.indexOf(':')) != -1) {
                    termin.addValue(line.substring(0, charOcc));

                    line = line.substring(charOcc + 1);
                }
                // Den gerade erstellten Termin mit denen im Vektor
                // vergleichen
                for (int i = 0; i < termine.size(); i++) {
                    Termin terminOld = (Termin) termine.elementAt(i);
                    // Wenn sich der neue Termin vom alten Termin in den
                    // Punkten Datum, Raum oder Thema unterscheidet...
                    if (terminOld.getFach().equals(termin.getFach()) & terminOld.getKurstyp().equals(termin.getKurstyp()) & terminOld.getLehrer().equals(termin.getLehrer()) & (!terminOld.getDatum().equals(termin.getDatum()) | !terminOld.getRaum().equals(termin.getRaum()) | !terminOld.getThema().equals(termin.getThema()))) {
                        // ... die Notiz des alten Termins uebernehmen...
                        termin.setNotiz(terminOld.getNotiz());
                        //... den alten Termin loeschen...
                        termine.removeElement(terminOld);
                        //... dafuer den neuen einfuegen...
                        termine.insertElementAt(termin, i);
                        //... und den Counter hoeher setzen.
                        counter++;
                    }
                }
            }
        } // Fehlermeldung ausgeben, wenn der Speicher nicht mehr reicht.
        catch (OutOfMemoryError e) {
            string = null;
            Runtime.getRuntime().gc();
            Message.show("Nicht mehr genug freier Arbeitsspeicher. Vorgang wurde abgebrochen!", AlertType.ERROR);
        }
        return counter;
    }

    /**
     * Macht aus dem Termine-Vektor eine Termine-Datei (bzw. String),
     * welcher bequem im persisten Speicher des Mobiltelephons abgelegt
     * werden kann.
     * 
     * @return	Informationen ueber alle Termine in einem String
     */
    public static String deparse() {
        StringBuffer buf = new StringBuffer();
        try {
            Termine termine = Termine.getTermine();
            if (termine.size() == 0) {
                return null;
            }
            for (int i = 0; i < termine.size(); i++) {
                Termin termin = (Termin) termine.elementAt(i);
                buf.append(termin.getFach());
                buf.append(':');
                buf.append(termin.getKurstyp());
                buf.append(':');
                buf.append(termin.getRaum());
                buf.append(':');
                buf.append(termin.getLehrer());
                buf.append(':');
                buf.append(termin.getThema());
                buf.append(':');
                buf.append(termin.getDatum());
                buf.append(':');
                buf.append(termin.getNotiz());
                buf.append(':');
                buf.append('\n');
            }

        } // Auch hier wieder eine Fehlermeldung ausgeben, sollte der Speicher nicht reichen
        catch (OutOfMemoryError e) {
            buf = null;
            Runtime.getRuntime().gc();
            Message.show("Nicht mehr genug freier Arbeitsspeicher. Vorgang wurde abgebrochen!", AlertType.ERROR);
        }
        return buf.toString(); // Im Fehlerfall "null"	
    }
}
