package com.dynast.civcraft.util;

import org.bukkit.Material;

public class MaterialUtils {
    public static Material getMaterialById(int id) {
        for (Material mat : Material.values()) {
            // Предположим, что у вас есть метод getId() в enum Material,
            // который возвращает ID для каждого материала.
            if (mat.getId() == id) {
                return mat;
            }
        }
        return null; // Возвращаем null, если материал с таким ID не найден
    }
}

