package sintaticoSemanticoAnalisador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.stream.Collectors;

import lexicAnalyser.Token;
import lexicAnalyser.Word;
import tabelaDeSimbolos.TabelaFuncao;
import tabelaDeSimbolos.TabelaStruct;
import tabelaDeSimbolos.TabelaVariavel;
import lexicAnalyser.Number;
import lexicAnalyser.Tag;

/**
 *
 * @author Vin�cius Maciel e Ricardo Porto
 */
public class SintaticoSemanticoAnalisador {

	//lista de tokens detectado no codigo fonte
	private List<Token> tokens = new ArrayList<Token>();
	//lista de erros sintaticos
	private List<String> erros = new ArrayList<String>();
	//lista de erros sem�nticos
	private List<String> semanticErrors = new ArrayList<String>();
	// index que controla qual token ser� pego na lista
	private int index = 0;
	// armazena o token da frente
	private Token lookahead;
	//HashTable para a tabela de simbolos
	Hashtable<String, TabelaVariavel> symbolTable = new Hashtable<String, TabelaVariavel>();
	//Hashtable para a tabela de simbolos para funcao
	Hashtable<String, TabelaFuncao> functionTableSymbol = new Hashtable<String, TabelaFuncao>();
	//Hashtable para a table de simbolos para struct
	Hashtable<String, TabelaStruct> structTableSymbol = new Hashtable<String, TabelaStruct>();
	//Hashtable para a tabela de simbolos dos tipos dos dados
	Hashtable<String, Integer> typeDataTableSymbol = new Hashtable<String, Integer>();
	//lista para verifica��o semantica da equa��o partir de valores fornecidos
	List<Token> lookExpressionData = new ArrayList<Token>();
	//lista para verifica��o semantica da equa��o partir das variaveis
	List<TabelaVariavel> lookExpressionVariable = new ArrayList<TabelaVariavel>();
	private TabelaVariavel variableAssign = null;
	private int indexlookExpressionData = 0;
	private int indexlookExpressionVariable = 0;
	
	public SintaticoSemanticoAnalisador(List<Token> tokens) {
		this.tokens = tokens; //recebe a lista de tokens
		lookahead = tokens.get(0); // come�a com o primeiro token
		//tipos primitivos
		typeDataTableSymbol.put("int", Tag.NRO);
		typeDataTableSymbol.put("string", Tag.CAD);
		typeDataTableSymbol.put("boolean", Tag.PRE);
		typeDataTableSymbol.put("real",Tag.NRO);
	}
	
	public List<String> getErros() {
		return erros;
	}
	
	public List<String> getSemanticErrors(){
		return semanticErrors;
	}
	
	//metodo que pega o lexema do token passado como argumento
	public String getLexeme(Token token) {
		// verifica qual sub-classe pertence o token
		if(token.getClass() == Word.class) {
			Word word = (Word) token;
			return word.lexeme;
		}
		else if(token.getClass() == Number.class) {
			Number number = (Number) token;
			return Integer.toString(number.value);
		}else
			return " ";
	}
	
	//metodo que realiza a compara��o do que recebeu com o que era esperado
	//obs: quando quiser comparar numero passa o waited como null que ai ele faz a compara��o pela TAG
	public void match(Token token, String waited, int type) {
		if(getLexeme(token).equals(waited) && type == token.tag) {
			if(index < tokens.size() - 1) {
				//pega o token da frente
				index++;
				lookahead = tokens.get(index);
			}
			else lookahead = new Word(" ",Tag.END,-1);
		}
		else if(waited == null && type == token.tag) {
			if(index < tokens.size() - 1) {
				//pega o token da frente
				index++;
				lookahead = tokens.get(index);
			}
			else lookahead = new Word(" ",Tag.END,-1);
		}
	}
	
	//metodo para achar o token de sincromiza��o
	public void error(String msgError, List<Token> symbSyncronization) {
		erros.add(msgError);
		//procura os tokens de sincroniza��o para poder continuar a verifica��o
		if(symbSyncronization == null) return;
		while(true) {
			//se chegou ao final da lista de tokens e n�o achou o token de sicroniza��o
			if(index == tokens.size() - 1) break;
			//verica o token de sincroniza��o pela tag
			if(lookahead.tag == Tag.IDE) {
				if(symbSyncronization.contains(new Word(" ", Tag.IDE, -1)))break;
			}
			if(lookahead.tag == Tag.NRO) {
				if(symbSyncronization.contains(new Word(" ", Tag.NRO, -1)))break;
			}
			if(lookahead.tag == Tag.CAD){
				if(symbSyncronization.contains(new Word(" ", Tag.CAD, -1)))break;
			}
			if(symbSyncronization.contains(lookahead)) {
				//achar o token com base somente na sua tag
				int aux  = symbSyncronization.indexOf(lookahead);
				if(getLexeme(symbSyncronization.get(aux)).equals(" "))break;
				//achar o token com base somente na sua tag e lexeme
				else {
					if(getLexeme(lookahead).equals(getLexeme(symbSyncronization.get(aux)))) {
						break;
					}
				}
			}
			index++;
			lookahead = tokens.get(index);
		}
	}
		
	//metodo para verificar se todos os valoress�o do mesmo tipo
	public boolean lookExpression(int type, boolean seeParamFunction) {
		boolean isEqualType = false;
		boolean isInitialiazed = false;
		boolean isEqualTypeVariable = false;
		if(seeParamFunction) {
			if(lookExpressionData.size() - 1 != indexlookExpressionData && indexlookExpressionData != -1) {
				isEqualType = lookExpressionData.subList(indexlookExpressionData+1, lookExpressionData.size()).stream().allMatch(element -> element.tag == type);
				lookExpressionData.subList(indexlookExpressionData+1, lookExpressionData.size()).clear();
				}
			else if(indexlookExpressionData == -1) {
				isEqualType = lookExpressionData.stream().allMatch(element -> element.tag == type);
				lookExpressionData.clear();
			}
			if(lookExpressionVariable.size() - 1 != indexlookExpressionVariable && indexlookExpressionVariable != -1) {
				isInitialiazed = lookExpressionVariable.subList(indexlookExpressionVariable+1, lookExpressionVariable.size()).stream().allMatch(element -> element.isInitialized());
				isEqualTypeVariable = lookExpressionVariable.subList(indexlookExpressionVariable+1, lookExpressionVariable.size()).stream().allMatch(element -> typeDataTableSymbol.get(element.getTypeData()) == type);
				lookExpressionVariable.subList(indexlookExpressionVariable+1, lookExpressionVariable.size()).clear();
				}
			else if(indexlookExpressionVariable == -1) {
				isInitialiazed = lookExpressionVariable.stream().allMatch(element -> element.isInitialized());
				isEqualTypeVariable = lookExpressionVariable.stream().allMatch(element -> typeDataTableSymbol.get(element.getTypeData()) == type);
				lookExpressionVariable.clear();
			}
			indexlookExpressionData = 0;
			indexlookExpressionVariable = 0;
		}else {
			//verifica se foram passados valores do tipo daatribui��o
			//lookExpressionData.forEach(element -> System.out.println(element.tag + " " + element.line));
			isEqualType = lookExpressionData.stream().allMatch(element -> element.tag == type);
			//verifica se as variaveis est�o inicializadas
			//lookExpressionVariable.forEach(element -> System.out.println("type: " + type + " " + element.getName() + " "+ element.getScope() + " " + element.getTypeData()));
			isInitialiazed = lookExpressionVariable.isEmpty() ? true : lookExpressionVariable.stream().allMatch(element -> element.isInitialized());
			//verifica se as variaveiss�o do mesmo tipo que daatribui��o
			isEqualTypeVariable = lookExpressionVariable.isEmpty() ? true: lookExpressionVariable.stream().allMatch(element -> typeDataTableSymbol.get(element.getTypeData()) == type);
			lookExpressionData.clear();
			lookExpressionVariable.clear();
		}
		if(!isEqualType) semanticErrors.add("Erro sem�ntico: este valor n�o corresponde ao tipo de dado, na linha:" + lookahead.line);
		if(!isInitialiazed) semanticErrors.add("Erro sem�ntico: foi usado variaveis que n�o foram inicializadas, na linha:" + lookahead.line);
		if(!isEqualTypeVariable) semanticErrors.add("Erro sem�ntico: foi usado variaveis com o tipo de dado errado, na linha:" + lookahead.line);
		return isEqualType && isInitialiazed && isEqualTypeVariable;
	}
	
	public void analyseCode(){
		//functionTableSymbol.put("proc_start", new FunctionTableSymbol("proc_start", "start","void", true));
		while(true) {
			//analise atras de bloco de const
			if(getLexeme(lookahead).equals("const")) {
				analyseConst();
				continue;
			}
			//analise atras de bloco de var
			else if(getLexeme(lookahead).equals("var")) {
				analyseVar("global");
				continue;
			}
			//analise atras fun��es e procedures
			else if(getLexeme(lookahead).equals("function") || getLexeme(lookahead).equals("procedure")) {
				analyseFunctionAndProcedureDeclaration();
				continue;
			}
			//analise atras de bloco de struct
			else if(getLexeme(lookahead).equals("struct")) {
				analyseStructDecl();
				continue;
			}
			//analise atras de typedef
			else if(getLexeme(lookahead).equals("typedef")) {
				analyseTypedef();
				continue;
			}
			//analise da funcao start
			if(getLexeme(lookahead).equals("start")) {
				analyseFuncionStart();
				break;
			}
			else{
				break;
			}
		}
	}
	
