/*******************************************************************************
 * Copyright (c) 2014 Transcends, LLC.
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU 
 * General Public License as published by the Free Software Foundation; either version 2 of the 
 * License, or (at your option) any later version. This program is distributed in the hope that it will 
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You 
 * should have received a copy of the GNU General Public License along with this program; if not, 
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, 
 * USA. 
 * http://www.gnu.org/licenses/gpl-2.0.html
 *******************************************************************************/
package org.rifidi.edge.sensors.sessions;

import java.util.Set;

import org.rifidi.edge.sensors.ByteMessage;

/**
 * This is a MessageProcessingStrategy that will notify subscribers when a new
 * message has arrived
 * 
 * @author Kyle Neumeier - kyle@pramari.com
 * 
 */
public class PubSubMessageProcessingStrategy implements
		MessageProcessingStrategy {

	/** The set of subscribers to notify when there is a new message */
	private final Set<IPSessionEndpoint> subscribers;

	/**
	 * @param subscribers
	 *            The set of subscribers to notify when there is a new message
	 */
	public PubSubMessageProcessingStrategy(
			Set<IPSessionEndpoint> subscribers) {
		this.subscribers = subscribers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.rifidi.edge.core.sensors.sessions.MessageProcessingStrategy#
	 * processMessage(byte[])
	 */
	@Override
	public void processMessage(byte[] message) {
		ByteMessage byteMessage = new ByteMessage(message);
		synchronized (subscribers) {
			for (IPSessionEndpoint subscriber : subscribers) {
				subscriber.handleMessage(byteMessage);
			}
		}
	}

}
