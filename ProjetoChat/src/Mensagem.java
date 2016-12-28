import net.jini.core.entry.Entry;

@SuppressWarnings("serial")
public class Mensagem implements Entry{

	public String remetente;
	public String destinatario;
	public String nomeSala;
	public String conteudo;
	public String identificador;
	
	public Mensagem(){}

}
