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

package eu.danieldk.nlp.jitar.data.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map.Entry;

/**
 * This class implements a comparator for {@link Entry} instances parametrized
 * over Integer, Double. The order is descending on the entry value.
 */
public class ProbEntryComparator implements Comparator<Entry<Integer, Double>>,
		Serializable {
	private static final long serialVersionUID = 20080720L;

	public int compare(Entry<Integer, Double> e1, Entry<Integer, Double> e2) {
		if (e1.getValue().equals(e2.getValue()))
			return e1.getKey().compareTo(e2.getKey());
		
		return e2.getValue().compareTo(e1.getValue());
	}
}
