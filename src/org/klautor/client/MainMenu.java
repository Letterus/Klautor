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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import java.io.IOException;
import javax.microedition.lcdui.List;

/**
 * Diese Klasse stellt das Hauptmenue dar und ermoeglicht den Zugriff
 * auf vier Untermenues (SubMenus).
 * Hingewiesen sei an dieser Stelle einmal wieder auf die Callback-
 * Technik: Nach Betaetigung eines Benutzerbefehls wird die MIDlet-Klasse
 * (genauer: dessen Objekt) aufgerufen, um weitere Schritte
 * <u>zentral</u> gesteuert einzuleiten.
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class MainMenu
        extends List
        implements CommandListener {

    /**
     * Befehl zum Beenden des MIDlets
     */
    private Command quit;
    /**
     * Referent zum KlautorMIDlet fuer Callbacks
     */
    private KlautorMIDlet midlet;
    // String-Konstanten fuer die einzelnen Menuepunkte
    private final String BROWSER = "Liste";
    private final String UPDATE = "Aktualisieren";
    private final String LOAD_NEW = "Neu laden";
    private final String PREFS = "Einstellungen";
    private final String HELP = "Hilfe";

    // Menue zusammenstellen
    public MainMenu(KlautorMIDlet midlet) {
        super("Hauptmen\u00FC", List.IMPLICIT);

        Image br_icon = null;
        Image upd_icon = null;
        Image load_icon = null;
        Image prefs_icon = null;
        Image help_icon = null;

        try {
            br_icon = Image.createImage("/browser.png");
            upd_icon = Image.createImage("/update.png");
            load_icon = Image.createImage("/load.png");
            prefs_icon = Image.createImage("/prefs.png");
            help_icon = Image.createImage("/help.png");
        } catch (IOException e) {
            //null-Images verwenden	
        }

        // Die fuenf Menuepunkte
        append(BROWSER, br_icon);
        append(UPDATE, upd_icon);
        append(LOAD_NEW, load_icon);
        append(PREFS, prefs_icon);
        append(HELP, help_icon);

        this.midlet = midlet;

        quit = new Command("Beenden", Command.EXIT, 0);
        addCommand(quit);
        setCommandListener(this);
    }

    /**
     * Reagiert auf Aktionen des Benutzers
     */
    public void commandAction(Command c, Displayable d) {
        //	Beenden oder neue Menues aufrufen lassen
        if (c == quit) {
            midlet.quitRequested();
        } else if (c == List.SELECT_COMMAND) {
            String menup = getString(getSelectedIndex());
            if (menup.equals(PREFS)) {
                midlet.showPrefs();
            } else if (menup.equals(UPDATE)) {
                midlet.showUpdate();
            } else if (menup.equals(LOAD_NEW)) {
                midlet.showLoadScreen();
            } else if (menup.equals(BROWSER)) {
                midlet.showBrowser(false);
            } else if (menup.equals(HELP)) {
                midlet.showHelp();
            }
        }
    }
}
