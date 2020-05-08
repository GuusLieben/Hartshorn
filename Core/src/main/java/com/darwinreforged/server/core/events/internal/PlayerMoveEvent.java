package com.darwinreforged.server.core.events.internal;

import com.darwinreforged.server.core.entities.living.Target;
import com.darwinreforged.server.core.events.CancellableEvent;

public class PlayerMoveEvent extends CancellableEvent {

    public PlayerMoveEvent(Target target) {
        super(target);
    }
}
