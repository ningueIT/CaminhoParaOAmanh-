package entities;

import physics.AABB;

public interface Interactable {
    AABB getInteractionBounds();

    void onInteract(Player player);
}
