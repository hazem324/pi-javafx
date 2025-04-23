package enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum CategoryGrp {
    History("History"),
    Art("Art"),
    Music("Music"),
    Cinema("Cinema"),
    Theater("Theater"),
    Architecture("Architecture"),
    Dance("Dance");

    private final String value;

    CategoryGrp(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static CategoryGrp fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        for (CategoryGrp category : CategoryGrp.values()) {
            if (category.value.equalsIgnoreCase(value)) {
                return category;
            }
        }

        throw new IllegalArgumentException("Unknown category group: " + value);
    }

    public static List<String> getAllValues() {
        return Arrays.stream(CategoryGrp.values())
                .map(CategoryGrp::getValue)
                .collect(Collectors.toList());
    }
}
