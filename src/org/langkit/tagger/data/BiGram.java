/*
 * Copyright 2008, 2009 Daniël de Kok
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.langkit.tagger.data;

/**
 * This class represents a word bi-gram.
 */
public class BiGram {
	private final int d_t1;
	private final int d_t2;
	
	public BiGram(int t1, int t2) {
		d_t1 = t1;
		d_t2 = t2;
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		
		if (otherObject == null)
			return false;
		
		if (getClass() != otherObject.getClass())
			return false;
		
		BiGram other = (BiGram) otherObject;
		
		return d_t1 == other.d_t1 && d_t2 == other.d_t2;
	}
	
	@Override
	public int hashCode() {
		int seed = d_t1;
		seed ^= d_t2 + 0x9e3779b9 + (seed << 6) + (seed >> 2);
		return seed;
	}
	
	public int t1() {
		return d_t1;
	}
	
	public int t2() {
		return d_t2;
	}
	
	@Override
	public String toString() {
		return d_t1 + " " + d_t2;
	}
}
