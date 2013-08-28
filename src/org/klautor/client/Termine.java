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

import java.util.Vector;

/**
 * Die Termine-Klasse ist ein Vektor, der jedoch zusaetzlich
 * die Singletontechnik implementiert.
 * In diesem Vektor werden alle Termine gespeichert, die fuer alle Programm-
 * module global erreichbar sein muessen.
 * 
 * Ein Vektor ist eine einem Array nachempfunde, dynamische Datenstruktur
 * (aus der API-Beschreibung).
 * Mit Hilfe der Singleton-Technik kann sich jedes beliebige Objekt mit
 * Hilfe der getTermine()-Methode eine Referenz der Instanz dieser Klasse
 * "abholen".
 * DIE Instanz, da dafuer gesorgt wird, dass nur ein einziges Objekt 
 * instantiiert wird, sodass sichergstellt ist, dass alle Module mit
 * denselben Daten arbeiten. 
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class Termine
        extends Vector {

    /**
     * Statische  Referenz auf sich selbst
     */
    private static Termine termine = null;

    /**
     * Die Singleton-Methode dieser Klasse
     * 
     * @return Die Instanz dieser Klasse
     */
    public static Termine getTermine() {
        if (termine == null) {
            termine = new Termine();
        }
        return termine;
    }

    /**
     * Gibt ein Element des Vektors als Termin zurueck.
     * 
     * @param num	Stelle des Termins im Vektor
     * @return	Der Termin an der Stelle num
     */
    public Termin terminAt(int num) {
        return (Termin) elementAt(num);
    }

    /**
     * Gibt die Singleton-Instanz wieder frei.
     */
    public static void freeMem() {
        if (termine != null) {
            termine.removeAllElements();
            termine = null;
        }
    }
}
