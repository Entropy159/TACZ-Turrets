package com.entropy.tacz_turrets.client;

import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.turret.TurretEntity;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.AnimationSoundChannelContent;
import com.tacz.guns.api.client.animation.Animations;
import com.tacz.guns.api.client.animation.ObjectAnimation;
import com.tacz.guns.api.client.animation.ObjectAnimationSoundChannel;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.client.resource.pojo.animation.bedrock.BedrockAnimationFile;
import com.tacz.guns.config.common.GunConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.entropy.tacz_turrets.TACZTurrets.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID)
public class TurretReloadSound {
    private static final String EMPTY_ANIMATION = "reload_empty";
    private static final String TACTICAL_ANIMATION = "reload_tactical";
    private static final double TICK_SECONDS = 0.05D;

    private static final Map<Integer, Playback> PLAYING = new HashMap<>();
    private static final Map<ResourceLocation, Map<String, ObjectAnimationSoundChannel>> CACHE = new HashMap<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.isPaused()) return;
        if (!TACZTurretsConfig.enableSounds || !TACZTurretsConfig.reloadSound) {
            PLAYING.clear();
            return;
        }

        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof TurretEntity turret)) continue;
            ReloadState.StateType state = IGunOperator.fromLivingEntity(turret).getSynReloadState().getStateType();
            if (!state.isReloading()) {
                PLAYING.remove(turret.getId());
                continue;
            }
            if (PLAYING.containsKey(turret.getId())) continue;
            ObjectAnimationSoundChannel channel = findSoundChannel(turret, state);
            if (channel != null) {
                PLAYING.put(turret.getId(), new Playback(channel));
            } else {
                playDisplaySound(turret, state);
                PLAYING.put(turret.getId(), Playback.SILENT);
            }
        }

        PLAYING.entrySet().removeIf(entry -> {
            Entity entity = minecraft.level.getEntity(entry.getKey());
            if (!(entity instanceof TurretEntity turret) || !turret.isAlive()) return true;
            entry.getValue().advance(turret);
            return false;
        });
    }

    private static void clear() {
        PLAYING.clear();
        CACHE.clear();
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ReloadHandler {
        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) manager -> clear());
        }
    }

    private static void playDisplaySound(TurretEntity turret, ReloadState.StateType state) {
        ItemStack gunStack = turret.getGunStack();
        String name = state.isReloadingTactical() ? TACTICAL_ANIMATION : EMPTY_ANIMATION;
        ResourceLocation sound = TimelessAPI.getGunDisplay(gunStack).map(display -> display.getSounds(name)).orElse(null);
        if (sound == null) return;
        SoundPlayManager.playClientSound(turret, sound, 1.0F, 1.0F, GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get());
    }

    @Nullable
    private static ObjectAnimationSoundChannel findSoundChannel(TurretEntity turret, ReloadState.StateType state) {
        ItemStack gunStack = turret.getGunStack();
        IGun iGun = IGun.getIGunOrNull(gunStack);
        if (iGun == null) return null;

        Map<String, ObjectAnimationSoundChannel> channels = CACHE.computeIfAbsent(iGun.getGunId(gunStack), TurretReloadSound::loadChannels);
        ObjectAnimationSoundChannel channel = channels.get(state.isReloadingTactical() ? TACTICAL_ANIMATION : EMPTY_ANIMATION);
        if (channel != null) return channel;
        return channels.get(state.isReloadingTactical() ? EMPTY_ANIMATION : TACTICAL_ANIMATION);
    }

    private static Map<String, ObjectAnimationSoundChannel> loadChannels(ResourceLocation gunId) {
        Map<String, ObjectAnimationSoundChannel> channels = new HashMap<>();
        BedrockAnimationFile file = ClientAssetsManager.INSTANCE.getBedrockAnimations(gunId);
        if (file == null) return channels;

        List<ObjectAnimation> animations = Animations.createAnimationFromBedrock(file);
        for (ObjectAnimation animation : animations) {
            if (!EMPTY_ANIMATION.equals(animation.name) && !TACTICAL_ANIMATION.equals(animation.name)) continue;
            ObjectAnimationSoundChannel channel = animation.getSoundChannel();
            if (channel != null && hasPackSounds(channel)) channels.put(animation.name, channel);
        }
        return channels;
    }

    private static boolean hasPackSounds(ObjectAnimationSoundChannel channel) {
        AnimationSoundChannelContent content = channel.content;
        if (content == null || content.keyframeSoundName == null) return false;
        for (ResourceLocation sound : content.keyframeSoundName) {
            if (sound != null && !"minecraft".equals(sound.getNamespace())) return true;
        }
        return false;
    }

    private static class Playback {
        private static final Playback SILENT = new Playback(null);

        private final ObjectAnimationSoundChannel channel;
        private double time = 0.0D;

        private Playback(@Nullable ObjectAnimationSoundChannel channel) {
            this.channel = channel;
        }

        private void advance(TurretEntity turret) {
            if (channel == null || time > channel.getEndTimeS()) return;
            double next = time + TICK_SECONDS;
            channel.playSound(time, next, turret, GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(), 1.0F, 1.0F);
            time = next;
        }
    }
}
