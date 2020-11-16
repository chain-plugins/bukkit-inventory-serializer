package com.github.chain.inventory.nms;

import java.lang.reflect.InvocationTargetException;

public interface InventoryNMS {

    boolean init(MinecraftVersion version);

    String encode(Object[] craftItemStacks) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException;

    Object[] decode(String encoded) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException;
}
