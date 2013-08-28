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

/**
 * Diese Klasse wird vom AbstractLoadScreen abgeleitet und 
 * implementiert die geforderten Methoden fuer das 
 * Benutzerinterface und zum Starten der Threads.
 * 
 * @author Johannes Brakensiek <a href="mailto:johannes@quackes.de"><code>&lt;johannes@quackes.de&gt;</code></a>
 */
public class LoadScreen
        extends AbstractLoadScreen {

    /**
     * @param midlet Referenz zum KlautorMIDlet fuer Callbacks
     */
    public LoadScreen(KlautorMIDlet midlet) {
        // Callbacks werden von der Super-Klasse durchgefuehrt
        super(midlet);
    }

    public String heading() {
        return "Neu laden";
    }

    public String text() {
        return "Alle Termine wirklich neu herunterladen?";
    }

    public boolean needTermine() {
        return false;
    }

    protected void startThreads() {
        loader = new TerminLoader(this, ok, cancel);
    }
}
