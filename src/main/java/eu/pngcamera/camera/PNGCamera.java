package eu.pngcamera.camera;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ICallable;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.HabboPlugin;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadedEvent;
import eu.pngcamera.camera.eventplugins.*;


public class PNGCamera extends HabboPlugin implements EventListener {

    public static PNGCamera INSTANCE = null;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Emulator.getPluginManager().registerEvents(this, this);

        if (Emulator.isReady) {
            ICallable callable4 = new CameraRoomPictureEventPlugin();
            Emulator.getGameServer().getPacketManager().registerCallable(Integer.valueOf(3226), callable4);
            ICallable callable5 = new CameraPurchaseEventPlugin();
            Emulator.getGameServer().getPacketManager().registerCallable(Integer.valueOf(2408), callable5);
            ICallable callable6 = new CameraThumbnailEventPlugin();
            Emulator.getGameServer().getPacketManager().registerCallable(Integer.valueOf(1982), callable6);
            ICallable callable7 = new CameraPublishToWebEventPlugin();
            Emulator.getGameServer().getPacketManager().registerCallable(Integer.valueOf(2068), callable7);
        }
        Emulator.getLogging().logStart("[PNGCamera] Started PNGCamera Plugin!");
    }
    @EventHandler


    public static void systemLoaded(EmulatorLoadedEvent event) throws Exception
    {
        Emulator.getConfig().register("camera.url", "http://yourdomain.com/swfdirectory/camera/");
        Emulator.getConfig().register("imager.location.output.camera", "C:\\yourdirectory\\swfdirectory\\camera\\");
        Emulator.getConfig().register("imager.location.output.thumbnail", "C:\\yourdirectory\\swfdirectory\\camera\\thumbnail_");
        ICallable callable4 = new CameraRoomPictureEventPlugin();
        Emulator.getGameServer().getPacketManager().registerCallable(Integer.valueOf(3226), callable4);
        ICallable callable5 = new CameraPurchaseEventPlugin();
        Emulator.getGameServer().getPacketManager().registerCallable(Integer.valueOf(2408), callable5);
        ICallable callable6 = new CameraThumbnailEventPlugin();
        Emulator.getGameServer().getPacketManager().registerCallable(Integer.valueOf(1982), callable6);
        ICallable callable7 = new CameraPublishToWebEventPlugin();
        Emulator.getGameServer().getPacketManager().registerCallable(Integer.valueOf(2068), callable7);
    }
    @Override
    public void onDisable() {
        Emulator.getGameServer().getPacketManager().unregisterCallables(Integer.valueOf(3226));
        Emulator.getGameServer().getPacketManager().unregisterCallables(Integer.valueOf(2408));
        Emulator.getGameServer().getPacketManager().unregisterCallables(Integer.valueOf(1982));
        Emulator.getGameServer().getPacketManager().unregisterCallables(Integer.valueOf(2068));
        Emulator.getLogging().logShutdownLine("[PNGCamera] Stopped PNGCamera Plugin!");
    }


    @Override
    public boolean hasPermission(Habbo habbo, String s) {
        return false;
    }


}