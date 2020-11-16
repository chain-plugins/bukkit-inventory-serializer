package com.github.chain.inventory;

import com.github.chain.inventory.nms.InvalidMinecraftVersionException;
import com.github.chain.inventory.nms.InventoryNMS;
import com.github.chain.inventory.nms.MinecraftVersion;
import com.github.chain.inventory.nms.v1_11_R1.v1_11_R1NMS;
import com.github.chain.inventory.nms.v1_14_R1.v1_14_R1NMS;
import com.github.chain.inventory.nms.v1_8_R3.v1_8_R3NMS;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

public class Serializer {

    private static InventoryNMS nmsBridge;
    private static MinecraftVersion version;

    public static String encode(@NonNull Inventory inventory) throws Exception {
        checkIfProviderIsSet();
        ItemStack[] inventoryContents = inventory.getContents();
        Class<?> nmsItemStackClazz = getItemStackClass();

        Object nmsItemArray = Array.newInstance(nmsItemStackClazz, inventoryContents.length);
        for (int i = 0; i < inventoryContents.length; i++) {
            Array.set(nmsItemArray, i, toNMSItem(inventoryContents[i]));
        }

        return nmsBridge.encode((Object[]) nmsItemArray);
    }

    public static void decode(@NonNull String encoded, @NonNull Inventory inventory) throws Exception {
        checkIfProviderIsSet();
        Object[] nmsItemStacks = nmsBridge.decode(encoded);

        ItemStack[] inventoryContents = new ItemStack[nmsItemStacks.length];
        for (int i = 0; i < nmsItemStacks.length; i++) {
            inventoryContents[i] = toBukkitItem(nmsItemStacks[i]);
        }

        inventory.setContents(inventoryContents);
    }

    private static ItemStack toBukkitItem(Object itemStack) throws Exception {
        Class<?> nmsItemStackClazz = getItemStackClass();
        Method asBukkitCopy = getCraftItemStackClass().getMethod("asBukkitCopy", nmsItemStackClazz);
        return (ItemStack) asBukkitCopy.invoke(null, itemStack);
    }

    private static Object toNMSItem(ItemStack itemStack) throws Exception {
        Method asNMSCopy = getCraftItemStackClass().getMethod("asNMSCopy", ItemStack.class);
        return asNMSCopy.invoke(null, itemStack);
    }

    private static Class<?> getCraftItemStackClass() throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + version.toString() + ".inventory.CraftItemStack");
    }

    private static Class<?> getItemStackClass() throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + version.toString() + ".ItemStack");
    }

    private static void checkIfProviderIsSet() throws InvalidMinecraftVersionException {
        if (nmsBridge == null) {
            String versionName = Bukkit.getServer().getClass().getPackage().getName();

            MinecraftVersion mcVersion;
            try {
                mcVersion = MinecraftVersion.valueOf(versionName.substring(versionName.lastIndexOf('.') + 1));
            } catch (IllegalArgumentException e) {
                throw new InvalidMinecraftVersionException("Version of Minecraft not supported.");
            }

            InventoryNMS bridge;
            switch (mcVersion) {
                case v1_8_R3:
                case v1_9_R1:
                case v1_9_R2:
                case v1_10_R1:
                    bridge = new v1_8_R3NMS();
                    break;
                case v1_11_R1:
                case v1_12_R1:
                case v1_13_R1:
                case v1_13_R2:
                    bridge = new v1_11_R1NMS();
                    break;
                case v1_14_R1:
                case v1_15_R1:
                case v1_16_R1:
                case v1_16_R2:
                case v1_16_R3:
                    bridge = new v1_14_R1NMS();
                    break;
                default:
                    throw new InvalidMinecraftVersionException("Version of Minecraft not supported.");
            }

            version = mcVersion;
            nmsBridge = bridge;

            if (!nmsBridge.init(mcVersion)) {
                throw new InvalidMinecraftVersionException("Version of Minecraft not supported.");
            }
        }
    }
}
