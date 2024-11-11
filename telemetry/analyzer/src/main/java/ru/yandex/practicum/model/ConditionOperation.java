package ru.yandex.practicum.model;


public enum ConditionOperation {

    MORE {
        @Override
        public boolean apply(Integer left, Integer right) {
            if (left != null && right != null) {
                return left > right;
            }
            return false;
        }
    },
    LESS {
        @Override
        public boolean apply(Integer left, Integer right) {
            if (left != null && right != null) {
                return left < right;
            }
            return false;
        }
    },
    EQUAL {
        @Override
        public boolean apply(Integer left, Integer right) {
            if (left != null && right != null) {
                return left.compareTo(right) == 0;
            }
            return false;
        }
    };

    public abstract boolean apply(Integer left, Integer right);
}