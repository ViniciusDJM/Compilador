package lexicAnalyser;

import java.util.ArrayList;
import java.util.List;
 
public abstract class Armazenador {
    private String descricao;
    private int numLinha;
    private String nomeArquivo;
    private static final List<Armazenador> armazena = new ArrayList();
    
    
    public abstract String showElementos();
    
    public static final void addElemento(Armazenador armazena){
        Armazenador.armazena.add(armazena);
    }
    
    public static final void limparElementos(){
        Armazenador.armazena.clear();
    }


    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getNumLinha() {
        return numLinha;
    }

    public void setNumLinha(int linha) {
        this.numLinha = linha;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public static List<Armazenador> getElementos() {
        return armazena;
    }
    
    
}


