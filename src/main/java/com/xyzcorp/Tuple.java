package com.xyzcorp;

import java.util.Objects;

public class Tuple<U, V> {
    private final U u;
    private final V v;

    public Tuple(U u, V v) {
        this.u = u;
        this.v = v;
    }

    public U getFirst() {
        return u;
    }

    public V getSecond() {
        return v;
    }

	@Override
	public String toString() {
		return "Tuple [first=" + u + ", second=" + v + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((u == null) ? 0 : u.hashCode());
		result = prime * result + ((v == null) ? 0 : v.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tuple other = (Tuple) obj;
		if (u == null) {
			if (other.u != null)
				return false;
		} else if (!u.equals(other.u))
			return false;
		if (v == null) {
			if (other.v != null)
				return false;
		} else if (!v.equals(other.v))
			return false;
		return true;
	}

	
	
    
}
