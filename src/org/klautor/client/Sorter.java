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
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

/**
 * Diese Klasse stellt einen Bildschirm dar, der zuest nach einem Sortier-
 * Kriterium fuer die Termine fragt und diese anschliessend danach sortiert.
 * 
 * Als Sortier-Algorithmus kommt hier der im Informatik-Unterricht des GSG
 * entwickelte QuickSort zum Einsatz.
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class Sorter
        extends Form
        implements CommandListener {

    /**
     * Referenz zum KlautorMIDelt
     */
    private KlautorMIDlet midlet;
    /**
     * Befehl zum Bestaerigen des Sortier-Vorgangs
     */
    private Command ok;
    /**
     * Befehl, um ohne Sortierung zum Browser zurueckzukehren
     */
    private Command cancel;
    /**
     * Auswahl-Gruppe fuer Auswahl des Suchkriteriums
     */
    private ChoiceGroup cg;
    /**
     * Referenz zum Termine-Vektor
     */
    private Termine termine;
    /*
     * IDs der einzelnen Kriterien in der Auswahl-Gruppe
     */
    private int DATUM;
    private int FACH;
    private int KURSTYP;
    private int LEHRER;

    /**
     * Beim Instantiieren eines Objekts dieser Klasse wird das UserInterface
     * zusammengestellt und die benoetigten Variablen gesetzt.
     * 
     * @param midlet	Referenz zum KlautorMIDlet fuer Callbacks
     */
    public Sorter(KlautorMIDlet midlet) {
        super("Sortieren");
        this.midlet = midlet;

        cg = new ChoiceGroup("Wonach soll sortiert werden?", ChoiceGroup.EXCLUSIVE);

        DATUM = cg.append("Datum", null);
        FACH = cg.append("Fach", null);
        KURSTYP = cg.append("Kurstyp", null);
        LEHRER = cg.append("Lehrer", null);

        append(cg);

        ok = new Command("OK", Command.OK, 2);
        cancel = new Command("Abbrechen", Command.CANCEL, 2);
        addCommand(ok);
        addCommand(cancel);
        setCommandListener(this);
    }

    /**
     * Reagiert auf Benutzeraktionen
     */
    public void commandAction(Command c, Displayable d) {
        // Nichts soll passieren, zurueck zum Browser
        if (c == cancel) {
            midlet.showBrowser(false);
        } // Es soll sortiert werden...
        else if (c == ok) {
            removeCommand(ok);
            removeCommand(cancel);
            append("Bitte warte einen Moment...");
            startSort(cg.getSelectedIndex());
        }
    }

    /**
     * Startet den Sortiervorgang anhand des uebergebenen Sortier-Kriteriums
     * und ueberprueft dabei, ob genug Speicher vorhanden ist.
     * 
     * @param kriterium	Kriterium, nach dem sortiert werden soll 
     *                      (const FACH, KURSTYP, LEHRER, DATUM).
     */
    private void startSort(int kriterium) {
        try {
            termine = Termine.getTermine();
            if (kriterium == FACH) // switch() funktioniert hier leider nicht
            {
                sortFach(0, termine.size() - 1);
            }
            if (kriterium == KURSTYP) {
                sortKurstyp(0, termine.size() - 1);
            }
            if (kriterium == LEHRER) {
                sortLehrer(0, termine.size() - 1);
            }
            if (kriterium == DATUM) {
                sortDatum(0, termine.size() - 1);
            }
            midlet.showBrowser(true);
        } // Fehlermeldung ausgeben, wenn beim Sortieren der 
        // Speicher nicht mehr reichen sollte (Stackoverflow)
        catch (OutOfMemoryError e) {
            Runtime.getRuntime().gc();
            Message.show("Nicht mehr genug freier Arbeitsspeicher. Vorgang wurde abgebrochen!", AlertType.ERROR);
        }
    }

    /**
     * Sortiert den Termine-Vektor alphatisch nach der Fachbezeichnung.
     * Der Sortieralgorithmus ist der im Informatikunterricht des GSG entwickelte
     * QuickSort mit leichten Anpassungen.
     * 
     * @param anfang	Erstes Element der zu sortierenden Menge aus dem Termine-Vektor
     * @param ende		Letztes Element der zu sortierenden Menge aus dem Termine-Vektor
     */
    private void sortFach(int anfang, int ende) {
        // Rechter Zeiger
        int z_r = ende;
        // Linker Zeiger
        int z_l = anfang;
        // Hilfstermin (zum Tauschen)
        Termin help = null;

        // Pivot(=Vergleichs)-Element
        String pivot = termine.terminAt((anfang + ende) / 2).getFach();

        // Solange die sich links vom Pivot-Element befindlichen Elemente
        // kleiner sind als das Pivot-Element (lexigraphisch davor stehen)
        // den linken Zeiger nach rechts weiterruecken.
        while (termine.terminAt(z_l).getFach().compareTo(pivot) < 0) {
            z_l++;
        }

        // Solange die sich rechts vom Pivot-Element befindlichen Elemente
        // groesser sind als das Pivot-Element (lexigrafisch danach stehen)
        // den rechten Zeiger nach links weiterruecken.
        while (termine.terminAt(z_r).getFach().compareTo(pivot) > 0) {
            z_r--;
        }

        // Wenn sich die Zeiger nicht ueberkrezt haben...
        if (z_l <= z_r) {
            // Die Termine vertauschen, bei denen die Zeiger stehen
            // geblieben sind => passen nicht in die Reihenfolge
            help = termine.terminAt(z_l);
            termine.setElementAt(termine.terminAt(z_r), z_l);
            termine.setElementAt(help, z_r);

            // Zeiger weiterruekcen
            z_l++;
            z_r--;
        }
        // Wenn der rechte Zeiger die Sortiermenge nicht "verlassen"
        // hat, die Teilmenge zwischen Anfangs-Element und rechten 
        // Zeiger sortieren.
        if (anfang <= z_r) {
            sortFach(anfang, z_r);
        }
        // Wenn der linke Zeiger die Sortiermenge nicht "verlassen"
        // hat, die Teilmeine zwischen linkem Zeiger und letztem
        // Element sortieren.
        if (z_l <= ende) {
            sortFach(z_l, ende);
        }
    }

    /**
     * Sortiert den Termine-Vektor alphatisch in umgekehrter Reihenfolge
     * nach dem Kurstyp, d.h. LKs werden zuerst aufgelistet und danach GKs.
     * Der Sortieralgorithmus ist der im Informatikunterricht des GSG entwickelte
     * QuickSort mit leichten Anpassungen.
     * 
     * @param anfang	Erstes Element der zu sortierenden Menge aus dem Termine-Vektor
     * @param ende		Letztes Element der zu sortierenden Menge aus dem Termine-Vektor
     */
    private void sortKurstyp(int anfang, int ende) {
        int z_r = ende;
        int z_l = anfang;
        Termin help = null;

        String pivot = termine.terminAt((anfang + ende) / 2).getKurstyp();

        while (termine.terminAt(z_l).getKurstyp().compareTo(pivot) >= 0 && z_l < z_r) //LKs nach oben
        {                                                       // ^ (Fast-)Endlosschleife bei zuvielen gleichen Elementen verhindern
            z_l++;
        }

        while (termine.terminAt(z_r).getKurstyp().compareTo(pivot) < 0) //LKs nach oben
        {
            z_r--;
        }

        if (z_l <= z_r) {
            help = termine.terminAt(z_l);
            termine.setElementAt(termine.terminAt(z_r), z_l);
            termine.setElementAt(help, z_r);

            z_l++;
            z_r--;
        }
        if (anfang <= z_r) {
            sortKurstyp(anfang, z_r);
        }
        if (z_l <= ende) {
            sortKurstyp(z_l, ende);
        }
    }

    /**
     * Sortiert den Termine-Vektor alphatisch nach dem Lehrernamen.
     * Der Sortieralgorithmus ist der im Informatikunterricht des GSG entwickelte
     * QuickSort mit leichten Anpassungen.
     * 
     * @param anfang	Erstes Element der zu sortierenden Menge aus dem Termine-Vektor
     * @param ende		Letztes Element der zu sortierenden Menge aus dem Termine-Vektor
     */
    private void sortLehrer(int anfang, int ende) {
        int z_r = ende;
        int z_l = anfang;
        Termin help = null;

        String pivot = termine.terminAt((anfang + ende) / 2).getLehrer();

        while (termine.terminAt(z_l).getLehrer().compareTo(pivot) < 0) {
            z_l++;
        }

        while (termine.terminAt(z_r).getLehrer().compareTo(pivot) > 0) {
            z_r--;
        }

        if (z_l <= z_r) {
            help = termine.terminAt(z_l);
            termine.setElementAt(termine.terminAt(z_r), z_l);
            termine.setElementAt(help, z_r);

            z_l++;
            z_r--;
        }
        if (anfang <= z_r) {
            sortLehrer(anfang, z_r);
        }
        if (z_l <= ende) {
            sortLehrer(z_l, ende);
        }
    }

    /**
     * Sortiert den Termine-Vektor nach dem Datum der Termine.
     * Der Sortieralgorithmus ist der im Informatikunterricht des GSG entwickelte
     * QuickSort mit leichten Anpassungen.
     * 
     * @param anfang	Erstes Element der zu sortierenden Menge aus dem Termine-Vektor
     * @param ende		Letztes Element der zu sortierenden Menge aus dem Termine-Vektor
     */
    private void sortDatum(int anfang, int ende) {
        int z_r = ende;
        int z_l = anfang;
        Termin help = null;

        long pivot = termine.terminAt((anfang + ende) / 2).getDatumLong();

        while (termine.terminAt(z_l).getDatumLong() < pivot) {
            z_l++;
        }

        while (termine.terminAt(z_r).getDatumLong() > pivot) {
            z_r--;
        }

        if (z_l <= z_r) {
            help = termine.terminAt(z_l);
            termine.setElementAt(termine.terminAt(z_r), z_l);
            termine.setElementAt(help, z_r);

            z_l++;
            z_r--;
        }
        if (anfang <= z_r) {
            sortDatum(anfang, z_r);
        }
        if (z_l <= ende) {
            sortDatum(z_l, ende);
        }
    }
}
