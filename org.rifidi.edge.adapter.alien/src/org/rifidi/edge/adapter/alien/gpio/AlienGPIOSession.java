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
package org.rifidi.edge.adapter.alien.gpio;

import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.edge.adapter.alien.Alien9800Reader;
import org.rifidi.edge.adapter.alien.Alien9800ReaderSession;
import org.rifidi.edge.adapter.alien.gpio.messages.AlienIOListMessageParsingStrategy;
import org.rifidi.edge.sensors.AbstractCommandConfiguration;
import org.rifidi.edge.sensors.AbstractSensor;
import org.rifidi.edge.sensors.CannotExecuteException;
import org.rifidi.edge.sensors.sessions.AbstractServerSocketSensorSession;
import org.rifidi.edge.sensors.sessions.MessageParsingStrategy;
import org.rifidi.edge.sensors.sessions.MessageParsingStrategyFactory;
import org.rifidi.edge.sensors.sessions.MessageProcessingStrategy;
import org.rifidi.edge.sensors.sessions.MessageProcessingStrategyFactory;

/**
 * This is a session that listens for events on an Alien reader's IOStream. It
 * opens up a port on the serverSocketPort and the Alien reader can then send IO
 * events to that port.
 * 
 * The session needs to be initialized before the connect method can be called
 * so that it can get the initial state of the external inputs and outputs.
 * 
 * This session should normally be started up as part of an interactive session
 * (Alien9800ReaderSession).
 * 
 * @author Kyle Neumeier - kyle@pramari.com
 * 
 */
public class AlienGPIOSession extends AbstractServerSocketSensorSession {

	/** The strategy used to parse incoming strings into AlienGPIMessage */
	private AlienIOListMessageParsingStrategy IOListParsingStrategy;
	/** The initial state of the external input */
	private int initialExternalInputValue = 0;
	/** The initial state of the external output */
	private int initialExternalOutputValue = 0;
	/** a variable that keeps track if this object has been initialized */
	private boolean hasBeenInitialized = false;
	/** The logger for this class */
	@SuppressWarnings("unused")
	private Log logger = LogFactory.getLog(AlienGPIOSession.class);

	/**
	 * Constructor
	 * 
	 * @param sensor
	 *            The sensor this session belongs to
	 * @param ID
	 *            The ID of this session
	 * @param serverSocketPort
	 *            The port to open up a listening socket on
	 * @param parsingStrategy
	 *            The strategy to use to parse strings into AlienGPIOMessages
	 *            (e.g. text, terse, xml, custom)
	 */
	public AlienGPIOSession(AbstractSensor<?> sensor, String ID,
			int serverSocketPort,
			AlienIOListMessageParsingStrategy parsingStrategy) {
		super(sensor, ID, serverSocketPort, 1,
				new HashSet<AbstractCommandConfiguration<?>>());
		this.IOListParsingStrategy = parsingStrategy;
	}

	/**
	 * Initialize must be called before connect can be called
	 * 
	 * @param interactiveSession
	 *            The live interactive session used to query the reader for it's
	 *            current state of external input and output
	 * @throws CannotExecuteException
	 *             if there was a problem when the initialization was attempted.
	 */
	public void initialize(Alien9800ReaderSession interactiveSession)
			throws CannotExecuteException {
		this.initialExternalInputValue = interactiveSession.getExternalInput();
		this.hasBeenInitialized = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.sensors.sessions.AbstractServerSocketSensorSession
	 * #_connect()
	 */
	@Override
	protected void _connect() throws IOException {
		if (!hasBeenInitialized) {
			throw new IOException("GPIO Session must be initialized");
		}
		super._connect();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.sensors.sessions.AbstractServerSocketSensorSession
	 * #disconnect()
	 */
	@Override
	public void disconnect() {
		super.disconnect();
		this.hasBeenInitialized = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.sensors.sessions.AbstractServerSocketSensorSession
	 * #getMessageParsingStrategyFactory()
	 */
	@Override
	protected MessageParsingStrategyFactory getMessageParsingStrategyFactory() {
		return new MessageParsingStrategyFactory() {

			@Override
			public MessageParsingStrategy createMessageParser() {
				return new AlienGPIOMessageParsingStrategy();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.sensors.sessions.AbstractServerSocketSensorSession
	 * #getMessageProcessingStrategyFactory()
	 */
	@Override
	protected MessageProcessingStrategyFactory getMessageProcessingStrategyFactory() {
		return new MessageProcessingStrategyFactory() {
			@Override
			public MessageProcessingStrategy createMessageProcessor() {
				return new AlienGPIOMessageProcessingStrategy(
						IOListParsingStrategy, (Alien9800Reader) getSensor(),
						initialExternalInputValue, initialExternalOutputValue);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rifidi.edge.sensors.sessions.AbstractServerSocketSensorSession
	 * #toString()
	 */
	@Override
	public String toString() {
		return "[GPIO Session " + super.toString() + "]";
	}

}
