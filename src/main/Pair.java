package main;

import java.io.Serializable;

public class Pair<T extends Comparable<T>, U extends Comparable<U>> implements Comparable<Pair<T, U>>, Serializable {
	private static final long serialVersionUID = 4345695581285112095L;
	
	public T left;
	public U right;

	/**
	 * Construct by value
	 * 
	 * @param left
	 * @param right
	 */
	public Pair(T left, U right) {
		this.left = left;
		this.right = right;
	}
	
	/**
	 * Simple copy constructor
	 * 
	 * @param o
	 */
	public Pair(Pair<T, U> o) {
		left = o.left;
		right = o.right;
	}

	/**
	 * Lexicographic comparison by (left, right)
	 */
	@Override
	public int compareTo(Pair<T, U> o) {
		int result = left.compareTo(o.left);
		if(result != 0) {
			return result;
		}
		return right.compareTo(o.right);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(left);
		sb.append(", ");
		sb.append(right);
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		int result = left.hashCode();
		result = Integer.rotateLeft(result, 5);
		result ^= 0x639ecf38;
		result += right.hashCode();
		result ^= result >> 7;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Pair)) {
			return false;
		}
		Pair other = (Pair) obj;
		if (left == null) {
			if (other.left != null) {
				return false;
			}
		} else if (!left.equals(other.left)) {
			return false;
		}
		if (right == null) {
			if (other.right != null) {
				return false;
			}
		} else if (!right.equals(other.right)) {
			return false;
		}
		return true;
	}

}
