/*
 * Copyright 2008, 2009 Daniel de Kok
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

package eu.danieldk.nlp.jitar.data.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map.Entry;

/**
 * This class implements a comparator for <i>Entry<String, Double></i> instances.
 * The order is descending on the entry value.
 */
public class ProbEntryComparator implements Comparator<Entry<Integer, Double>>,
		Serializable {
	private static final long serialVersionUID = 20080720L;

	public int compare(Entry<Integer, Double> e1, Entry<Integer, Double> e2) {
		if (e1.getValue() == e2.getValue())
			return e1.getKey().compareTo(e2.getKey());
		
		return e2.getValue().compareTo(e1.getValue());
	}
}
