/**
 * 
 */
package org.rifidi.edge.epcglobal.aleread;

import java.net.URI;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rifidi.edge.core.messages.DatacontainerEvent;
import org.rifidi.edge.core.messages.TagReadEvent;
import org.rifidi.edge.epcglobal.ale.api.read.data.ECReport;
import org.rifidi.edge.epcglobal.ale.api.read.data.ECReports;
import org.rifidi.edge.epcglobal.ale.api.read.data.ECSpec;
import org.rifidi.edge.epcglobal.ale.api.read.data.ECReports.Reports;
import org.rifidi.edge.epcglobal.aleread.ALEReadAPI.TriggerCondition;
import org.rifidi.edge.epcglobal.aleread.rifidievents.StartEvent;
import org.rifidi.edge.epcglobal.aleread.wrappers.RifidiECSpec;
import org.rifidi.edge.epcglobal.aleread.wrappers.RifidiReport;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.event.map.MapEventBean;

/**
 * 
 * 
 * @author Jochen Mader - jochen@pramari.com
 * 
 */
public class ECReportmanager implements Runnable, StatementAwareUpdateListener {
	/** Logger for this class. */
	private static final Log logger = LogFactory.getLog(ECReportmanager.class);
	/** Reports that need to be processed. */
	private List<RifidiReport> rifidiReports;

	// TODO: synchronization?
	/** List containing statements that trigger when duration ran out. */
	private List<EPStatement> durationStatements;
	/** List containing statements that trigger when the tag set is stable. */
	private List<EPStatement> stableSetIntervalStatements;
	/** List containing statements that trigger when a stop trigger arrived. */
	private List<EPStatement> stopTriggerStatements;
	/** List containing statements that trigger when data is available. */
	private List<EPStatement> whenDataAvailableStatements;
	/**
	 * List containing statements that trigger when a destroy event was
	 * received.
	 */
	private List<EPStatement> destroyStatements;
	/** Incoming events. */
	private LinkedBlockingQueue<EventTuple> eventQueue;
	/** Target uris for the rifidiReports. */
	private List<URI> uris;
	/** Esper instance used by this instance. */
	private EPServiceProvider esper;
	/** Name of the ec spec. */
	private String specName;
	/** True if the spec has been destroied by a delete event. */
	private boolean destroied;
	/**
	 * The spec associated with this reportmanager. Null if the spec is not sent
	 * back with the rifidiReports.
	 */
	private ECSpec spec;
	/** Rifidi ec spec owning this manager. */
	private RifidiECSpec owner;
	/** Handler for the triggers. */
	private ReadTriggerHandler readTriggerHandler;

	/**
	 * Constructor.
	 */
	public ECReportmanager(String specName, EPServiceProvider esper,
			List<RifidiReport> reports, RifidiECSpec owner, List<URI> uris) {
		rifidiReports = reports;
		eventQueue = new LinkedBlockingQueue<EventTuple>();
		destroied = false;
		readTriggerHandler = new ReadTriggerHandler(this, owner
				.getRifidiBoundarySpec());
		this.uris = uris;
		this.esper = esper;
		this.specName = specName;
		this.owner = owner;
	}

	/**
	 * Constructor.
	 */
	public ECReportmanager(String specName, EPServiceProvider esper,
			ECSpec spec, List<RifidiReport> reports, RifidiECSpec owner,
			List<URI> uris) {
		this(specName, esper, reports, owner, uris);
		this.spec = spec;
	}

	/**
	 * Add the incoming list of events to the processing queue.
	 * 
	 * @param eventTuple
	 */
	public void addEvents(EventTuple eventTuple) {
		eventQueue.add(eventTuple);
	}

	/**
	 * @param durationStatements
	 *            the durationStatements to set
	 */
	public void setDurationStatements(List<EPStatement> durationStatements) {
		this.durationStatements = durationStatements;
	}

	/**
	 * @param stableSetIntervalStatements
	 *            the stableSetIntervalStatements to set
	 */
	public void setStableSetIntervalStatements(
			List<EPStatement> stableSetIntervalStatements) {
		this.stableSetIntervalStatements = stableSetIntervalStatements;
	}

	/**
	 * @param stopTriggerStatements
	 *            the stopTriggerStatements to set
	 */
	public void setStopTriggerStatements(List<EPStatement> stopTriggerStatements) {
		this.stopTriggerStatements = stopTriggerStatements;
	}

	/**
	 * @param whenDataAvailableStatements
	 *            the whenDataAvailableStatements to set
	 */
	public void setWhenDataAvailableStatements(
			List<EPStatement> whenDataAvailableStatements) {
		this.whenDataAvailableStatements = whenDataAvailableStatements;
	}

	/**
	 * @param destroyStatements
	 *            the destroyStatements to set
	 */
	public void setDestroyStatements(List<EPStatement> destroyStatements) {
		this.destroyStatements = destroyStatements;
	}

	protected void startTrigger(String uri) {
		logger.debug("Start trigger went off: " + uri);
	}

