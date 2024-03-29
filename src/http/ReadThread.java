package http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Will
 */
public class ReadThread extends Thread
{
    private final List<String> request;
    private final BufferedReader in;
    private final Integer timeOut;

	private Boolean isRunning;
	private Boolean error;

    public ReadThread(Socket connection, int timeOut) throws IOException
    {
		this.in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		this.request = new ArrayList<String>();
		this.timeOut = timeOut;

		this.isRunning = true;
		this.error = false;
    }

	public void halt()
	{
		isRunning = false;
	}

    @Override
    public void run()
    {
		try
		{
			String buffer;

			while (isRunning)
			{
				if (in.ready())
				{
					buffer = in.readLine();

					if (buffer == null || buffer.length() <= 0)
						break;

					request.add(buffer);
				}

				sleep(10);
			}
		}
		catch (IOException ex)
		{
			error = true;
		}
		catch (InterruptedException ex)
		{
			
		}
    }

    public List<String> read()
    {
		start();

		try
		{
			join((timeOut > 0 ? timeOut : 5) * 1000);
		}
		catch (InterruptedException ex) { }

		if (isAlive()) // Thread is still alive.
		{
			request.clear();
			
			interrupt(); 
		}
		else if (error) // Thread is already dead.
		{
			request.clear();
		}

		return request;
    }
}
