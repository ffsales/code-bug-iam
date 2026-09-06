package br.com.sales.code.bug.iam.domain;

import java.util.Optional;

public record Permission(
        String value
) {
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || this.getClass() != other.getClass())
            return false;

        Permission otherPermission = (Permission)other;
        return otherPermission.value.equals(this.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
}
