import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import net.jini.space.JavaSpace;

@SuppressWarnings("serial")
public class Cliente extends JFrame {

	public JButton botaoEnviar,botaoObterSalas, botaoObterUsuarios, botaoCriarUsuario, botaoCriarSala, botaoMudarSala;
	public JTextField textTextoParaEnviar, textUsuario, textNomeUsuario, textSala, textNomeSala,
	textDestinatario, textNomeDestinatario;
	public JTextArea textTextoRecebido;
	public JScrollPane scrollTextoRecebido;
	public JavaSpace javaSpace = null;
	public String usuarioAtual = null;
	public String salaAtual = null;
	public Context context = null;
	public QueueConnectionFactory qfactoryQueue;
	public Thread threadEscutaFila;
	public int idInstancia = 0;
	public int contator = 0;
	public ArrayList<String> idMensagens = new ArrayList<String>();

	//Recebo o espaço e o contexto como parâmetros
	public Cliente(JavaSpace space, Context context) {
		super();
		this.javaSpace = space;
		this.context = context;

		/**
		 * Gerando o ID da instância de forma aleatória.
		 * Esse ID é usado para formar o identificador das mensagens públicas.
		 */
		Random random = new Random();
		idInstancia = random.nextInt(1000000);

		try {

			qfactoryQueue = (QueueConnectionFactory) context.lookup("ConnectionFactory");

		} catch (Exception e) {
			System.err.println("Erro na obtenção do Factory!");
		}

		/**
		 * Configuração do botão ObterSalas 
		 */
		botaoObterSalas = new JButton("Obter Salas");
		botaoObterSalas.addActionListener(new BotaoObterSalasListener());
		configurarBotao(botaoObterSalas);

		/**
		 * Configuração do botão ObterClientes 
		 */
		botaoObterUsuarios = new JButton("Obter Usuários");
		botaoObterUsuarios.addActionListener(new BotaoObterUsuariosSalaListener());
		configurarBotao(botaoObterUsuarios);

		/**
		 * Configuração do botão Enviar 
		 */
		botaoEnviar = new JButton("Enviar");
		botaoEnviar.addActionListener(new BotaoEnviarListener());
		configurarBotao(botaoEnviar);
		botaoEnviar.setEnabled(false);

		/**
		 * Configuração do botão CriarUsuario 
		 */
		botaoCriarUsuario = new JButton("Criar Usuário");
		botaoCriarUsuario.addActionListener(new BotaoCriarUsuarioListener());
		configurarBotao(botaoCriarUsuario);

		/**
		 * Configuração do botão MudarSala 
		 */
		botaoMudarSala = new JButton("Mudar Sala");
		botaoMudarSala.addActionListener(new BotaoMudarSalaListener());
		configurarBotao(botaoMudarSala);
		botaoMudarSala.setEnabled(false);

		/**
		 * Configuração do botão CriarSala 
		 */
		botaoCriarSala = new JButton("Criar Sala");
		botaoCriarSala.addActionListener(new BotaoCriarSalaListener());
		configurarBotao(botaoCriarSala);

		/**
		 * Configuração do textField composto pelas informações a serem enviadas pelo chat
		 */
		textTextoParaEnviar = new JTextField();
		configurarTextField(textTextoParaEnviar);
		textTextoParaEnviar.setBackground(new Color(255, 250, 240));
		textTextoParaEnviar.setEditable(true);

		/**
		 * Configuração do textField usuario
		 */
		textUsuario = new JTextField("Usuário:");
		configurarTextField(textUsuario);

		/**
		 * Configuração do textField que receberá o nome do usuario
		 */
		textNomeUsuario = new JTextField();
		configurarTextField(textNomeUsuario);
		textNomeUsuario.setEditable(true);

		/**
		 * Configuração do textField sala
		 */
		textSala = new JTextField("Sala:");
		configurarTextField(textSala);

		/**
		 * Configuração do textFieldque receberá o nome da sala
		 */
		textNomeSala = new JTextField();
		configurarTextField(textNomeSala);
		textNomeSala.setEditable(true);

		/**
		 * Configuração do textField destinatário
		 */
		textDestinatario = new JTextField("Destinatário:");
		configurarTextField(textDestinatario);

		/**
		 * Configuração do textFieldque receberá o nome do destinatário
		 */
		textNomeDestinatario = new JTextField();
		configurarTextField(textNomeDestinatario);
		textNomeDestinatario.setEditable(true);

		/**
		 * Configurando a textArea composta pelas informações do chat vindas do servidor
		 */
		textTextoRecebido = new JTextArea();
		textTextoRecebido.setBackground(new Color(255, 250, 240));
		textTextoRecebido.setLineWrap(true);
		textTextoRecebido.setFont(new Font("Serif", Font.PLAIN, 20));
		textTextoRecebido.setEditable(false);

		/**
		 * Configuração do scroll composto pela textArea textoRecebido
		 */
		scrollTextoRecebido = new JScrollPane(textTextoRecebido);
		scrollTextoRecebido.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollTextoRecebido.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollTextoRecebido.setPreferredSize(new Dimension(scrollTextoRecebido.getWidth(), 120));
		getContentPane().add(BorderLayout.CENTER, scrollTextoRecebido);

		/**
		 * Container com os botões principais do jogo
		 */
		Container botoes = new JPanel();
		((JComponent) botoes).setBorder(BorderFactory.createLineBorder(Color.gray));
		botoes.setLayout(new GridLayout(3,3,4,4));

		botoes.add(textSala,new Point(0,0));
		botoes.add(textNomeSala,new Point(0,1));
		botoes.add(botaoCriarSala,new Point(0,2));
		botoes.add(textUsuario,new Point(1,0));
		botoes.add(textNomeUsuario,new Point(1,1));
		botoes.add(botaoCriarUsuario,new Point(1,2));
		botoes.add(botaoObterSalas,new Point(2,0));
		botoes.add(botaoObterUsuarios,new Point(2,1));
		botoes.add(botaoMudarSala,new Point(2,2));

		getContentPane().add(BorderLayout.NORTH, botoes);

		/**
		 * Container que possui apenas o textField TextoParaEnviar e botaoEnviar
		 */
		Container textoEnvio = new JPanel();
		textoEnvio.setLayout(new BorderLayout());
		textoEnvio.add(BorderLayout.CENTER,textTextoParaEnviar);
		textoEnvio.add(BorderLayout.EAST,botaoEnviar);

		/**
		 * Container que possui os campos do destinatário
		 */
		Container destinatario = new JPanel();
		destinatario.setLayout(new GridLayout(1,2,4,4));
		destinatario.add(textDestinatario,new Point(0,0));
		destinatario.add(textNomeDestinatario,new Point(0,1));

		Container envio = new JPanel();
		envio.setLayout(new BorderLayout());
		envio.add(BorderLayout.CENTER,destinatario);
		envio.add(BorderLayout.SOUTH,textoEnvio);

		getContentPane().add(BorderLayout.SOUTH, envio);

		/**
		 * Configurações da Janela
		 */
		setResizable(false);
		setSize(600, 600);
		setVisible(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter());
	}