	protected void stopTrigger(String uri) {
		logger.debug("Stop trigger went off: " + uri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.espertech.esper.client.StatementAwareUpdateListener#update(com
	 * .espertech.esper.client.EventBean[],
	 * com.espertech.esper.client.EventBean[],
	 * com.espertech.esper.client.EPStatement,
	 * com.espertech.esper.client.EPServiceProvider)
	 */
	@Override
	public synchronized void update(EventBean[] arg0, EventBean[] arg1,
			EPStatement arg2, EPServiceProvider arg3) {
		EventTuple tuple = null;
		if (destroied) {
			// ECSpec is destroied.
			return;
		}
		if (destroyStatements.contains(arg2)) {
			destroied = true;
			tuple = new EventTuple();
			tuple.condition = TriggerCondition.DESTROIED;
			// callback for destroying the statements
			owner.discard();
		} else {
			tuple = new EventTuple();
		}

		// TODO: fix, needs timing
		esper.getEPRuntime().sendEvent(new StartEvent("wuhu"));
		if (whenDataAvailableStatements.contains(arg2)) {
			tuple.condition = TriggerCondition.DATA_AVAILABLE;
		}
		if (stableSetIntervalStatements.contains(arg2)) {
			tuple.condition = TriggerCondition.STABLE_SET;
		}
		if (stopTriggerStatements.contains(arg2)) {
			tuple.condition = TriggerCondition.STOP_TRIGGER;
		}
		if (durationStatements.contains(arg2)) {
			tuple.condition = TriggerCondition.DURATION;
		}
		if (destroyStatements.contains(arg2)) {
			tuple.condition = TriggerCondition.UNDEFINE;
		}
		tuple.beans = arg0;
		addEvents(tuple);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		readTriggerHandler.start();
		// subscribe to all statements
		for (EPStatement statement : durationStatements) {
			statement.addListener(this);
		}
		for (EPStatement statement : stableSetIntervalStatements) {
			statement.addListener(this);
		}
		for (EPStatement statement : stopTriggerStatements) {
			statement.addListener(this);
		}
		for (EPStatement statement : whenDataAvailableStatements) {
			statement.addListener(this);
		}
		for (EPStatement statement : destroyStatements) {
			statement.addListener(this);
		}
		while (!Thread.currentThread().isInterrupted()) {
			try {

				// get the eventtuple submitted last
				EventTuple tagevents = eventQueue.take();
				Set<DatacontainerEvent> events = new HashSet<DatacontainerEvent>();
				// collect tags from event beans
				for (EventBean tagevent : tagevents.beans) {
					if (((MapEventBean) tagevent).getProperties().containsKey(
							"tags")) {
						events.add(((TagReadEvent) ((MapEventBean) tagevent)
								.get("tags")).getTag());
					} else {
						break;
					}
				}
				// process the set of events
				for (RifidiReport rifidiReport : rifidiReports) {
					rifidiReport.processEvents(events);
				}
				// send the report
				ECReports ecreports = new ECReports();
				ecreports.setALEID("Rifidi Edge Server");
				ecreports.setDate(getCurrentCalendar());
				if (spec != null) {
					ecreports.setECSpec(spec);
				}
				// one of TRIGGER REPEAT_PERIOD REQUESTED UNDEFINE
				ecreports.setInitiationCondition(ALEReadAPI.conditionToName
						.get(tagevents.condition));
				if (ecreports.getInitiationCondition().equals("TRIGGER")) {
				}
				ecreports.setTerminationCondition("TRIGGER");
				// one of TRIGGER DURATION STABLE_SET DATA_AVAILABLE
				// UNREQUEST
				// UNDEFINE
				ecreports.setTerminationCondition(ALEReadAPI.conditionToName
						.get(tagevents.condition));
				if (TriggerCondition.STOP_TRIGGER.equals(tagevents.condition)) {
					// ecreports.setTerminationTrigger(value);
				}

				ecreports.setSpecName(specName);
				// running time
				ecreports.setTotalMilliseconds(10);
				Reports reportsPoltergeist = new Reports();
				ecreports.setReports(reportsPoltergeist);
				for (RifidiReport rifidiReport : rifidiReports) {
					ECReport rep = rifidiReport.send(tagevents.condition);
					if (rep != null) {
						ecreports.getReports().getReport().add(rep);
					}
				}
			} catch (InterruptedException e) {
				readTriggerHandler.stop();
				Thread.currentThread().interrupt();
			}
		}
		readTriggerHandler.stop();
	}

	// TODO: aaaargh performance!!!!!!!!!!!
	private XMLGregorianCalendar getCurrentCalendar() {
		GregorianCalendar gc = (GregorianCalendar) GregorianCalendar
				.getInstance();
		DatatypeFactory dataTypeFactory = null;
		try {
			dataTypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException ex) {
			logger.fatal("epic fail: " + ex);
		}
		return dataTypeFactory.newXMLGregorianCalendar(gc);
	}

	private class EventTuple {
		public EventBean[] beans;
		public TriggerCondition condition;
	}
}
