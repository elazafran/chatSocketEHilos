

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.*;

import java.net.*;

public class Cliente {

	public static void main(String[] args) {
		
		
		MarcoCliente mimarco=new MarcoCliente();
		
		mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

}


class MarcoCliente extends JFrame{
	
	public MarcoCliente(){
		
		setBounds(600,300,280,350);
				
		LaminaMarcoCliente milamina=new LaminaMarcoCliente();
		
		add(milamina);
		
		setVisible(true);
		}	
	
}

/**
 * para uqe la clase esté permanente a la escucha necesitamos implementar la interfaz Runnable
 * @author elaza
 *
 */
class LaminaMarcoCliente extends JPanel implements Runnable{
	
	public LaminaMarcoCliente(){
	
		nick = new JTextField(5);
		add(nick);
		
		JLabel texto=new JLabel("- Chat -");
		add(texto);
		
		ip = new JTextField(8);
		add(ip);
		
		campochat = new JTextArea(12,20);
		add(campochat);
		
		campo1=new JTextField(20);
		add(campo1);		
	
		miboton=new JButton("Enviar");
		
		//Estamos a la escucha del evento en el boton y reaccionamos a él
		EnviaTexto mievento = new EnviaTexto();
		miboton.addActionListener(mievento);
		
		add(miboton);	
		
		//creamos y arrancamos el hilo al crear la interfaz para que esté a la escucha
		
		Thread mihilo = new Thread(this);
		mihilo.start();
		
	}
	
	
	
	private class EnviaTexto implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			System.out.println(campo1.getText());
			//Enviamos el texto que hay en jTextfield
			campochat.append("\n yo: "+campo1.getText());
			
			try {
				//canal de comunicacion, el puente. Falta que el servidor escuche por el mismo puerto y permanezca a la escucha
				Socket misocket = new Socket("192.168.1.105", 9999);
				
				//instanciamos la clase para los datos y enviaremos este paquete al servidor que lo gestionará 
				PaqueteEnvio datos = new PaqueteEnvio();
				
				datos.setNick(nick.getText());
				datos.setIp(ip.getText());
				datos.setMensaje(campo1.getText());
				campo1.setText("");
				
				//utilizamos esta clase para enviar el objeto y creamos el flujo de salida
				ObjectOutputStream paquetedatos = new ObjectOutputStream(misocket.getOutputStream());
				
				//en vez de usar writeUTF como en ocasiones anteriores lo hacemos writeObject 
				//este objeto los serializamos para convertirlo enun grupo de bytes y enviarlo por la red y eso lo hacemos en la clase PaqueteEnvio
				paquetedatos.writeObject(datos);
				
				misocket.close();
				/*//creamos un flujo de datos de salida, y el constructor necesita un OutputStream
				DataOutputStream flujo_salida = new DataOutputStream(misocket.getOutputStream());
				//Escribe en el flujo lo que hay en el campo1
				flujo_salida.writeUTF(campo1.getText());
				//los flujos hay que cerrarlos
				
				flujo_salida.close();*/
				
				
				
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				//mensaje para consola del posible error
				System.out.println(e1.getMessage());
				e1.printStackTrace();
			}
			
		}
		
	}
		
		
	private JTextField campo1,nick,ip;
	private JTextArea campochat;
	private JButton miboton;
	
	@Override
	public void run() {
		//aqui codificamos para que la clase este a la escucha
		try {
			ServerSocket servidorCliente = new ServerSocket(9090);
			
			//creamos un canal o socket para recibir la información
			Socket cliente;
			PaqueteEnvio paqueteRecibido;
			
			while(true) {
				//aqui le decimos que acepte las conexiones que le venga por el puerto indicado
				cliente = servidorCliente.accept();
				
				//creamos un flujo de datos de entrada capaz de transportar objetos 
				ObjectInputStream flujoentrada = new ObjectInputStream(cliente.getInputStream());
				
				paqueteRecibido = (PaqueteEnvio) flujoentrada.readObject();
				//ahora escribimos la información que recibimos
				campochat.append("\n" + paqueteRecibido.getNick()+": "+ paqueteRecibido.getMensaje());
			}
			
		} catch (Exception e) {
			//controlamos y mostramos por consola si da algún error
			System.out.println(e.getMessage());
		}
		
	}
	
}
/**
 * 
 * Usamos esta clase para enviar un objeto empaquetando asi los siguiente datos:
 *  
 * 1. Nickname
 * 2. Dirección ip
 * 3. Mensaje que enviamos
 * 
 * Para el envio implementamos la clase con la interfaz serializable e indicamos que los objetos de dicha clase 
 * pueden convertirse en bytes para que ese objeto pueda viajar por la red
 * 
 * @author elaza
 *
 */

class PaqueteEnvio implements Serializable{
	
	private String nick,ip,mensaje;

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}	

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
}