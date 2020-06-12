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

/**
 * <p> TLS Channel is a library that implements a ByteChannel interface to a TLS (Transport Layer Security) connection.
 * The library delegates all cryptographic operations to the standard Java TLS implementation: SSLEngine; effectively
 * hiding it behind an easy-to-use streaming API, that allows to securitize JVM applications with minimal added
 * complexity.</p>
 *
 * <p> In other words, a simple library that allows the programmer to have TLS using the same standard socket API used
 * for plaintext, just like OpenSSL does for C, only for Java, filling a specially painful missing feature of the
 * standard Java library.</p>
 */
package org.choral.runtime.TLSByteChannel.tlschannel;
