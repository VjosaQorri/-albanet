package com.example.albanet.ticket.internal.enums;

/**
 * Predefined ticket problem types with automatic team and priority assignment
 */
public enum TicketProblemType {
    // TV Problems - Finance Team
    TV_INVOICE_COPY("Copy of Invoice", TicketCategory.TV, "FINANCE", TicketPriority.LOW),
    TV_DOUBLE_CHARGE("I've been charged twice", TicketCategory.TV, "FINANCE", TicketPriority.MEDIUM),
    TV_PAYMENT_NO_ACTIVATION("Charged but subscription not activated", TicketCategory.TV, "FINANCE", TicketPriority.HIGH),

    // TV Problems - IT1 Team
    TV_NO_SIGNAL("No signal/channels not working", TicketCategory.TV, "IT1", TicketPriority.HIGH),
    TV_POOR_QUALITY("Poor picture quality/buffering", TicketCategory.TV, "IT1", TicketPriority.MEDIUM),
    TV_CHANNEL_MISSING("Missing channels from my package", TicketCategory.TV, "IT1", TicketPriority.LOW),

    // TV Problems - IT2 Team
    TV_EQUIPMENT_ISSUE("TV box/equipment malfunction", TicketCategory.TV, "IT2", TicketPriority.HIGH),
    TV_INSTALLATION_HELP("Need help with installation", TicketCategory.TV, "IT2", TicketPriority.MEDIUM),
    TV_UPGRADE_PACKAGE("Want to upgrade my package", TicketCategory.TV, "IT2", TicketPriority.LOW),

    // Mobile Problems - Finance Team
    MOBILE_INVOICE_COPY("Copy of Invoice", TicketCategory.MOBILE, "FINANCE", TicketPriority.LOW),
    MOBILE_DOUBLE_CHARGE("I've been charged twice", TicketCategory.MOBILE, "FINANCE", TicketPriority.MEDIUM),
    MOBILE_PAYMENT_NO_ACTIVATION("Charged but package not activated", TicketCategory.MOBILE, "FINANCE", TicketPriority.HIGH),

    // Mobile Problems - IT1 Team
    MOBILE_NO_SERVICE("No mobile service/cannot make calls", TicketCategory.MOBILE, "IT1", TicketPriority.HIGH),
    MOBILE_SLOW_DATA("Slow data connection/internet not working", TicketCategory.MOBILE, "IT1", TicketPriority.MEDIUM),
    MOBILE_SMS_ISSUE("Cannot send/receive SMS", TicketCategory.MOBILE, "IT1", TicketPriority.LOW),

    // Mobile Problems - IT2 Team
    MOBILE_SIM_ISSUE("SIM card not working/need replacement", TicketCategory.MOBILE, "IT2", TicketPriority.HIGH),
    MOBILE_NETWORK_SETTINGS("Need help with network settings", TicketCategory.MOBILE, "IT2", TicketPriority.MEDIUM),
    MOBILE_UPGRADE_PACKAGE("Want to upgrade my package", TicketCategory.MOBILE, "IT2", TicketPriority.LOW),

    // Internet Problems - Finance Team
    INTERNET_INVOICE_COPY("Copy of Invoice", TicketCategory.INTERNET, "FINANCE", TicketPriority.LOW),
    INTERNET_DOUBLE_CHARGE("I've been charged twice", TicketCategory.INTERNET, "FINANCE", TicketPriority.MEDIUM),
    INTERNET_PAYMENT_NO_ACTIVATION("Charged but internet not activated", TicketCategory.INTERNET, "FINANCE", TicketPriority.HIGH),

    // Internet Problems - IT1 Team
    INTERNET_NO_CONNECTION("No internet connection", TicketCategory.INTERNET, "IT1", TicketPriority.HIGH),
    INTERNET_SLOW_SPEED("Internet is very slow", TicketCategory.INTERNET, "IT1", TicketPriority.MEDIUM),
    INTERNET_INTERMITTENT("Internet keeps disconnecting", TicketCategory.INTERNET, "IT1", TicketPriority.MEDIUM),

    // Internet Problems - IT2 Team
    INTERNET_ROUTER_ISSUE("Router not working/lights blinking", TicketCategory.INTERNET, "IT2", TicketPriority.HIGH),
    INTERNET_INSTALLATION_HELP("Need help with router setup", TicketCategory.INTERNET, "IT2", TicketPriority.MEDIUM),
    INTERNET_UPGRADE_PACKAGE("Want to upgrade my package", TicketCategory.INTERNET, "IT2", TicketPriority.LOW);

    private final String displayName;
    private final TicketCategory category;
    private final String assignedTeam;
    private final TicketPriority priority;

    TicketProblemType(String displayName, TicketCategory category, String assignedTeam, TicketPriority priority) {
        this.displayName = displayName;
        this.category = category;
        this.assignedTeam = assignedTeam;
        this.priority = priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public String getAssignedTeam() {
        return assignedTeam;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    /**
     * Get all problem types for a specific category
     */
    public static TicketProblemType[] getByCategory(TicketCategory category) {
        return java.util.Arrays.stream(values())
                .filter(type -> type.getCategory() == category)
                .toArray(TicketProblemType[]::new);
    }
}
