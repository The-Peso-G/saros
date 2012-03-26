package de.fu_berlin.inf.dpp.ui.commandHandlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.fu_berlin.inf.dpp.Messages;
import de.fu_berlin.inf.dpp.util.Utils;

public class GettingStartedHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {

        Utils.openInternalBrowser(Messages.Saros_tutorial_url,
            Messages.Saros_tutorial_title);

        return null;
    }

}