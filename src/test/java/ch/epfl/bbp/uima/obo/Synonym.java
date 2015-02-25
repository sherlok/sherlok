/**
 * Copyright (C) 2014-2015 Renaud Richardet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.epfl.bbp.uima.obo;

/** A synonyn held in an OBO Ontology.
 * 
 * @author ptc24
 *
 */
public final class Synonym {

	private String syn;
	private String type;
	private String source;
	
	Synonym(String syn, String type, String source) {
		this.syn = syn;
		if(type != null && type.length() > 0) this.type = type;
		if(source != null && source.length() > 0) this.source = source;
	}
	
	/**Gets the synonym string.
	 * 
	 * @return The synonym string.
	 */
	public String getSyn() {
		return syn;
	}
	
	/**Gets the type of the synonym.
	 * 
	 * @return The type of the synonym.
	 */
	public String getType() {
		return type;
	}
	
	/**Gets the source of the synonym.
	 * 
	 * @return The source of the synonym.
	 */
	public String getSource() {
		return source;
	}
	
	/**A string representation of the Synonym object.
	 * 
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(syn);
		if(type != null) sb.append(", type=" + type);
		if(source != null) sb.append(", source=" + source);
		sb.append("]");
		return sb.toString();
	}
	
}
