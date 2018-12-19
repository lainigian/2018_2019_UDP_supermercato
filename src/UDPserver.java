
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class UDPserver extends Thread
{

	private DatagramSocket socket;
	private String fileVendite="vendite.txt";
	
	public UDPserver (int port) throws SocketException
	{
		socket=new DatagramSocket(port);
		socket.setSoTimeout(1000);
		
	}
	
	public void run()
	{
		byte[] bufferRequest= new byte[5];
	//	byte[] bufferAnswer= new byte[5];
		DatagramPacket request=new DatagramPacket(bufferRequest, bufferRequest.length);
		DatagramPacket answer;
		String messaggioRicevuto;
		String messaggioRisposta;
		String scritturaFile;
		
		
		while (!interrupted())
		{
			try 
			{
				socket.receive(request);
				messaggioRicevuto= new String(request.getData(), "ISO-8859-1");
				
				if (request.getData()[0]=='?')
				{
					try 
					{
						messaggioRisposta=cercaInFile(messaggioRicevuto);
					} 
					catch (EccezioneFile | IOException e) 
					{
						messaggioRisposta="ERROR";
					}
				}
				
				else 
				{
					try 
					{
						messaggioRisposta=scriviFile(messaggioRicevuto);
					} 
					catch (EccezioneFile | IOException e) 
					{
						messaggioRisposta="ERROR";
					}
				}
				
				//messaggioRisposta="EOK"+messaggioRicevuto;
				
				
				answer=new DatagramPacket(messaggioRisposta.getBytes("ISO-8859-1"), messaggioRisposta.length(), request.getAddress(), request.getPort());
				socket.send(answer);		
			} 
			catch (SocketTimeoutException e) 
			{
				System.err.println("Timeout");
			}
			catch (IOException e) 
			{
				
				e.printStackTrace();
			}
		}
		closeSocket();
		
	}
	
	public void closeSocket()
	{
		socket.close();
	}
	
	private String scriviFile(String vendita) throws IOException, EccezioneFile, FileNotFoundException
	{
		TextFile file= new TextFile(fileVendite, 'w');
		file.toFile(vendita+";"+LocalDateTime.now().toString());
		file.closeFile();
		return "OK";
	}
	
	private String cercaInFile(String codice) throws IOException, EccezioneFile
	{
		String recordLetto;
		TextFile file= new TextFile(fileVendite, 'r');
		String codiceCercato;
		String[] splitMessaggioRicevuto;
		String[] elementiRecord;
		int contaVendite = 0;
		
		splitMessaggioRicevuto=codice.split(";");
		codiceCercato=splitMessaggioRicevuto[1];
		
			try 
			{
				while(true)
				{
					recordLetto=file.fromFile();
					elementiRecord=recordLetto.split(";");
					if (elementiRecord[0].compareTo(codiceCercato)==0)
					{
						contaVendite=contaVendite+Integer.parseInt(elementiRecord[1]);					
					}
					
				} 
			}
			catch (EccezioneTextFileEOF e) 
			{
				file.closeFile();
				return Integer.toString(contaVendite);
			}
		
		
	}
	public static void main(String[] args)
	{
		ConsoleInput tastiera= new ConsoleInput();
		try 
		{
			UDPserver echoServer= new UDPserver(2000);
			echoServer.start();
			tastiera.readLine();
			echoServer.interrupt();
			
		} 
		catch (SocketException e) 
		{
			System.err.println("Impossibile istanziare il socket");
		} 
		catch (IOException e) 
		{
			System.out.println("Errore generico di I/O dalla tastiera");
		}

	}

}