	//metodo para analisar a fun��o start 
	public void analyseFuncionStart() {
		if(getLexeme(lookahead).equals("start")) {
			match(lookahead,"start",Tag.PRE);
		}
		if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
			match(lookahead,"(",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(")",Tag.DEL,-1),
					new Word("{",Tag.DEL,-1)
					);
			error("Esperava encontrar um ( na linha " + lookahead.line, symbSyncronization);
		}
		if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
			match(lookahead,")",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(")",Tag.DEL,-1),
					new Word("{",Tag.DEL,-1),
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
					);
			error("Esperava encontrar um ) na linha " + lookahead.line, symbSyncronization);
		}
		if(getLexeme(lookahead).equals("{")) {
			match(lookahead,"{",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(")",Tag.DEL,-1),
					new Word("{",Tag.DEL,-1),
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
					);
			error("Esperava encontrar um { na linha " + lookahead.line, symbSyncronization);
		}
		body(true,"start");
		if(lookahead == null) return;
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead,"}",Tag.DEL);
		}
	}
	
	//metodo para analisar o bloco const
	public void analyseConst(){
		//verifica se o token da frente tem o lexema const
		if(getLexeme(lookahead).equals("const")) {
			//realiza a compara��o do token esperado pelo recebido
			match(lookahead,"const",Tag.PRE);
			//verifica se o token da frente tem o lexema {
		} else return;
		if(getLexeme(lookahead).equals("{")) {
			match(lookahead,"{",Tag.DEL);
		}else {
			// a lista de sincroniza��o s�o os tokens que tem a tag como PRE e lexeme (int,boolean,string,struct,real) e os first do n�o terminal pai
			List<Token> symbSyncronization = Arrays.asList(
					new Word("int",Tag.PRE,-1),
					new Word("boolean",Tag.PRE,-1),
					new Word("real",Tag.PRE,-1),
					new Word("string",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1),
					new Word("function",Tag.PRE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("procedure",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("start",Tag.PRE,-1));
			error("Esperava encontrar uma { depois do const na linha " + lookahead.line, symbSyncronization);
		}
		//fun��o que realiza o procedimento de verifica��o de instancias de atributos
		attributeList("const","global");
		//verifica se o token da frente tem o lexema }
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead,"}",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("function",Tag.PRE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("procedure",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("start",Tag.PRE,-1));
			error("Esperava encontrar uma } depois do const na linha " + lookahead.line, symbSyncronization);
		}
	}
	
	//metodo para analisar o bloco var
	public void analyseVar(String scope) {
		if(getLexeme(lookahead).equals("var")) {
			match(lookahead, "var", Tag.PRE);
		} else return;
		if(getLexeme(lookahead).equals("{")) {
			match(lookahead, "{", Tag.DEL);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("int",Tag.PRE,-1),
					new Word("boolean",Tag.PRE,-1),
					new Word("real",Tag.PRE,-1),
					new Word("string",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1),
					new Word("function",Tag.PRE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("procedure",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("start",Tag.PRE,-1));
			error("Esperava encontrar uma { depois do var na linha " + lookahead.line, symbSyncronization);
		}
		
		attributeList("var",scope);
		
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead, "}", Tag.DEL);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("function",Tag.PRE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("procedure",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("start",Tag.PRE,-1));
			error("Esperava encontrar uma } depois do var na linha " + lookahead.line, symbSyncronization);
		}
	}
	
	//metodo para analisar o bloco struct
	public void analyseStructDecl() {
		TabelaStruct simbolo;
		String structName =  "";
		while(true) {
			if(getLexeme(lookahead).equals("struct") && lookahead.tag == Tag.PRE) {
				match(lookahead, "struct", Tag.PRE);
			}else break;
			
			simbolo = new TabelaStruct();
			if(lookahead.tag == Tag.IDE) {
				boolean check = structTableSymbol.contains(new TabelaStruct("", getLexeme(lookahead)));
				if(check) {
					semanticErrors.add("Erro sem�ntico (struct): j� existe uma struct com esse nome");
				}
				else {
					simbolo.setName(getLexeme(lookahead));
					structTableSymbol.put("struct_" + simbolo.getName(), new TabelaStruct("struct_" + simbolo.getName(), getLexeme(lookahead)));
					typeDataTableSymbol.put("struct_" + simbolo.getName(), 100);
					structName = simbolo.getName();
				}
				
				match(lookahead, null, Tag.IDE);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word("extends", Tag.PRE, -1),
						new Word("{", Tag.DEL, -1),
						new Word("int",Tag.PRE,-1),
						new Word("boolean",Tag.PRE,-1),
						new Word("real",Tag.PRE,-1),
						new Word("string",Tag.PRE,-1)
						);
				error("Esperava encontrar um identificador depois do struct na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals("extends") && lookahead.tag == Tag.PRE) {
				match(lookahead, "extends", Tag.PRE);
				if(lookahead.tag == Tag.IDE) {
					match(lookahead, null, Tag.IDE);
					
				}
				else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("{", Tag.DEL, -1),
							new Word("int",Tag.PRE,-1),
							new Word("boolean",Tag.PRE,-1),
							new Word("real",Tag.PRE,-1),
							new Word("string",Tag.PRE,-1)
							);
					error("Esperava encontrar um identificador depois do extends na linha " + lookahead.line, symbSyncronization);
				}
				
			}
			if(getLexeme(lookahead).equals("{") && lookahead.tag == Tag.DEL) {
				match(lookahead, "{", Tag.DEL);
				attributeList("struct", structName);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
					new Word("int",Tag.PRE,-1),
					new Word("boolean",Tag.PRE,-1),
					new Word("real",Tag.PRE,-1),
					new Word("string",Tag.PRE,-1),
					new Word("struct", Tag.PRE, -1),
					new Word("}",Tag.DEL,-1),
					new Word("function",Tag.PRE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("procedure",Tag.PRE,-1),
					new Word("struct",Tag.PRE,-1),
					new Word("start",Tag.PRE,-1));
			error("Esperava encontrar um { na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals("}")) {
				match(lookahead, "}", Tag.DEL);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word("function", Tag.PRE, -1),
						new Word("var", Tag.PRE, -1),
						new Word("procedure", Tag.PRE, -1),
						new Word("struct", Tag.PRE, -1),
						new Word("start", Tag.PRE, -1)
						);
				error("Esperava encontrar um tipo de dado linha " + lookahead.line, symbSyncronization);
				return;
			}
		}
	}
	
	//metodo para analisar o typedef
	public void analyseTypedef() {
		List<String> keyWords = Arrays.asList("int","string", "struct","real","boolean");
		while(true) {
			if(getLexeme(lookahead).equals("typedef") && lookahead.tag == Tag.PRE) {
				match(lookahead, "typedef", Tag.PRE);
			}
			else break;
			if(getLexeme(lookahead).equals("struct")) {
				match(lookahead,"struct",Tag.PRE);
				if(lookahead.tag == Tag.IDE) {
					match(lookahead,null,Tag.IDE);
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(";", Tag.DEL, -1),
							new Word("function", Tag.PRE, -1),
							new Word("var", Tag.PRE, -1),
							new Word("procedure", Tag.PRE, -1),
							new Word("struct", Tag.PRE, -1),
							new Word("start", Tag.PRE, -1),
							new Word(" ", Tag.IDE, -1)
							);
					error("Esperava encontrar um identificador na linha " + lookahead.line, symbSyncronization);
				}
			}
			if(typeDataTableSymbol.containsKey(getLexeme(lookahead))){
				match(lookahead,getLexeme(lookahead),lookahead.tag);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ", Tag.IDE, -1),
						new Word(";", Tag.DEL, -1)
						);
				error("Esperava encontrar um tipo de dado linha " + lookahead.line, symbSyncronization);
			}
			
			if(lookahead.tag == Tag.IDE) {
				match(lookahead,null,Tag.IDE);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(";", Tag.DEL, -1),
						new Word("function", Tag.PRE, -1),
						new Word("var", Tag.PRE, -1),
						new Word("procedure", Tag.PRE, -1),
						new Word("struct", Tag.PRE, -1),
						new Word("start", Tag.PRE, -1)
						);
				error("Esperava encontrar um identificador na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals(";")) {
				match(lookahead,";",Tag.DEL);	
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(";", Tag.DEL, -1),
						new Word("function", Tag.PRE, -1),
						new Word("var", Tag.PRE, -1),
						new Word("procedure", Tag.PRE, -1),
						new Word("struct", Tag.PRE, -1),
						new Word("start", Tag.PRE, -1)
						);
				error("Esperava encontrar um ; na linha " + lookahead.line, symbSyncronization);
			}
		}
	}
	
	//metodo para analisar a fun��o read
	public void analyseRead() {
		if(getLexeme(lookahead).equals("read") && lookahead.tag == Tag.PRE) {
			match(lookahead, "read", Tag.PRE);
			
		}
		
		if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
			match(lookahead, "(", Tag.DEL);	
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ", Tag.IDE, -1),
					new Word(",", Tag.DEL, -1),
					new Word(")", Tag.DEL, -1)
					);
			error("Esperava encontrar um ( na linha " + lookahead.line, symbSyncronization);
		}
		
		while(true) {
			if(lookahead.tag == Tag.IDE) {
				match(lookahead, null, Tag.IDE);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(";", Tag.DEL, -1),
						new Word(")", Tag.DEL, -1),
						new Word(",", Tag.DEL, -1),
						new Word("if", Tag.PRE, -1),
						new Word("while", Tag.PRE, -1),
						new Word("print", Tag.PRE, -1),
						new Word("}", Tag.DEL, -1)
						);
				error("Esperava encontrar um identificador na linha " + lookahead.line, symbSyncronization);
			}
			
			if(getLexeme(lookahead).equals(",") && lookahead.tag == Tag.DEL) {
				match(lookahead,",",Tag.DEL);
				continue;
			}
			
			else if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
				match(lookahead,")",Tag.DEL);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(";", Tag.DEL, -1),
						new Word("if", Tag.PRE, -1),
						new Word("while", Tag.PRE, -1),
						new Word("print", Tag.PRE, -1),
						new Word("}", Tag.DEL, -1)
						);
				error("Esperava encontrar um ) na linha " + lookahead.line, symbSyncronization);
			}
			
			if(getLexeme(lookahead).equals(";")) {
				match(lookahead,";",Tag.DEL);
				break;
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word("if", Tag.PRE, -1),
						new Word("while", Tag.PRE, -1),
						new Word("print", Tag.PRE, -1),
						new Word("}", Tag.DEL, -1)
						);
				error("Esperava encontrar ; na linha " + lookahead.line, symbSyncronization);
				break;
			}
		}
	}
	
	//metodo para analisar a fun��o print
	public void analysePrint(String scope) {
			if(getLexeme(lookahead).equals("print") && lookahead.tag == Tag.PRE) {
				match(lookahead, "print", Tag.PRE);
				
			}
			if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
				match(lookahead, "(", Tag.DEL);
				
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ", Tag.IDE, -1),
						new Word(" ", Tag.CAD, -1),
						new Word(",", Tag.DEL, -1),
						new Word(")", Tag.DEL, -1)
						);
				error("Esperava encontrar um ( na linha " + lookahead.line, symbSyncronization);
			}
			
				
			while(true) {
				if(lookahead.tag == Tag.CAD) {
					match(lookahead,null,Tag.CAD);
				}
				else if (lookahead.tag == Tag.IDE || lookahead.tag == Tag.NRO) {
					expression(scope,false);
				}
				else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(";", Tag.DEL, -1),
							new Word(")", Tag.DEL, -1),
							new Word(",", Tag.DEL, -1),
							new Word("if", Tag.PRE, -1),
							new Word("var", Tag.PRE, -1),
							new Word(" ", Tag.IDE, -1),
							new Word("while", Tag.PRE, -1),
							new Word("print", Tag.PRE, -1),
							new Word("}", Tag.DEL, -1)
							);
					error("Esperava encontrar uma string ou umaexpress�o na linha " + lookahead.line, symbSyncronization);
				}
					
				if(getLexeme(lookahead).equals(",")) {
					match(lookahead, ",", Tag.DEL);
					continue;
				}
				
				else if(getLexeme(lookahead).equals(")")) {
					match(lookahead, ")", Tag.DEL);
				}
				
				else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(";", Tag.DEL, -1),
							new Word("if", Tag.PRE, -1),
							new Word("while", Tag.PRE, -1),
							new Word("print", Tag.PRE, -1),
							new Word("}", Tag.DEL, -1)
							);
					error("Esperava encontrar um ) na linha " + lookahead.line, symbSyncronization);
					
				}
				
				if(getLexeme(lookahead).equals(";")) {
					match(lookahead, ";", Tag.DEL);
					break;
						
				}
				
				else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("if", Tag.PRE, -1),
							new Word("while", Tag.PRE, -1),
							new Word("print", Tag.PRE, -1),
							new Word("}", Tag.DEL, -1)
							);
					error("Esperava encontrar ; na linha " + lookahead.line, symbSyncronization);
					break;
				}
			}

	}
	
	public void attributeList(String type,String scope) {
		//la�o que permite a verifica��o de linhas que possam ser atributos
		List<String> keyWords = Arrays.asList("int","string","real","boolean","struct");
		while(true) {
			if(type == "const") {
				if(lookahead.tag == Tag.IDE || keyWords.contains(getLexeme(lookahead))) {
					attribute("global");
				}
				else break;

			}
			else if (type == "var"){
				if(lookahead.tag == Tag.IDE || keyWords.contains(getLexeme(lookahead))) {
					attributeVar(scope);
				}
				else break;
			}

			else if (type == "struct") {
				if(lookahead.tag == Tag.IDE || keyWords.contains(getLexeme(lookahead))) {
					attributeStruct(scope);
				}
				else break;
			}
		}	
	}
	
	public void attribute(String scope) {
		switch(getLexeme(lookahead)) {
			/*case "struct":
				match(lookahead,"struct",Tag.PRE);
				attributeValue("struct",scope);
				break;*/
			case "int":
				match(lookahead,"int",Tag.PRE);
				attributeValue("int",scope);
				break;
			case "real":
				match(lookahead,"real",Tag.PRE);
				attributeValue("real", scope);
				break;
			case "boolean":
				match(lookahead,"boolean",Tag.PRE);
				attributeValue("boolean",scope);
				break;
			case "string":
				match(lookahead,"string",Tag.PRE);
				attributeValue("string",scope);
				break;	
			default:
				if(lookahead.tag == Tag.IDE) {
					String type = getLexeme(lookahead);
					match(lookahead,null,Tag.IDE);
					attributeValue(type,scope);
				}	
		}
	}
	
	public void attributeVar(String scope) {
		switch(getLexeme(lookahead)) {
			case "struct":
				String nameStruct = "";
				match(lookahead,"struct",Tag.PRE);
				if(lookahead.tag == Tag.IDE) {
					nameStruct = getLexeme(lookahead);
					match(lookahead,null,Tag.IDE);
				}
				attributeValueVar("struct_"+nameStruct,scope);
				break;
			case "int":
				match(lookahead,"int",Tag.PRE);
				attributeValueVar("int",scope);
				break;
			case "real":
				match(lookahead,"real",Tag.PRE);
				attributeValueVar("real",scope);
				break;
			case "boolean":
				match(lookahead,"boolean",Tag.PRE);
				attributeValueVar("boolean",scope);
				break;
			case "string":
				match(lookahead,"string",Tag.PRE);
				attributeValueVar("string",scope);
				break;
			default:
				if(lookahead.tag == Tag.IDE) {
					String type = getLexeme(lookahead);
					match(lookahead,null,Tag.IDE);
					attributeValueVar(type,scope);
				}
				else{
					
				}
		}
	}
	
	public void attributeStruct(String name) {
		switch(getLexeme(lookahead)) {
			case "struct":
				match(lookahead,"struct",Tag.PRE);
				attributeValueStruct("struct", name);
				break;
			case "int":
				match(lookahead,"int",Tag.PRE);
				attributeValueStruct("int", name);
				break;
			case "real":
				match(lookahead,"real",Tag.PRE);
				attributeValueStruct("real", name);
				break;
			case "boolean":
				match(lookahead,"boolean",Tag.PRE);
				attributeValueStruct("boolean", name);
				break;
			case "string":
				match(lookahead,"string",Tag.PRE);
				attributeValueStruct("string", name);
				break;	
			default:
				if(lookahead.tag == Tag.IDE) {
					String type = getLexeme(lookahead);
					match(lookahead,null,Tag.IDE);
					attributeValueStruct(type, name);
				}	
		}
	}
	
	
	
	public void assign(String scope) {
		String assignTo = "";
		TabelaVariavel variable = null;
		boolean isStruct = false;
		if(getLexeme(lookahead).equals("global") || getLexeme(lookahead).equals("local")) {
			prefixGlobalOrLocal(scope,true);
		}
		else if(lookahead.tag == Tag.IDE) {
			assignTo = getLexeme(lookahead);
			match(lookahead,null,Tag.IDE);
			if(getLexeme(lookahead).equals(".")) {
				isStruct = true;
				prefixStruct(assignTo,scope,true);
			}
			else if(getLexeme(lookahead).equals("[")){
				int count = 0;
				while(count < 2) {
					if(getLexeme(lookahead).equals("[")) {
						match(lookahead, "[", Tag.DEL);
						if(lookahead.tag == Tag.NRO || lookahead.tag == Tag.IDE) {
							while(count < 2) {
								if(getLexeme(lookahead).equals("[")) {
									match(lookahead, "[", Tag.DEL);
									if(lookahead.tag == Tag.NRO || lookahead.tag == Tag.IDE) {
										expression(scope,false);
										boolean isOnlyType = lookExpression(Tag.NRO,false);
										if(!isOnlyType)semanticErrors.add("Erro sem�ntico: Na declara��o de vetores os indices s�o do tipo int erro encontrado em:" + lookahead.line);
									}
									else {
										List<Token> symbSyncronization = Arrays.asList(
												new Word (",", Tag.DEL, -1),
												new Word ("]", Tag.DEL, -1)
												);
										error("Esperava encontrar um identificador ou um n�mero no processo de atribui��o de vetor ou matriz, na linha " + lookahead.line, symbSyncronization);
									}
									if(getLexeme(lookahead).equals("]")) {
										match(lookahead, "]", Tag.DEL);
										count++;
									}
									else {
										List<Token> symbSyncronization = Arrays.asList(
												new Word (",", Tag.DEL, -1),
												new Word ("[", Tag.DEL, -1)
												);
										error("Esperava encontrar um ] processo de atribui��o de vetor ou matriz, na linha " + lookahead.line, symbSyncronization);
									}
								}
								else count = 2;
							}
						}
						else {
							List<Token> symbSyncronization = Arrays.asList(
									new Word("]",Tag.DEL,-1),
									new Word("[",Tag.DEL,-1),
									new Word("=",Tag.REL,-1),
									new Word(" ",Tag.IDE,-1),
									new Word(" ",Tag.NRO,-1),
									new Word("]",Tag.CAD,-1)
							);
							error("Esperava encontrar um identificador ou n�mero na linha " + lookahead.line, symbSyncronization);
						}
						if(getLexeme(lookahead).equals("]")) {
							match(lookahead, "]", Tag.DEL);
							count++;
						}else {
							List<Token> symbSyncronization = Arrays.asList(
									new Word("[",Tag.DEL,-1),
									new Word("=",Tag.REL,-1),
									new Word(" ",Tag.IDE,-1),
									new Word(" ",Tag.NRO,-1),
									new Word("]",Tag.CAD,-1)
							);
							error("Esperava encontrar um [ na linha " + lookahead.line, symbSyncronization);
						}
					}
					else count = 2;
				}
			}else {
				if(symbolTable.containsKey(scope+ "_"+ assignTo)) {
					variable = symbolTable.get(scope+ "_"+ assignTo);
					variableAssign =  variable;
					assignTo = scope+ "_"+ assignTo;
				}
				else if(symbolTable.containsKey("global_"+ assignTo)) {
					variable = symbolTable.get("global_"+ assignTo);
					variableAssign =  variable;
					assignTo = "global_"+ assignTo;
				}else {
					semanticErrors.add("Erro sem�ntico: essa variavel n�o existe, erro encontrado na linha:" + lookahead.line );
				}
			}
		}
		if(getLexeme(lookahead).equals("=") && lookahead.tag == Tag.REL) {
			match(lookahead,"=",Tag.REL);
			if(variableAssign == null) {
				expression(scope,false);
				lookExpressionData.clear();
				lookExpressionVariable.clear();
			}else {
				expression(scope,false);
				boolean isOnlyType = lookExpression(typeDataTableSymbol.get(variableAssign.getTypeData()),false);
				if(!isOnlyType) semanticErrors.add("Erro sem�ntico: express�o de atribui��o de maneira errada(variavel n�o inicializada ou tipo de dado errado) erro encontrado em:" + lookahead.line);
				if(isStruct) {
					if(!symbolTable.containsKey(variableAssign.getName())) {
						symbolTable.put(variableAssign.getName(), variableAssign);
					}
				}else {
					//variable.setInitialized(true);
					//symbolTable.replace(assignTo, variable);
				}
			}
			if(lookahead.tag == Tag.REL) {
				relational(scope);
			}
		}
		if(getLexeme(lookahead).equals(";")) {
			variableAssign = null;
			match(lookahead,";",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
			);
			error("Esperava encontrar um ; na linha " + lookahead.line, symbSyncronization);
		}
	}
	
	public void expression(String scope,boolean seeParamFunction) {
		if(seeParamFunction) {
			indexlookExpressionData = lookExpressionData.size() - 1;
			indexlookExpressionVariable = lookExpressionVariable.size() - 1;
		}
		while(true) {
			if(getLexeme(lookahead).equals("&&") || getLexeme(lookahead).equals("||") || getLexeme(lookahead).equals(";")|| getLexeme(lookahead).equals(")"))break;
			value(scope);
			if(getLexeme(lookahead).equals("/") ||  getLexeme(lookahead).equals("*") || getLexeme(lookahead).equals("+") ||  getLexeme(lookahead).equals("-")) {
				term();
			}else break;
		}
	}
	
	public void logicalExpression(String scope) {
		if(getLexeme(lookahead).equals("!")) {
			match(lookahead,"!", Tag.LOG);
		}
		if(getLexeme(lookahead).equals("true")) match(lookahead,"true",Tag.PRE);
		else if(getLexeme(lookahead).equals("false")) match(lookahead,"false",Tag.PRE);
		else if(getLexeme(lookahead).equals("global") || getLexeme(lookahead).equals("local")) {
			prefixGlobalOrLocal(scope,false);
		}
		else if(lookahead.tag == Tag.IDE){
			expression(scope,false);
			if(lookahead.tag == Tag.REL  || getLexeme(lookahead).equals("!")){
				relationalExpression(scope);
			}
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(")",Tag.DEL,-1),
					new Word("{",Tag.DEL,-1),
					new Word("then",Tag.PRE,-1));
			error("Esperava encontrar um identificador,true,false,global. ou local. na linha" + lookahead.line, symbSyncronization);
		}
		logicalOperator(scope);
	}
	
	
	public void attributeValueStruct(String type, String name) {
		TabelaStruct symbol;
		boolean existType = typeDataTableSymbol.containsKey(type);
		symbol = structTableSymbol.get("struct_" + name);
		while(true){
			if(lookahead.tag == Tag.IDE){
				if(symbol.getAttributes().containsKey(getLexeme(lookahead)))
					semanticErrors.add("Erro Sem�ntico (struct): J� existe essa vari�vel, erro encontrado em:" + lookahead.line + getLexeme(lookahead));
				else if(existType) {
					symbol.add(getLexeme(lookahead), type);
				}
				else
					semanticErrors.add("Erro Sem�ntico (struct): n�o existe esse tipo de variavel erro encontrado em:" + lookahead.line +  type);
				match(lookahead, null, Tag.IDE);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word("=",Tag.REL,-1),
						new Word(",",Tag.DEL,-1),
						new Word(";",Tag.DEL,-1));
				error("Esperava encontrar um identificador no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals(",")) {
				match(lookahead,",",Tag.DEL);
				structTableSymbol.replace("struct_" + name, symbol);
			}
			else if(getLexeme(lookahead).equals(";")) {
				match(lookahead,";",Tag.DEL);
				structTableSymbol.replace("struct_" + name, symbol);
				return;
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word("int",Tag.PRE,-1),
						new Word("boolean",Tag.PRE,-1),
						new Word("real",Tag.PRE,-1),
						new Word("string",Tag.PRE,-1),
						new Word("struct",Tag.PRE,-1),
						new Word("}",Tag.DEL,-1),
						new Word("function",Tag.PRE,-1),
						new Word("var",Tag.PRE,-1),
						new Word("procedure",Tag.PRE,-1),
						new Word("struct",Tag.PRE,-1),
						new Word("start",Tag.PRE,-1)
						);
				error("Esperava encontrar o s�mbolo de , ou ; no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
				if(lookahead.tag == Tag.IDE) continue; 
				else break;
			}
		}
		
	}
	
	
	public void attributeVector(String scope, String type) {
		while(true) {
			if(lookahead.tag == Tag.CAD) {
				if(typeDataTableSymbol.get(type) != Tag.CAD) 
					semanticErrors.add("Erro sem�ntico: alocagem de informa��o com o tipo de dado errado erro encontrado em:" + lookahead.line );
				match(lookahead, null, Tag.CAD);
			}
			else {
				if(typeDataTableSymbol.get(type) == Tag.CAD) {
					semanticErrors.add("Erro sem�ntico: alocagem de informa��o com o tipo de dado errado erro encontrado em:" + lookahead.line);
					expression(scope,false);	
					lookExpressionData.clear();
					lookExpressionVariable.clear();
				}else {
					expression(scope,false);	
					boolean isOnlyType = lookExpression(typeDataTableSymbol.get(type),false);
					if(!isOnlyType) semanticErrors.add("Erro sem�ntico: alocagem de informa��o com o tipo de dado errado erro encontrado em:" + lookahead.line);
				}
			}
			if(getLexeme(lookahead).equals(",")) {
				match(lookahead,",",Tag.DEL);
			}
			else break;
		}
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead,"}",Tag.DEL);
		}
	}
	
	public void attributeMatrix(String scope,String type) {
		while(true) {
			if(getLexeme(lookahead).equals("{")) {
				match(lookahead,"{",Tag.DEL);
				attributeVector(scope,type);
			}
			if(getLexeme(lookahead).equals(",")) {
				match(lookahead,",",Tag.DEL);
			}
			else break;
		}
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead,"}",Tag.DEL);
		}
	}
	
	public void analyseFunctionAndProcedureDeclaration() {
		String name = "";
		boolean alreadyExist;
		while(true) {
			alreadyExist = false;
			if(getLexeme(lookahead).equals("function") && lookahead.tag == Tag.PRE) {
				match(lookahead,"function",Tag.PRE);
				TabelaFuncao function = new TabelaFuncao();
				function.setProcedure(false);
				if(typeDataTableSymbol.containsKey(getLexeme(lookahead))){
					function.setType(getLexeme(lookahead));
					match(lookahead,getLexeme(lookahead),lookahead.tag);
				}else {
					if(lookahead.tag == Tag.IDE) { //ignora o identificador que foi passado como retorno da fun��o
						match(lookahead,null,Tag.IDE);
					}
					List<Token> symbSyncronization = Arrays.asList(
							new Word(" ",Tag.IDE,-1));
					function.setProcedure(true);
					System.out.println("Erro no tipo do retorno ");
					error("Erro Sint�tico/Sem�ntico: Esperava encontrar um tipo existente no processo de atribui��o do retorno da fun��o, na linha " + lookahead.line, symbSyncronization);
				}
				if(lookahead.tag == Tag.IDE) {
					name = getLexeme(lookahead);
					match(lookahead,null,Tag.IDE);
					function.setName(name);
					if(functionTableSymbol.contains(new TabelaFuncao("", name, "", false))) {
						alreadyExist = true;
						//functionTableSymbol.put("func_" + name, new FunctionTableSymbol("func_" + name, function.getName(), function.getType(), function.isProcedure()));
					}
					/*else {
						System.out.println("j� existe fun��o com esse nome");
					}*/
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("(",Tag.DEL,-1),
							new Word(" ",Tag.IDE,-1)
							);
					error("Esperava encontrar um identificador no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
				}
				if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
					match(lookahead,"(",Tag.DEL);
				}
				paramsList(function);
				//function.getParameters().forEach((key,value)-> System.out.println("key: " + key + " " + "v: " + value));
				if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
					//FunctionTableSymbol aux = functionTableSymbol.get("func_" + name);
					if(!alreadyExist) {
						TabelaFuncao save = new TabelaFuncao("func_" + name, function.getName(), function.getType(), function.isProcedure());
						save.setParameters(function.getParameters());
						functionTableSymbol.put("func_" + name, save);
					}else {
						TabelaFuncao aux = functionTableSymbol.get("func_" + name);
						if(!function.getParameters().equals(aux.getParameters())) {
							Random gerador = new Random();
							TabelaFuncao save = new TabelaFuncao("func_" + name, function.getName(), function.getType(), function.isProcedure());
							save.setParameters(function.getParameters());
							functionTableSymbol.put("func_" + name + "_" + gerador.nextInt(), save);
						}else {
							semanticErrors.add("Erro sem�ntico: n�o � permitido ter uma fun��o com o mesmo nome e par�etros de uma outra fun��o que j� existe erro encontrado em:" + lookahead.line);
						}
					}
					//aux.setParameters(function.getParameters());
					//functionTableSymbol.replace("func_" + name, aux);
					//match(lookahead,")",Tag.DEL);
				}
				if(getLexeme(lookahead).equals("{") && lookahead.tag == Tag.DEL) {
					match(lookahead,"{",Tag.DEL);
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("var",Tag.PRE,-1),
							new Word("if",Tag.PRE,-1),
							new Word("while",Tag.PRE,-1),
							new Word("global",Tag.PRE,-1),
							new Word("local",Tag.PRE,-1),
							new Word("read",Tag.PRE,-1),
							new Word("print",Tag.PRE,-1),
							new Word("return",Tag.PRE,-1)
							);
					error("Esperava encontrar { na linha " + lookahead.line, symbSyncronization);
				}
				body(false,function.getName());
				if(getLexeme(lookahead).equals("}") && lookahead.tag == Tag.DEL) {
					match(lookahead,"}",Tag.DEL);
				}
			}
			
			
			else if(getLexeme(lookahead).equals("procedure") && lookahead.tag == Tag.PRE) {
				TabelaFuncao procedure = new TabelaFuncao();
				match(lookahead,"procedure",Tag.PRE);
				procedure.setProcedure(true);
				procedure.setType("void");
				if(getLexeme(lookahead).equals("start") && lookahead.tag == Tag.PRE) {
					analyseFuncionStart();
				}
				else {
				if(lookahead.tag == Tag.IDE) {
					name = getLexeme(lookahead);
					procedure.setName(name);
					if(!functionTableSymbol.contains(new TabelaFuncao("", name, "", true))) {
						functionTableSymbol.put("proc_" + name, new TabelaFuncao("proc_" + name, procedure.getName(), procedure.getType(), procedure.isProcedure()));
					}else
						semanticErrors.add("J� existe procedure com esse nome, erro encontrado na linha:" + lookahead.line);
					match(lookahead,null,Tag.IDE);
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("(",Tag.DEL,-1),
							new Word(" ",Tag.IDE,-1)
							);
					error("Esperava encontrar um identificador no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
				}
				if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
					match(lookahead,"(",Tag.DEL);
				}
				paramsList(procedure);
				//procedure.getParameters().forEach((key,value)-> System.out.println("key: " + key + " " + "v: " + value));
				if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
					TabelaFuncao aux = functionTableSymbol.get("proc_" + name);
					aux.setParameters(procedure.getParameters());
					functionTableSymbol.replace("proc_" + name, aux);
					match(lookahead,")",Tag.DEL);
				}
				if(getLexeme(lookahead).equals("{") && lookahead.tag == Tag.DEL) {
					match(lookahead,"{",Tag.DEL);
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("var",Tag.PRE,-1),
							new Word("if",Tag.PRE,-1),
							new Word("while",Tag.PRE,-1),
							new Word("global",Tag.PRE,-1),
							new Word("local",Tag.PRE,-1),
							new Word("read",Tag.PRE,-1),
							new Word("print",Tag.PRE,-1),
							new Word("return",Tag.PRE,-1)
							);
					error("Esperava encontrar { na linha " + lookahead.line, symbSyncronization);
				}
				body(true,procedure.getName());
				if(getLexeme(lookahead).equals("}") && lookahead.tag == Tag.DEL) {
					match(lookahead,"}",Tag.DEL);
				}
			}
			}
			else return;
		}
	}
	
	public void paramsList(TabelaFuncao function) {
		List<String> keyWords = Arrays.asList("int","string","real","boolean");
		String type="", name="";
		boolean error = false;
		while(true) {
			if(keyWords.contains(getLexeme(lookahead))){
				int indexKeyWord = keyWords.indexOf(getLexeme(lookahead));
				type = keyWords.get(indexKeyWord);
				match(lookahead,keyWords.get(indexKeyWord),Tag.PRE);
			}else {
				if(getLexeme(lookahead).equals(")")) return;
				error = true;
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1));
				error("Esperava encontrar int,real,string,boolean  na linha " + lookahead.line, symbSyncronization);
			}
			if(lookahead.tag == Tag.IDE) {
				name = getLexeme(lookahead);
				if(!function.getParameters().containsKey(name))
					function.addParameter(type, name);
				else
					System.out.println("Ja salvou");
				match(lookahead,null,Tag.IDE);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word(",",Tag.DEL,-1),
						new Word(")",Tag.DEL,-1),
						new Word("{",Tag.DEL,-1)
						);
				error("Esperava encontrar int,real,string,boolean na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals(",") && lookahead.tag == Tag.DEL) {
				match(lookahead,",",Tag.DEL);
			}
			else if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL){
				return;
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word("var",Tag.PRE,-1),
						new Word("if",Tag.PRE,-1),
						new Word("while",Tag.PRE,-1),
						new Word("global",Tag.PRE,-1),
						new Word("local",Tag.PRE,-1),
						new Word("read",Tag.PRE,-1),
						new Word("print",Tag.PRE,-1),
						new Word("return",Tag.PRE,-1),
						new Word("{",Tag.DEL,-1)
						);
				error("Esperava encontrar int,real,string,boolean na linha " + lookahead.line, symbSyncronization);
				break;
			}
		}
		//System.out.println(function.getName() + " " + function.getParameters().size());
	}
	
	
	public void body(boolean isProcedure, String nameFunction) {
		//System.out.println(nameFunction + " " + getLexeme(lookahead));
		while(true) {
			//System.out.println(getLexeme(lookahead));
			if(lookahead.tag == Tag.END) {
				error("Esperava encontrar uma } para finalizar o start", null);
				break;
			}
			if(getLexeme(lookahead).equals("var")) {
				analyseVar(nameFunction);
			}
			else if(getLexeme(lookahead).equals("read")) {
				analyseRead();
			}
			else if(getLexeme(lookahead).equals("print")) {
				analysePrint(nameFunction);
			}
			else if(getLexeme(lookahead).equals("procedure")) {
				match(lookahead,"procedure",Tag.PRE);
				if(getLexeme(lookahead).equals(".")) {
					match(lookahead,".",Tag.DEL);
					if(lookahead.tag == Tag.IDE) {
						String function = getLexeme(lookahead);
						match(lookahead,null,Tag.IDE);
						if(getLexeme(lookahead).equals("(")) {
							functionCall(nameFunction,function,true);
						}
					}else {
						List<Token> symbSyncronization = Arrays.asList(
								new Word(";",Tag.IDE,-1)
						);
						error("Esperava encontrar um identificador na linha " + lookahead.line, symbSyncronization);
					}
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(" ",Tag.IDE,-1)
					);
					error("Esperava encontrar um . na linha " + lookahead.line, symbSyncronization);
				}
			}
			else if(lookahead.tag == Tag.IDE ||getLexeme(lookahead).equals("global") || getLexeme(lookahead).equals("local")) {
				//System.out.println(nameFunction);
				assign(nameFunction);
			}
			else if(getLexeme(lookahead).equals("if")) {
				analyseIfElse(isProcedure, nameFunction);
			}
			else if(getLexeme(lookahead).equals("while")) {
				analyseWhile(isProcedure,nameFunction);
			}
			else if(getLexeme(lookahead).equals("return")) {
				if(!isProcedure) {
					TabelaFuncao function = functionTableSymbol.get("func_" + nameFunction);
					match(lookahead,"return", Tag.PRE);
					if(function.getType() != "string") {
						expression(nameFunction,false);
						if(function != null && !function.isProcedure()) {
							//lookExpressionVariable.forEach(element -> System.out.println(element.getName() + " "+ element.getScope() + " " + element.getTypeData()));
							//lookExpressionData.forEach(element -> System.out.println(element.tag + " " + element.line));
							boolean isOnlyOneType = lookExpression(typeDataTableSymbol.get(function.getType()),false);
							if(!isOnlyOneType) semanticErrors.add("N�o retorna o tipo de dado definido pela fun��o: " + function.getName()+ ", erro encontrado em:" + lookahead.line );
						}else {
							semanticErrors.add("Erro sem�ntico: O tipo do retorno est� incorreto erro encontrado em:" + lookahead.line);
						}
					}else if(function.getType() == "string" && lookahead.tag != Tag.CAD) {
						expression(nameFunction,false);
						lookExpression(typeDataTableSymbol.get(function.getType()),false);
						semanticErrors.add("N�o retorna o tipo de dado definido pela fun��o, erro encontrado em:" + lookahead.line);
						
					}
					if(getLexeme(lookahead).equals(";")) {
						match(lookahead,";",Tag.DEL);
					}
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(";",Tag.DEL,-1)
					);
					error("Procedure n�o pode ter return ( linha " + lookahead.line + " )", symbSyncronization);
					if(getLexeme(lookahead).equals(";")) {
						match(lookahead,";",Tag.DEL);
					}
				}
			}
			else if(getLexeme(lookahead).equals("}")) {
				break;
			}else {
				if(index == tokens.size() - 1) {
					error("Esperava encontrar uma } na linha " + lookahead.line, null);
					break;
				}
				else if(getLexeme(lookahead).equals("else")) {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("then",Tag.PRE,-1),
							new Word(" ",Tag.NRO,-1),
							new Word(" ",Tag.IDE,-1),
							new Word(" ",Tag.CAD,-1),
							new Word("var",Tag.PRE,-1),
							new Word("if",Tag.PRE,-1),
							new Word("while",Tag.PRE,-1),
							new Word("global",Tag.PRE,-1),
							new Word("local",Tag.PRE,-1),
							new Word("read",Tag.PRE,-1),
							new Word("print",Tag.PRE,-1),
							new Word("return",Tag.PRE,-1),
							new Word("}",Tag.DEL,-1),
							new Word("int",Tag.PRE,-1),
							new Word("boolean",Tag.PRE,-1),
							new Word("real",Tag.PRE,-1),
							new Word("string",Tag.PRE,-1),
							new Word("struct",Tag.PRE,-1),
							new Word("function",Tag.PRE,-1),
							new Word("var",Tag.PRE,-1),
							new Word("procedure",Tag.PRE,-1),
							new Word("struct",Tag.PRE,-1),
							new Word("start",Tag.PRE,-1));
					error("Esperava encontrar uma } depois na linha " + lookahead.line, symbSyncronization);	
				}
				else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word("int",Tag.PRE,-1),
							new Word("boolean",Tag.PRE,-1),
							new Word("real",Tag.PRE,-1),
							new Word("string",Tag.PRE,-1),
							new Word("struct",Tag.PRE,-1),
							new Word("function",Tag.PRE,-1),
							new Word("var",Tag.PRE,-1),
							new Word("procedure",Tag.PRE,-1),
							new Word("struct",Tag.PRE,-1),
							new Word("start",Tag.PRE,-1));
					error("Esperava encontrar uma } depois na linha " + lookahead.line, symbSyncronization);
					break;
				}
			}
		}
	}
	
	
	public void analyseIfElse(boolean isProcedure,String scope) {
		if(getLexeme(lookahead).equals("if")) {
			match(lookahead,"if",Tag.PRE);
		}
		if(getLexeme(lookahead).equals("(")) {
			match(lookahead,"(",Tag.DEL);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("!",Tag.LOG,-1),
					new Word(" ",Tag.NRO,-1),
					new Word(" ",Tag.IDE,-1),
					new Word(" ",Tag.CAD,-1)
			);
			error("Esperava encontrar um ( na linha " + lookahead.line, symbSyncronization);
		}
		logicalExpression(scope);
		if(getLexeme(lookahead).equals(")")) {
			match(lookahead,")",Tag.DEL);
		}else {
			//verificar
			List<Token> symbSyncronization = Arrays.asList(
					new Word("then",Tag.PRE,-1),
					new Word("{",Tag.DEL,-1),
					new Word(" ",Tag.IDE,-1),
					new Word(" ",Tag.CAD,-1),
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
			);
			error("Esperava encontrar um ) na linha " + lookahead.line, symbSyncronization);
		}
		if(getLexeme(lookahead).equals("then")) {
			match(lookahead,"then",Tag.PRE);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ",Tag.IDE,-1),
					new Word("{",Tag.DEL,-1),
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
			);
			error("Esperava encontrar um then na linha " + lookahead.line, symbSyncronization);
		}
		if(getLexeme(lookahead).equals("{")) {
			match(lookahead,"{",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ",Tag.IDE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
			);
			error("Esperava encontrar um { na linha " + lookahead.line, symbSyncronization);
		}
		body(isProcedure,"if");
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead,"}",Tag.DEL);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ",Tag.IDE,-1),
					new Word("var",Tag.PRE,-1),
					new Word("if",Tag.PRE,-1),
					new Word("while",Tag.PRE,-1),
					new Word("global",Tag.PRE,-1),
					new Word("local",Tag.PRE,-1),
					new Word("read",Tag.PRE,-1),
					new Word("print",Tag.PRE,-1),
					new Word("return",Tag.PRE,-1),
					new Word("else",Tag.PRE,-1),
					new Word("}",Tag.DEL,-1)
			);
			error("Esperava encontrar um } na linha " + lookahead.line, symbSyncronization);
		}
		if(getLexeme(lookahead).equals("else")){
			match(lookahead,"else",Tag.PRE);
			if(getLexeme(lookahead).equals("{")) {
				match(lookahead,"{",Tag.DEL);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word("var",Tag.PRE,-1),
						new Word("if",Tag.PRE,-1),
						new Word("while",Tag.PRE,-1),
						new Word("global",Tag.PRE,-1),
						new Word("local",Tag.PRE,-1),
						new Word("read",Tag.PRE,-1),
						new Word("print",Tag.PRE,-1),
						new Word("return",Tag.PRE,-1),
						new Word("}",Tag.DEL,-1)
				);
				error("Esperava encontrar um { na linha " + lookahead.line, symbSyncronization);
			}
			body(isProcedure,"else");
			if(getLexeme(lookahead).equals("}")) {
				match(lookahead,"}",Tag.DEL);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word("var",Tag.PRE,-1),
						new Word("if",Tag.PRE,-1),
						new Word("while",Tag.PRE,-1),
						new Word("global",Tag.PRE,-1),
						new Word("local",Tag.PRE,-1),
						new Word("read",Tag.PRE,-1),
						new Word("print",Tag.PRE,-1),
						new Word("return",Tag.PRE,-1),
						new Word("else",Tag.PRE,-1),
						new Word("}",Tag.DEL,-1)
				);
				error("Esperava encontrar um } na linha " + lookahead.line, symbSyncronization);
			}
		}
	}
	
	public void analyseWhile(boolean isProcedure, String scope) {
		if(getLexeme(lookahead).equals("while")) {
			match(lookahead, "while", Tag.PRE);
		}
		if(getLexeme(lookahead).equals("(")) {
			match(lookahead, "(", Tag.DEL);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ", Tag.IDE, -1),
					new Word(" ", Tag.NRO, -1),
					new Word(")", Tag.DEL, -1),
					new Word("{", Tag.DEL, -1)
					);
			error("Esperava encontrar um ( na linha " + lookahead.line, symbSyncronization);
		}
		
		logicalExpression(scope);
		if(getLexeme(lookahead).equals(")")) {
			match(lookahead, ")", Tag.DEL);
			
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("{", Tag.DEL, -1),
					new Word(" ", Tag.IDE, -1),
					new Word("}", Tag.DEL, -1)
					);
			error("Esperava encontrar um ) na linha " + lookahead.line, symbSyncronization);
		}
		if(getLexeme(lookahead).equals("{")) {
			match(lookahead, "{", Tag.DEL);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ", Tag.IDE, -1),
					new Word("if", Tag.PRE, -1),
					new Word("while", Tag.PRE, -1),
					new Word("print", Tag.PRE, -1),
					new Word("read", Tag.PRE, -1),
					new Word("}", Tag.DEL, -1)
					);
			error("Esperava encontrar um { na linha " + lookahead.line, symbSyncronization);
		}
		
		body(isProcedure,"while");
		
		if(getLexeme(lookahead).equals("}")) {
			match(lookahead, "}", Tag.DEL);
		}
	}
	
	
	
	public void functionCall(String calledBy, String nameFunction, boolean isProcedure){
		List<TabelaFuncao> possibleFunctions = new ArrayList<TabelaFuncao>();
		boolean isOnlyOneType = true; 
		TabelaFuncao function; //referencia para a fun��o
		Object typesOfParams[] = null; //referencia para os parametros da fun��o
		nameFunction = isProcedure ? "proc_"+ nameFunction : "func_" + nameFunction;
		calledBy = isProcedure ? "proc_"+ calledBy : "func_" + calledBy;
		//verfica se existe um registro de uma fun��o que contenha o nome passado como parametro na tabela de simbolos
		if(functionTableSymbol.containsKey(nameFunction)) {
			//se estiver executa normalmente
			function = functionTableSymbol.get(nameFunction);
		} else {
			function = null;
			semanticErrors.add("N�o existe essa fun��o " + nameFunction + "Erro encontrado na linha:" + lookahead.line);
		}
		if(getLexeme(lookahead).equals("(") && lookahead.tag == Tag.DEL) {
			match(lookahead,"(",Tag.DEL);
			//verifica se a fun��o tem ou n�o parametros
			if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
				if(function != null) {
					if(!function.getParameters().isEmpty()) semanticErrors.add("Falta colocar os parametros, erro encontrado na linha:" + lookahead.line);
				}
				match(lookahead,")",Tag.DEL);
			}else {
				int indexParam = 0;
				if(function != null) {
					typesOfParams = functionTableSymbol.get(nameFunction).getParameters().values().toArray();
					//verifica se a passagem de parametros esta correta
					
					while(true){
						if(typeDataTableSymbol.get(typesOfParams[indexParam].toString()) == Tag.CAD) {
							if(lookahead.tag != Tag.CAD) {
								semanticErrors.add("Erro Sem�ntico: par�metros com tipos diferentes " + nameFunction + "erro encontrado em:" + lookahead.line);
								 expression(calledBy,true);
								 if(lookExpressionData.size() - 1 != indexlookExpressionData && indexlookExpressionData != -1) {
										lookExpressionData.subList(indexlookExpressionData+1, lookExpressionData.size()).clear();
												 }else if(indexlookExpressionData == -1) {
										lookExpressionData.clear();
								 }
								 if(lookExpressionVariable.size() - 1 != indexlookExpressionVariable && indexlookExpressionVariable != -1) {
										lookExpressionVariable.subList(indexlookExpressionVariable+1, lookExpressionVariable.size()).clear();
											 }
								 else if(indexlookExpressionVariable == -1) {
										lookExpressionVariable.clear();
								}
								indexlookExpressionData = 0;
								indexlookExpressionVariable = 0;
							}else {
								match(lookahead,null,Tag.CAD);
							}
						}else {
							if(lookahead.tag == Tag.CAD) {
								semanticErrors.add("Erro Sem�ntico: par�metros com tipos diferentes " + nameFunction + "erro encontrado em:" + lookahead.line);
								match(lookahead,null,Tag.CAD);
							}
							else{
								expression(calledBy,true);
								isOnlyOneType = lookExpression(typeDataTableSymbol.get(typesOfParams[indexParam].toString()),true);
								if(!isOnlyOneType)semanticErrors.add("Erro Sem�ntico: par�metros com tipos diferentes (" + nameFunction + "erro encontrado em:" + lookahead.line); 
							}
						}
						if(getLexeme(lookahead).equals(",") && lookahead.tag == Tag.DEL) {
							match(lookahead,",",Tag.DEL);
							indexParam++;
						}
						else if(getLexeme(lookahead).equals(")") && lookahead.tag == Tag.DEL) {
							match(lookahead,")",Tag.DEL);
							break;
						}else {
							//tokens de sincroniza��o para o tratamento de erro sintatico
							List<Token> symbSyncronization = Arrays.asList(
									new Word("=",Tag.REL,-1),
									new Word(";",Tag.DEL,-1),
									new Word(",",Tag.DEL,-1),
									new Word("+",Tag.ART,-1),
									new Word("-",Tag.ART,-1),
									new Word("/",Tag.ART,-1),
									new Word("*",Tag.ART,-1),
									new Word(">",Tag.REL,-1),
									new Word("<",Tag.REL,-1),
									new Word(">=",Tag.REL,-1),
									new Word("<=",Tag.REL,-1),
									new Word("==",Tag.REL,-1),
									new Word("!=",Tag.REL,-1),
									new Word("&&",Tag.LOG,-1),
									new Word("||",Tag.LOG,-1));
							error("Esperava encontrar um , ou ) na linha " + lookahead.line, symbSyncronization);
							break;
						}
					}
					if(indexParam != typesOfParams.length - 1)
						semanticErrors.add("Erro! Par�metros ausentes " + nameFunction + " " + lookahead.line + function.getParameters().size());
					if(indexParam > typesOfParams.length + 1)
						semanticErrors.add("Erro! Par�metros que n�o  " + nameFunction + " " + lookahead.line + function.getParameters().size());
					if(!function.isProcedure())
						lookExpressionVariable.add(new TabelaVariavel("", function.getName(), "function", true, calledBy, function.getType()));
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(")", Tag.DEL, -1));
					error(" N�o existe a fun��o", symbSyncronization);
					match(lookahead,")",Tag.DEL);
				}
			}
		}
	}
	
	public void prefixGlobalOrLocal(String scope, boolean isToAssign) {
		boolean isGlobal = false;
		if(getLexeme(lookahead).equals("global") && lookahead.tag == Tag.PRE) {
			isGlobal = true;
			match(lookahead,"global",Tag.PRE);
		}
		else if(getLexeme(lookahead).equals("local") && lookahead.tag == Tag.PRE) {
			isGlobal = false;
			match(lookahead,"local",Tag.PRE);
		}
		if(getLexeme(lookahead).equals(".") && lookahead.tag == Tag.DEL) {
			match(lookahead,".",Tag.DEL);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word(" ",Tag.IDE,-1));
			error("Esperava encontrar um . depois do global ou local na linha " + lookahead.line, symbSyncronization);
		}
		if(lookahead.tag == Tag.IDE) {
			if(isGlobal) {
				if(!symbolTable.containsKey("global" + "_" + getLexeme(lookahead)))
					semanticErrors.add("N�o existe essa variavel: " + getLexeme(lookahead) + ", erro encontrado na linha: " + lookahead.line);
				else {
					if(isToAssign)
						variableAssign = symbolTable.get("global" + "_" + getLexeme(lookahead));	
					else
						lookExpressionVariable.add(symbolTable.get("global" + "_" + getLexeme(lookahead)));
				}
			}else {
				if(!symbolTable.containsKey(scope + "_" + getLexeme(lookahead)))
					semanticErrors.add("N�o existe essa variavel: " + getLexeme(lookahead) + ", erro encontrado na linha: " + lookahead.line);
				else {
					if(isToAssign)
						variableAssign = symbolTable.get("global" + "_" + getLexeme(lookahead));	
					else
						lookExpressionVariable.add(symbolTable.get("global" + "_" + getLexeme(lookahead)));
				}
			}
			match(lookahead,null,Tag.IDE);
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("=",Tag.REL,-1),
					new Word(";",Tag.DEL,-1),
					new Word(")",Tag.DEL,-1),
					new Word("+",Tag.ART,-1),
					new Word("-",Tag.ART,-1),
					new Word("/",Tag.ART,-1),
					new Word("*",Tag.ART,-1),
					new Word(">",Tag.REL,-1),
					new Word("<",Tag.REL,-1),
					new Word(">=",Tag.REL,-1),
					new Word("<=",Tag.REL,-1),
					new Word("==",Tag.REL,-1),
					new Word("!=",Tag.REL,-1)
					);
			error("Esperava encontrar um identificador depois do . na linha " + lookahead.line, symbSyncronization);
		}
	}
	
	public void prefixStruct(String nameVariable, String scope, boolean isAssign) {
		TabelaVariavel structVariable = null;
		if(symbolTable.containsKey(scope + "_" + nameVariable)) {
			structVariable = symbolTable.get(scope + "_" + nameVariable);
		}else if(symbolTable.containsKey("global" + "_" + nameVariable)){ //verifica se uma variavel global
			structVariable = symbolTable.get("global" + "_" + nameVariable);
		}else if(functionTableSymbol.containsKey("func_" + scope)) { //verifica se  um parametro da function
			if(functionTableSymbol.get("func_" + scope).getParameters().containsKey(nameVariable)) {
				String type = functionTableSymbol.get("func_" + scope).getParameters().get(nameVariable);
				structVariable =  new TabelaVariavel("", scope, "param", true, scope, type);
			}
		}else if(functionTableSymbol.containsKey("proc_" + scope)) {//verifica se  um parametro da procedure
			if(functionTableSymbol.get("proc_" + scope).getParameters().containsKey(nameVariable)) {
				String type = functionTableSymbol.get("proc_" + scope).getParameters().get(nameVariable);
				structVariable =  new TabelaVariavel("", scope, "param", true, scope, type);
			}
		}else {
	
			semanticErrors.add("N�o existe essa variavel: " + nameVariable + ", erro encontrado na linha: " + lookahead.line);
		}
		match(lookahead,".",Tag.DEL);
		if(lookahead.tag == Tag.IDE) {
			String attribute = getLexeme(lookahead);
			match(lookahead,null,Tag.IDE);
			if(isAssign && structVariable != null) {
				if(structTableSymbol.get(structVariable.getTypeData()).getAttributes().containsKey(attribute)) {
					String typeOfDataAttribute = structTableSymbol.get(structVariable.getTypeData()).getAttributes().get(attribute);
					variableAssign = new TabelaVariavel(scope + "_" + nameVariable + "." + attribute,scope + "_" + nameVariable + "." + attribute, "struct", true, scope,typeOfDataAttribute);
				}else {
					semanticErrors.add("Erro sem�ntico: n�o existe esse atributo nesta struct erro encontrado em:" + lookahead.line);
				}
			}else if(!isAssign && structVariable != null) {
				if(symbolTable.containsKey(scope + "_" + nameVariable + "." + attribute)) {
					lookExpressionVariable.add(symbolTable.get(scope + "_" + nameVariable + "." + attribute));
				}else {
					if(structTableSymbol.get(structVariable.getTypeData()).getAttributes().containsKey(attribute)) {
						String typeOfDataAttribute = structTableSymbol.get(structVariable.getTypeData()).getAttributes().get(attribute);
						lookExpressionVariable.add(new TabelaVariavel(scope + "_" + nameVariable + "." + attribute,scope + "_" + nameVariable + "." + attribute, "struct", false, scope,typeOfDataAttribute));
					}else {
						semanticErrors.add("Erro sem�ntico: n�o existe esse atributo na struct escolhida, erro encontrado na linha:" + lookahead.line);
					}
				}
			}
		}else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("=",Tag.REL,-1),
					new Word(";",Tag.DEL,-1),
					new Word(",",Tag.DEL,-1),
					new Word(")",Tag.DEL,-1),
					new Word("+",Tag.ART,-1),
					new Word("-",Tag.ART,-1),
					new Word("/",Tag.ART,-1),
					new Word("*",Tag.ART,-1),
					new Word(">",Tag.REL,-1),
					new Word("<",Tag.REL,-1),
					new Word(">=",Tag.REL,-1),
					new Word("<=",Tag.REL,-1),
					new Word("==",Tag.REL,-1),
					new Word("!=",Tag.REL,-1),
					new Word("&&",Tag.LOG,-1),
					new Word("||",Tag.LOG,-1)
					);
			error("Esperava encontrar um identificador na linha " + lookahead.line, symbSyncronization);
		}
	}
	
	public void value(String scope) {
		if(getLexeme(lookahead).equals("global") || getLexeme(lookahead).equals("local")) {
			prefixGlobalOrLocal(scope,false);
		}
		else if(lookahead.tag == Tag.NRO){
			lookExpressionData.add(lookahead);
			match(lookahead,null,Tag.NRO);
		}
		else if(lookahead.tag == Tag.IDE) {
			String name = getLexeme(lookahead);
			match(lookahead,null,Tag.IDE);
			if(getLexeme(lookahead).equals("(")) {
				functionCall(scope, name, false);
				//FunctionTableSymbol function = functionTableSymbol.get("func_" + name);
			}
			else if(getLexeme(lookahead).equals(".")) {
				match(lookahead,null,Tag.IDE);
				prefixStruct(name,scope,false);
			}else {
				//verifica se uma variavel local
				String split[] = scope.split("_");
				if(split.length == 2){
					scope = split[1];
				}
				if(symbolTable.containsKey(scope + "_" + name)) {
					lookExpressionVariable.add(symbolTable.get(scope + "_" + name));
				}else if(symbolTable.containsKey("global" + "_" + name)){ //verifica se uma variavel global
					lookExpressionVariable.add(symbolTable.get("global" + "_" + name));
				}else if(functionTableSymbol.containsKey("func_" + scope)) { //verifica se um parametro da function
					if(functionTableSymbol.get("func_" + scope).getParameters().containsKey(name)) {
						String type = functionTableSymbol.get("func_" + scope).getParameters().get(name);
						lookExpressionVariable.add(new TabelaVariavel("", scope, "param", true, scope, type));
					}
				}else if(functionTableSymbol.containsKey("proc_" + scope)) {//verifica se um parametro da procedure
					if(functionTableSymbol.get("proc_" + scope).getParameters().containsKey(name)) {
						String type = functionTableSymbol.get("proc_" + scope).getParameters().get(name);
						lookExpressionVariable.add(new TabelaVariavel("", scope, "param", true, scope, type));
					}
				}else {
					semanticErrors.add("Variavel n�o existe" + name + " erro encontrado na linha" + lookahead.line);
					
				}
				match(lookahead,null,Tag.IDE);
			}
		}
		else if(getLexeme(lookahead).equals("(")) {
			match(lookahead,"(",Tag.DEL);
			expression(scope,false);
			if(getLexeme(lookahead).equals(")")) {
				match(lookahead,")",Tag.DEL);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(";",Tag.DEL,-1));
				error("Esperava encontrar um )b na linha " + lookahead.line, symbSyncronization);
			}
		}
		else if(getLexeme(lookahead).equals("true")) {
			lookExpressionData.add(lookahead);
			match(lookahead,"true",Tag.PRE);
		}
		else if(getLexeme(lookahead).equals("false")) {
			lookExpressionData.add(lookahead);
			match(lookahead,"false",Tag.PRE);
		}
		else {
			List<Token> symbSyncronization = Arrays.asList(
					new Word("=",Tag.REL,-1),
					new Word(";",Tag.DEL,-1),
					new Word(",",Tag.DEL,-1),
					new Word(")",Tag.DEL,-1),
					new Word("+",Tag.ART,-1),
					new Word("-",Tag.ART,-1),
					new Word("/",Tag.ART,-1),
					new Word("*",Tag.ART,-1),
					new Word(">",Tag.REL,-1),
					new Word("<",Tag.REL,-1),
					new Word(">=",Tag.REL,-1),
					new Word("<=",Tag.REL,-1),
					new Word("==",Tag.REL,-1),
					new Word("!=",Tag.REL,-1),
					new Word("&&",Tag.LOG,-1),
					new Word("||",Tag.LOG,-1)
					);
			error("Esperava encontrar um identificador,numero,cadeia de caracteres,true,false,global., local. ou - como valor dentro de uma express�o na linha " + lookahead.line, symbSyncronization);
		}
	}
	
	public void multExp() {
		if(getLexeme(lookahead).equals("*")) {
			//lookExpression.add(lookahead);
			match(lookahead,"*",Tag.ART);
		}
		else if(getLexeme(lookahead).equals("/")) {
			//lookExpression.add(lookahead);
			match(lookahead,"/",Tag.ART);
		}
	}
	
	public void addExp(){
		if(getLexeme(lookahead).equals("+")) {
			//stack.addElement(lookahead);
			match(lookahead,"+",Tag.ART);
		}
		else if(getLexeme(lookahead).equals("-")){
			//stack.addElement(lookahead);
			match(lookahead,"-",Tag.ART);
		}
	}
	
	public void term() {
		if(getLexeme(lookahead).equals("/") ||  getLexeme(lookahead).equals("*")) {
			multExp();
		}
		else if(getLexeme(lookahead).equals("+") ||  getLexeme(lookahead).equals("-")) {
			addExp();
		}
	}
	
	public void relationalExpression(String scope) {
		if(lookahead.tag == Tag.CAD) {
			match(lookahead, null, Tag.CAD);
			if(getLexeme(lookahead).equals("==")) {
				match(lookahead,"==", Tag.REL);
				if(lookahead.tag == Tag.CAD) {
					match(lookahead, null, Tag.CAD);
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(")",Tag.DEL,-1),
							new Word(";",Tag.DEL,-1));
					error("Esperava encontrar uma cadeia de caracteres " + lookahead.line, symbSyncronization);	
				}
			}
			else if(getLexeme(lookahead).equals("!")) {
				match(lookahead,"!", Tag.LOG);
				if(getLexeme(lookahead).equals("=")) {
					match(lookahead,"=", Tag.REL);
					if(lookahead.tag == Tag.CAD) {
						match(lookahead, null, Tag.CAD);
					}else {
						List<Token> symbSyncronization = Arrays.asList(
								new Word(")",Tag.DEL,-1),
								new Word(";",Tag.DEL,-1));
						error("Esperava encontrar uma cadeia de caracteres " + lookahead.line, symbSyncronization);	
					}
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(" ",Tag.IDE,-1),
							new Word(" ",Tag.NRO,-1));
					error("Esperava encontrar um = " + lookahead.line, symbSyncronization);
				}
			}
		}else {
			relational(scope);
			expression(scope,false);
		}
	}
	
	public void relational(String scope) {
		switch(getLexeme(lookahead)){
			case ">":
			case ">=":
			case "<":
			case "<=":
			case "==":
				match(lookahead,null, Tag.REL);
				break;
			default:
				if(getLexeme(lookahead).equals("!")) {
					match(lookahead,"!", Tag.LOG);
					if(getLexeme(lookahead).equals("=")) {
						match(lookahead,"=", Tag.REL);
					}else {
						List<Token> symbSyncronization = Arrays.asList(
								new Word(" ",Tag.IDE,-1),
								new Word(" ",Tag.NRO,-1));
						error("Esperava encontrar um = " + lookahead.line, symbSyncronization);
					}
				}else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word(" ",Tag.IDE,-1),
							new Word(" ",Tag.NRO,-1),
							new Word(")",Tag.DEL,-1));
					error("Esperava encontrar um == ou express�o logica na linha " + lookahead.line, symbSyncronization);
				}
		}
		expression(scope,false);
	}
	
	
	
	public void logicalOperator(String scope) {
		if(getLexeme(lookahead).equals("&&")){
			match(lookahead,"&&",Tag.LOG);
			if(lookahead.tag == Tag.CAD) relationalExpression(scope);
			else {
				expression(scope,false);
				relationalExpression(scope);
			}
		}
		else if(getLexeme(lookahead).equals("||")){
			match(lookahead,"||",Tag.LOG);
			if(lookahead.tag == Tag.CAD) relationalExpression(scope);
			else {
				expression(scope,false);
				relationalExpression(scope);
			}
		}
	}
	
	public void attributeValue(String type, String scope) {
		TabelaVariavel symbol;
		boolean existType = typeDataTableSymbol.containsKey(type);
		while(true){
			symbol = new TabelaVariavel();
			if(lookahead.tag == Tag.IDE){
				if(symbolTable.contains(new TabelaVariavel("", getLexeme(lookahead), "const",false, scope, type)))
					semanticErrors.add("Erro Sem�ntico (const): J� existe essa vari�vel " + getLexeme(lookahead));
				else if(existType) 
					symbol.setName(getLexeme(lookahead));
				else
					semanticErrors.add("Erro Sem�ntico (const): tipo de vari�vel inexistente " + type);
				match(lookahead, null, Tag.IDE);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word("=",Tag.REL,-1),
						new Word(",",Tag.DEL,-1),
						new Word(";",Tag.DEL,-1));
				error("Esperava encontrar um identificador no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals("=")) {
				match(lookahead, "=", Tag.REL);
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.NRO,-1),
						new Word(" ",Tag.CAD,-1),
						new Word("true",Tag.PRE,-1),
						new Word("false",Tag.PRE,-1));
				error("Esperava encontrar o simbolo de = no processo de atribuia��o, na linha " + lookahead.line, symbSyncronization);
			}
			if(lookahead.tag == Tag.NRO || lookahead.tag == Tag.IDE || lookahead.tag == Tag.PRE || getLexeme(lookahead).equals("-")) {
				if(existType) {
					expression(scope,false);
					boolean isOnlyType = lookExpression(typeDataTableSymbol.get(type),false);
					if(!isOnlyType)
						semanticErrors.add("Erro Sem�ntico (const): atribui��o incorreta" + lookahead.line);
				}else {
					expression(scope,false);
					lookExpressionData.clear();
					lookExpressionVariable.clear();
				}
			}
			else if(lookahead.tag == Tag.CAD) {
				if(existType) {
					if(typeDataTableSymbol.get(type) != Tag.CAD)
						semanticErrors.add("Erro Sem�ntico (const): atribui��o incorreta" + lookahead.line);
					match(lookahead,null,Tag.CAD);
				}else
					match(lookahead,null,Tag.CAD);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.NRO,-1),
						new Word(";",Tag.DEL,-1),
						new Word(",",Tag.DEL,-1));
				error("Esperava encontrar um numero,identificador,true,false ou uma cadeia de caracteres no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
			}
			if(symbol.getName() != null)
				symbolTable.put(scope + "_" + symbol.getName(), new TabelaVariavel(scope + "_" + symbol.getName(), symbol.getName(), "const", true, scope, type));
			if(getLexeme(lookahead).equals(",")) {
				match(lookahead,",",Tag.DEL);
			}
			else if(getLexeme(lookahead).equals(";")) {
				match(lookahead,";",Tag.DEL);
				return;
			}else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word("int",Tag.PRE,-1),
						new Word("boolean",Tag.PRE,-1),
						new Word("real",Tag.PRE,-1),
						new Word("string",Tag.PRE,-1),
						new Word("struct",Tag.PRE,-1),
						new Word("}",Tag.DEL,-1),
						new Word("function",Tag.PRE,-1),
						new Word("var",Tag.PRE,-1),
						new Word("procedure",Tag.PRE,-1),
						new Word("struct",Tag.PRE,-1),
						new Word("start",Tag.PRE,-1)
						);
				error("Esperava encontrar o s�mbolo de , ou ; no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
				if(lookahead.tag == Tag.IDE) continue; 
				else break;
			}
		}
	}
	
	public void attributeValueVar(String type, String scope) {
		TabelaVariavel symbol;
		boolean existType = typeDataTableSymbol.containsKey(type);
		while(true) {
			symbol = new TabelaVariavel();
			symbol.setInitialized(false);
			if(lookahead.tag == Tag.IDE){
				//serve para verificar se o var global e que n�o exista uma const com o mesmo nome
				boolean verify =  scope.equals("global") ? symbolTable.contains(new TabelaVariavel("",getLexeme(lookahead),"const", false, "global", type)) : false;
				boolean check = symbolTable.contains(new TabelaVariavel("",getLexeme(lookahead),"var", false, scope, type));
				if(check || verify) {
					semanticErrors.add("Erro sem�ntico (var): j� exite variavel com esse nome, erro na linha:" + lookahead.line);
				}
				else if(existType) 
					symbol.setName(getLexeme(lookahead));
				else
					semanticErrors.add("Erro Sem�ntico (const): n�o existe esse tipo de variavel " + type + "erro encontrado na linha: "+ lookahead.line);
				match(lookahead, null, Tag.IDE);
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						//new Word (" ", Tag.IDE, -1),
						new Word ("=", Tag.REL, -1),
						new Word (",", Tag.DEL, -1),
						new Word ("[", Tag.DEL, -1),
						//new Word ("]", Tag.DEL, -1),
						new Word (";", Tag.DEL, -1)
						);
				error("Esperava encontrar um identificador no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
			}
			if(getLexeme(lookahead).equals("[")) {
				int count = 0;
				int dimension = 0;
				while(count < 2) {
					if(getLexeme(lookahead).equals("[")) {
						match(lookahead, "[", Tag.DEL);
						if(lookahead.tag == Tag.NRO || lookahead.tag == Tag.IDE) {
							expression(scope,false);
							boolean isOnlyType = lookExpression(Tag.NRO,false);
							if(!isOnlyType)semanticErrors.add("Erro sem�ntico: Na declara��o de vetores os indices s�o do tipo int, linha:" + lookahead.line);
						}
						else {
							List<Token> symbSyncronization = Arrays.asList(
									new Word (",", Tag.DEL, -1),
									new Word ("]", Tag.DEL, -1)
									);
							error("Esperava encontrar um identificador ou um n�mero no processo de atribui��o de vetor ou matriz, na linha: " + lookahead.line, symbSyncronization);
						}
						if(getLexeme(lookahead).equals("]")) {
							match(lookahead, "]", Tag.DEL);
							count++;
							dimension++;
						}
						else {
							List<Token> symbSyncronization = Arrays.asList(
									new Word (",", Tag.DEL, -1),
									new Word ("[", Tag.DEL, -1)
									);
							error("Esperava encontrar um ] processo deatribui��o de vetor ou matriz, na linha " + lookahead.line, symbSyncronization);
						}
					}
					else count = 2;
				}
				if(getLexeme(lookahead).equals("=")) {
					match(lookahead, "=", Tag.REL);
					if(getLexeme(lookahead).equals("{")) {
						match(lookahead, "{", Tag.DEL);
						if(getLexeme(lookahead).equals("{")) {
							if(dimension == 1) semanticErrors.add("Erro sem�ntico: dimens�o errada da aloca��o de mem�ria na linha:" + lookahead.line);
							attributeMatrix(scope,type);
						}else {
							if(dimension == 2) semanticErrors.add("Erro sem�ntico: dimens�o errada da aloca��o de mem�ria na linha:" + lookahead.line);
							attributeVector(scope,type);
						}
					}
				}
			}
			else if(getLexeme(lookahead).equals("=")) {
				match(lookahead, "=", Tag.REL);
				if(lookahead.tag == Tag.NRO || lookahead.tag == Tag.IDE || getLexeme(lookahead).equals("true") || getLexeme(lookahead).equals("false")) {
					if(existType) {
						expression(scope,false);
						symbol.setInitialized(true);
						boolean isOnlyType = lookExpression(typeDataTableSymbol.get(type),false);
						if(!isOnlyType)
							semanticErrors.add("Erro Sem�ntico (const): atribui��o incorreta na linha:" + lookahead.line);
					}else {
						expression(scope,false);
						lookExpressionData.clear();
						lookExpressionVariable.clear();
					}
				}
				else if(lookahead.tag == Tag.CAD) {
					if(existType) {
						symbol.setInitialized(true);
						if(typeDataTableSymbol.get(type) != Tag.CAD)
							semanticErrors.add("Erro Sem�ntico (const): atribui��o incorreta na linha:" + lookahead.line);
						match(lookahead,null,Tag.CAD);
					}else
						match(lookahead,null,Tag.CAD);
				}
				else {
					List<Token> symbSyncronization = Arrays.asList(
							new Word (",", Tag.DEL, -1),
							new Word (";", Tag.DEL, -1)
							);
					error("Esperava encontrar um n�mero, identificador, string ou um boolean no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
				}
				
			}
			if(symbol.getName() != null) {
				symbolTable.put(scope + "_" + symbol.getName(), new TabelaVariavel(scope + "_" + symbol.getName(), symbol.getName(), "var", symbol.isInitialized(), scope, type));
			}
			if(getLexeme(lookahead).equals(",")) {
				match(lookahead,",",Tag.DEL);
			}
			else if(getLexeme(lookahead).equals(";")) {
				match(lookahead,";",Tag.DEL);
				return;
			}
			else {
				List<Token> symbSyncronization = Arrays.asList(
						new Word(" ",Tag.IDE,-1),
						new Word("int",Tag.PRE,-1),
						new Word("boolean",Tag.PRE,-1),
						new Word("real",Tag.PRE,-1),
						new Word("string",Tag.PRE,-1),
						new Word("struct",Tag.PRE,-1),
						new Word("}",Tag.DEL,-1),
						new Word("function",Tag.PRE,-1),
						new Word("var",Tag.PRE,-1),
						new Word("procedure",Tag.PRE,-1),
						new Word("struct",Tag.PRE,-1),
						new Word("start",Tag.PRE,-1),
						new Word("if",Tag.PRE,-1),
						new Word("while",Tag.PRE,-1),
						new Word("print",Tag.PRE,-1),
						new Word("read",Tag.PRE,-1),
						new Word("global",Tag.PRE,-1),
						new Word("local",Tag.PRE,-1)
						);
				error("Esperava encontrar o s�mbolo de , ou ; no processo de atribui��o, na linha " + lookahead.line, symbSyncronization);
				if(lookahead.tag == Tag.IDE) continue; 
				else break;
			}
			
		}
	}
	
	
}