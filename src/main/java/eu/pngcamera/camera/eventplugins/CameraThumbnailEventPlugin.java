package eu.pngcamera.camera.eventplugins;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ICallable;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.camera.CameraRoomThumbnailSavedComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CameraThumbnailEventPlugin implements ICallable {


    @Override
    public void call(MessageHandler messageHandler) {
        messageHandler.isCancelled = true;
        if (! messageHandler.client.getHabbo().hasPermission("acc_camera")) {
            messageHandler.client.sendResponse(new GenericAlertComposer(Emulator.getTexts().getValue("camera.permission")));
            return;
        }

        if (!messageHandler.client.getHabbo().getHabboInfo().getCurrentRoom().isOwner(messageHandler.client.getHabbo()))
            return;

        Room room = messageHandler.client.getHabbo().getHabboInfo().getCurrentRoom();

        if (room == null)
            return;

        if (!room.isOwner(messageHandler.client.getHabbo()) && !messageHandler.client.getHabbo().hasPermission("acc_modtool_ticket_q"))
            return;

        final int count = messageHandler.packet.readInt();

        ByteBuf image = messageHandler.packet.getBuffer().readBytes(count);

        if(image == null)
            return;

        messageHandler.packet.readString();
        messageHandler.packet.readString();
        messageHandler.packet.readInt();
        messageHandler.packet.readInt();

        BufferedImage theImage = null;
        try {
            theImage = ImageIO.read(new ByteBufInputStream(image));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ImageIO.write(theImage, "png", new File(Emulator.getConfig().getValue("imager.location.output.thumbnail") + room.getId() + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        messageHandler.client.sendResponse(new CameraRoomThumbnailSavedComposer());
    }
}