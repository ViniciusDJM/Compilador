package manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lexicAnalyser.Token;



/**
 *
 * @author Vin�cius Maciel e Ricardo Porto
 */

public class Files {

	/***
	 *
	 * @param path: O caminho do diretorio raiz
	 * @return A lista de arquivos que seguem a determina�ao do regex nos seus nomes
	 */
	static public File[] getFiles(String path) {
		File parentPath = new File(path);
		File[] files = parentPath.listFiles(file -> file.getName().matches("entrada\\d*.txt$"));
		return files;
	}
	
	static public List<String> getContentFile(File input) {
		List<String> lines = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(input))){ //try-catch com inicia��o
			String line = br.readLine(); //ler a linha do arquivo
			while(line != null){
				lines.add(line);
				line = br.readLine();					
			}
			br.close(); //fecha o arquivo
			return lines;
		}catch(IOException e){
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	static public void setContentFile(List<Token> tokens, List<String> syntaticErros, List<String> semanticErrors, String path) {
		List<String> lines = new ArrayList<String>();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))){ //try-catch com inicia��o
			for(Token token : tokens) {
				bw.write(token.toString());
			}
			if(!syntaticErros.isEmpty()) {
				bw.write("Erros sint�ticos : \n");
				bw.write("------------------------------------------------------------------------ \n");
				for(String erro : syntaticErros) {
					bw.write(erro + "\n");
				}
			}else {
				bw.write("Sem erros sint�ticos\n");
			}
			
			if(!semanticErrors.isEmpty()) {
				bw.write("Erros sem�nticos: \n"); 
				bw.write("------------------------------------------------------------------------ \n");
				for(String erro : semanticErrors) {
					bw.write(erro + "\n");
						}
			}else {
				bw.write("Sem erros sem�nticos \n");
			}
			bw.close(); //fecha o arquivo
			
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
	}
	
	static public int numberFile(String path){
		String number = path.replaceAll("[^0-9]","");
		return Integer.parseInt(number);
	}
	
}