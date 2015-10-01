//
// Copyright 2008, 2015 Daniël de Kok
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package eu.danieldk.nlp.jitar.data;

import java.io.Serializable;

/**
 * This class represents a word tri-gram.
 */
public class TriGram implements Serializable {
    private static final long serialVersionUID = 1L;

	private final int d_t1;
	private final int d_t2;
	private final int d_t3;
	
	public TriGram(int t1, int t2, int t3) {
		d_t1 = t1;
		d_t2 = t2;
		d_t3 = t3;
	}
	
	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject)
			return true;
		
		if (otherObject == null)
			return false;
		
		if (getClass() != otherObject.getClass())
			return false;
		
		TriGram other = (TriGram) otherObject;
		
		return d_t1 == other.d_t1 && d_t2 == other.d_t2 &&
			d_t3 == other.d_t3;
	}
	
	@Override
	public int hashCode() {
		int seed = d_t1;
		seed ^= d_t2 + 0x9e3779b9 + (seed << 6) + (seed >> 2);
		seed ^= d_t3 + 0x9e3779b9 + (seed << 6) + (seed >> 2);
		return seed;
	}
	
	public int t1() {
		return d_t1;
	}
	
	public int t2() {
		return d_t2;
	}

	public int t3() {
		return d_t3;
	}
	
	@Override
	public String toString() {
		return d_t1 + " " + d_t2 + " " + d_t3;
	}
}
