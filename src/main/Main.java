package main;


/**
 *
 * @author Vinícius Maciel e Ricardo Porto
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lexicAnalyser.LexicoR;
import lexicAnalyser.Token;
import manager.Files;
import sintaticoSemanticoAnalisador.SintaticoSemanticoAnalisador;

public class Main { 

	public static void main(String[] args) {
		List<Token> tokens = new ArrayList<Token>();
		//Pega os arquivos no diretório raíz
		File[] files =  Files.getFiles("C:\\Users\\vinyf\\eclipse-workspace\\VersaoFinalCompiladores\\entradas");// PROFESSOR, INSIRA O CAMINHO DO SEU DIRETORIO AQUI
		LexicoR analyser = new LexicoR();
		for(File file : files) {
			int numberFile = Files.numberFile(file.getName());
			List<String> contentFile = Files.getContentFile(file);
			tokens = analyser.analyseCode(contentFile);
			if(analyser.getQntErrors() == 0) {
				System.out.println("O arquivo " + file.getName() + " não possui erros léxicos");
				SintaticoSemanticoAnalisador syntatic = new SintaticoSemanticoAnalisador(tokens);
				syntatic.analyseCode();
				if(syntatic.getErros().isEmpty()) System.out.println("Sem erros sintáticos");
				Files.setContentFile(tokens, syntatic.getErros(), syntatic.getSemanticErrors(), "C:\\Users\\vinyf\\eclipse-workspace\\VersaoFinalCompiladores\\saidas\\saida" + numberFile+ ".txt"); // PROFESSOR, INSIRA O CAMINHO DO SEU DIRETORIO AQUI 
			}else {
				Files.setContentFile(tokens,null,null, "C:\\Users\\vinyf\\eclipse-workspace\\VersaoFinalCompiladores\\saidas\\saida" + numberFile+ ".txt"); // PROFESSOR, INSIRA O CAMINHO DO SEU DIRETORIO AQUI
				analyser.resetQntErrors();
			}
		}
	}

}
