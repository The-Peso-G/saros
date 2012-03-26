package de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.impl;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;

import de.fu_berlin.inf.dpp.stf.server.StfRemoteObject;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotMenu;
import de.fu_berlin.inf.dpp.stf.server.rmi.remotebot.widget.IRemoteBotStyledText;

public final class RemoteBotStyledText extends StfRemoteObject implements
    IRemoteBotStyledText {

    private static final RemoteBotStyledText INSTANCE = new RemoteBotStyledText();

    private SWTBotStyledText widget;

    public static RemoteBotStyledText getInstance() {
        return INSTANCE;
    }

    public IRemoteBotStyledText setWidget(SWTBotStyledText styledText) {
        this.widget = styledText;
        return this;
    }

    public IRemoteBotMenu contextMenu(String text) throws RemoteException {
        return RemoteBotMenu.getInstance().setWidget(widget.contextMenu(text));
    }

    public String getText() throws RemoteException {

        return widget.getText();
    }

    public String getToolTipText() throws RemoteException {
        return widget.getToolTipText();
    }

    public String getTextOnCurrentLine() throws RemoteException {
        return widget.getTextOnCurrentLine();
    }

    public String getSelection() throws RemoteException {
        return widget.getSelection();
    }

    public List<String> getLines() throws RemoteException {
        return widget.getLines();
    }

    public int getLineCount() throws RemoteException {
        return widget.getLineCount();
    }
}
