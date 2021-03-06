/*******************************************************************************
 * Copyright (c) 2015 Eclipse Foundation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mikael Barbero - initial implementation
 *******************************************************************************/
package org.eclipse.cbi.common.util;

import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.ImmutableList;

final class MorePosixFilePermissions {

	// indexed by the standard binary representation of permission
	// if permission is 644; in binary 110 100 100 
	// the positions of the ones (little endian) give the index of the permission in the list below
	private static final ImmutableList<PosixFilePermission> POSIX_PERMISSIONS = ImmutableList.of(
			PosixFilePermission.OTHERS_EXECUTE, PosixFilePermission.OTHERS_WRITE, PosixFilePermission.OTHERS_READ, 
			PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.GROUP_WRITE, PosixFilePermission.GROUP_READ, 
			PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ
	);
	private static final int MAX_MODE = (1 << POSIX_PERMISSIONS.size()) - 1;

	private MorePosixFilePermissions() {
		throw new AssertionError();
	}

	/**
	 * Converts the set of {@link PosixFilePermission} to a file mode.
	 */
	public static long toFileMode(Set<PosixFilePermission> permissions) {
		int filemode = 0;
		for (int i = 0; i < POSIX_PERMISSIONS.size(); i++) {
			if (permissions.contains(POSIX_PERMISSIONS.get(i))) {
				filemode |= 1 << i;
			}
		}
		return filemode;
	}

	public static Set<PosixFilePermission> fromFileMode(final long filemode) {
		if ((filemode & MAX_MODE) != filemode) {
			throw new IllegalStateException("Invalid file mode '"+Integer.toOctalString((int)filemode)+"'. File mode must be between 0 and " + MAX_MODE + " ("+Integer.toOctalString(MAX_MODE)+" in octal)");
		}

		final Set<PosixFilePermission> ret = EnumSet.noneOf(PosixFilePermission.class);
		
		for (int i = 0; i < POSIX_PERMISSIONS.size(); i++) {
			if ((filemode & (1 << i)) != 0) {
				ret.add(POSIX_PERMISSIONS.get(i));
			}
		}

		return ret;
	}
}