	class WindowAdapter implements WindowListener{

		/**
		 * Método não utilizado
		 */
		@Override
		public void windowOpened(WindowEvent e) {}

		/**
		 * Método chamado quando o usuário fecha a janela.
		 * Quando esta ação é feita, a tupla do usuário correspondente é excluída do espaço,
		 * caso essa tupla exista.
		 */
		@Override
		public void windowClosing(WindowEvent arg0) {

			if (!(JOptionPane.showConfirmDialog(null, "Deseja mesmo fechar a janela?") == JOptionPane.OK_OPTION)) {
				return;
			}
			else{
				/**
				 * Se o usuário confirmar o fechamento da janela e estiver online, sua tupla é retirada do espaço.
				 */
				try {
					//Template para verificar se o usuário existe.
					Usuario templateUsuario = new Usuario();
					templateUsuario.nome = usuarioAtual;
					templateUsuario.nomeSala = salaAtual;

					Usuario usuario = (Usuario) javaSpace.read(templateUsuario, null, 400);

					//Se o usuário existir
					if(usuario != null){

						Usuario usuarioSala = (Usuario)javaSpace.take(templateUsuario, null, 400);
						
						/**
						 * Template para atualizar a sala em que o usuário se encontra.
						 * Quando eu tiro o usuário de uma sala, essa deve ser atualizada para 10 minutos
						 * novamente(em relação a esse usuário), já que ele pode sair da sala a
						 * qualquer momento. Caso essa sala tenha outros usuários ativos, ela será
						 * atualizada(para 12 minutos) de acordo com as ações dos usuários.
						 */
						Sala salaAntiga = new Sala();
						salaAntiga.nomeSala = usuarioSala.nomeSala;
						
						javaSpace.take(salaAntiga, null, 400);
						javaSpace.write(salaAntiga, null, 600000);
						
					}

				} catch (Exception e) {
					System.err.println("Erro na remoção do usuário ao fechar a janela!");
				}
			}
			System.exit(0);
		}

