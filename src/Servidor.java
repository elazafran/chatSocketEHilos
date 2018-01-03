

import javax.swing.*;

import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class Servidor  {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		MarcoServidor mimarco=new MarcoServidor();

		mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}	
}

class MarcoServidor extends JFrame implements Runnable {

	public MarcoServidor(){

		setBounds(1200,300,280,350);				

		JPanel milamina= new JPanel();

		milamina.setLayout(new BorderLayout());

		areatexto=new JTextArea();

		milamina.add(areatexto,BorderLayout.CENTER);

		add(milamina);

		setVisible(true);

		//creamos el hilo desde la instancia hilo y lo iniciamos
		Thread mihilo = new Thread(this);
		mihilo.start();

	}



	@Override
	public void run() {
		System.out.println("Estamos a la escucha");

		try {
			//ponemos a la escucha y abrimos el puerto que le pasamos en el constructor
			ServerSocket servidor = new ServerSocket(9999);
			
			//creamos ttres variables para guardar la información que llega por la red
			String nick,ip,mensaje;
			
			//necesitamos una instacia de la clase PaqueteEnvio para recibir la informacion
			PaqueteEnvio paqueterecibido;
			
			//necesitamos un bucle infinito para que se repita la operacion
			while (true) {
				
				//aceptamos las conexiones que vienen del exterior
				Socket misocket = servidor.accept();
				
				ObjectInputStream paquetedatos = new ObjectInputStream(misocket.getInputStream());
				
				//metemos en paqueterecibido lo que le llega por la red
				paqueterecibido = (PaqueteEnvio) paquetedatos.readObject();
				
				//desmenuzamos el paquete recibido para porder trabajar con los datos que nos llegan
				nick = paqueterecibido.getNick();
				ip = paqueterecibido.getIp();
				mensaje = paqueterecibido.getMensaje();
				
				/*//creamos un flujo de entrada para recoger lo que viene del cliente
				DataInputStream flujo_entrada = new DataInputStream(misocket.getInputStream());
				//almacenamos en el string lo que viene por el flujo
				String mensaje_texto = flujo_entrada.readUTF();
				areatexto.append("\n" + mensaje_texto);
				*/ 
				//agregamos el contenido que nos llega al jtextarea, replicamos lo uqe nos llega para enviarlo al otro cliente destinatario o cliente final del mensaje 
				areatexto.append("\n" + nick +": "+ mensaje + " para " + ip);
				
				//socket/puente para enviar la informacion
				Socket enviaDestinatario = new Socket(ip, 9090);
				
				//creamo este objeto para enviar la informacion que ha recibido el servidor
				ObjectOutputStream paqueteReenvio = new ObjectOutputStream(enviaDestinatario.getOutputStream());
				paqueteReenvio.writeObject(paqueterecibido);
				
				paqueteReenvio.close();
				
				//cerramos el socket una vez que hemos enviado el objeto
				enviaDestinatario.close();
				misocket.close();
			}
			
			
		} catch (IOException | ClassNotFoundException e) {
		
			e.printStackTrace();
		}

		
	}

	private	JTextArea areatexto;
}
