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
 * This class represents a word uni-gram.
 */
public class UniGram implements Serializable {
    private static final long serialVersionUID = 1L;

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
