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

package org.choral.runtime.TLSByteChannel.tlschannel;

import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import java.util.Optional;

/**
 * Factory for {@link SSLContext}s, based in an optional {@link SNIServerName}. Implementations of this interface are
 * supplied to {@link ServerTlsChannel} instances, to select the correct context (and so the correct
 * certificate) based on the server name provided by the client.
 */
@FunctionalInterface
public interface SniSslContextFactory {

    /**
     * Return a proper {@link SSLContext}.
     *
     * @param sniServerName an optional {@link SNIServerName}; an empty value means that the client did not send and SNI
     *                      value.
     * @return the chosen context, or an empty value, indicating that no context is supplied and the connection should
     * be aborted.
     */
    Optional<SSLContext> getSslContext(Optional<SNIServerName> sniServerName);
}
