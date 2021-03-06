/*
 * Copyright (C) 2013   Bob Rutledge
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * and open the template in the editor.
 */
package org.lreqpcr.amplicon_ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.lreqpcr.amplicon_ui.amplicon_io.AmpliconImport;
import org.lreqpcr.core.database_services.DatabaseServices;
import org.lreqpcr.core.database_services.DatabaseType;
import org.lreqpcr.core.utilities.UniversalLookup;
import org.openide.windows.WindowManager;

public final class AmpliconImportAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        DatabaseServices ampliconDB = (DatabaseServices) UniversalLookup.
                getDefault().getAll(DatabaseType.AMPLICON).get(0);
        if(!ampliconDB.isDatabaseOpen()){
            Toolkit.getDefaultToolkit().beep();
            String msg = "An amplicon database has not been opened";
            JOptionPane.showMessageDialog(
                    WindowManager.getDefault().getMainWindow(),
                    msg,
                    "No amplicon database",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        AmpliconImport.importAmplicon(ampliconDB);
    }
}
