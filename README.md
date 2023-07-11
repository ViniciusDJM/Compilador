<div id="inicio">
    <h1>Compilador em Java - Analisador Léxico, Sintático e Semântico</h1>
</div>

Este é um projeto de compilador que inclui um analisador léxico, sintático e semântico. O objetivo deste compilador é processar um arquivo de entrada, realizar a análise das estruturas léxicas, sintáticas e semânticas e fornecer uma saída correspondente.

<h2>Considerações Importantes</h2>
Antes de executar o compilador, por favor, leve em consideração as seguintes informações:

É necessário modificar o arquivo Main para especificar o caminho do arquivo de entrada e saída. No código-fonte do arquivo Main, você encontrará um comentário indicando onde essa alteração deve ser feita.

Durante a refatoração do código, algumas classes do analisador sintático foram retiradas e agrupadas em uma única classe chamada AnalisadorSintaticoSemantico. Essa mudança foi realizada com o objetivo de melhorar a qualidade do código e facilitar o entendimento.

<h2>Funcionalidades</h2>
O compilador possui as seguintes funcionalidades principais:

Analisador Léxico: Realiza a análise léxica do arquivo de entrada, identificando os tokens e suas categorias gramaticais. Ele é responsável por reconhecer e classificar as estruturas léxicas válidas.

Analisador Sintático: Realiza a análise sintática do arquivo de entrada, verificando a correta estrutura gramatical do código fonte. Ele garante que as sequências de tokens estejam de acordo com as regras gramaticais da linguagem.

Analisador Semântico: Realiza a análise semântica do arquivo de entrada, verificando se as instruções e estruturas do código fonte são semanticamente corretas. Ele verifica se as variáveis foram declaradas corretamente, se os tipos são compatíveis e se as operações são válidas.

<h2>Como Utilizar</h2>
Siga as instruções abaixo para utilizar o compilador:

Faça o clone deste repositório para obter o código-fonte do compilador.

Abra o arquivo Main e localize o trecho de código indicado pelo comentário para especificar o caminho do arquivo de entrada e saída.

Execute o código e aguarde o processamento. O compilador realizará a análise léxica, sintática e semântica do arquivo de entrada.

Verifique a saída gerada pelo compilador. Ela fornecerá informações sobre possíveis erros léxicos, sintáticos ou semânticos encontrados no código fonte.

<h2>Contribuição</h2>
Contribuições para o aprimoramento deste projeto são sempre bem-vindas. Se você tiver sugestões de melhorias, correções de bugs ou novas funcionalidades, sinta-se à vontade para enviar um pull request.

<h2>Licença</h2>
Este projeto está licenciado sob a Licença MIT. Sinta-se livre para utilizar, modificar e distribuir o código conforme necessário.
