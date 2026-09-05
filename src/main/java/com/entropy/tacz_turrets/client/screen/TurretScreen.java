package com.entropy.tacz_turrets.client.screen;

import com.entropy.tacz_turrets.TACZTurrets;
import com.entropy.tacz_turrets.config.TACZTurretsConfig;
import com.entropy.tacz_turrets.menu.TurretLayout;
import com.entropy.tacz_turrets.menu.TurretMenu;
import com.entropy.tacz_turrets.network.TACZTurretsNetwork;
import com.entropy.tacz_turrets.network.ToggleAllyPacket;
import com.entropy.tacz_turrets.turret.HealthBarStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class TurretScreen extends AbstractContainerScreen<TurretMenu> {
    private static final ResourceLocation TEXTURE = TACZTurrets.id("textures/gui/turret.png");

    private static final int PANEL_SLICE = 4;
    private static final int PANEL_SIZE = 16;
    private static final int SLOT_U = 32;
    private static final int SLOT_V = 0;

    private static final int BAR_BACKGROUND = 0xFF101010;
    private static final int BAR_BORDER = 0xFF373737;
    private static final int ENERGY_COLOR = 0xE08010;
    private static final int ALLY_ROW_HEIGHT = 11;
    private static final int ALLY_LIST_WIDTH = 96;
    private static final int ALLY_MAX_ROWS = 8;
    private static final int ALLY_BUTTON_HEIGHT = 16;

    private final TurretLayout layout;
    private Button enableTypeButton;
    private Button modeButton;
    private Button playerTargetingButton;
    private Button allyButton;
    private boolean allyListPinned = false;

    public TurretScreen(TurretMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        layout = menu.getLayout();
        imageWidth = layout.width;
        imageHeight = layout.height;
        inventoryLabelY = layout.inventoryLabelY;
        titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = (layout.barWidth - 4) / 2;
        enableTypeButton = addRenderableWidget(Button.builder(enableTypeLabel(), button -> pressButton(TurretMenu.BUTTON_ENABLE_TYPE))
                .bounds(leftPos + TurretLayout.MARGIN, topPos + layout.buttonsY, buttonWidth, TurretLayout.BUTTON_HEIGHT)
                .build());
        modeButton = addRenderableWidget(Button.builder(modeLabel(), button -> pressButton(TurretMenu.BUTTON_MODE))
                .bounds(leftPos + TurretLayout.MARGIN + buttonWidth + 4, topPos + layout.buttonsY, buttonWidth, TurretLayout.BUTTON_HEIGHT)
                .build());
        playerTargetingButton = addRenderableWidget(Button.builder(playerTargetingLabel(), button -> pressButton(TurretMenu.BUTTON_PLAYER_TARGETING))
                .bounds(leftPos + TurretLayout.MARGIN, topPos + layout.secondButtonRowY, layout.barWidth, TurretLayout.BUTTON_HEIGHT)
                .build());
        allyButton = addRenderableWidget(Button.builder(Component.translatable("gui.tacz_turrets.allies"), button -> allyListPinned = !allyListPinned)
                .bounds(leftPos + allyButtonX(), topPos + layout.gunSlotY + 1, allyButtonWidth(), ALLY_BUTTON_HEIGHT)
                .build());

        enableTypeButton.active = menu.canModify();
        modeButton.active = menu.canModify();
        playerTargetingButton.active = menu.canModify();
        allyButton.active = menu.canModify() && !menu.getOwnerName().isEmpty();
    }

    private void pressButton(int id) {
        if (minecraft == null || minecraft.gameMode == null) return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    private Component enableTypeLabel() {
        return Component.translatable("gui.tacz_turrets.enable_type." + menu.getEnableType().name().toLowerCase());
    }

    private Component playerTargetingLabel() {
        return Component.translatable("gui.tacz_turrets.player_targeting." + menu.getPlayerTargeting().name().toLowerCase());
    }

    private Component modeLabel() {
        return Component.translatable("gui.tacz_turrets.mode." + menu.getMode().name().toLowerCase());
    }

    private int healthColor(float fraction) {
        if (TACZTurretsConfig.healthBarStyle == HealthBarStyle.COLOR) return TACZTurretsConfig.healthBarColor;
        float clamped = Mth.clamp(fraction, 0.0F, 1.0F);
        if (clamped >= 0.5F) {
            float upper = (clamped - 0.5F) * 2.0F;
            return rgb(Mth.lerp(upper, 255.0F, 70.0F), Mth.lerp(upper, 150.0F, 200.0F), Mth.lerp(upper, 30.0F, 60.0F));
        }
        float lower = clamped * 2.0F;
        return rgb(Mth.lerp(lower, 200.0F, 255.0F), Mth.lerp(lower, 30.0F, 150.0F), 30.0F);
    }

    private static int rgb(float red, float green, float blue) {
        return ((int) red << 16) | ((int) green << 8) | (int) blue;
    }

    private void drawBar(GuiGraphics graphics, int x, int y, int width, int height, float fraction, int color) {
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, BAR_BORDER);
        graphics.fill(x, y, x + width, y + height, BAR_BACKGROUND);
        int filled = Mth.clamp(Math.round(width * fraction), 0, width);
        if (filled > 0) graphics.fill(x, y, x + filled, y + height, 0xFF000000 | color);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blitNineSliced(TEXTURE, leftPos, topPos, imageWidth, imageHeight, PANEL_SLICE, PANEL_SLICE, PANEL_SIZE, PANEL_SIZE, 0, 0);

        for (Slot slot : menu.slots) {
            graphics.blit(TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1, SLOT_U, SLOT_V, TurretLayout.SLOT, TurretLayout.SLOT);
        }

        float healthFraction = (float) menu.getHealth() / menu.getMaxHealth();
        drawBar(graphics, leftPos + TurretLayout.MARGIN, topPos + layout.healthBarY, layout.barWidth, TurretLayout.BAR_HEIGHT, healthFraction, healthColor(healthFraction));

        if (menu.usesEnergy()) {
            float energyFraction = (float) menu.getEnergy() / menu.getMaxEnergy();
            drawBar(graphics, leftPos + TurretLayout.MARGIN, topPos + layout.energyBarY, layout.barWidth, TurretLayout.BAR_HEIGHT, energyFraction, ENERGY_COLOR);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        Component health = Component.literal(menu.getHealth() + " / " + menu.getMaxHealth());
        graphics.drawString(font, health, imageWidth - TurretLayout.MARGIN - font.width(health), titleLabelY, 0x404040, false);
        if (!menu.getOwnerName().isEmpty()) {
            graphics.drawString(font, trimmedOwnerLabel(), TurretLayout.MARGIN, layout.gunSlotY + 5, 0x404040, false);
        }
    }

    private Component ownerLabel() {
        return Component.translatable("gui.tacz_turrets.owner", menu.getOwnerName());
    }

    private int ownerMaxWidth() {
        return layout.gunSlotX - TurretLayout.MARGIN - 3;
    }

    private boolean ownerNameTrimmed() {
        return font.width(ownerLabel()) > ownerMaxWidth();
    }

    private String trimmedOwnerLabel() {
        String text = ownerLabel().getString();
        if (font.width(text) <= ownerMaxWidth()) return text;
        return font.plainSubstrByWidth(text, ownerMaxWidth() - font.width("...")) + "...";
    }

    private List<PlayerInfo> onlinePlayers() {
        if (minecraft == null || minecraft.getConnection() == null) return List.of();
        return minecraft.getConnection().getOnlinePlayers().stream()
                .filter(info -> minecraft.player == null || !info.getProfile().getId().equals(minecraft.player.getUUID()))
                .filter(info -> !info.getProfile().getName().equals(menu.getOwnerName()))
                .sorted(Comparator.comparing(info -> info.getProfile().getName()))
                .limit(ALLY_MAX_ROWS)
                .toList();
    }

    private int allyButtonX() {
        return layout.gunSlotX + TurretLayout.SLOT + 6;
    }

    private int allyButtonWidth() {
        return layout.width - TurretLayout.MARGIN - allyButtonX();
    }

    private int allyListWidth() {
        int width = Math.max(allyButtonWidth(), ALLY_LIST_WIDTH);
        width = Math.max(width, font.width(Component.translatable("gui.tacz_turrets.allies.empty")) + 8);
        int allyTag = font.width(Component.translatable("gui.tacz_turrets.allies.ally"));
        for (PlayerInfo info : onlinePlayers()) {
            width = Math.max(width, 14 + font.width(info.getProfile().getName()) + 8 + allyTag + 4);
        }
        return width;
    }

    private int allyListY() {
        return layout.gunSlotY + 1 + ALLY_BUTTON_HEIGHT;
    }

    private int allyListRows() {
        return Math.max(1, onlinePlayers().size());
    }

    private boolean allyListOpen(int mouseX, int mouseY) {
        if (!allyButton.active) return false;
        if (allyListPinned) return true;
        int height = ALLY_BUTTON_HEIGHT + 1 + allyListRows() * ALLY_ROW_HEIGHT;
        return isHovering(allyButtonX(), layout.gunSlotY + 1, allyListWidth(), height, mouseX, mouseY);
    }

    private void renderAllyList(GuiGraphics graphics, int mouseX, int mouseY) {
        List<PlayerInfo> players = onlinePlayers();
        int x = leftPos + allyButtonX();
        int y = topPos + allyListY();
        int width = allyListWidth();
        int rows = allyListRows();

        graphics.fill(x - 1, y - 1, x + width + 1, y + rows * ALLY_ROW_HEIGHT + 1, 0xFF373737);
        graphics.fill(x, y, x + width, y + rows * ALLY_ROW_HEIGHT, 0xFF101010);

        if (players.isEmpty()) {
            graphics.drawString(font, Component.translatable("gui.tacz_turrets.allies.empty"), x + 3, y + 2, 0xA0A0A0, false);
            return;
        }

        for (int index = 0; index < players.size(); index++) {
            PlayerInfo info = players.get(index);
            int rowY = y + index * ALLY_ROW_HEIGHT;
            boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= rowY && mouseY < rowY + ALLY_ROW_HEIGHT;
            if (hovered) graphics.fill(x, rowY, x + width, rowY + ALLY_ROW_HEIGHT, 0xFF303030);

            PlayerFaceRenderer.draw(graphics, info.getSkinLocation(), x + 2, rowY + 1, 8);
            boolean ally = menu.isAlly(info.getProfile().getId());
            graphics.drawString(font, info.getProfile().getName(), x + 13, rowY + 2, ally ? 0x40D040 : 0xC0C0C0, false);
            if (ally) {
                Component tag = Component.translatable("gui.tacz_turrets.allies.ally");
                graphics.drawString(font, tag, x + width - 3 - font.width(tag), rowY + 2, 0x40D040, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (allyListOpen((int) mouseX, (int) mouseY)) {
            List<PlayerInfo> players = onlinePlayers();
            int x = leftPos + allyButtonX();
            int y = topPos + allyListY();
            int width = allyListWidth();
            for (int index = 0; index < players.size(); index++) {
                int rowY = y + index * ALLY_ROW_HEIGHT;
                if (mouseX >= x && mouseX < x + width && mouseY >= rowY && mouseY < rowY + ALLY_ROW_HEIGHT) {
                    UUID target = players.get(index).getProfile().getId();
                    menu.toggleAllyLocally(target);
                    TACZTurretsNetwork.CHANNEL.sendToServer(new ToggleAllyPacket(target));
                    if (minecraft != null) minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        enableTypeButton.setMessage(enableTypeLabel());
        enableTypeButton.setTooltip(Tooltip.create(Component.translatable("gui.tacz_turrets.enable_type." + menu.getEnableType().name().toLowerCase() + ".tooltip")));
        modeButton.setMessage(modeLabel());
        modeButton.setTooltip(Tooltip.create(Component.translatable("gui.tacz_turrets.mode." + menu.getMode().name().toLowerCase() + ".tooltip")));
        playerTargetingButton.setMessage(playerTargetingLabel());
        playerTargetingButton.setTooltip(Tooltip.create(Component.translatable("gui.tacz_turrets.player_targeting." + menu.getPlayerTargeting().name().toLowerCase() + ".tooltip")));
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (allyListOpen(mouseX, mouseY)) renderAllyList(graphics, mouseX, mouseY);

        if (ownerNameTrimmed() && isHovering(TurretLayout.MARGIN, layout.gunSlotY + 5, ownerMaxWidth(), 9, mouseX, mouseY)) {
            graphics.renderTooltip(font, ownerLabel(), mouseX, mouseY);
        }

        if (isHovering(TurretLayout.MARGIN, layout.healthBarY, layout.barWidth, TurretLayout.BAR_HEIGHT, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("gui.tacz_turrets.health", menu.getHealth(), menu.getMaxHealth()), mouseX, mouseY);
        } else if (menu.usesEnergy() && isHovering(TurretLayout.MARGIN, layout.energyBarY, layout.barWidth, TurretLayout.BAR_HEIGHT, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("gui.tacz_turrets.energy", menu.getEnergy(), menu.getMaxEnergy()), mouseX, mouseY);
        }
    }
}
