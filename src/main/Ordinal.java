package main;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents an ordinal below epsilon 0 using Cantor normal form (CNF).
 * 
 * @author Komi Amiko
 */
public class Ordinal implements Comparable<Ordinal>, Serializable {
	private static final long serialVersionUID = -4891573930645590304L;
	
	/**
	 * The ordinal 0
	 */
	public static final Ordinal ZERO = new Ordinal(null, 0);
	/**
	 * The ordinal 1
	 */
	public static final Ordinal ONE = new Ordinal(null, 1);
	/**
	 * The ordinal omega (aka w)
	 */
	public static final Ordinal W = ONE.wexp();
	/**
	 * The ordinal w^w
	 */
	public static final Ordinal WW = W.wexp();
	
	/**
	 * Additive terms which are at least w but below e0
	 * The pair (a, n) represents w^a n
	 */
	public ArrayList<Pair<Ordinal,Integer>> _cnf;
	/**
	 * Additive term, below w
	 */
	public int _nat;

	/**
	 * Construct by value
	 * 
	 * @param cnf
	 * @param nat
	 */
	public Ordinal(ArrayList<Pair<Ordinal,Integer>> cnf, int nat) {
		if(cnf == null) {
			cnf = new ArrayList<>();
		}
		_cnf = cnf;
		_nat = nat;
	}
	
	/**
	 * Make a shallow copy
	 * 
	 * @return
	 */
	public Ordinal copy() {
		return new Ordinal(new ArrayList<>(_cnf), _nat);
	}
	
	/**
	 * Map a to w^a
	 * 
	 * @return
	 */
	public Ordinal wexp() {
		if(_nat == 0 && _cnf.size() == 0)return ONE;
		ArrayList<Pair<Ordinal,Integer>> rcnf = new ArrayList<>();
		rcnf.add(new Pair<>(this, 1));
		return new Ordinal(rcnf, 0);
	}
	
	/**
	 * What kind of ordinal is this?
	 * 
	 * @return
	 */
	public OrdinalKind kind() {
		if(_nat == 0 && _cnf.size() == 0)return OrdinalKind.ZERO;
		if(_nat != 0)return OrdinalKind.SUCCESSOR;
		return OrdinalKind.LIMIT;
	}
	
	/**
	 * From successor a+1, get a
	 * 
	 * @return
	 */
	public Ordinal predecessor() {
		assert kind() == OrdinalKind.SUCCESSOR;
		return new Ordinal(_cnf, _nat-1);
	}
	
	/**
	 * Left subtraction
	 * 
	 * @param other
	 * @return
	 */
	public Ordinal lsub(Ordinal other) {
		assert this.compareTo(other) >= 0;
		boolean wipe = false;
		ArrayList<Pair<Ordinal,Integer>> rcnf = new ArrayList<>();
		int optr = 0;
		for(var sc:_cnf) {
			if(!wipe && optr >= other._cnf.size()) {
				wipe = true;
			}
			if(wipe) {
				rcnf.add(sc);
				continue;
			}
			var oc = other._cnf.get(optr++);
			if(sc.left.equals(oc.left)) {
				if(sc.right > oc.right) {
					wipe = true;
					rcnf.add(new Pair<>(sc.left, sc.right - oc.right));
				}
				continue;
			}
			wipe = true;
			rcnf.add(sc);
			continue;
		}
		int rnat = wipe?_nat:(_nat-other._nat);
		return new Ordinal(rcnf, rnat);
	}
	
	/**
	 * Take a fundamental sequence value a[n]
	 * 
	 * @param n
	 * @return
	 */
	public Ordinal fundamental(int n) {
		assert kind() == OrdinalKind.LIMIT;
		assert n >= 0;
		var rcnf = new ArrayList<>(_cnf);
		int rnat = 0;
		var lastPair = rcnf.remove(rcnf.size()-1);
		var lastOrd = lastPair.left;
		if(lastPair.right > 1) {
			rcnf.add(new Pair<>(lastOrd, lastPair.right - 1));
		}
		OrdinalKind lastKind = lastOrd.kind();
		if(lastKind == OrdinalKind.LIMIT) {
			var up = lastOrd.fundamental(n);
			if(up.equals(ZERO)) {
				rnat = 1;
			} else {
				rcnf.add(new Pair<>(up, 1));
			}
		} else {
			if(lastOrd.equals(ONE)) { // ends in +w
				rnat = n;
			} else { // ends in +w^(a+1)
				if(n > 0) {
					rcnf.add(new Pair<>(lastOrd.predecessor(), n));
				}
			}
		}
		return new Ordinal(rcnf, rnat);
	}
	
	public static Ordinal parse(String s) {
		ArrayList<Ordinal> stack = new ArrayList<>();
		for(int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			switch(c) {
			case '0': {
				stack.add(ZERO);
				break;
			}
			case 'C': {
				Ordinal oc = stack.remove(stack.size()-1);
				Ordinal ob = stack.remove(stack.size()-1);
				Ordinal o;
				if(ob.equals(ZERO)) {
					o = new Ordinal(oc._cnf, oc._nat + 1);
				} else {
					var rcnf = new ArrayList<>(oc._cnf);
					if(rcnf.size() != 0 && rcnf.get(rcnf.size()-1).left.equals(ob)) {
						var lastPair = rcnf.get(rcnf.size()-1);
						rcnf.add(new Pair<>(ob, lastPair.right + 1));
					} else {
						rcnf.add(new Pair<>(ob, 1));
					}
					o = new Ordinal(rcnf, 0);
				}
				stack.add(o);
				break;
			}
			default: assert false;
			}
		}
		assert stack.size() == 1;
		return stack.get(0);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("O(");
		sb.append(_cnf.toString());
		sb.append(", ");
		sb.append(Integer.toString(_nat));
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int compareTo(Ordinal o) {
		int lim = Math.min(_cnf.size(), o._cnf.size());
		for(int i = 0; i < lim; ++i) {
			var sterm = _cnf.get(i);
			var oterm = o._cnf.get(i);
			int iresult = sterm.compareTo(oterm);
			if(iresult != 0)return iresult;
		}
		int iresult = Integer.compare(_cnf.size(), o._cnf.size());
		if(iresult != 0)return iresult;
		return Integer.compare(_nat, o._nat);
	}

	@Override
	public int hashCode() {
		long collect = _cnf.size() ^ 0x27ccdb6061a69176L;
		for(var term:_cnf) {
			collect ^= term.hashCode();
			collect -= collect << 7;
			collect ^= collect >> 10;
		}
		collect *= 1 | (((long)_nat) << 2);
		return Long.hashCode(collect);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Ordinal)) {
			return false;
		}
		Ordinal other = (Ordinal) obj;
		if (_nat != other._nat) {
			return false;
		}
		if (_cnf == null) {
			if (other._cnf != null) {
				return false;
			}
		} else if (!_cnf.equals(other._cnf)) {
			return false;
		}
		return true;
	}

}
