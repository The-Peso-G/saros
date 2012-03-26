package de.fu_berlin.inf.dpp.ui.wizards.pages;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jivesoftware.smack.Roster;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.events.DiscoveryManagerListener;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.selection.retriever.SelectionRetrieverFactory;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.BuddySelectionComposite;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.BuddySelectionChangedEvent;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.BuddySelectionListener;
import de.fu_berlin.inf.dpp.ui.widgets.viewer.roster.events.FilterNonSarosBuddiesChangedEvent;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * Allows the user to select a {@link JID} from the {@link Roster}.
 * 
 * @author bkahlert
 */
public class BuddySelectionWizardPage extends WizardPage {
    private final Logger log = Logger.getLogger(BuddySelectionWizardPage.class);

    public static final String TITLE = Messages.BuddySelectionWizardPage_title;
    public static final String DESCRIPTION = Messages.BuddySelectionWizardPage_description;

    public static final String NO_BUDDY_SELECTED_ERROR_MESSAGE = Messages.BuddySelectionWizardPage_error_select_one_buddy;
    public static final String OFFLINE_BUDDY_SELECTED_ERROR_MESSAGE = Messages.BuddySelectionWizardPage_error_selected_offline;
    public static final String BUDDIES_WITHOUT_SAROS_SUPPORT_WARNING_MESSAGE = Messages.BuddySelectionWizardPage_warn_only_saros_buddies;

    protected BuddySelectionComposite buddySelectionComposite;

    @Inject
    protected IPreferenceStore preferenceStore;

    /**
     * This {@link BuddySelectionListener} changes the {@link WizardPage}'s
     * state according to the selected {@link JID}s.
     */
    protected BuddySelectionListener buddySelectionListener = new BuddySelectionListener() {
        public void buddySelectionChanged(BuddySelectionChangedEvent event) {
            updatePageCompletion();
        }

        public void filterNonSarosBuddiesChanged(
            FilterNonSarosBuddiesChangedEvent event) {

            preferenceStore.setValue(
                PreferenceConstants.BUDDYSELECTION_FILTERNONSAROSBUDDIES,
                event.isFilterNonSarosBuddies());
        }
    };

    @Inject
    protected DiscoveryManager discoveryManager;
    /**
     * This listener update the page completion if someone's presence changed.
     */
    protected DiscoveryManagerListener discoveryManagerListener = new DiscoveryManagerListener() {
        public void featureSupportUpdated(final JID jid, String feature,
            boolean isSupported) {
            if (Saros.NAMESPACE.equals(feature)) {
                Utils.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        updatePageCompletion();
                    }
                });
            }
        }
    };

    /**
     * This flag is true as soon as the user selected buddies without problems.
     */
    protected boolean selectionWasValid = false;

    public BuddySelectionWizardPage() {
        super(BuddySelectionWizardPage.class.getName());
        setTitle(TITLE);
        setDescription(DESCRIPTION);

        SarosPluginContext.initComponent(this);
        discoveryManager.addDiscoveryManagerListener(discoveryManagerListener);
    }

    @Override
    public void dispose() {
        discoveryManager
            .removeDiscoveryManagerListener(discoveryManagerListener);
        super.dispose();
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);

        composite.setLayout(new GridLayout(2, false));

        /*
         * Row 1
         */
        Label buddySelectionLabel = new Label(composite, SWT.NONE);
        buddySelectionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP,
            false, true));
        buddySelectionLabel
            .setText(Messages.BuddySelectionWizardPage_label_buddies);

        createBuddySelectionComposite(composite);
        this.buddySelectionComposite.setLayoutData(new GridData(SWT.FILL,
            SWT.FILL, true, true));

        /*
         * Page completion
         */
        updatePageCompletion();
    }

    protected void createBuddySelectionComposite(Composite parent) {
        if (this.buddySelectionComposite != null
            && !this.buddySelectionComposite.isDisposed())
            this.buddySelectionComposite.dispose();

        boolean initialFilterConfig = true;

        String preferredFilterConfig = preferenceStore
            .getString(PreferenceConstants.BUDDYSELECTION_FILTERNONSAROSBUDDIES);
        if (preferredFilterConfig.equals("true")
            || preferredFilterConfig.equals("false")) {
            initialFilterConfig = preferenceStore
                .getBoolean(PreferenceConstants.BUDDYSELECTION_FILTERNONSAROSBUDDIES);
        }

        // if nothing is set yet, use YES as initial config...

        this.buddySelectionComposite = new BuddySelectionComposite(parent,
            SWT.BORDER | SWT.V_SCROLL, initialFilterConfig);
        this.buddySelectionComposite
            .setSelectedBuddies(SelectionRetrieverFactory
                .getSelectionRetriever(JID.class).getOverallSelection());
        this.buddySelectionComposite
            .addBuddySelectionListener(buddySelectionListener);

        /*
         * If no buddy is selected and exactly one with saros support is
         * available, use it.
         */
        if (this.buddySelectionComposite.getSelectedBuddies().size() == 0) {
            List<JID> buddies = this.buddySelectionComposite
                .getBuddiesWithSarosSupport();

            if (buddies.size() == 1) {
                this.buddySelectionComposite.setSelectedBuddies(buddies);
            }
        }
    }

    protected void updatePageCompletion() {
        if (buddySelectionComposite != null
            && !buddySelectionComposite.isDisposed()) {

            List<JID> selectedBuddies = this.buddySelectionComposite
                .getSelectedBuddies();
            List<JID> selectedBuddiesWithSarosSupport = this.buddySelectionComposite
                .getSelectedBuddiesWithSarosSupport();

            if (selectedBuddies == null
                || selectedBuddiesWithSarosSupport == null)
                return;

            /*
             * Condition: at least one buddy selected
             */
            if (selectedBuddies.size() == 0) {
                if (selectionWasValid)
                    setErrorMessage(NO_BUDDY_SELECTED_ERROR_MESSAGE);
                setPageComplete(false);
            } else if (!this.buddySelectionComposite.areAllSelectedOnline()) {
                setErrorMessage(OFFLINE_BUDDY_SELECTED_ERROR_MESSAGE);
                setPageComplete(false);
            } else {
                selectionWasValid = true;
                this.setErrorMessage(null);

                /*
                 * Warning if buddy without Saros support is selected
                 */
                if (selectedBuddies.size() > selectedBuddiesWithSarosSupport
                    .size()) {
                    setMessage(BUDDIES_WITHOUT_SAROS_SUPPORT_WARNING_MESSAGE,
                        IMessageProvider.WARNING);
                } else {
                    this.setMessage(DESCRIPTION);
                }

                setPageComplete(true);
            }
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible)
            return;

        this.buddySelectionComposite.setFocus();
    }

    /*
     * WizardPage Results
     */

    public List<JID> getSelectedBuddies() {
        if (this.buddySelectionComposite == null
            || this.buddySelectionComposite.isDisposed())
            return null;

        return this.buddySelectionComposite.getSelectedBuddies();
    }

    public List<JID> getSelectedBuddiesWithSarosSupport() {
        if (this.buddySelectionComposite == null
            || this.buddySelectionComposite.isDisposed())
            return null;

        return this.buddySelectionComposite
            .getSelectedBuddiesWithSarosSupport();
    }
}
