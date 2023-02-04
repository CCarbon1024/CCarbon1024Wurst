package net.wurstclient.mixin;

import net.minecraftforge.fml.network.FMLHandshakeMessages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(FMLHandshakeMessages.C2SModListReply.class)
public class FMLHandshakeMessagesMixin {

    @Shadow(remap = false)
    private List<String> mods;

    @Inject(at = {@At(value = "RETURN")},
            remap = false,
            method = {
                    "<init>()V"})
    private void onInit(CallbackInfo ci) {
        blockMessage();
    }

    @Inject(at = {@At(value = "RETURN")},
            remap = false,
            method = {
                    "<init>(Ljava/util/List;Ljava/util/Map;Ljava/util/Map;)V"})
    private void onInit(List mods, Map channels, Map registries, CallbackInfo ci) {
        blockMessage();
    }

    private void blockMessage() {

        for (int i = 0; i < mods.size(); i++) {
            if (mods.get(i).equals("wurst")) {
                mods.remove(i);
            }
        }
        this.mods.add("\u00A74Don't too much matter this.");

        for (int j = 0; j < 4; j++) {
            String aLotEnter = "";
            for (int i = 0; i < 255; i++) {
                aLotEnter += "\n";
            }
            this.mods.add(aLotEnter);
        }
    }
}
