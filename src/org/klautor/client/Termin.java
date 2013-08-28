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

/**
 * Diese Klasse repraesentiert einen Klausurtermin mit den benoetigten
 * Eigenschaften und Methoden.
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class Termin {

    /**
     * Fach/Kurs, in dem die Klausur stattfindet.
     */
    private String fach;
    /**
     * Kurstyp des Fachs
     * (i.d.R. Sek II: "LK" => Leistungskurs oder "GK" => Grundkurs, 
     * aber auch andere Werte sind moeglich. z.B. fuer die Sek.I:
     * Klassenbezeichnung: "5A", "6B", "C" usw.).
     */
    private String kurstyp;
    /**
     * Raum, in dem die Klausur stattfindet
     */
    private String raum;
    /**
     * Lehrer. Entweder der Lehrer, der die Aufsicht fuehrt, oder
     * der Kursleiter.
     */
    private String lehrer;
    /**
     * Thema der Klausur
     */
    private String thema;
    /**
     * Datum, an dem die Klausurstattfindet.
     * Bestehend aus Tag, Monat, Jahr, getrennt durch einen Punkt.
     * z.B.: "1.3.04", besser noch "01.03.2004" (es ist beides 
     * - auch in Kombination - moeglich)
     */
    private String datum;
    /**
     * Notiz des Benutzers zu dem jeweiligen Termin
     */
    private String notiz;

    /**
     * Beim Erzeugen eines Termin-Objekts werden alle Werte erst
     * einmal auf den "null-Wert" gesetzt.
     */
    public Termin() {
        fach = null;
        kurstyp = null;
        raum = null;
        lehrer = null;
        thema = null;
        datum = null;
        notiz = null;
    }

    /**
     * Fuegt dem Termin in der auch oben angegeben Reihenfolge Werte
     * hinzu (Fach, Kurstyp, Raum, Lehrer, Thema, Datum, Notiz).
     * Diese Methode wird beim Parsen der Daten aus dem Internet oder
     * Telephonspeicher verwendet.
     * 
     * @param value	Wert, der dem Termin hinzugefuegt werden soll.
     */
    public void addValue(String value) {
        if (fach == null) {
            fach = value;
        } else if (fach != null & kurstyp == null) {
            kurstyp = value;
        } else if (fach != null & kurstyp != null & raum == null) {
            raum = value;
        } else if (fach != null & kurstyp != null & raum != null & lehrer == null) {
            lehrer = value;
        } else if (fach != null & kurstyp != null & raum != null & lehrer != null & thema == null) {
            thema = value;
        } else if (fach != null & kurstyp != null & raum != null & lehrer != null & thema != null & datum == null) {
            datum = value;
        } else if (fach != null & kurstyp != null & raum != null & lehrer != null & thema != null & datum != null & notiz == null) {
            notiz = value;
        }
    }

    /**
     * Konvertiert das als String gespeichert Datum in eine Zahl vom
     * Typ long, damit die Daten beim Sortieren vergleichen werden
     * koennen.
     * 
     * @return	Das Datum des Termins als Zahl vom Typ long.
     */
    public long getDatumLong() {
        // Ein Datum besteht aus drei Teilen..
        String ergebnis[] = new String[3];
        int i = 0;
        StringBuffer buf = new StringBuffer();
        // Die Teile zwischen den Punkten zuerst in einem StringBuffer,
        // wenn der Punkt dann auftaucht, in dem Array speichern.
        for (int j = 0; j < datum.length(); j++) {
            if (datum.charAt(j) != '.') {
                buf.append(datum.charAt(j));
            } else {
                ergebnis[i] = buf.toString();
                i++;
                buf = new StringBuffer();
            }
        }
        ergebnis[2] = buf.toString();

        // Format angleichen (wenn nicht genuegend Nullen im Datum
        // sind, koennen wir daraus keine vergleichbare Zahl machen)
        if (Integer.parseInt(ergebnis[0]) < 10) {
            ergebnis[0] = "0" + ergebnis[0];
        }
        if (Integer.parseInt(ergebnis[1]) < 10) {
            ergebnis[1] = "0" + ergebnis[1];
        }
        if (Integer.parseInt(ergebnis[2]) < 2000) {
            ergebnis[2] = "20" + ergebnis[2];
        }

        // Aufgeteiltes und formatiertes Datum als Zahl im Format
        // YYYYMMDD zurueckgeben (z.B. aus "1.3.04" wird 20040301)
        return Long.parseLong(ergebnis[2] + ergebnis[1] + ergebnis[0]);
    }

    /**
     * @return	Fach, in dem die Klausur geschrieben wird, oder "n/a",
     * wenn der Wert nicht gesetzt wurde.
     */
    public String getFach() {
        if (fach == null) {
            return "n/a";
        } else {
            return fach;
        }
    }

    /**
     * @return	Kurstyp des Fachf, in dem die Klausur geschrieben 
     * wird, oder "n/a", wenn der Wert nicht gesetzt wurde.
     */
    public String getKurstyp() {
        if (kurstyp == null) {
            return "n/a";
        } else {
            return kurstyp;
        }
    }

    /**
     * @return	Lehrer, der die Klausur beaufsichtigt oder der Kursleiter,
     * oder "n/a", wenn der Wert nicht gesetzt wurde.
     */
    public String getLehrer() {
        if (lehrer == null) {
            return "n/a";
        } else {
            return lehrer;
        }
    }

    /**
     * @return	Raum, in dem die Klausur geschrieben wird, oder "n/a",
     * wenn der Wert nicht gesetzt wurde.
     */
    public String getRaum() {
        if (raum == null) {
            return "n/a";
        } else {
            return raum;
        }
    }

    /**
     * @return	Thema, ueber das die Klausur geschrieben wird, 
     * oder "n/a", wenn der Wert nicht gesetzt wurde.
     */
    public String getThema() {
        if (thema == null) {
            return "n/a";
        } else {
            return thema;
        }
    }

    /**
     * @return	Datum, an dem die Klausur geschrieben wird, oder 
     * "n/a", wenn der Wert nicht gesetzt wurde.
     */
    public String getDatum() {
        if (datum == null) {
            return "n/a";
        } else {
            return datum;
        }
    }

    /**
     * @return	Notiz des Benutzers zu dem Termin, oder "n/a",
     * wenn der Wert nicht gesetzt wurde.
     */
    public String getNotiz() {
        if (notiz == null) {
            return "n/a";
        } else {
            return notiz;
        }
    }

    /**
     * @param string Fach, in dem die Klausur geschrieben wird.
     */
    public void setFach(String string) {
        fach = string;
    }

    /**
     * @param string Kurstyp des Fachs, in dem die Klausur geschrieben wird.
     */
    public void setKurstyp(String string) {
        kurstyp = string;
    }

    /**
     * @param string Lehrer, der Aufsicht fuehrt oder den Kurs leitet.
     */
    public void setLehrer(String string) {
        lehrer = string;
    }

    /**
     * @param string Raum, in dem die Klausur geschrieben wird.
     */
    public void setRaum(String string) {
        raum = string;
    }

    /**
     * @param string Thema der Klausur.
     */
    public void setThema(String string) {
        thema = string;
    }

    /**
     * @param string Datum, an dem die Klausur geschrieben wird.
     */
    public void setDatum(String string) {
        datum = string;
    }

    /**
     * @param string Notiz des Benutzers zu dem Termin
     */
    public void setNotiz(String string) {
        notiz = string;
    }
}
