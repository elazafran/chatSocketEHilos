

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.*;

import java.net.*;
import java.util.ArrayList;

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

		setTitle("Chat cliente");

		setLocationRelativeTo(null);

		setVisible(true);

		addWindowListener(new EnvioOnline());
		EnvioIP enviar = new EnvioIP();
	}	

}

class EnvioIP{

	//Todo lo que este dentro se ejecutará al abrir la ventana, sobreescribimos el metodo windowOppened
	public EnvioIP(){

		try {

			//puerto del servidor que está a la escucha y el puerto
			Socket misocket = new Socket("192.168.1.105",9999);
			PaqueteEnvio datos = new PaqueteEnvio();
			datos.setMensaje("online");

			//creamos el flujo de datos
			ObjectOutputStream paquete_datos = new ObjectOutputStream(misocket.getOutputStream());

			//escribimos dentro del flujo de datos 
			paquete_datos.writeObject(datos);

			misocket.close();

		} catch (Exception e2) {
			System.out.println(e2.getMessage());
		}

	}

}
//------------------- ENVIO DE SEÑAL ONLINE ---------------------- //
/**
 * Cuando nos conectamos y abrimos la ventana mandamos un paquete al servidor con los datos 
 * 
 * @author elaza
 *
 */
class EnvioOnline extends WindowAdapter{

	//Todo lo que este dentro se ejecutará al abrir la ventana, sobreescribimos el metodo windowOppened
	public void windowOppened(WindowEvent e) {
		try {

			//puerto del servidor que está a la escucha y el puerto
			Socket misocket = new Socket("192.168.1.105",9999);
			PaqueteEnvio datos = new PaqueteEnvio();
			datos.setMensaje("online");

			//creamos el flujo de datos
			ObjectOutputStream paquete_datos = new ObjectOutputStream(misocket.getOutputStream());

			//escribimos dentro del flujo de datos 
			paquete_datos.writeObject(datos);

			misocket.close();

		} catch (Exception e2) {
			System.out.println(e2.getMessage());
		}

	}

}
//--------------------------------------------------------------- //
/**
 * para uqe la clase esté permanente a la escucha necesitamos implementar la interfaz Runnable
 * @author elaza
 *
 */
class LaminaMarcoCliente extends JPanel implements Runnable{

	public LaminaMarcoCliente(){

		String nickUsuario = JOptionPane.showInputDialog("Introduzca Nickname: ");

		JLabel nNick = new JLabel("Nick: ");
		add(nNick);

		nick = new JLabel();
		nick.setText(nickUsuario);
		add(nick);

		JLabel texto=new JLabel("Online: ");
		add(texto);

		ip = new JComboBox();
		/*ip.addItem("usuario 1");
		ip.addItem("usuario 2");
		ip.addItem("usuario 3");*/

		/*ip.addItem("192.168.1.107");
		ip.addItem("192.168.1.108");*/
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
				//pasamo el objeto que nos devuelve getSelectedItem() y lo convertimos a String
				datos.setIp(ip.getSelectedItem().toString());
				datos.setMensaje(campo1.getText());


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


	private JTextField campo1;
	private JComboBox ip;
	private JTextArea campochat;
	private JButton miboton;

	//creamos un JLabel para no permitir que cambie el nombre durante el funcionamiento del chat
	private JLabel nick;

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

				//usamos el mensaje que mandamos cuando nos conectamos para comprobar "online"
				if(!paqueteRecibido.getMensaje().equals("online")) {
					
					//ahora escribimos la información que recibimos, extraemos del paquete que recibimos el nick y el mensaje 
					campochat.append("\n" + paqueteRecibido.getNick()+": "+ paqueteRecibido.getMensaje());

				}
				
				//si es la primera vez mostramos las ip que hay en el arraylist
				else {
					//comprobamos que funciona bien y extraemos el arraylist
					//campochat.append("\n"+paqueteRecibido.getIps());
					
					ArrayList<String> ipsMenu = new ArrayList<String>();
					ipsMenu = paqueteRecibido.getIps();
					ip.removeAllItems();
					for (String z : ipsMenu) {
						ip.addItem(z);
					}
					
				}
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
	private ArrayList<String> Ips;

	public ArrayList<String> getIps() {
		return Ips;
	}

	public void setIps(ArrayList<String> ips) {
		Ips = ips;
	}

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