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

public class CorpusReaderException extends Exception {
	public CorpusReaderException(String msg, CorpusReadError error) {
		super(msg);
		d_error = error;
	}
	
	public CorpusReadError error() {
		return d_error;
	}
	
	public enum CorpusReadError { MISSING_TAG, ZERO_LENGTH_TAG, ZERO_LENGTH_WORD }
	
	private static final long serialVersionUID = 2636057373916773934L;
	private CorpusReadError d_error;
}
