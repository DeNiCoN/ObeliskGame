package ua.denicon.obelisks.Classes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ua.denicon.obelisks.Utils.ObeliskUtil;

public class Default extends Class {
    public Default() {
        super("По умолчанию", 0, "default", ObeliskUtil.createItem(Material.BREAD, 1, (short) 0, "По умолчанию"));
    }
}
