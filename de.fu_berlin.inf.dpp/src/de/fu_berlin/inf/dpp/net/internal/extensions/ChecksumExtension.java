/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.concurrent.management.DocumentChecksum;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions.SessionDefaultPacketExtension;

public abstract class ChecksumExtension extends SessionDefaultPacketExtension {

    @Override
    public PacketFilter getFilter() {
        return new AndFilter(super.getFilter(), PacketExtensions
            .getInSessionFilter());
    }

    public ChecksumExtension() {
        super("DocChecksum");
    }

    public PacketExtension create(Collection<DocumentChecksum> checksums) {
        DefaultPacketExtension extension = create();

        // TODO create string constants for the used keys
        extension.setValue("quantity", Integer.toString(checksums.size()));

        int i = 1;
        for (DocumentChecksum checksum : checksums) {
            extension.setValue("path" + Integer.toString(i), checksum.getPath()
                .toPortableString());
            extension.setValue("length" + Integer.toString(i), Integer
                .toString(checksum.getLength()));
            extension.setValue("hash" + Integer.toString(i), Integer
                .toString(checksum.getHash()));
            i++;
        }

        return extension;

    }

    public static ChecksumExtension getDefault() {
        return PacketExtensions.getContainer().getComponent(
            ChecksumExtension.class);
    }

    @Override
    public void processMessage(JID sender, Message message) {

        final DefaultPacketExtension ext = getExtension(message);

        int count = Integer.parseInt(ext.getValue("quantity"));
        DocumentChecksum[] checksums = new DocumentChecksum[count];

        for (int i = 1; i <= count; i++) {
            IPath path = Path.fromPortableString(ext.getValue("path" + i));
            int length = Integer.parseInt(ext.getValue("length" + i));
            int hash = Integer.parseInt(ext.getValue("hash" + i));
            checksums[i - 1] = new DocumentChecksum(path, length, hash);
        }

        checksumsReceived(sender, Arrays.asList(checksums));
    }

    public abstract void checksumsReceived(JID sender,
        List<DocumentChecksum> checksums);
}