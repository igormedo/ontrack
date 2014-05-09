package net.nemerosa.ontrack.model;

import lombok.Data;

@Data
public class PromotionRun {

    private final String description;
    private final Signature signature;
    private final PromotionLevel promotionLevel;

}
