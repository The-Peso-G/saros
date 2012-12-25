package de.fu_berlin.inf.dpp.activities.business;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.StopFollowingActivityDataObject;
import de.fu_berlin.inf.dpp.project.ISarosSession;

/**
 * This activity notifies the recipient that the local user is following someone
 * in the running session
 * 
 * @author Alexander Waldmann (contact@net-corps.de)
 */
public class StopFollowingActivity extends AbstractActivity {
    public StopFollowingActivity(User source) {
        super(source);
    }

    @Override
    public void dispatch(IActivityReceiver receiver) {
        receiver.receive(this);
    }

    @Override
    public IActivityDataObject getActivityDataObject(ISarosSession sarosSession) {
        return new StopFollowingActivityDataObject(source.getJID());
    }

    @Override
    public String toString() {
        return "StopFollowingActivity(" + source + ")";
    }

    public SPath getPath() {
        return null;
    }
}
