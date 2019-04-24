package eu.pngcamera.camera.eventplugins;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ICallable;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.camera.CameraURLComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class CameraRoomPictureEventPlugin implements ICallable {
    @Override
    public void call(MessageHandler messageHandler) {
        messageHandler.isCancelled = true;
        if (Emulator.getIntUnixTimestamp() - messageHandler.client.getHabbo().getHabboStats().lastPurchaseTimestamp >= CatalogManager.PURCHASE_COOLDOWN) {
            messageHandler.client.getHabbo().getHabboStats().lastPurchaseTimestamp = Emulator.getIntUnixTimestamp();
            if (!messageHandler.client.getHabbo().hasPermission("acc_camera")) {
                messageHandler.client.sendResponse(new GenericAlertComposer(Emulator.getTexts().getValue("camera.permission")));
                return;
            }

            Room room = messageHandler.client.getHabbo().getHabboInfo().getCurrentRoom();

            if (room == null)
                return;

            final int count = messageHandler.packet.readInt();

            ByteBuf image = messageHandler.packet.getBuffer().readBytes(count);

            if (image == null)
                return;
            messageHandler.packet.readString();
            messageHandler.packet.readString();
            messageHandler.packet.readInt();
            messageHandler.packet.readInt();
            int timestamp = Emulator.getIntUnixTimestamp();

            String URL = messageHandler.client.getHabbo().getHabboInfo().getId() + "_" + timestamp + ".png";
            String URL_small = messageHandler.client.getHabbo().getHabboInfo().getId() + "_" + timestamp + "_small.png";
            String base = Emulator.getConfig().getValue("camera.url");
            String json = Emulator.getConfig().getValue("camera.extradata").replace("%timestamp%", timestamp + "").replace("%room_id%", room.getId() + "").replace("%url%", base + URL);
            messageHandler.client.getHabbo().getHabboInfo().setPhotoURL(base + URL);
            messageHandler.client.getHabbo().getHabboInfo().setPhotoTimestamp(timestamp);
            messageHandler.client.getHabbo().getHabboInfo().setPhotoRoomId(room.getId());
            messageHandler.client.getHabbo().getHabboInfo().setPhotoJSON(json);
            BufferedImage theImage = null;
            try {
                theImage = ImageIO.read(new ByteBufInputStream(image));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                ImageIO.write(theImage, "png", new File(Emulator.getConfig().getValue("imager.location.output.camera") + URL));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                ImageIO.write(theImage, "png", new File(Emulator.getConfig().getValue("imager.location.output.camera") + URL_small));
            } catch (IOException e) {
                e.printStackTrace();
            }
            messageHandler.client.sendResponse(new CameraURLComposer(URL));
        }
    }
}