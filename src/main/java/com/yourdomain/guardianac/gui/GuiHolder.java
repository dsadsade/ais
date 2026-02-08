package com.yourdomain.guardianac.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Custom InventoryHolder to identify GuardianAC GUI inventories.
 * Each GUI type has a unique GuiType and optional metadata.
 */
public class GuiHolder implements InventoryHolder {

    private final GuiType guiType;
    private final String metadata;

    public GuiHolder(GuiType guiType) {
        this(guiType, null);
    }

    public GuiHolder(GuiType guiType, String metadata) {
        this.guiType = guiType;
        this.metadata = metadata;
    }

    public GuiType getGuiType() {
        return guiType;
    }

    /** Extra data, e.g. check category name or check violation key. */
    public String getMetadata() {
        return metadata;
    }

    @Override
    public @NotNull Inventory getInventory() {
        // Not used — Bukkit requires this but we create inventories separately
        throw new UnsupportedOperationException();
    }
}
