package eu.pngcamera.camera.eventplugins;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ICallable;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.camera.CameraPurchaseSuccesfullComposer;
import com.eu.habbo.messages.outgoing.catalog.AlertPurchaseFailedComposer;
import com.eu.habbo.messages.outgoing.catalog.NotEnoughPointsTypeComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.HotelWillCloseInMinutesComposer;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import com.eu.habbo.threading.runnables.ShutdownEmulator;

public class CameraPurchaseEventPlugin implements ICallable {


    @Override
    public void call(MessageHandler messageHandler) {
        messageHandler.isCancelled = true;
        if (Emulator.getIntUnixTimestamp() - messageHandler.client.getHabbo().getHabboStats().lastPurchaseTimestamp >= CatalogManager.PURCHASE_COOLDOWN) {
            messageHandler.client.getHabbo().getHabboStats().lastPurchaseTimestamp = Emulator.getIntUnixTimestamp();
            if (ShutdownEmulator.timestamp > 0) {
                messageHandler.client.sendResponse(new HotelWillCloseInMinutesComposer((ShutdownEmulator.timestamp - Emulator.getIntUnixTimestamp()) / 60));
            } else if (messageHandler.client.getHabbo().getHabboInfo().getCredits() < Emulator.getConfig().getInt("camera.price.credits") || messageHandler.client.getHabbo().getHabboInfo().getCurrencyAmount(0) < Emulator.getConfig().getInt("camera.price.points")) {
                messageHandler.client.sendResponse(new NotEnoughPointsTypeComposer(messageHandler.client.getHabbo().getHabboInfo().getCredits() < Emulator.getConfig().getInt("camera.price.credits"), messageHandler.client.getHabbo().getHabboInfo().getCurrencyAmount(0) < Emulator.getConfig().getInt("camera.price.points"), 0));
            } else if (messageHandler.client.getHabbo().getHabboInfo().getPhotoTimestamp() != 0) {
                HabboItem photoItem = Emulator.getGameEnvironment().getItemManager().createItem(messageHandler.client.getHabbo().getHabboInfo().getId(), Emulator.getGameEnvironment().getItemManager().getItem(Emulator.getConfig().getInt("camera.item_id")), 0, 0, messageHandler.client.getHabbo().getHabboInfo().getPhotoJSON());

                if (photoItem != null) {
                    photoItem.setExtradata(photoItem.getExtradata().replace("%id%", photoItem.getId() + ""));
                    photoItem.needsUpdate(true);
                    messageHandler.client.getHabbo().getInventory().getItemsComponent().addItem(photoItem);

                    messageHandler.client.sendResponse(new CameraPurchaseSuccesfullComposer());
                    messageHandler.client.sendResponse(new AddHabboItemComposer(photoItem));
                    messageHandler.client.sendResponse(new InventoryRefreshComposer());

                    messageHandler.client.getHabbo().giveCredits(-Emulator.getConfig().getInt("camera.price.credits"));
                    messageHandler.client.getHabbo().givePixels(-Emulator.getConfig().getInt("camera.price.points"));

                    AchievementManager.progressAchievement(messageHandler.client.getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("CameraPhotoCount"));
                }
            }
        }
        else
        {
            messageHandler.client.sendResponse(new AlertPurchaseFailedComposer(AlertPurchaseFailedComposer.SERVER_ERROR).compose());
        }
    }
}