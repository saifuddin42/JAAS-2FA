import java.security.Principal;
import java.io.Serializable;

public class MyPrincipal implements Principal, Serializable {
    private String name;

    public MyPrincipal(String name) {
        if (name == null) {
            throw new NullPointerException("null name is illegal");
        }
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MyPrincipal) {
            return name.equals(((MyPrincipal) other).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}