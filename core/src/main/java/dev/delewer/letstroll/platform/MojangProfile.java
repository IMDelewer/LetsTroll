package dev.delewer.letstroll.platform;

import java.util.List;
import java.util.Optional;

public final class MojangProfile {

    public static final class Property {

        public String name;
        public String value;
    }

    public String id;
    public String name;
    public List<Property> properties;

    public Optional<String> uuid() {
        return id == null || id.isBlank() ? Optional.empty() : Optional.of(id);
    }

    public Optional<String> texture() {
        if (properties == null) {
            return Optional.empty();
        }
        return properties.stream()
                .filter(property -> "textures".equals(property.name))
                .map(property -> property.value)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }
}