		/**
		 * Método não utilizado
		 */
		@Override
		public void windowClosed(WindowEvent e) {}

		/**
		 * Método não utilizado
		 */
		@Override
		public void windowIconified(WindowEvent e) {}

		/**
		 * Método não utilizado
		 */
		@Override
		public void windowDeiconified(WindowEvent e) {}

		/**
		 * Método não utilizado
		 */
		@Override
		public void windowActivated(WindowEvent e) {}

		/**
		 * Método não utilizado
		 */
		@Override
		public void windowDeactivated(WindowEvent e) {}
	}

	/**
	 * Método utilizado para configurar os botões.
	 */
	public void configurarBotao(JButton botao){

		botao.setBackground(new Color(255, 255, 255));
		botao.setForeground(new Color(31, 58, 147));
		botao.setFont(new Font("Serif", Font.PLAIN, 20));
	}

	/**
	 * Método utilizado para configurar os textFields.
	 */
	public void configurarTextField(JTextField textField){

		textField.setEditable(false);
		textField.setBackground(new Color(255, 255, 255));
		textField.setForeground(Color.DARK_GRAY);
		textField.setFont(new Font("Serif", Font.PLAIN, 20));
	}

	/**
	 * Método utilizado para mostrar um aviso ao usuário.
	 */
	public void mostrarAviso(String texto){

		JOptionPane.showMessageDialog(null, texto);
	}

	/**
	 * Método utilizado para verificar o status(on/off) do usuário.
	 * Caso esteja online, retorna true.
	 * Caso contrário, retorna false e também uma mensagem informando que a sessão expirou.
	 */
	public boolean verificarStatus(){

		try {
			//Template para verificar a existência do usuárioAtual.
			Usuario templateUsuarioAtual = new Usuario();
			templateUsuarioAtual.nome = usuarioAtual;
			templateUsuarioAtual.nomeSala = salaAtual;

			Usuario usuarioA = (Usuario)javaSpace.read(templateUsuarioAtual, null, 400);

			if(usuarioA == null){
				mostrarAviso("Sua sessão expirou!!!");
				return false;
			}

			return true;

		} catch (Exception e) {
			System.err.println("Erro na função verificarStatus!");
		}
		return true;
	}

	/**
	 * Método utilizado para atualizar determinada sala.
	 * A partir da criação de um usuário, a sala a qual esse usuário pertence
	 * passa a ter uma duração de 12 minutos, já que o usuário tem uma duração de 2 minutos.
	 */
	public void atualizarSala(){

		try {
			//Template para a atualização da sala
			Sala templateSala = new Sala();
			templateSala.nomeSala = salaAtual;
			javaSpace.take(templateSala, null, 400);

			javaSpace.write(templateSala, null, 720000);

		} catch (Exception e) {
			System.err.println("Erro na função atualizarSala!");
		}
	}

	/**
	 * Método utilizado para atualizar determinado usuário, o qual possui
	 * uma duração de 2 minutos.
	 */
	public void atualizarUsuario(){

		try {
			//Template para atualiza o usuário
			Usuario templateUsuario = new Usuario();
			templateUsuario.nome = usuarioAtual;
			templateUsuario.nomeSala = salaAtual;
			javaSpace.take(templateUsuario, null, 400);

			javaSpace.write(templateUsuario, null, 120000);

		} catch (Exception e) {
			System.err.println("Erro na função atualizarUsuário!");
		}
	}

	/**
	 * Método chamado quando o botão Criar Sala é clicado.
	 * Esse método cria uma nova sala pegando a informação do textField NomeSala.
	 */
	public class BotaoCriarSalaListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {

