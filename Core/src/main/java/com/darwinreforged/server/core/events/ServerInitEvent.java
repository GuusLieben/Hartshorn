package com.darwinreforged.server.core.events;

import com.darwinreforged.server.core.entities.Target;
import com.darwinreforged.server.core.events.test.Event;

public class ServerInitEvent extends Event {
    public ServerInitEvent(Target target) {
        super(target);
    }
}
