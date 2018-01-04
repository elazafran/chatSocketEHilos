

import javax.swing.*;

import java.awt.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;

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

		setTitle("Servidor");

		setLocationRelativeTo(null);

		milamina.setLayout(new BorderLayout());

		areatexto=new JTextArea();

		milamina.add(areatexto,BorderLayout.CENTER);

		add(milamina);

		setLocationRelativeTo(null);
		setVisible(true);

		//creamos el hilo desde la instancia hilo y lo iniciamos
		Thread mihilo = new Thread(this);
		mihilo.start();

	}



	@Override
	public void run() {

		try {
			//ponemos a la escucha y abrimos el puerto que le pasamos en el constructor
			ServerSocket servidor = new ServerSocket(9999);

			//creamos tres variables para guardar la información que llega por la red
			String nick,ip,mensaje;
			
			//ArrayDinaminco donde guardaremos las ip que se conecten
			ArrayList<String> listaIp = new ArrayList<String>();
			
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

				//creamos un condicional para ver si es la primera vez que se conecta
				if(!mensaje.equals("online")) {
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
					
				}else {
					//System.out.println("entramos por primera vez");
					// ------------- Detecta online ------------------------ //

					//Almacenamos dentro de la variable localicacion la ip address
					InetAddress localizacion = misocket.getInetAddress();
					
					//pasamos a string la dirección que nos llega
					String ipremota = localizacion.getHostAddress();
					System.out.println("Online " + ipremota);
					
					//añadimos la ip al arraylist 
					listaIp.add(ipremota);
					
					//incluimos en el paquete los datos qeu tenemos almacenados en el Arraylist
					paqueterecibido.setIps(listaIp);
					
					//mostramos por consola las ips que contiene el array
					for (String z: listaIp) {
						
						System.out.println("Array :" + z);
						
						//socket/puente para enviar la informacion a la ip de Z
						Socket enviaDestinatario = new Socket(z, 9090);

						//creamo este objeto para enviar la informacion que ha recibido el servidor
						ObjectOutputStream paqueteReenvio = new ObjectOutputStream(enviaDestinatario.getOutputStream());
						paqueteReenvio.writeObject(paqueterecibido);

						paqueteReenvio.close();

						//cerramos el socket una vez que hemos enviado el objeto
						enviaDestinatario.close();
						misocket.close();
						
					}
					// ---------------------------------------------------- //				

				}
			}


		} catch (IOException | ClassNotFoundException e) {

			e.printStackTrace();
		}


	}

	private	JTextArea areatexto;
}