			try{

				/**
				 * Caso o usuário já tenha sido criado, verifico o seu status(on/off).
				 * Caso esteja off(porque passou de 2 minutos), aparecerá aviso informando
				 * que a sessão expirou.
				 */
				if(usuarioAtual != null){

					if(!verificarStatus()){
						System.exit(0);
					}
				}

				String nome = textNomeSala.getText();
				textNomeSala.setText("");
				textNomeUsuario.setText("");

				if(nome.isEmpty() || nome.trim().isEmpty()){
					mostrarAviso("Nome da sala inválido!");
					System.out.println("Nome da sala inválido!");
					return;
				}

				//Verificando se a sala já existe
				Sala template = new Sala();
				template.nomeSala = nome;

				if(!(javaSpace.read(template, null, 400) == null)){
					mostrarAviso("Sala existente!");
					System.out.println("Sala existente!");
					return;
				}

				//Criando uma nova sala
				Sala sala = new Sala();
				sala.nomeSala = nome;

				//Escrevendo a sala no espaço de tuplas. Tem inicialmente 10 minutos
				javaSpace.write(sala, null, 600000);

				System.out.println("Sala criada!");
				textTextoRecebido.append("Sala criada!\n");
				textTextoRecebido.setCaretPosition(textTextoRecebido.getText().length()-1);

				//Se já houver usuário, atualizo os tempos da sala e do usuário
				if(usuarioAtual != null){

					atualizarUsuario();
					atualizarSala();
				}

			}catch(Exception b){
				System.err.println("Erro na criação da sala!");
			}
		}
	}

	/**
	 * Método chamado quando o botão Criar Usuário é clicado.
	 * Esse método cria um usuário em uma sala pegando as informações
	 * dos textFields nomeSala e nomeUsuario.
	 */
	public class BotaoCriarUsuarioListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {

			try{

				String nomeSala = textNomeSala.getText();
				String nomeUsuario = textNomeUsuario.getText();
				textNomeSala.setText("");
				textNomeUsuario.setText("");

				if(nomeSala.isEmpty() || nomeSala.trim().isEmpty()){
					mostrarAviso("Nome da sala inválido!");
					System.out.println("Nome da sala inválido!");
					return;
				}

				if(nomeUsuario.isEmpty() || nomeUsuario.trim().isEmpty() || nomeUsuario.equals("Todos") || nomeUsuario.equals("todos")){
					mostrarAviso("Nome do usuário inválido!");
					System.out.println("Nome do usuário inválido!");
					return;
				}

				//Template para verificar a existência da sala
				Sala templateSala = new Sala();
				templateSala.nomeSala = nomeSala;

				//Template para verificar a existência do usuário no chat
				Usuario templateUsuario = new Usuario();
				templateUsuario.nome = nomeUsuario;

				//Verificando se a sala já existe
				if(javaSpace.read(templateSala, null, 400) == null){
					mostrarAviso("Sala não existente!");
					System.out.println("Sala não existente!");
					return;
				}

				//Verificando se o usuário já existe
				if(!(javaSpace.read(templateUsuario, null, 400) == null)){
					mostrarAviso("Usuário existente!");
					System.out.println("Usuário existente!");
					return;
				}

				//Criando um usuário novo e atualizando o usuarioAtual e a salaAtual
				Usuario usuario = new Usuario();
				usuario.nomeSala = nomeSala;
				usuario.nome = nomeUsuario;
				usuarioAtual = usuario.nome;
				salaAtual = usuario.nomeSala;

				//Escrevendo o usuário no espaço de tuplas. Tem duração de 2 minutos
				javaSpace.write(usuario, null, 120000);
				setTitle(usuarioAtual + " - " + salaAtual);
				botaoCriarUsuario.setEnabled(false);
				textNomeUsuario.setEditable(false);

				System.out.println("Usuário criado!");
				textTextoRecebido.append("Usuário criado!\n");
				textTextoRecebido.setCaretPosition(textTextoRecebido.getText().length()-1);
				botaoMudarSala.setEnabled(true);
				botaoEnviar.setEnabled(true);

				//Atualizando a sala em que o usuário acaba de ser criado
				atualizarSala();

				//Iniciando a Thread que escuta o espaço
				new Thread(new ClienteEscutaEspaco()).start();

				//Iniciando a Thread que criará a fila de um determinado usuário e sala 
				threadEscutaFila = new Thread(new ClienteEscutaFila());
				threadEscutaFila.start();

			}catch(Exception b){
				System.err.println("Erro na criação do usuário!");
			}
		}
	}

	/**
	 * Método chamado quando o botão Mudar Sala é clicado.
	 * Esse método coloca um usuário em uma nova sala pegando a informação
	 * do textField nomeSala.
	 */
	public class BotaoMudarSalaListener implements ActionListener{

		@SuppressWarnings("deprecation")
		@Override
		public void actionPerformed(ActionEvent e){

			try{

				/**
				 * Verificando o status(on/off) do usuárioAtual
				 * Caso esteja off(porque passou de 2 minutos), aparecerá aviso informando
				 * que a sessão expirou.
				 */
				if(!verificarStatus()){
					System.exit(0);
				}

				String nomeSala = textNomeSala.getText();
				textNomeSala.setText("");

				if(nomeSala.isEmpty() || nomeSala.trim().isEmpty()){
					mostrarAviso("Nome da sala inválido!");
					System.out.println("Nome da sala inválido!");
					return;
				}

				//Template para verificar a existência da nova sala
				Sala templateNovaSala = new Sala();
				templateNovaSala.nomeSala = nomeSala;

				//Template para retirar o usuário do espaço de tuplas
				Usuario templateUsuario = new Usuario();
				templateUsuario.nome = usuarioAtual;

				//Verificando se a sala já existe
				if(javaSpace.read(templateNovaSala, null, 400) == null){
					mostrarAviso("Sala não existente!");
					System.out.println("Sala não existente!");
					return;
				}

				//Retirando o usuário da sala atual do espaço de tuplas
				Usuario usuarioSala = (Usuario)javaSpace.take(templateUsuario, null, 400);

				/**
				 * Template para atualizar a sala "antiga".
				 * Quando eu tiro o usuário de uma sala, essa deve ser atualizada para 10 minutos
				 * novamente(em relação a esse usuário), já que ele pode mudar de sala a
				 * qualquer momento. Caso essa sala tenha outros usuários ativos, ela será
				 * atualizada(para 12 minutos) de acordo com as ações dos usuários.
				 */
				Sala salaAntiga = new Sala();
				salaAntiga.nomeSala = usuarioSala.nomeSala;
				
				javaSpace.take(salaAntiga, null, 400);
				javaSpace.write(salaAntiga, null, 600000);
				
				//Colocando o usuário em sua nova sala
				Usuario usuario = new Usuario();
				usuario.nomeSala = nomeSala;
				usuario.nome = usuarioAtual;
				salaAtual = usuario.nomeSala;

				javaSpace.write(usuario, null, 120000);
				setTitle(usuarioAtual + " - " + salaAtual);
				textTextoRecebido.setText("");
				idMensagens.removeAll(idMensagens);

				System.out.println(usuarioAtual + " mudou a sala!");
				textTextoRecebido.append(usuarioAtual + " mudou a sala!\n");
				textTextoRecebido.setCaretPosition(textTextoRecebido.getText().length()-1);

				//Atualizando a sala em que o usuário acaba de ser inserido
				atualizarSala();

				/**
				 * Iniciando a Thread que criará a fila de um determinado usuário em sua nova sala e 
				 * interrompendo a Thread que escutava na fila anterior. 
				 */
				threadEscutaFila.stop();
				threadEscutaFila = new Thread(new ClienteEscutaFila());
				threadEscutaFila.start();

			}catch(Exception b){
				System.err.println("Erro na mudança da sala!");
			}
		}
	}

	/**
	 * Método chamado quando o botão Obter Salas é clicado.
	 * Esse método retorna uma lista com as salas que estão abertas em determinado momento.
	 */
	public class BotaoObterSalasListener implements ActionListener{

		ArrayList<String> salasAbertas = new ArrayList<String>();

		@Override
		public void actionPerformed(ActionEvent e) {

			boolean listaVazia = false;

			try{

				/**
				 * Caso o usuário já tenha sido criado, verifico o seu status(on/off).
				 * Caso esteja off(porque passou de 2 minutos), aparecerá aviso informando
				 * que a sessão expirou.
				 */
				if(usuarioAtual != null){

					if(!verificarStatus()){
						System.exit(0);
					}
				}

				//Enquanto houver salas no espaço de tuplas, eu retiro e guardo-as no arrayList salasAbertas 
				while(listaVazia == false){
					Sala template = new Sala();

					Sala sala = (Sala) javaSpace.take(template, null, 400);

					if(sala == null){
						System.out.println("Tempo de espera esgotado. Encerrando...");
						listaVazia = true;
					}

					if(listaVazia == false){
						salasAbertas.add(sala.nomeSala);
					}
				}

				if(salasAbertas.isEmpty()){
					mostrarAviso("Não existem salas abertas!");
					System.out.println("Não existem salas abertas!");
					return;
				}

				textTextoRecebido.append("Salas abertas!\n");

				for (String string : salasAbertas) {

					Sala sala1 = new Sala();
					sala1.nomeSala = string;

					//Reescrevendo as salas
					javaSpace.write(sala1, null, 600000);

					System.out.println(string);
					textTextoRecebido.append(string+"\n");
				}

				textTextoRecebido.setCaretPosition(textTextoRecebido.getText().length()-1);
				salasAbertas.removeAll(salasAbertas);

				//Se já houver usuário, tenho de atualizar os tempos da sala e do usuário
				if(usuarioAtual != null){

					atualizarUsuario();
					atualizarSala();
				}

			}catch(Exception b){
				System.err.println("Erro na obtenção das salas!");
			}
		}
	}

	/**
	 * Método chamado quando o botão Obter Usuários é clicado.
	 * Esse método retorna uma lista com os usuários que estão em determinda sala
	 * em determinado momento.
	 */
	public class BotaoObterUsuariosSalaListener implements ActionListener{

		ArrayList<String> usuariosSala = new ArrayList<String>();

		@Override
		public void actionPerformed(ActionEvent e) {

			boolean semUsuarios = false;

			try{

				/**
				 * Caso o usuário já tenha sido criado, verifico o seu status(on/off).
				 * Caso esteja off(porque passou de 2 minutos), aparecerá aviso informando
				 * que a sessão expirou.
				 */
				if(usuarioAtual != null){

					if(!verificarStatus()){
						System.exit(0);
					}
				}

				String nomeSala = textNomeSala.getText();
				textNomeSala.setText("");
				textNomeUsuario.setText("");

				if(nomeSala.isEmpty() || nomeSala.trim().isEmpty()){
					mostrarAviso("Nome da sala inválido!");
					System.out.println("Nome da sala inválido!");
					return;
				}

				//Template para verificar a existência da sala
				Sala templateSala = new Sala();
				templateSala.nomeSala = nomeSala;

				//Verificando se a sala já existe
				if(javaSpace.read(templateSala, null, 400) == null){
					mostrarAviso("Sala não existente!");
					System.out.println("Sala não existente!");
					return;
				}

				//Enquanto houver usuários na sala específica, eu retiro e guardo-os no arrayList usuarios 
				while(semUsuarios == false){

					//Template para retirar os usuários da sala
					Usuario templateUsuario = new Usuario();
					templateUsuario.nomeSala = nomeSala;

					Usuario usuario = (Usuario)javaSpace.take(templateUsuario, null, 400);

					if(usuario == null){
						System.out.println("Tempo de espera esgotado. Encerrando...");
						semUsuarios = true;
					}

					if(semUsuarios == false){
						usuariosSala.add(usuario.nome);
					}
				}

				if(usuariosSala.isEmpty()){
					mostrarAviso("Não existe usuário na sala indicada!");
					System.out.println("Não existe usuário na sala indicada!");
					return;
				}

				textTextoRecebido.append("Usuários da sala " + nomeSala + "\n");

				for (String string : usuariosSala) {

					Usuario usuario1 = new Usuario();
					usuario1.nome = string;
					usuario1.nomeSala = nomeSala;

					//Reescrevendo os usuários na sala 
					javaSpace.write(usuario1, null, 120000);

					System.out.println(string);
					textTextoRecebido.append(string + "\n");
				}

				textTextoRecebido.setCaretPosition(textTextoRecebido.getText().length()-1);
				usuariosSala.removeAll(usuariosSala);

				//Se já houver usuário, tenho de atualizar os tempos da sala e do usuário
				if(usuarioAtual != null){

					atualizarUsuario();
					atualizarSala();
				}

			}catch(Exception b){
				System.err.println("Erro na obtenção dos usuários de uma sala!");
			}
		}
	}

	/**
	 * Método chamado quando o botão Enviar é clicado.
	 * Esse método pega as informações do textField textTextoParaEnviar e envia para o "servidor".
	 * Caso o usuário queira mandar a mensagem para todos os usuários conectados,
	 * o mesmo deve inserir "todos" ou "Todos" no campo destinatário.
	 */
	public class BotaoEnviarListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {

			try{

				/**
				 * Verificando o status(on/off) do usuárioAtual
				 * Caso esteja off(porque passou de 2 minutos), aparecerá aviso informando
				 * que a sessão expirou.
				 */
				if(!verificarStatus()){
					System.exit(0);
				}

				String destinatario = textNomeDestinatario.getText();
				String conteudoMensagem = textTextoParaEnviar.getText();
				textNomeDestinatario.setText("");
				textTextoParaEnviar.setText("");

				//Verificando se os dados são válidos
				if(destinatario.isEmpty() || destinatario.trim().isEmpty()){
					mostrarAviso("Destinatário inválido!\n Para enviar uma mensagem pública, insira \"Todos\" ou \"todos\" no destinatário!");
					System.out.println("Destinatário inválido!");
					return;
				}

				//Verificando se os dados são válidos
				if(conteudoMensagem.isEmpty() || conteudoMensagem.trim().isEmpty()){
					mostrarAviso("Conteúdo da mensagem inválido!");
					System.out.println("Conteúdo da mensagem inválido!");
					return;
				}

				//Template para verificar a existência do destinatário na sala
				Usuario templateUsuarioSala = new Usuario();
				templateUsuarioSala.nome = destinatario;
				templateUsuarioSala.nomeSala = salaAtual;

				//Template para verificar a existência do destinatário no chat
				Usuario templateUsuarioChat = new Usuario();
				templateUsuarioChat.nome = destinatario;

				//Verificando junto ao espaço de tuplas
				Usuario destinatarioChat = (Usuario)javaSpace.read(templateUsuarioChat, null, 400);
				Usuario destinatarioSala = (Usuario)javaSpace.read(templateUsuarioSala, null, 400);

				/**
				 * Verificando a exitência do destinatário no chat.
				 * Caso esteja off no chat, ou seja, não esteja em nenhuma sala,
				 * uma mensagem informando que o destinatário não está no chat é mostrada
				 */
				if(destinatarioChat == null && (!destinatario.equals("todos") && !destinatario.equals("Todos"))){
					mostrarAviso(destinatario + " não está conectado ao Chat!");
					return;
				}

				/**
				 * Mensagem pública
				 */
				if(destinatario.equals("todos") || destinatario.equals("Todos")){

					String identificador = Integer.toString(idInstancia) + Integer.toHexString(contator);
					contator++;

					Mensagem mensagem = new Mensagem();
					mensagem.conteudo = conteudoMensagem;
					mensagem.nomeSala = salaAtual;
					mensagem.remetente = usuarioAtual;
					mensagem.destinatario = "todos";
					mensagem.identificador = identificador;

					javaSpace.write(mensagem, null, 300000);

					System.out.println("Mensagem pública enviada!");

					//Atualizando usuário e sala
					atualizarUsuario();
					atualizarSala();

					return;
				}

				/**
				 * Mensagem enviada para a fila.
				 * Usuário inexistente na sala, mas conectado ao chat.
				 */
				if(destinatarioSala == null && (!destinatario.equals("todos") && !destinatario.equals("Todos"))){

					QueueConnection qconnection = qfactoryQueue.createQueueConnection();
					QueueSession qsession = qconnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

					TextMessage message = qsession.createTextMessage();
					message.setText(usuarioAtual + " enviou: " + conteudoMensagem);

					Queue fila = qsession.createQueue(destinatario+salaAtual);

					QueueSender sender = qsession.createSender(fila);

					sender.setTimeToLive(300000);
					sender.send(message);
					System.out.println("Mensagem enviada para fila!");

					qsession.close();
					qconnection.close();

					//Atualizando usuário e sala
					atualizarUsuario();
					atualizarSala();

					return;
				}

				/**
				 * Mensagem privada.
				 * Caso em que o usuário está online.
				 * Não posso enviar mensagem privada para o usuárioAtual :D
				 */
				if(!destinatario.equals(usuarioAtual)){
					Mensagem mensagem = new Mensagem();
					mensagem.conteudo = conteudoMensagem;
					mensagem.nomeSala = salaAtual;
					mensagem.remetente = usuarioAtual;
					mensagem.destinatario = destinatario;

					javaSpace.write(mensagem, null, 300000);

					System.out.println("Mensagem privada enviada!");

					//Atualizando usuário e sala
					atualizarUsuario();
					atualizarSala();
				}
				
			}catch(Exception b){
				System.err.println("Erro no envio da mensagem!");
			}
		}
	}

	/**
	 * Método que verifica, junto ao espaço de tuplas, se há mensagens para determinado usuário
	 * ou para todos que estão conectados(em determinada sala). 
	 */
	public class ClienteEscutaEspaco implements Runnable{

		@Override
		public void run() {

			while(true){

				try{

					//Quando o usuário está online. Vou buscar a mensagem no espaço de tuplas.
					//Criando template para verificar a exitência da mensagem privada
					Mensagem templateMensagemPrivada = new Mensagem();
					templateMensagemPrivada.destinatario = usuarioAtual;
					templateMensagemPrivada.nomeSala = salaAtual;
					templateMensagemPrivada.identificador = null;

					//Criando template para verificar a exitência da mensagem pública
					Mensagem templateMensagemPublica = new Mensagem();
					templateMensagemPublica.destinatario = "todos";
					templateMensagemPublica.nomeSala = salaAtual;

					Mensagem mensagemRecebidaPrivada = (Mensagem)javaSpace.take(templateMensagemPrivada, null, 400);

					//Retiro a mensagem e coloco novamente depois de ser exibida.
					Mensagem mensagemRecebidaPublica = (Mensagem)javaSpace.take(templateMensagemPublica, null, 400);

					//Imprimindo as mensagens privadas
					if(mensagemRecebidaPrivada != null && usuarioAtual != null){

						String conteudoMensagem = mensagemRecebidaPrivada.conteudo;
						String remetenteMensagem = mensagemRecebidaPrivada.remetente;

						textTextoRecebido.append("** " + remetenteMensagem + " enviou: " + conteudoMensagem + " **\n");
						textTextoRecebido.setCaretPosition(textTextoRecebido.getText().length()-1);
					}

					//Imprimindo as mensagens públicas
					if(mensagemRecebidaPublica != null && usuarioAtual != null){

						//Verificando se as mensagens já foram lidas
						for (String string : idMensagens) {

							if(string.equals(mensagemRecebidaPublica.identificador)){
								mensagemRecebidaPublica = null;
							}
						}

						if(mensagemRecebidaPublica != null){

							String conteudoMensagem = mensagemRecebidaPublica.conteudo;
							String remetenteMensagem = mensagemRecebidaPublica.remetente;

							textTextoRecebido.append(remetenteMensagem + " enviou: " + conteudoMensagem + "\n");
							textTextoRecebido.setCaretPosition(textTextoRecebido.getText().length()-1);

							idMensagens.add(mensagemRecebidaPublica.identificador);

							//Colocando a mensagem novamente no espaço
							Mensagem novaMensagem = mensagemRecebidaPublica;
							javaSpace.write(novaMensagem, null, 300000);

						}
					}

				}catch(Exception x){
					System.err.println("Erro ao tentar escutar mensagem privada no espaço");
				}
			}
		}
	}

	/**
	 * Método que verifica, junto a determinada fila, se existem mensagens para determinado usuário. 
	 */
	public class ClienteEscutaFila implements Runnable{

		@Override
		public void run() {

			try {

				QueueConnectionFactory qfactoryQueue = (QueueConnectionFactory) context.lookup("ConnectionFactory");
				QueueConnection qconnection = qfactoryQueue.createQueueConnection();
				QueueSession qsession = qconnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				qconnection.start();

				Queue fila = qsession.createQueue(usuarioAtual+salaAtual);

				QueueReceiver qreceiver = qsession.createReceiver(fila);

				TextMessage textMessage = null;

				//Executo o laço até a thread ser interrompida
				while(!threadEscutaFila.isInterrupted()) {

					textMessage = (TextMessage) qreceiver.receive();
					textTextoRecebido.append("**f " + textMessage.getText()+ " **\n");
					textTextoRecebido.setCaretPosition(textTextoRecebido.getText().length()-1);

				}

			} catch (Exception e) {
				System.err.println("Erro na classe ClienteEscutaFila!");
			}
		}
	}

	public static void main(String[] args) {

		JavaSpace space = null;
		Context context = null;

		try {
			//Encontrando o Contexto JMS
			Hashtable properties = new Hashtable();
			properties.put(Context.INITIAL_CONTEXT_FACTORY,"org.exolab.jms.jndi.InitialContextFactory");
			properties.put(Context.PROVIDER_URL, "tcp://localhost:3035/");

			System.out.println("Procurando pelo Contexto JMS...");
			context = new InitialContext(properties);

			System.out.println("Contexto JMS encontrado!");

			//Encontrando o JavaSpace
			System.out.println("Procurando pelo serviço do JavaSpace...");

			Lookup finder = new Lookup(JavaSpace.class);
			space = (JavaSpace)finder.getService();

			if(space == null){
				System.out.println("O serviço JavaSpace nao foi encontrado. Encerrando...");
				System.exit(-1);
			}

			System.out.println("O serviço JavaSpace foi encontrado.");

			new Cliente(space, context);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro na busca dos serviços!");
			System.err.println("Erro na busca do serviço do JavaSpace ou do Contexto JMS!!!");
		}

	}
}
