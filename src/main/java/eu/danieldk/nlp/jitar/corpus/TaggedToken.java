// Copyright 2008, 2009 Daniel de Kok
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

package eu.danieldk.nlp.jitar.corpus;

/**
 * Instances of <i>TaggedWord</i> represent a word/tag combination.
 */
public class TaggedToken {
	private final String d_word;
	private final String d_tag;
	
	public TaggedToken(String word, String tag) {
		d_word = word;
		d_tag = tag;
	}

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;

    if (!(o instanceof TaggedToken))
      return false;

    TaggedToken other = (TaggedToken) o;

    return other.d_word.equals(d_word) && other.d_tag.equals(d_tag);
  }

	public String tag() {
		return d_tag;
	}

  @Override
  public String toString() {
    return d_word + "/" + d_tag;
  }

	public String word() {
		return d_word;
	}
}
