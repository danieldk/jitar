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
 * This class represents a word uni-gram.
 */
public class UniGram {
	private final int d_t1;
	
	public UniGram(int t1) {
		d_t1 = t1;
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		
		if (otherObject == null)
			return false;
		
		if (getClass() != otherObject.getClass())
			return false;
		
		UniGram other = (UniGram) otherObject;
		
		return d_t1 == other.d_t1;
	}
	
	@Override
	public int hashCode() {
		return d_t1;
	}
	
	public int t1() {
		return d_t1;
	}
	
	@Override
	public String toString() {
		return Integer.toString(d_t1);
	}
}
