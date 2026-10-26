package com.entropy.tacz_turrets.client.renderer;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.item.CamoItem;
import com.entropy.tacz_turrets.turret.TurretEntity;
import com.entropy.tacz_turrets.turret.TurretState;
import com.entropy.tacz_turrets.turret.TurretType;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
//? if forge {
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
//?} else {
/*import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
*///?}

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TurretPaintLayer extends GeoRenderLayer<TurretEntity> {
    private static final int FLAG_WIDTH = 20;
    private static final int FLAG_HEIGHT = 40;
    private static final Map<String, Optional<ResourceLocation>> TEXTURES = new HashMap<>();
    private static int nextId;

    public TurretPaintLayer(GeoRenderer<TurretEntity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, TurretEntity turret, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        ItemStack paint = turret.getPaint();
        if (paint.isEmpty()) return;
        TurretType type = turret.getTurretType();
        String key = paint.getItem() instanceof CamoItem camo ? camo.pattern().toString() : layers(paint).toString();
        Optional<ResourceLocation> texture = TEXTURES.computeIfAbsent(type.name() + key, k -> Optional.ofNullable(bake(type, bakedModel, paint)));
        if (texture.isEmpty()) return;
        RenderType paintType = RenderType.entityCutoutNoCull(texture.get());
        //? if forge {
        getRenderer().reRender(bakedModel, poseStack, bufferSource, turret, paintType, bufferSource.getBuffer(paintType), partialTick, packedLight, packedOverlay, 1, 1, 1, 1);
        //?} else {
        /*getRenderer().reRender(bakedModel, poseStack, bufferSource, turret, paintType, bufferSource.getBuffer(paintType), partialTick, packedLight, packedOverlay, 0xFFFFFFFF);
        *///?}
    }

    private static List<Layer> layers(ItemStack paint) {
        if (paint.getItem() instanceof DyeItem dye) return List.of(new Layer(null, rgb(dye.getDyeColor())));
        if (!(paint.getItem() instanceof BannerItem banner)) return List.of();
        List<Layer> layers = new ArrayList<>();
        layers.add(new Layer(null, rgb(banner.getColor())));
        //? if forge {
        List<Pair<Holder<BannerPattern>, DyeColor>> patterns = BannerBlockEntity.createPatterns(banner.getColor(), BannerBlockEntity.getItemPatterns(paint));
        for (Pair<Holder<BannerPattern>, DyeColor> pattern : patterns.subList(1, patterns.size())) {
            pattern.getFirst().unwrapKey().ifPresent(key -> layers.add(new Layer(BannerPattern.location(key, true), rgb(pattern.getSecond()))));
        }
        //?} else {
        /*for (BannerPatternLayers.Layer layer : paint.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY).layers()) {
            layers.add(new Layer(Sheets.getBannerMaterial(layer.pattern()).texture(), rgb(layer.color())));
        }
        *///?}
        return layers;
    }

    private static int rgb(DyeColor color) {
        //? if forge {
        float[] diffuse = color.getTextureDiffuseColors();
        return (int) (diffuse[0] * 255) << 16 | (int) (diffuse[1] * 255) << 8 | (int) (diffuse[2] * 255);
        //?} else {
        /*return color.getTextureDiffuseColor() & 0xFFFFFF;
        *///?}
    }

    @Nullable
    private static ResourceLocation bake(TurretType type, BakedGeoModel model, ItemStack paint) {
        try (NativeImage base = read(type.texture(TurretState.ACTIVE)); NativeImage mask = paint.getItem() instanceof CamoItem ? camoMask(type, base) : read(type.paintMask())) {
            Sampler sampler = sampler(paint);
            float brightest = 1;
            for (int y = 0; y < mask.getHeight(); y++) {
                for (int x = 0; x < mask.getWidth(); x++) {
                    if (painted(mask, x, y)) brightest = Math.max(brightest, luma(base.getPixelRGBA(x, y)));
                }
            }
            NativeImage out = new NativeImage(base.getWidth(), base.getHeight(), true);
            for (GeoBone bone : model.topLevelBones()) paint(bone, base, mask, out, sampler, brightest);
            ResourceLocation id = TACZTurrets.id("paint/" + nextId++);
            Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(out));
            return id;
        } catch (IOException e) {
            return null;
        }
    }

    private static NativeImage camoMask(TurretType type, NativeImage base) throws IOException {
        NativeImage mask = new NativeImage(base.getWidth(), base.getHeight(), true);
        try (NativeImage glow = read(type.texture(TurretState.ACTIVE).withPath(path -> path.replace(".png", "_glowmask.png")))) {
            for (int y = 0; y < base.getHeight(); y++) {
                for (int x = 0; x < base.getWidth(); x++) {
                    if ((base.getPixelRGBA(x, y) >>> 24) > 0 && !painted(glow, x, y)) mask.setPixelRGBA(x, y, 0xFFFFFFFF);
                }
            }
        }
        return mask;
    }

    private static Sampler sampler(ItemStack paint) throws IOException {
        if (paint.getItem() instanceof CamoItem camo) {
            try (NativeImage image = read(camo.pattern())) {
                int width = image.getWidth(), height = image.getHeight();
                int[] tile = new int[width * height];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int abgr = image.getPixelRGBA(x, y);
                        tile[y * width + x] = (abgr & 0xFF) << 16 | (abgr >> 8 & 0xFF) << 8 | (abgr >> 16 & 0xFF);
                    }
                }
                return (x, y, fx, fy) -> tile[y % height * width + x % width];
            }
        }
        int[] flag = flag(layers(paint));
        return (x, y, fx, fy) -> flag[(int) (fy * FLAG_HEIGHT) * FLAG_WIDTH + (int) (fx * FLAG_WIDTH)];
    }

    private static void paint(GeoBone bone, NativeImage base, NativeImage mask, NativeImage out, Sampler sampler, float brightest) {
        for (GeoCube cube : bone.getCubes()) {
            for (GeoQuad quad : cube.quads()) {
                float minU = 1, minV = 1, maxU = 0, maxV = 0;
                for (GeoVertex vertex : quad.vertices()) {
                    minU = Math.min(minU, vertex.texU());
                    minV = Math.min(minV, vertex.texV());
                    maxU = Math.max(maxU, vertex.texU());
                    maxV = Math.max(maxV, vertex.texV());
                }
                int x0 = Math.round(minU * out.getWidth()), x1 = Math.round(maxU * out.getWidth());
                int y0 = Math.round(minV * out.getHeight()), y1 = Math.round(maxV * out.getHeight());
                for (int y = Math.max(0, y0); y < Math.min(y1, out.getHeight()); y++) {
                    for (int x = Math.max(0, x0); x < Math.min(x1, out.getWidth()); x++) {
                        if (!painted(mask, x, y)) continue;
                        int rgb = sampler.rgb(x, y, (x - x0 + 0.5F) / (x1 - x0), (y - y0 + 0.5F) / (y1 - y0));
                        out.setPixelRGBA(x, y, shade(rgb, luma(base.getPixelRGBA(x, y)) / brightest));
                    }
                }
            }
        }
        for (GeoBone child : bone.getChildBones()) paint(child, base, mask, out, sampler, brightest);
    }

    private static int[] flag(List<Layer> layers) {
        int[] flag = new int[FLAG_WIDTH * FLAG_HEIGHT];
        for (Layer layer : layers) {
            if (layer.texture() == null) {
                Arrays.fill(flag, layer.rgb());
                continue;
            }
            try (NativeImage pattern = read(layer.texture().withPath(path -> "textures/" + path + ".png"))) {
                float scale = pattern.getWidth() / 64F;
                for (int y = 0; y < FLAG_HEIGHT; y++) {
                    for (int x = 0; x < FLAG_WIDTH; x++) {
                        int pixel = pattern.getPixelRGBA((int) ((x + 1.5F) * scale), (int) ((y + 1.5F) * scale));
                        flag[y * FLAG_WIDTH + x] = blend(flag[y * FLAG_WIDTH + x], layer.rgb(), (pixel & 0xFF) / 255F, (pixel >>> 24) / 255F);
                    }
                }
            } catch (IOException ignored) {
            }
        }
        return flag;
    }

    private static int blend(int under, int color, float gray, float alpha) {
        int rgb = 0;
        for (int shift = 0; shift <= 16; shift += 8) {
            float channel = (under >> shift & 0xFF) * (1 - alpha) + (color >> shift & 0xFF) * gray * alpha;
            rgb |= Math.min(255, Math.round(channel)) << shift;
        }
        return rgb;
    }

    private static int shade(int rgb, float light) {
        int red = Math.min(255, Math.round((rgb >> 16 & 0xFF) * light));
        int green = Math.min(255, Math.round((rgb >> 8 & 0xFF) * light));
        int blue = Math.min(255, Math.round((rgb & 0xFF) * light));
        return 0xFF000000 | blue << 16 | green << 8 | red;
    }

    private static float luma(int abgr) {
        return (abgr & 0xFF) * 0.299F + (abgr >> 8 & 0xFF) * 0.587F + (abgr >> 16 & 0xFF) * 0.114F;
    }

    private static boolean painted(NativeImage mask, int x, int y) {
        return x < mask.getWidth() && y < mask.getHeight() && (mask.getPixelRGBA(x, y) >>> 24) > 0;
    }

    private static NativeImage read(ResourceLocation location) throws IOException {
        try (InputStream stream = Minecraft.getInstance().getResourceManager().open(location)) {
            return NativeImage.read(stream);
        }
    }

    private record Layer(@Nullable ResourceLocation texture, int rgb) {
    }

    private interface Sampler {
        int rgb(int x, int y, float faceX, float faceY);
    }
}
