package com.example.albanet.subscription.internal;

import com.example.albanet.subscription.internal.enums.CatalogCode;

import java.util.HashMap;
import java.util.Map;

public class PlanHierarchy {

    private static final Map<CatalogCode, Integer> TV_HIERARCHY = new HashMap<>();
    private static final Map<CatalogCode, Integer> INTERNET_HIERARCHY = new HashMap<>();

    static {
        // TV Plans: Basic < Standard < Premium
        TV_HIERARCHY.put(CatalogCode.TV_BASIC, 1);
        TV_HIERARCHY.put(CatalogCode.TV_STANDARD, 2);
        TV_HIERARCHY.put(CatalogCode.TV_PREMIUM, 3);

        // Internet Plans: Basic < Standard < Premium
        INTERNET_HIERARCHY.put(CatalogCode.WIFI_BASIC, 1);
        INTERNET_HIERARCHY.put(CatalogCode.WIFI_STANDARD, 2);
        INTERNET_HIERARCHY.put(CatalogCode.WIFI_PREMIUM, 3);
    }

    /**
     * Compare two plans. Returns:
     * 0 if same tier
     * > 0 if newPlan is higher (upgrade)
     * < 0 if newPlan is lower (downgrade)
     */
    public static int comparePlans(CatalogCode currentPlan, CatalogCode newPlan) {
        Map<CatalogCode, Integer> hierarchy = getHierarchy(currentPlan);
        if (hierarchy == null) {
            return 0; // Not hierarchical (e.g., mobile plans)
        }

        Integer currentTier = hierarchy.get(currentPlan);
        Integer newTier = hierarchy.get(newPlan);

        if (currentTier == null || newTier == null) {
            return 0;
        }

        return newTier - currentTier;
    }

    /**
     * Check if a plan is hierarchical (TV/Internet)
     */
    public static boolean isHierarchical(CatalogCode plan) {
        return TV_HIERARCHY.containsKey(plan) || INTERNET_HIERARCHY.containsKey(plan);
    }

    private static Map<CatalogCode, Integer> getHierarchy(CatalogCode plan) {
        if (TV_HIERARCHY.containsKey(plan)) {
            return TV_HIERARCHY;
        } else if (INTERNET_HIERARCHY.containsKey(plan)) {
            return INTERNET_HIERARCHY;
        }
        return null;
    }
}
