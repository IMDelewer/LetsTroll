package dev.delewer.letstroll.modules.hub;

import dev.delewer.letstroll.module.LetsTrollModule;
import dev.delewer.letstroll.module.ModuleContext;
import dev.delewer.letstroll.module.TrollModule;

@TrollModule(id = "hub", name = "Hub", order = 0)
public final class HubModule implements LetsTrollModule {

    @Override
    public void enable(ModuleContext context) {
        context.screen(new HubScreen());
        context.command(new TrollCommand(context.core()));
    }
}
