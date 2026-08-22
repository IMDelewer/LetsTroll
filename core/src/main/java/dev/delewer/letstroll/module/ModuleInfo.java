package dev.delewer.letstroll.module;

public record ModuleInfo(String id, String name, int order) {

    static ModuleInfo of(LetsTrollModule module) {
        TrollModule annotation = module.getClass().getAnnotation(TrollModule.class);
        if (annotation == null) {
            return new ModuleInfo(module.getClass().getSimpleName().toLowerCase(), module.getClass().getSimpleName(), 100);
        }
        String name = annotation.name().isBlank() ? annotation.id() : annotation.name();
        return new ModuleInfo(annotation.id(), name, annotation.order());
    }
}
