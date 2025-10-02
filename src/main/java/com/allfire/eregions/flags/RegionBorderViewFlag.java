package com.allfire.eregions.flags;

import com.sk89q.worldguard.protection.flags.StateFlag;

/**
 * Флаг для отображения границ региона
 * Срабатывает когда игрок подходит к границе региона
 */
public class RegionBorderViewFlag extends StateFlag {
    
    public RegionBorderViewFlag(String name) {
        super(name, false);
    }
}

