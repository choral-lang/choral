/*
 * Copyright (C) 2019 by Saverio Giallorenzo <saverio.giallorenzo@gmail.com>
 * Copyright (C) 2019 by Fabrizio Montesi <famontesi@gmail.com>
 * Copyright (C) 2019 by Marco Peressotti <marco.peressotti@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.choral.runtime.UI;

import javax.swing.*;

public class Panel {

	private Panel(){}

	public static String prompt( String world, String prompt ) {
		JFrame jf = new JFrame( world );
		String text = JOptionPane.showInputDialog( jf , prompt, world, JOptionPane.PLAIN_MESSAGE );
		jf.dispose();
		return text;
	}

	public static void show( String world, Object text ) {
		JFrame jf = new JFrame( world );
		JOptionPane.showMessageDialog( jf, text.toString(), world, JOptionPane.PLAIN_MESSAGE );
		jf.dispose();
	}

}
