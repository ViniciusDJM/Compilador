package tabelaDeSimbolos;


/**
 *
 * @author Vin�cius Maciel e Ricardo Porto
 */
import java.util.LinkedHashMap;
import java.util.Map;

public class TabelaStruct {
	//identificao �nica
		private String id;
		//nome da variavel
		private String name;
		
		private Map<String, String> attributes = new LinkedHashMap<String, String>();
		
		public TabelaStruct(String id, String name) {
			super();
			this.id = id;
			this.name = name;
		}
		
		public TabelaStruct() {}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public Map<String,String> getAttributes() {
			return attributes;
		}

		public void setAttributes(Map<String,String> attributes) {
			this.attributes = attributes;
		}
		
		public void add(String name, String type) {
			attributes.put(name, type);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TabelaStruct other = (TabelaStruct) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "StructSymbol [id=" + id + ", name=" + name + "]";
		}



}