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

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;
import java.io.IOException;

/**
 * Diese Klasse ermoeglicht es ueber eine statische Methode aus jedem
 * Teil des MIDlets eine Fehler- bzw. Informationsmeldung ausgeben 
 * zu lassen.
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class Message
        extends Alert {

    /**
     * Das Icon, das zu der Meldung angezeigt werden soll.
     */
    private static Image icon;
    /**
     * Referenz zum Display des MIDlets
     */
    private static Display display;
    /**
     * Instanz dieser Klasse
     */
    private static Message instanz = null;
    
    /**
     * Beim Erzeugen der Instanz steht noch nicht fest, welche Ueberschrift
     * die Meldung haben soll, deswegen: null
     *
     */
    public Message() {
        super(null);
    }

    /**
     * Initialisiert die Meldung.
     * Die Referenz zum Display wird uebergeben und eine Instanz
     * dieser Klasse wird erzeugt.
     * 
     * @param disp Das Display, auf dem die Meldung erscheinen soll
     */
    public static void init(Display disp) {
        display = disp;
        if (instanz == null) {
            instanz = new Message();
        }
    }

    /**
     * Gibt die Meldung auf dem Bildschirm aus und setzt je nach
     * "Alarmtyp" eine entsprechende Ueberschrift und Icon
     * 
     * @param message	Der anzuzeigende Text
     * @param type		Der "Alarmtyp"
     */
    public static void show(String message, AlertType type) {
        instanz.setType(type);
        if (type == AlertType.INFO) {
            instanz.setTitle("Information");
        } else if (type == AlertType.WARNING) {
            instanz.setTitle("Achtung!");
        } else if (type == AlertType.ERROR) {
            instanz.setTitle("Fehler!");
        } else {
            instanz.setTitle("Meldung");
        }

        instanz.setString(message);

        try {
            if (type == AlertType.INFO) {
                icon = Image.createImage("/info.png");
            } else if (type == AlertType.WARNING) {
                icon = Image.createImage("/warnung.png");
            } else if (type == AlertType.ERROR) {
                icon = Image.createImage("/fehler.png");
            } else {
                icon = Image.createImage("/haupt.png");
            }
        } catch (IOException ioe) {
            icon = null;
        }

        instanz.setImage(icon);
        instanz.setTimeout(Alert.FOREVER);
        display.setCurrent(instanz, display.getCurrent());
    }
        
    /**
     * Gibt die Willkommensmeldung (z.B. fuer den Programmstart) mit
     * Versionnummer aus.
     * 
     * @param midletVersion Die anzuzeigende Versionsnummer von Klautor
     */
    public static void splashScreen(String midletVersion) {
        instanz.setType(AlertType.INFO);
        instanz.setString("Willkommen zum KlausurTerminOrganisator!\nVersion "+midletVersion+"\n");
        
        try {
            icon = Image.createImage("/haupt.png");
        } catch (IOException ioe) {
        }

        instanz.setImage(icon);
        instanz.setTimeout(Alert.FOREVER);
        display.setCurrent(instanz, display.getCurrent());
    }

    /**
     * Gibt die genutzten Resourcen wieder frei
     */
    public static void freeMem() {
        icon = null;
        display = null;
        instanz = null;
    }
}
