# Nails Schedule

### Um aplicativo com agendamento de serviços realizados em unhas, com atualização em tempo real e com galeria para as clientes poderem guardar as fotos das unhas favoritas.


### Para implementação deste aplicativo, foram utilizados(as):

- Kotlin - linguagem de programação ofical da Google;
- Framework Android;
- Android Studio - IDE (Ambiente de desenvolvimento Integrado);
- Firebase - serviço da Google que ajuda as equipes de aplicativos para dispositivos móveis e da web a alcançar o sucesso através da criação de apps rapidamente, sem precisar gerenciar a infraestrutura, com baixa latência e sem custos iniciais. Ele foi utilizado para armazenamento de dados do app;
Do Firebase foram utilizados 3 produtos:
- Firebase Authentication - no app em questão  está sendo usado para a autenticação das clientes através do método de login da Google e do Facebook e 
usado para a autenticação dos profissionais através do método de login com email e senha;
- Firebase Cloud Firestore (Firestore database) - no app em questão está sendo usado para guardar a lista de horários agendados do dia atual e mais 
6 dias posteriores e guardar os dados das clientes agendadas; 
- Firebase Cloud Storage (Storage) - no app em questão está sendo usado para guardar as fotos das unhas que as clientes colocam na galeria do app.
- ConstraintLayout (em maior parte) e LinaerLayout;
- ViewFlipper - uma subclasse de ViewAnimator que é usada para alternar entre visualizações. Ele é um elemento de widget de transição que nos ajuda a - adicionar transição nas visualizações (ou seja, mudar de uma visualização para outra). Um exemplo seria a alternância da visualização entre a visualização da Lista da RecyclerView, do Progress e da imagem que sinaliza a falta de internet;
- NavigationView - Representa um menu de navegação padrão para o aplicativo. O conteúdo do menu pode ser preenchido por um arquivo de recurso de menu.
NavigationView é normalmente colocado dentro de um DrawerLayout (como neste app em questão);
Bibliotecas:  
- Glide - Para carregamento de imagem;  
- Lifecycle -  Componentes compatíveis com o ciclo de vida realizam ações em resposta a uma mudança no status do ciclo de vida de outro componente, como atividades e fragmentos. Esses componentes ajudam você a produzir códigos mais organizados e, normalmente, mais leves e mais fáceis de manter.  
- Coroutines - o uso delas é recomendada para programação assíncrona no Android. Os recursos notáveis incluem o seguinte: Leve, menos vazamento de memória, suporte a cancelamento integrado e integração com o Jetpack;  
- viewBinding - Usada para a vinculação de visualizações. É um recurso que facilita a programação de códigos que interagem com visualizações. Quando a vinculação de visualizações é ativada em um módulo, ela gera uma classe de vinculação para cada arquivo de layout XML presente nesse módulo. A instância de uma classe de vinculação contém referências diretas a todas as visualizações que têm um código no layout correspondente;  
- SharedPreferences - Interface para acessar e modificar dados de preferência retornados por Context.getSharedPreferences(String, int). Para qualquer conjunto específico de preferências, há uma única instância dessa classe que todos os clientes compartilham. As modificações nas preferências devem passar por um objeto Editor para garantir que os valores de preferência permaneçam em um estado e controle consistentes quando forem confirmados para armazenamento. 
- DiffUtil é uma classe de utilitário que calcula a diferença entre duas listas e gera uma lista de operações de atualização que converte a primeira lista na segunda. Evitando assim o uso do NotifyDataChanged que faz com que toda lista seja atualizada, não sendo assim tão performático quando o DiffUtil.
