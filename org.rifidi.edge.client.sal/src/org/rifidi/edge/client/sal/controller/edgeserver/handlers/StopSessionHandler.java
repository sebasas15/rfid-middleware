/**
 * 
 */
package org.rifidi.edge.client.sal.controller.edgeserver.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.rifidi.edge.client.model.sal.RemoteSession;
import org.rifidi.edge.client.sal.controller.edgeserver.EdgeServerController;
import org.rifidi.edge.client.sal.controller.edgeserver.EdgeServerTreeContentProvider;

/**
 * A handler to stop the session. Selection must be a RemoteSession and the
 * session must not be either in Created or closed state.
 * 
 * @author Kyle Neumeier - kyle@pramari.com
 * 
 */
public class StopSessionHandler extends AbstractHandler implements IHandler2 {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		EdgeServerController controller = EdgeServerTreeContentProvider
				.getEdgeServerController();

		Object obj = ((TreeSelection) sel).getFirstElement();
		RemoteSession session = (RemoteSession) obj;
		controller.stopSession(session.getReaderID(), session.getSessionID());

		return null;
	}

}