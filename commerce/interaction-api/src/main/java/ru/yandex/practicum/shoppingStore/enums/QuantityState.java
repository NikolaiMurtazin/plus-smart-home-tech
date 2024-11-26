package ru.yandex.practicum.shoppingStore.enums;

public enum QuantityState {
    ENDED,
    FEW,
    ENOUGH,
    MANY;

    public static QuantityState determineState(int quantity) {
        if (quantity == 0) {
            return QuantityState.ENDED;
        } else if (quantity > 0 && quantity < 5) {
            return QuantityState.FEW;
        } else if (quantity >= 5 && quantity <= 20) {
            return QuantityState.ENOUGH;
        } else {
            return QuantityState.MANY;
        }
    }
}
